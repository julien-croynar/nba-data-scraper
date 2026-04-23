package com.yusuke.nbadatascraper.util;

import com.yusuke.nbadatascraper.config.ScraperConfig;

public class Jitter {
    public static void applyJitter() {
        try {
            long delay = ScraperConfig.BASE_DELAY_MS +
                    java.util.concurrent.ThreadLocalRandom.current().nextLong(ScraperConfig.JITTER_MS);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
