package com.ftn.sbnz.model;

public class OpponentKnowsCard {
    private Card card;
    private boolean value;

    public OpponentKnowsCard(Card c, boolean v) {
        this.card = c;
        this.value = v;
    }

    public boolean isValue() {
        return value;
    }
}
