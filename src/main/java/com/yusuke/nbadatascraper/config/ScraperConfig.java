package com.yusuke.nbadatascraper.config;

public class ScraperConfig {
    public static final String BASE_URL      = "https://www.basketball-reference.com/";
    public static final String IMG_DIR       = "src/main/resources/images/";
    public static final String USER_AGENT    = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36";
    public static final int    TIMEOUT_MS    = 10_000;
    public static final int    MAX_RETRIES   = 3;
    public static final long   BASE_DELAY_MS = 3_000;
    public static final long   JITTER_MS     = 2_000;
}