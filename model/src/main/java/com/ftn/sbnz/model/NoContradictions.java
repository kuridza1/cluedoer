package com.ftn.sbnz.model;

public class NoContradictions {
    private Card card;
    private boolean value;

    public NoContradictions(Card c, boolean v) {
        this.card = c;
        this.value = v;
    }

    public boolean isValue() {
        return value;
    }
}
