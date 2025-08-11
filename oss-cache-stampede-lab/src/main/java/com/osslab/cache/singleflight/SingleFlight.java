package com.osslab.cache.singleflight;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * A class that provides a mechanism to prevent cache stampede (thundering herd) problem
 * for a given key. It ensures that for a given key, only one operation (e.g., a database call)
 * is in flight at a time. Other concurrent requests for the same key will wait for the result
 * of the first request.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
public class SingleFlight<K, V> {

    private final ConcurrentMap<K, CompletableFuture<V>> inProgress = new ConcurrentHashMap<>();

    /**
     * Executes the given task for the given key, ensuring that only one task is executed
     * for the same key at any given time.
     *
     * @param key  the key to identify the task
     * @param task the task to execute, which supplies the value
     * @return a CompletableFuture that will complete with the value from the task
     */
    public CompletableFuture<V> execute(K key, Supplier<CompletableFuture<V>> task) {
        return inProgress.computeIfAbsent(key, k ->
                task.get().whenComplete((result, throwable) -> {
                    // Once the future completes (normally or exceptionally), remove it from the map.
                    inProgress.remove(k);
                })
        );
    }

    /**
     * A convenience overload for synchronous tasks. The synchronous task will be wrapped
     * in a CompletableFuture.
     *
     * @param key  the key to identify the task
     * @param syncTask the synchronous task to execute
     * @return a CompletableFuture that will complete with the value from the task
     */
    public CompletableFuture<V> executeSync(K key, Supplier<V> syncTask) {
        return this.execute(key, () -> CompletableFuture.supplyAsync(syncTask));
    }
}
