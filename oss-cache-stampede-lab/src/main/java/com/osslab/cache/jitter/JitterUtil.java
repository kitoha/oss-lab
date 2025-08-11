package com.osslab.cache.jitter;

import java.util.concurrent.ThreadLocalRandom;

public final class JitterUtil {

    private JitterUtil() {}

    /**
     * Adds a random jitter to a given duration.
     * The jitter will be a random value between -jitterAmount and +jitterAmount.
     *
     * @param duration     The base duration.
     * @param jitterAmount The maximum amount of jitter to add or subtract.
     * @return The duration with jitter applied, ensured to be non-negative.
     */
    public static long applyJitter(long duration, long jitterAmount) {
        if (jitterAmount <= 0) {
            return duration;
        }
        long randomJitter = ThreadLocalRandom.current().nextLong(-jitterAmount, jitterAmount + 1);
        long result = duration + randomJitter;
        return Math.max(0, result);
    }
}
