package com.ftn.sbnz.model;

import java.util.Objects;

/**
 * Tvrdnja: igrač 'player' sigurno poseduje kartu 'card'.
 * Generiše se forward chaining pravilima iz baze znanja (Nivo 1).
 * Jednom izvedena činjenica se ne povlači (monotona dedukcija).
 */
public class Owns {

    private Player player;
    private Card card;

    public Owns() {
    }

    public Owns(Player player, Card card) {
        this.player = player;
        this.card = card;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
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
        if (!(o instanceof Owns)) return false;
        Owns owns = (Owns) o;
        return Objects.equals(player, owns.player) && Objects.equals(card, owns.card);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, card);
    }

    @Override
    public String toString() {
        return "Owns(" + player.getName() + " -> " + card + ")";
    }
}
