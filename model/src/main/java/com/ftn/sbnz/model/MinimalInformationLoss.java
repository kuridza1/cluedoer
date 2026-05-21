package com.ftn.sbnz.model;

public class MinimalInformationLoss {
    private Card card;
    private boolean value;

    public MinimalInformationLoss(Card c, boolean v) {
        this.card = c;
        this.value = v;
    }

    public boolean isValue() {
        return value;
    }
}
