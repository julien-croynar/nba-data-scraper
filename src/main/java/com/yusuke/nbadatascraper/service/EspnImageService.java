package com.yusuke.nbadatascraper.service;

import com.yusuke.nbadatascraper.config.ScraperConfig;
import com.yusuke.nbadatascraper.util.Jitter;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EspnImageService {
    private static final String ESPN_SEARCH_API_URL =
            "https://site.web.api.espn.com/apis/search/v2?limit=1&type=player&query=";

    private static final String PLAYER_IMG_URL =
            "https://a.espncdn.com/combiner/i?img=/i/headshots/nba/players/full/%s.png&w=350&h=254";

    private static final String TEAM_LOGO_URL =
            "https://a.espncdn.com/i/teamlogos/nba/500/%s.png";

    private Optional<String> findEspnPlayerId(String playerName) {
        try {
            String encodedQuery = URLEncoder.encode(playerName, StandardCharsets.UTF_8);

            String jsonResponse = Jsoup.connect(ESPN_SEARCH_API_URL + encodedQuery)
                    .userAgent(ScraperConfig.USER_AGENT)
                    .timeout(ScraperConfig.TIMEOUT_MS)
                    .ignoreContentType(true)
                    .get()
                    .body()
                    .text();

            Matcher matcher = Pattern.compile("/id/(\\d+)/").matcher(jsonResponse);
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }

            return Optional.empty();

        } catch (IOException e) {
            System.err.printf("ESPN API failed for %s: %s\n", playerName, e.getMessage());
            return Optional.empty();
        }
    }

    public void getPlayerHeadShot(String playerName, String directoryName, String fileName) {
        Path destination = Path.of(ScraperConfig.IMG_DIR, directoryName, fileName);

        if (Files.exists(destination)) {
            System.out.printf("Image already exists, skipping: %s\n", fileName);
            return;
        }

        findEspnPlayerId(playerName).ifPresentOrElse(
                espnId -> download(String.format(PLAYER_IMG_URL, espnId), destination),
                () -> System.err.printf("ESPN ID not found for: %s\n", playerName)
        );
    }

    public void getTeamLogo(String teamName) {
        String validatedName = validateTeamName(teamName);
        Path destination = Path.of(ScraperConfig.IMG_DIR, "team", teamName + ".png");

        if (Files.exists(destination)) {
            System.out.printf("Logo already exists, skipping: %s\n", teamName);
            return;
        }

        download(String.format(TEAM_LOGO_URL, validatedName), destination);
        Jitter.applyJitter();
    }

    private String validateTeamName(String teamName){
        return switch (teamName.toLowerCase()) {
            case "uta" -> "utah";
            case "nop" -> "no";
            case "brk" -> "bkn";
            case "cho" -> "cha";
            default -> teamName.toLowerCase();
        };
    }

    private void download(String url, Path destination) {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            Files.createDirectories(destination.getParent());
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.printf("Image saved: %s\n", destination);
        } catch (IOException e) {
            System.err.printf("Failed to download image %s: %s\n", url, e.getMessage());
        }
    }
}