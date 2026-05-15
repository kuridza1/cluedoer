package com.ftn.sbnz.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Reveal {

    private Player revealer;
    private Card card;
    private long timestamp;

    public Reveal() {
    }

    public Reveal(Player revealer, Card card) {
        this.revealer = revealer;
        this.card = card;
    }

    public Reveal(Player revealer, Card card, long timestamp) {
        this.revealer = revealer;
        this.card = card;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Reveal(" + revealer.getName() + " showed " + card.getName() + ")";
    }
}
