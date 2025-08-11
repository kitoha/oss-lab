package com.osslab.cache.singleflight;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class HotKeySingleFlightService {

    private static final Logger logger = LoggerFactory.getLogger(HotKeySingleFlightService.class);

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final SingleFlight<String, String> singleFlight = new SingleFlight<>();

    private final ThreadLocal<Boolean> wasLeader = new ThreadLocal<>();

    public String getValue(String key) {
        String cachedValue = cache.get(key);
        if (cachedValue != null) {
            logger.info("[{}] Cache Hit! Returning cached value.", key);
            return "Source: Cache, Value: " + cachedValue;
        }

        logger.info("[{}] Cache Miss. Attempting to fetch value using SingleFlight.", key);
        wasLeader.set(false); // Reset leader status for the current thread

        CompletableFuture<String> future = singleFlight.executeSync(key, () -> {
            wasLeader.set(true); // This thread is the leader
            logger.info("[{}] I am the LEADER. Fetching data from the source...", key);
            String value = fetchDataFromSource(key);
            cache.put(key, value);
            return value;
        });

        try {
            String resultValue = future.join(); // Wait for the result
            if (wasLeader.get()) {
                return "Source: DB (Leader), Value: " + resultValue;
            } else {
                logger.info("[{}] I am a FOLLOWER. Got result from the leader.", key);
                return "Source: DB (Follower), Value: " + resultValue;
            }
        } catch (Exception e) {
            logger.error("[{}] Error while waiting for future to complete", key, e);
            return "Error: " + e.getMessage();
        } finally {
            wasLeader.remove(); // Clean up ThreadLocal
        }
    }

    private String fetchDataFromSource(String key) {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch data", e);
        }
        return "Data for " + key;
    }

    public void clearCache() {
        cache.clear();
        logger.info("Cache cleared.");
    }
}
