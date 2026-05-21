package com.ftn.sbnz.model;

// Subgoal classes for backward chaining
public class HighInformationGain {
    private SuggestCard suggestion;
    private boolean value;
    public HighInformationGain(SuggestCard s, boolean v) {
        this.suggestion = s; this.value = v;
    }
    public boolean isValue() { return value; }
}