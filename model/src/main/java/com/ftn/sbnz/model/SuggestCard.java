package com.ftn.sbnz.model;

/**
 * Preporuka jedne karte iz određene kategorije za sledeću sugestiju (6.2.1).
 * Strateški sistem insertuje po jednu SuggestCard za SUSPECT, WEAPON i ROOM —
 * zajedno čine preporučenu trojku (S, W, R).
 */
public class SuggestCard {

    private CardType category;
    private Card     card;

    public SuggestCard(CardType category, Card card) {
        this.category = category;
        this.card     = card;
    }

    public CardType getCategory() { return category; }
    public Card     getCard()     { return card; }

    @Override
    public String toString() {
        return "SuggestCard[" + category + " -> " + card.getName() + "]";
    }
}