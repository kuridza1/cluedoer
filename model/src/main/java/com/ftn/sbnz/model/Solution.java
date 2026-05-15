package com.ftn.sbnz.model;

import java.util.Objects;

/**
 * Tvrdnja: karta 'card' se nalazi u koverti (deo finalnog rešenja) za svoju kategoriju.
 * Iz proposala (5.1.3): Rešenje(K) = X.
 */
public class Solution {

    private Card card;

    public Solution() {
    }

    public Solution(Card card) {
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
        if (!(o instanceof Solution)) return false;
        Solution solution = (Solution) o;
        return Objects.equals(card, solution.card);
    }

    @Override
    public int hashCode() {
        return Objects.hash(card);
    }

    @Override
    public String toString() {
        return "Solution(" + card + ")";
    }
}
