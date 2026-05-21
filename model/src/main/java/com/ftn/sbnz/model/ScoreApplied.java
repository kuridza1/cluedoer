package com.ftn.sbnz.model;

import java.util.Objects;


public class ScoreApplied {
    private String ruleId;
    private String key;

    public ScoreApplied() {}

    public ScoreApplied(String ruleId, String key) {
        this.ruleId = ruleId;
        this.key = key;
    }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScoreApplied)) return false;
        ScoreApplied sa = (ScoreApplied) o;
        return Objects.equals(ruleId, sa.ruleId) && Objects.equals(key, sa.key);
    }

    @Override
    public int hashCode() { return Objects.hash(ruleId, key); }

    @Override
    public String toString() { return "ScoreApplied(" + ruleId + ", " + key + ")"; }
}