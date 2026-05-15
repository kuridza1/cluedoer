package com.ftn.sbnz.model;

/**
 * Opservacija: igrač 'revealer' je pokazao kartu 'card' u odgovor na neku pretpostavku.
 * Pokazivanje karte za nas je definitivna informacija (vidimo je) → ova klasa NE pokriva
 * slučaj "neko je nekom pokazao kartu koju mi ne vidimo" (tu nemamo info o kojoj se karti radi).
 *
 * Iz proposala (5.1.2): "IF protivnik A pokaže kartu X THEN X ∈ Karte(A) AND Score(X) = 0"
 */
public class Reveal {

    private Player revealer;
    private Card card;

    public Reveal() {
    }

    public Reveal(Player revealer, Card card) {
        this.revealer = revealer;
        this.card = card;
    }

    public Player getRevealer() { return revealer; }
    public void setRevealer(Player revealer) { this.revealer = revealer; }

    public Card getCard() { return card; }
    public void setCard(Card card) { this.card = card; }

    @Override
    public String toString() {
        return "Reveal(" + revealer.getName() + " showed " + card.getName() + ")";
    }
}
