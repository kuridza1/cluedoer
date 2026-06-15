package com.ftn.sbnz.model;

public class AppliedRule {
    private final String ruleId;
    private final String detail;
    private final long timestamp;

    public AppliedRule(String ruleId, String detail) {
        this.ruleId = ruleId;
        this.detail = detail;
        this.timestamp = System.currentTimeMillis();
    }
    public AppliedRule(String ruleId, String description, long timestamp) {
        this.ruleId = ruleId;
        this.detail = description;
        this.timestamp = timestamp;
    }
    public String getRuleId() { return ruleId; }
    public String getDetail() { return detail; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() { return ruleId + "|" + detail + "|" + timestamp; }
}