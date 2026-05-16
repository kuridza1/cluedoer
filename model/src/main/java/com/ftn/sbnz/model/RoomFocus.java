package com.ftn.sbnz.model;

import java.util.Objects;

/**
 * Označava da je igrač fokusiran na određenu prostoriju.
 */
public class RoomFocus {

    private Player player;
    private Card room;

    public RoomFocus() {
    }

    public RoomFocus(Player player, Card room) {
        this.player = player;
        this.room = room;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Card getRoom() {
        return room;
    }

    public void setRoom(Card room) {
        this.room = room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomFocus)) return false;
        RoomFocus that = (RoomFocus) o;
        return Objects.equals(player, that.player)
                && Objects.equals(room, that.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, room);
    }

    @Override
    public String toString() {
        return "RoomFocus(" + player.getName()
                + " -> " + room.getName() + ")";
    }
}