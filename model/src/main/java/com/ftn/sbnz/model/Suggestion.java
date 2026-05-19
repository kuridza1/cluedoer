package com.ftn.sbnz.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Suggestion {

    private Player suggester;
    private Card suspect;
    private Card weapon;
    private Card room;
    private int turnNumber;
    private long timestamp;

    public Suggestion() {
    }


    public Suggestion(Player suggester, Card suspect, Card weapon, Card room) {
        this.suggester = suggester;
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
    }

    public Suggestion(int turnNumber, Player suggester, Card suspect, Card weapon, Card room) {
        this(suggester, suspect, weapon, room);
        this.turnNumber = turnNumber;
    }

    public Suggestion(int turnNumber, long timestamp, Player suggester,
                      Card suspect, Card weapon, Card room) {
        this(turnNumber, suggester, suspect, weapon, room);
        this.timestamp = timestamp;
    }

    public Card getCardByCategory(String category) {
        switch (category) {
            case "suspect": return suspect;
            case "weapon": return weapon;
            case "room": return room;
            default: return null;
        }
    }

    @Override
    public String toString() {
        return "Suggestion(" + suggester.getName() + ": " + suspect.getName()
                + ", " + weapon.getName() + ", " + room.getName() + ")";
    }
}
