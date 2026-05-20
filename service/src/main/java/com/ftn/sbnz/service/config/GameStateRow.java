package com.ftn.sbnz.service.config;

public class GameStateRow {

    private String minRatio;
    private String maxRatio;
    private String status;

    public GameStateRow(String minRatio, String maxRatio, String status) {
        this.minRatio = minRatio;
        this.maxRatio = maxRatio;
        this.status = status;
    }

    public String getMinRatio() {
        return minRatio;
    }

    public String getMaxRatio() {
        return maxRatio;
    }

    public String getStatus() {
        return status;
    }
}