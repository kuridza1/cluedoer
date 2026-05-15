package com.ftn.sbnz.model;

import java.util.Objects;

/**
 * Tvrdnja: igrač 'player' sigurno NE poseduje kartu 'card'.
 * Koristi se kao eliminacija u tabeli mogućnosti (oznaka X iz tabele).
 */
public class NotOwns {

    private Player player;
    private Card card;

    public NotOwns() {
    }

    public NotOwns(Player player, Card card) {
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
        if (!(o instanceof NotOwns)) return false;
        NotOwns notOwns = (NotOwns) o;
        return Objects.equals(player, notOwns.player) && Objects.equals(card, notOwns.card);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, card);
    }

    @Override
    public String toString() {
        return "NotOwns(" + player.getName() + " -/-> " + card + ")";
    }
}
