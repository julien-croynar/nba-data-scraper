package com.yusuke.nbadatascraper.model;
public record Contract(
        long capHit,
        int seasonRemaining,
        long nextSeasonEarning,
        Option option
){}