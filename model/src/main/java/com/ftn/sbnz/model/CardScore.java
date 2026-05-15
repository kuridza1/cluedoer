package com.ftn.sbnz.model;

import java.util.Objects;

/**
 * Score(X) iz sekcije 5 proposala — vrednost pouzdanosti da karta pripada rešenju.
 * Na početku Score(X) = 1 za sve karte. Forward chaining pravila postavljaju
 * Score na 0 čim utvrde da karta sigurno nije u rešenju.
 * CEP nivo (kasnije) inkrementira score kao heuristiku.
 */
public class CardScore {

    private Card card;
    private int score;

    public CardScore() {
    }

    public CardScore(Card card, int score) {
        this.card = card;
        this.score = score;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardScore)) return false;
        CardScore that = (CardScore) o;
        return Objects.equals(card, that.card);
    }

    @Override
    public int hashCode() {
        return Objects.hash(card);
    }

    @Override
    public String toString() {
        return "Score(" + card.getName() + ") = " + score;
    }
}
