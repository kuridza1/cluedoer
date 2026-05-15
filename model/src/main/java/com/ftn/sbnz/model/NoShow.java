package com.ftn.sbnz.model;

/**
 * Opservacija: igrač 'player' nije imao šta da pokaže na pretpostavku 'suggestion'.
 * Iz proposala (5.1.2): "IF protivnik A nije pokazao nijednu kartu na pretpostavku
 *                       (X, Y, Z) THEN {X, Y, Z} ∩ Karte(A) = ∅"
 *
 * Napomena: u proposalu se ovaj koncept zove PropustEvent i biće tretiran kao CEP event
 * u Nivou 2. Ovde ga modelujemo kao običnu Java činjenicu za potrebe Nivoa 1
 * (monotona dedukcija nad statičkim opservacijama). Kasnije će se ova ista informacija
 * gađati i kao @role(event) za CEP pravila.
 */
public class NoShow {

    private Player player;
    private Suggestion suggestion;

    public NoShow() {
    }

    public NoShow(Player player, Suggestion suggestion) {
        this.player = player;
        this.suggestion = suggestion;
    }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Suggestion getSuggestion() { return suggestion; }
    public void setSuggestion(Suggestion suggestion) { this.suggestion = suggestion; }

    @Override
    public String toString() {
        return "NoShow(" + player.getName() + " on " + suggestion + ")";
    }
}
