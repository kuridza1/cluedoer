package com.ftn.sbnz.model;

public class HighSuspicion {
    private Player player;
    private Card card;
    private long timestamp;

    public HighSuspicion(Player player, Card card) {
        this.player = player;
        this.card = card;
        this.timestamp = System.currentTimeMillis();
    }

    public HighSuspicion(Player player, Card card, long timestamp) {
        this.player = player;
        this.card = card;
        this.timestamp = timestamp;
    }

    public Player getPlayer() {
        return player;
    }

    public Card getCard() {
        return card;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "HighSuspicion[" + player.getName() + " -> " + card.getName() + "]";
    }
}