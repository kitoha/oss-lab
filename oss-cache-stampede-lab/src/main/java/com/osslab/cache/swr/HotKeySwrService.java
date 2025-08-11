package com.osslab.cache.swr;

import com.osslab.cache.singleflight.SingleFlight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class HotKeySwrService {

    private static final Logger logger = LoggerFactory.getLogger(HotKeySwrService.class);

    private static final long SOFT_TTL_SECONDS = 5;
    private static final long HARD_TTL_SECONDS = 10;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final SingleFlight<String, String> singleFlight = new SingleFlight<>();

    private record CacheEntry(String value, long softExpiryTime, long hardExpiryTime) {}

    public String getValue(String key) {
        CacheEntry entry = cache.get(key);
        long currentTime = System.currentTimeMillis();

        if (entry == null) {
            logger.info("[{}] Cache empty. Blocking fetch.", key);
            return blockAndFetch(key);
        }

        if (currentTime > entry.hardExpiryTime()) {
            logger.info("[{}] Hard TTL expired. Blocking fetch.", key);
            return blockAndFetch(key);
        }

        if (currentTime > entry.softExpiryTime()) {
            logger.info("[{}] Soft TTL expired. Returning stale data and refreshing in background.", key);
            refreshInBackground(key);
            return "Source: Stale Cache, Value: " + entry.value();
        }

        logger.info("[{}] Cache Hit (Fresh). Returning fresh data.", key);
        return "Source: Fresh Cache, Value: " + entry.value();
    }

    private String blockAndFetch(String key) {
        // Blocking call using SingleFlight. Both leader and followers wait.
        String value = singleFlight.executeSync(key, () -> {
            logger.info("[{}] (SWR-Blocking) I am the LEADER. Fetching data...", key);
            return fetchDataFromSource(key);
        }).join(); // .join() makes it blocking

        logger.info("[{}] (SWR-Blocking) Fetched new value. Caching it.", key);
        cache.put(key, createNewCacheEntry(value));
        return "Source: DB (Blocking), Value: " + value;
    }

    private void refreshInBackground(String key) {
        // Non-blocking call. Only one leader will execute the task.
        // Followers will not be created; they just get the stale data and move on.
        singleFlight.executeSync(key, () -> {
            logger.info("[{}] (SWR-Background) I am the LEADER. Refreshing data...", key);
            String newValue = fetchDataFromSource(key);

            // Check if the new value is different from the old one before updating
            CacheEntry oldEntry = cache.get(key);
            if (oldEntry != null && oldEntry.value().equals(newValue)) {
                // If data is the same, just extend the TTLs
                cache.put(key, new CacheEntry(oldEntry.value, System.currentTimeMillis() + SOFT_TTL_SECONDS * 1000, System.currentTimeMillis() + HARD_TTL_SECONDS * 1000));
                logger.info("[{}] (SWR-Background) Data hasn't changed. TTLs extended.", key);
            } else {
                cache.put(key, createNewCacheEntry(newValue));
                logger.info("[{}] (SWR-Background) New data fetched and cached.", key);
            }
            return newValue;
        });
    }

    private CacheEntry createNewCacheEntry(String value) {
        long currentTime = System.currentTimeMillis();
        return new CacheEntry(
                value,
                currentTime + SOFT_TTL_SECONDS * 1000,
                currentTime + HARD_TTL_SECONDS * 1000
        );
    }

    private String fetchDataFromSource(String key) {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch data", e);
        }
        return "Data for " + key + " @" + System.currentTimeMillis() / 1000;
    }

    public void clearCache() {
        cache.clear();
        logger.info("SWR Cache cleared.");
    }
}
