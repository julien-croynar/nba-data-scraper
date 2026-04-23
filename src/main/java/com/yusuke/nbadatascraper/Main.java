package com.yusuke.nbadatascraper;

import com.yusuke.nbadatascraper.model.Player;
import com.yusuke.nbadatascraper.service.EspnImageService;
import com.yusuke.nbadatascraper.service.PlayerScraper;
import com.yusuke.nbadatascraper.service.TeamScraper;
import com.yusuke.nbadatascraper.util.DataSaver;
import com.yusuke.nbadatascraper.util.HtmlParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final List<String> ALL_TEAMS = List.of(
            "ATL", "BOS", "BRK", "CHO", "CHI", "CLE", "DAL", "DEN", "DET", "GSW",
            "HOU", "IND", "LAC", "LAL", "MEM", "MIA", "MIL", "MIN", "NOP", "NYK",
            "OKC", "ORL", "PHI", "PHO", "POR", "SAC", "SAS", "TOR", "UTA", "WAS"
    );

    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {

        HtmlParser parser = new HtmlParser();
        EspnImageService espnImageService = new EspnImageService();
        PlayerScraper playerScraper = new PlayerScraper(parser);
        TeamScraper teamScraper = new TeamScraper(playerScraper, parser, espnImageService);
        DataSaver saver = new DataSaver();

        System.out.println("=== WELCOME TO THE NBA DATA SCRAPER ===");

        if (askScrapeLogos()) {
            scrapeLogos(espnImageService);
        } else {
            scrapePlayers(teamScraper, saver);
        }

        System.out.println("\nScraping completed successfully!");
    }

    private static void scrapeLogos(EspnImageService espnImageService) {
        for (String teamCode : ALL_TEAMS) {
            System.out.printf("--- FETCHING LOGO: %s ---\n", teamCode);
            espnImageService.getTeamLogo(teamCode.toLowerCase());
        }
    }

    private static void scrapePlayers(TeamScraper teamScraper, DataSaver saver) {
        List<String> teamsToScrape = resolveTeamsToScrape(saver);

        for (String teamCode : teamsToScrape) {
            System.out.printf("\n--- STARTING SCRAPING: %s ---\n", teamCode);

            List<Player> teamPlayers = teamScraper.scrapeTeam(teamCode);
            teamPlayers.forEach(p -> saver.addObject("players-data.json", p));

            saver.addObject("team-scraper.json", teamCode);
            saver.saveObjects();

            System.out.printf("Team %s completed — progress saved.\n", teamCode);
        }
    }

    private static List<String> resolveTeamsToScrape(DataSaver saver) {
        if (askResume()) {
            List<String> finished = saver.loadList("team-scraper.json", String.class);

            List<String> remaining = new ArrayList<>(ALL_TEAMS);
            remaining.removeAll(finished);

            System.out.printf("Resuming: %d/%d teams remaining.\n", remaining.size(), ALL_TEAMS.size());
            return remaining;
        }

        System.out.printf("Full scraping restart (%d teams).\n", ALL_TEAMS.size());
        return new ArrayList<>(ALL_TEAMS);
    }

    private static boolean askResume() {
        while (true) {
            System.out.print("Resume previous scraping progress? (y/n): ");
            String input = SCANNER.nextLine().trim().toLowerCase();
            if (input.equals("y")) return true;
            if (input.equals("n")) return false;
            System.out.println("Invalid input, please enter 'y' or 'n'.");
        }
    }

    private static boolean askScrapeLogos() {
        while (true) {
            System.out.print("What do you want to do?\n1 - Fetch team logos\n2 - Scrape player data\nChoice (1/2): ");
            String input = SCANNER.nextLine().trim();
            if (input.equals("1")) return true;
            if (input.equals("2")) return false;
            System.out.println("Invalid input, please enter '1' or '2'.");
        }
    }
}