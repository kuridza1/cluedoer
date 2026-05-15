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
}