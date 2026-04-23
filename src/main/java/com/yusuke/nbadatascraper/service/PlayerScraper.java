package com.yusuke.nbadatascraper.service;

import com.yusuke.nbadatascraper.config.ScraperConfig;
import com.yusuke.nbadatascraper.model.PlayerStats;
import com.yusuke.nbadatascraper.util.HtmlParser;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Optional;

public class PlayerScraper {
    private final HtmlParser parser;

    public PlayerScraper(HtmlParser parser) {
        this.parser = parser;
    }

    public Optional<PlayerStats> fetchStats(String playerPath) {
        String url = ScraperConfig.BASE_URL + playerPath;

        for (int attempt = 1; attempt <= ScraperConfig.MAX_RETRIES; attempt++) {
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent(ScraperConfig.USER_AGENT)
                        .timeout(ScraperConfig.TIMEOUT_MS)
                        .get();

                return parseStats(doc);

            } catch (HttpStatusException e) {
                if (e.getStatusCode() == 404) {
                    System.err.printf("404 Error: Player not found at %s. Aborting.\n", url);
                    return Optional.empty();
                }
                if (e.getStatusCode() == 429) {
                    System.err.printf("BAN ALERT (429) on %s! The site is blocking us.\n", url);
                }

                handleRetry(attempt, url, e.getMessage());

            } catch (IOException e) {
                handleRetry(attempt, url, e.getMessage());
            }
        }

        System.err.printf("Attempt %d/%d failed for %s: %s\n", ScraperConfig.MAX_RETRIES, url);
        return Optional.empty();
    }

    private void handleRetry(int attempt, String url, String errorMessage) {
        System.err.printf("Giving up after %d attempts for %s\n",
                attempt, ScraperConfig.MAX_RETRIES, url, errorMessage);

        if (attempt < ScraperConfig.MAX_RETRIES) {
            boolean isInterrupted = !sleepWithBackoff(attempt);
            if (isInterrupted) {
                throw new RuntimeException("Scraping forcefully interrupted.");
            }
        }
    }

    private Optional<PlayerStats> parseStats(Document doc) {
        Element pullout = doc.selectFirst("div.stats_pullout");
        if (pullout == null) return Optional.of(new PlayerStats(0, 0, 0, 0, "N/A"));

        double points   = parser.parseDoubleStat(parser.extractStat(pullout, "Points").orElse("0"));
        double assists  = parser.parseDoubleStat(parser.extractStat(pullout, "Assists").orElse("0"));
        double rebounds = parser.parseDoubleStat(parser.extractStat(pullout, "Total Rebounds").orElse("0")); // J'ai retiré les guillemets échappés ici qui semblaient risqués
        int    games    = parser.parseIntStat(parser.extractStat(pullout, "Games").orElse("0"));
        String position = parser.resolvePosition(doc);

        return Optional.of(new PlayerStats(points, rebounds, assists, games, position));
    }

    private boolean sleepWithBackoff(int attempt) {
        long delay = ScraperConfig.BASE_DELAY_MS * (1L << (attempt - 1))
                + (long) (Math.random() * ScraperConfig.JITTER_MS);
        try {
            System.out.printf("Waiting %d ms before next attempt...\n", delay);
            Thread.sleep(delay);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}