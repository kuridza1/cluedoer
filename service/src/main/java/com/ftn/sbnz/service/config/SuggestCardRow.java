package com.ftn.sbnz.service.config;

public class SuggestCardRow {

    private String category;
    private String accumulateOwns;
    private String ruleOwns;

    public SuggestCardRow(
            String category,
            String accumulateOwns,
            String ruleOwns
    ) {
        this.category = category;
        this.accumulateOwns = accumulateOwns;
        this.ruleOwns = ruleOwns;
    }

    public String getCategory() {
        return category;
    }

    public String getAccumulateOwns() {
        return accumulateOwns;
    }

    public String getRuleOwns() {
        return ruleOwns;
    }
}