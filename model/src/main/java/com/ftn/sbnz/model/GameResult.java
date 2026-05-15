package com.ftn.sbnz.model;

public class GameResult {

    private Card suspect;
    private Card weapon;
    private Card room;
    private boolean solved;

    public GameResult() {}

    public GameResult(Card suspect, Card weapon, Card room, boolean solved) {
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
        this.solved = solved;
    }

    public Card getSuspect() { return suspect; }
    public void setSuspect(Card suspect) { this.suspect = suspect; }

    public Card getWeapon() { return weapon; }
    public void setWeapon(Card weapon) { this.weapon = weapon; }

    public Card getRoom() { return room; }
    public void setRoom(Card room) { this.room = room; }

    public boolean isSolved() { return solved; }
    public void setSolved(boolean solved) { this.solved = solved; }

    @Override
    public String toString() {
        return "GameResult[solved=" + solved + "]: ("
                + (suspect != null ? suspect.getName() : "?") + ", "
                + (weapon != null ? weapon.getName() : "?") + ", "
                + (room != null ? room.getName() : "?") + ")";
    }
}