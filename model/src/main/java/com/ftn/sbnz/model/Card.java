package com.ftn.sbnz.model;

import java.util.Objects;


public class Card {

    private CardType type;
    private String name;

    public Card() {
    }

    public Card(CardType type, String name) {
        this.type = type;
        this.name = name;
    }

    public CardType getType() {
        return type;
    }

    public void setType(CardType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return type == card.type && Objects.equals(name, card.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}
