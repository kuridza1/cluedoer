package com.ftn.sbnz.model;

import java.util.Objects;

/**
 * Igrač u partiji. handSize predstavlja broj karata koje igrač drži u ruci
 * (potrebno za pravilo "ako je preostalo tačno N nepoznatih spotova").
 */
public class Player {

    private String name;
    private int handSize;
    private boolean self; // true ako predstavlja "ja"

    public Player() {
    }

    public Player(String name, int handSize, boolean self) {
        this.name = name;
        this.handSize = handSize;
        this.self = self;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHandSize() {
        return handSize;
    }

    public void setHandSize(int handSize) {
        this.handSize = handSize;
    }

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name + (self ? " (ja)" : "");
    }
}
