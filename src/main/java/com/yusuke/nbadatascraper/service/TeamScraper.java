package com.yusuke.nbadatascraper.service;

import com.yusuke.nbadatascraper.config.ScraperConfig;
import com.yusuke.nbadatascraper.exception.ScraperException;
import com.yusuke.nbadatascraper.model.Contract;
import com.yusuke.nbadatascraper.model.Option;
import com.yusuke.nbadatascraper.model.Player;
import com.yusuke.nbadatascraper.model.PlayerStats;
import com.yusuke.nbadatascraper.util.HtmlParser;
import com.yusuke.nbadatascraper.util.Jitter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamScraper {
    private static final String ROW_SELECTOR =
            "table#contracts tbody tr:not(.partial_table):not(.thead)";

    private final PlayerScraper playerScraper;
    private final HtmlParser parser;
    private final EspnImageService espnImageService;

    public TeamScraper(PlayerScraper playerScraper, HtmlParser parser, EspnImageService espnImageService) {
        this.playerScraper = playerScraper;
        this.parser = parser;
        this.espnImageService = espnImageService;
    }

    public List<Player> scrapeTeam(String teamCode) {
        String url = ScraperConfig.BASE_URL + "contracts/" + teamCode + ".html";
        List<Player> players = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(ScraperConfig.USER_AGENT)
                    .timeout(ScraperConfig.TIMEOUT_MS)
                    .get();

            Elements rows = doc.select(ROW_SELECTOR);

            for (Element row : rows) {
                parseRow(row, teamCode).ifPresent(players::add);
                Jitter.applyJitter();
            }

        } catch (IOException e) {
            throw new ScraperException("Unable to access team " + teamCode, teamCode, e);
        }

        System.out.printf("Team %s: %d players fetched\n", teamCode, players.size());
        return players;
    }

    private Optional<Player> parseRow(Element row, String teamCode) {
        Contract contract = parseContract(row);
        if (contract == null) return Optional.empty();

        Element linkEl = row.selectFirst("th[data-stat=player] a");
        if (linkEl == null) return Optional.empty();

        String name = row.select("th[data-stat=player]").text();
        int age = parser.parseIntStat(row.select("td[data-stat=age_today]").text());
        String playerPath = linkEl.attr("href");
        String fileName = extractFileName(playerPath).replace("jpg", "png");

        System.out.printf("Traitement : %s [%s] \n", name, teamCode);

        PlayerStats stats = playerScraper.fetchStats(playerPath).orElse(null);

        espnImageService.getPlayerHeadShot(name, "players/" + teamCode, fileName);

        return Optional.of(new Player(name, teamCode, age, contract, stats, fileName));
    }

    private String extractFileName(String playerPath) {
        return playerPath
                .substring(playerPath.lastIndexOf('/') + 1)
                .replace(".html", ".jpg");
    }

    private Contract parseContract(Element row) {
        String nextSeasonText = row.select("td[data-stat=y2]").text();
        String capHitText = row.select("td[data-stat=remain_gtd]").text();

        if (nextSeasonText.isEmpty() || capHitText.isEmpty()) return null;

        long capHit = 0;
        for (int i = 2; ; i++) {
            Element seasonElement = row.selectFirst("td[data-stat=y" + i + "]");
            if (seasonElement == null || seasonElement.text().isEmpty()) break;
            capHit += parser.parseIntStat(seasonElement.text());
        }

        Option option = resolveContractOption(row);
        int seasonsRemaining = countRemainingSeasons(row);
        long nextSeason = parser.parseIntStat(nextSeasonText);

        return new Contract(capHit, seasonsRemaining, nextSeason, option);
    }

    private Option resolveContractOption(Element row) {
        if (row.selectFirst("td.salary-pl") != null) return Option.PLAYER;
        if (row.selectFirst("td.salary-tm") != null) return Option.TEAM;
        return Option.NONE;
    }

    private int countRemainingSeasons(Element row) {
        int count = 1;
        for (int i = 3; ; i++) {
            Element seasonElement = row.selectFirst("td[data-stat=y" + i + "]");
            if (seasonElement == null || seasonElement.text().isEmpty()) break;
            count++;
        }
        return count;
    }
}