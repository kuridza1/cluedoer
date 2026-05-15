package com.ftn.sbnz.model;

/**
 * Pretpostavka koju je neki igrač izneo u toku partije: (S, W, R) trojka.
 * Iz proposala: "Istorija pretpostavki – ko je postavio pretpostavku i koje tri karte".
 */
public class Suggestion {

    private Player suggester;
    private Card suspect;
    private Card weapon;
    private Card room;

    public Suggestion() {
    }

    public Suggestion(Player suggester, Card suspect, Card weapon, Card room) {
        this.suggester = suggester;
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
    }

    public Player getSuggester() { return suggester; }
    public void setSuggester(Player suggester) { this.suggester = suggester; }

    public Card getSuspect() { return suspect; }
    public void setSuspect(Card suspect) { this.suspect = suspect; }

    public Card getWeapon() { return weapon; }
    public void setWeapon(Card weapon) { this.weapon = weapon; }

    public Card getRoom() { return room; }
    public void setRoom(Card room) { this.room = room; }

    @Override
    public String toString() {
        return "Suggestion(" + suggester.getName() + ": " + suspect.getName()
                + ", " + weapon.getName() + ", " + room.getName() + ")";
    }
}
