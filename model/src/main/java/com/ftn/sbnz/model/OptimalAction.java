// OptimalAction.java
package com.ftn.sbnz.model;

public class OptimalAction {
    private String action;  // "SUGGESTION", "SHOW", "ACCUSE", "DEFAULT"
    private Card card;
    private CardType category;
    private String reasoning;

    public OptimalAction(String action, Card card, CardType category, String reasoning) {
        this.action = action;
        this.card = card;
        this.category = category;
        this.reasoning = reasoning;
    }

    public String getAction() { return action; }
    public Card getCard() { return card; }
    public CardType getCategory() { return category; }
    public String getReasoning() { return reasoning; }

    @Override
    public String toString() {
        return "OptimalAction[" + action + " -> " + card.getName() + "]";
    }
}

