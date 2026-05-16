package com.ftn.sbnz.model;

import java.util.Date;

public class HighSuspicion {
    private Player player;
    private Card card;
    private Date timestamp;

    public HighSuspicion(Player player, Card card) {
        this.player = player;
        this.card = card;
        this.timestamp = new Date();
    }

    public Player getPlayer() {
        return player;
    }

    public Card getCard() {
        return card;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "HighSuspicion[" + player.getName() + " -> " + card.getName() + "]";
    }
}