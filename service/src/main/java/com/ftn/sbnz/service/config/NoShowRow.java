package com.ftn.sbnz.service.config;

public class NoShowRow {
    private String ruleName;
    private String getter;

    public NoShowRow(String ruleName, String getter) {
        this.ruleName = ruleName;
        this.getter = getter;
    }
    public String getRuleName() { return ruleName; }
    public String getGetter()   { return getter; }
}