package com.ftn.sbnz.model;

/**
 * Strateška preporuka generisana za određenu fazu igre (6.2.1).
 * Čuva phase kako bi retract logika u pravilima mogla da targetuje
 * tačno preporuku iz stare faze bez brisanja sve ostale memorije.
 */
public class StrategicRecommendation {

    private GameStatus phase;
    private String     message;

    // Konstruktor sa fazom (novi, preferirani)
    public StrategicRecommendation(GameStatus phase, String message) {
        this.phase   = phase;
        this.message = message;
    }

    // Backwards-compatible konstruktor bez faze (za stari kod)
    public StrategicRecommendation(String message) {
        this.phase   = null;
        this.message = message;
    }

    public GameStatus getPhase()   { return phase; }
    public String     getMessage() { return message; }

    @Override
    public String toString() {
        return "StrategicRecommendation[phase=" + phase + ", msg=" + message + "]";
    }
}