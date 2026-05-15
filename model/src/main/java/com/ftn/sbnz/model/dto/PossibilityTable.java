package com.ftn.sbnz.model.dto;

import java.util.List;
import java.util.Map;

/**
 * Tabela mogućnosti — prikaz "ko šta ima / nema / možda" za sve karte i igrače.
 * Format prati sekciju 8 proposala:
 *   "X" - eliminisana (igrač sigurno NE poseduje), Score = 0
 *   "O" - igrač sigurno poseduje kartu (Owns)
 *   "?" - moguće, nema informacije ni za ni protiv
 *   "✓" - karta je deo finalnog rešenja (Solution)
 */
public class PossibilityTable {

    public List<String> players;            // imena igrača, kolone
    public List<RowGroup> groups;           // po jedan grupa po kategoriji (SUSPECT, WEAPON, ROOM)
    public Map<String, Integer> scores;     // score po imenu karte

    public static class RowGroup {
        public String category;             // "SUSPECT" | "WEAPON" | "ROOM"
        public List<Row> rows;
    }

    public static class Row {
        public String cardName;
        public int score;
        public List<String> cells;          // simbol po igraču - "X", "O", "?", "✓"
    }
    public List<TurnEntry> turns;          // tok partije
    public List<DisjunctionEntry> hints;   // "X ima bar jednu od {a, b, c}"

    public static class TurnEntry {
        public int number;
        public String suggester;
        public String suspect;
        public String weapon;
        public String room;
        public List<String> noShowPlayers;  // ko je preskočio
        public String shownTo;              // kome je pokazana karta (recipient)
        public String shower;               // ko je pokazao (ako znamo)
        public String revealedCard;         // tačno koja karta (samo ako sam ja sugester)
        public boolean isPrivate;           // true ako je PrivateShow, false ako je Reveal
    }

    public static class DisjunctionEntry {
        public String player;               // ko ima bar jednu kartu
        public List<String> candidateCards; // skup kandidata
        public int turnNumber;              // iz koje sugestije
    }
}