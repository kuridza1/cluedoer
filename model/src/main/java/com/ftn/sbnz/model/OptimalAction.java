package com.ftn.sbnz.model;

public class OptimalAction {
    private String action;  // "SUGGESTION", "SHOW", "ACCUSE", "DEFAULT"
    private Card card;      // Za SUGGESTION, SHOW, DEFAULT
    private CardType category;
    private String reasoning;

    // Za ACCUSE - cela kombinacija
    private Card suspect;
    private Card weapon;
    private Card room;

    // Konstruktor za SUGGESTION, SHOW, DEFAULT
    public OptimalAction(String action, Card card, CardType category, String reasoning) {
        this.action = action;
        this.card = card;
        this.category = category;
        this.reasoning = reasoning;
    }

    // Konstruktor za ACCUSE
    public OptimalAction(String action, Card suspect, Card weapon, Card room, String reasoning) {
        this.action = action;
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
        this.reasoning = reasoning;
    }

    // Getteri
    public String getAction() { return action; }
    public Card getCard() { return card; }
    public CardType getCategory() { return category; }
    public String getReasoning() { return reasoning; }
    public Card getSuspect() { return suspect; }
    public Card getWeapon() { return weapon; }
    public Card getRoom() { return room; }

    @Override
    public String toString() {
        if ("ACCUSE".equals(action)) {
            return "OptimalAction[ACCUSE: " + suspect.getName() + ", " + weapon.getName() + ", " + room.getName() + "]";
        }
        return "OptimalAction[" + action + " -> " + card.getName() + "]";
    }
}