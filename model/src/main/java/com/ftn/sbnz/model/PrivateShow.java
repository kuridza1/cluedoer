package com.ftn.sbnz.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrivateShow {

    private Player shower;
    private Player recipient;
    private Suggestion suggestion;
    private long timestamp;

    public PrivateShow() {
    }
    public PrivateShow(Player shower, Player recipient, Suggestion suggestion) {
        this.shower = shower;
        this.recipient = recipient;
        this.suggestion = suggestion;
    }

    public PrivateShow(Player shower, Player recipient, Suggestion suggestion, long timestamp) {
        this.shower = shower;
        this.recipient = recipient;
        this.suggestion = suggestion;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PrivateShow(" + shower.getName() + " -> " + recipient.getName()
                + " on " + suggestion + ")";
    }
}