package com.ftn.sbnz.model;

/**
 * Preporuka koju kartu pokazati kao odgovor na određenu sugestiju (6.2.2).
 * Strateški sistem može ubaciti više ShowRecommendation činjenica, ali
 * pravila su sređena po prioritetu (salience) pa je prva odgovarajuća
 * ujedno i najoptimalnija.
 */
public class ShowRecommendation {

    private Card       card;
    private Suggestion suggestion;
    private String     reason;

    public ShowRecommendation(Card card, Suggestion suggestion, String reason) {
        this.card       = card;
        this.suggestion = suggestion;
        this.reason     = reason;
    }

    public Card       getCard()       { return card; }
    public Suggestion getSuggestion() { return suggestion; }
    public String     getReason()     { return reason; }

    @Override
    public String toString() {
        return "ShowRecommendation[card=" + card.getName() +
                ", reason=" + reason + "]";
    }
}