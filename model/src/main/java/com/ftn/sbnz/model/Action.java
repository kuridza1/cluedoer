package com.ftn.sbnz.model;

public class Action {

    private String type; // SUGGESTION / ACCUSATION
    private Card suspect;
    private Card weapon;
    private Card room;
    private String reason;

    public Action(String type, Card suspect, Card weapon, Card room, String reason) {
        this.type = type;
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
        this.reason = reason;
    }

    public String getType() { return type; }
    public Card getSuspect() { return suspect; }
    public Card getWeapon() { return weapon; }
    public Card getRoom() { return room; }
    public String getReason() { return reason; }
}