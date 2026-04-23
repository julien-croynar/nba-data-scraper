package com.yusuke.nbadatascraper.util;

import org.jsoup.nodes.Element;
import java.util.Optional;

public class HtmlParser {
    public Optional<String> extractStat(Element pullout, String dataTip) {
        return Optional.ofNullable(pullout.selectFirst("span[data-tip=" + dataTip + "] + p"))
                .map(Element::text)
                .filter(s -> !s.isBlank());
    }

    public int parseIntStat(String raw) {
        if (raw == null || raw.isBlank() || raw.equals("-")) return 0;
        try {
            return Integer.parseInt(raw.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public double parseDoubleStat(String raw) {
        if (raw == null || raw.isBlank() || raw.equals("-")) return 0.0;
        try {
            return Double.parseDouble(raw.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    public String resolvePosition(Element doc) {
        Element row = doc.select("tr[id~=per_game_stats\\.\\d{4}]").last();
        if (row == null) {
            row = doc.select("tr.full_table:not(.stat_total)").last();
        }

        if (row == null) return "N/A";
        Element cell = row.selectFirst("td[data-stat=pos]");
        return cell != null && !cell.text().isBlank() ? cell.text() : "N/A";
    }
}