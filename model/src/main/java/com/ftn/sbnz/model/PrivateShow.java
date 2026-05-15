package com.ftn.sbnz.model;

public class PrivateShow {

    private Player shower;
    private Player recipient;
    private Suggestion suggestion;

    public PrivateShow() {
    }

    public PrivateShow(Player shower, Player recipient, Suggestion suggestion) {
        this.shower = shower;
        this.recipient = recipient;
        this.suggestion = suggestion;
    }

    public Player getShower() { return shower; }
    public void setShower(Player shower) { this.shower = shower; }

    public Player getRecipient() { return recipient; }
    public void setRecipient(Player recipient) { this.recipient = recipient; }

    public Suggestion getSuggestion() { return suggestion; }
    public void setSuggestion(Suggestion suggestion) { this.suggestion = suggestion; }

    @Override
    public String toString() {
        return "PrivateShow(" + shower.getName() + " -> " + recipient.getName()
                + " on " + suggestion + ")";
    }
}