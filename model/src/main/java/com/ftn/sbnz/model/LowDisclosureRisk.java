package com.ftn.sbnz.model;

import com.ftn.sbnz.model.SuggestCard;

public class LowDisclosureRisk {
    private SuggestCard suggestion;
    private boolean value;
    public LowDisclosureRisk(SuggestCard s, boolean v) {
        this.suggestion = s; this.value = v;
    }
    public boolean isValue() { return value; }
}