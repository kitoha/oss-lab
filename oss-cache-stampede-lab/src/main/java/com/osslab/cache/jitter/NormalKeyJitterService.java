package com.osslab.cache.jitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class NormalKeyJitterService {

    private static final Logger logger = LoggerFactory.getLogger(NormalKeyJitterService.class);

    private static final long BASE_TTL_SECONDS = 10;
    private static final long JITTER_SECONDS = 5;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private record CacheEntry(String value, long expiryTime) {}

    public String getValue(String key) {
        CacheEntry entry = cache.get(key);

        if (entry != null && System.currentTimeMillis() < entry.expiryTime()) {
            long expiresIn = (entry.expiryTime() - System.currentTimeMillis()) / 1000;
            logger.info("[{}] Cache Hit. Expires in {} seconds.", key, expiresIn);
            return "Source: Cache, Value: " + entry.value() + " (Expires in " + expiresIn + "s)";
        }

        logger.info("[{}] Cache Miss or expired. Fetching from source.", key);
        String value = fetchDataFromSource(key);

        long ttlWithJitter = JitterUtil.applyJitter(BASE_TTL_SECONDS, JITTER_SECONDS);
        long expiryTime = System.currentTimeMillis() + ttlWithJitter * 1000;
        cache.put(key, new CacheEntry(value, expiryTime));

        logger.info("[{}] Fetched from DB. Caching with TTL {} seconds.", key, ttlWithJitter);
        return "Source: DB, Value: " + value + " (Cached with TTL: " + ttlWithJitter + "s)";
    }

    private String fetchDataFromSource(String key) {
        try {
            TimeUnit.SECONDS.sleep(1); // Simulate 1-second DB query
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch data", e);
        }
        return "Data for " + key;
    }

    public void clearCache() {
        cache.clear();
        logger.info("Jitter Cache cleared.");
    }
}
