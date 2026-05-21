package com.ftn.sbnz.model;

public class DoesNotRevealNewCategory {
    private Card card;
    private boolean value;

    public DoesNotRevealNewCategory(Card c, boolean v) {
        this.card = c;
        this.value = v;
    }

    public boolean isValue() {
        return value;
    }
}
