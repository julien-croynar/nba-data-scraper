package com.yusuke.nbadatascraper.model;

public record PlayerStats(
    double points,
    double rebound,
    double assists,
    int gamePlayed,
    String position
){}
