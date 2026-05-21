package com.ftn.sbnz.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoShow {

    private Player player;
    private Suggestion suggestion;
    private long timestamp;

    public NoShow() {
    }

    public NoShow(Player player, Suggestion suggestion) {
        this.player = player;
        this.suggestion = suggestion;
    }

    public NoShow(Player player, Suggestion suggestion, long timestamp) {
        this.player = player;
        this.suggestion = suggestion;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "NoShow(" + player.getName() + " on " + suggestion + ")";
    }
}
