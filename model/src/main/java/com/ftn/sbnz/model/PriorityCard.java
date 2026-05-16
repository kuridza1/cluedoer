package com.ftn.sbnz.model;

import java.util.Objects;

/**
 * Karta kojoj strateški sistem daje prioritet.
 */
public class PriorityCard {

    private Card card;

    public PriorityCard() {
    }

    public PriorityCard(Card card) {
        this.card = card;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PriorityCard)) return false;
        PriorityCard that = (PriorityCard) o;
        return Objects.equals(card, that.card);
    }

    @Override
    public int hashCode() {
        return Objects.hash(card);
    }

    @Override
    public String toString() {
        return "PriorityCard(" + card.getName() + ")";
    }
}