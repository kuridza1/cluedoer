package com.ftn.sbnz.model;

public class AllAlternativesEliminated {
    private Card card;
    private boolean value;

    public AllAlternativesEliminated(Card c, boolean v) {
        this.card = c;
        this.value = v;
    }

    public boolean isValue() {
        return value;
    }
}
