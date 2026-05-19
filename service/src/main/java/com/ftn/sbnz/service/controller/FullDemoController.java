package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.model.*;
import com.ftn.sbnz.model.dto.PossibilityTable;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.api.time.SessionPseudoClock;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Finalna integracija: deduktivni Sistem 1 + strateški Sistem 2, sve nivoe.
 *   Sistem 1 (proposal 5):  forward (5.1) + CEP (5.2) + backward (5.3)
 *   Sistem 2 (proposal 6):  forward (6.1-6.2) + CEP (6.3) + backward (6.4)
 *
 * Sva pravila su u istoj `cluedoKBase` (stream mode + pseudo-clock).
 * Backward chaining query "resenje" se eksplicitno poziva iz Java koda
 * jer Drools query-je ne pokreće automatski kroz fireAllRules() ako
 * ih nijedno pravilo ne triguje.
 *
 * GET /api/full -> kompletan snapshot stanja sistema
 */
@RestController
@RequestMapping("/api/full")
public class FullDemoController {

    private final KieContainer kieContainer;

    public FullDemoController(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @GetMapping("")
    public Map<String, Object> runFullDemo() {
        KieSession ks = kieContainer.newKieSession("cluedoKSession");
        SessionPseudoClock clock = ks.getSessionClock();

        System.out.println("\n========== /api/full POKRENUT ==========");

        // ====================================================================
        // 1. Karte (pun Cluedo skup)
        // ====================================================================
        List<Card> suspects = Arrays.asList(
                new Card(CardType.SUSPECT, "Green"),
                new Card(CardType.SUSPECT, "Mustard"),
                new Card(CardType.SUSPECT, "Peacock"),
                new Card(CardType.SUSPECT, "Plum"),
                new Card(CardType.SUSPECT, "Scarlet"),
                new Card(CardType.SUSPECT, "White")
                );
        List<Card> weapons = Arrays.asList(
                new Card(CardType.WEAPON, "Svecnjak"),
                new Card(CardType.WEAPON, "Bodez"),
                new Card(CardType.WEAPON, "Revolver"),
                new Card(CardType.WEAPON, "OlovnaCev"),
                new Card(CardType.WEAPON, "Konopac"),
                new Card(CardType.WEAPON, "Kljuc")
        );
        List<Card> rooms = Arrays.asList(
                new Card(CardType.ROOM, "Trem"),
                new Card(CardType.ROOM, "Plesnjak"),
                new Card(CardType.ROOM, "Kuhinja"),
                new Card(CardType.ROOM, "Trpezarija"),
                new Card(CardType.ROOM, "Salon"),
                new Card(CardType.ROOM, "Hodnik"),
                new Card(CardType.ROOM, "RadnaSoba"),
                new Card(CardType.ROOM, "Biblioteka"),
                new Card(CardType.ROOM, "BilijarSoba")
        );
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(suspects);
        allCards.addAll(weapons);
        allCards.addAll(rooms);
        for (Card c : allCards) {
            ks.insert(c);
            ks.insert(new CardScore(c, 1));
        }


        Player ja = new Player("Ja", 3, true);
        Player A = new Player("A", 3, false);
        Player B = new Player("B", 3, false);
        Player C = new Player("C", 3, false);
        Player D = new Player("D", 3, false);
        Player E = new Player("E", 3, false);
        ks.insert(ja); ks.insert(A); ks.insert(B);
        ks.insert(C); ks.insert(D); ks.insert(E);


        ks.insert(new Owns(ja, suspects.get(4))); // Scarlet
        ks.insert(new Owns(ja, weapons.get(1)));  // Bodez
        ks.insert(new Owns(ja, rooms.get(2)));    // Kuhinja

        int firedInit = ks.fireAllRules();


        long t = clock.getCurrentTime();
        Suggestion sug1 = new Suggestion(1, t, A, suspects.get(0), weapons.get(0), rooms.get(0));
        ks.insert(sug1);
        ks.insert(new NoShow(B, sug1, t));
        ks.insert(new PrivateShow(C, A, sug1, t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        int f1 = ks.fireAllRules();

        t = clock.getCurrentTime();
        Suggestion sug2 = new Suggestion(2, t, B, suspects.get(1), weapons.get(1), rooms.get(1));
        ks.insert(sug2);
        ks.insert(new PrivateShow(C, B, sug2, t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        int f2 = ks.fireAllRules();

        t = clock.getCurrentTime();
        Suggestion sug3 = new Suggestion(3, t, C, suspects.get(1), weapons.get(0), rooms.get(0));
        ks.insert(sug3);
        ks.insert(new PrivateShow(D, C, sug3, t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        int f3 = ks.fireAllRules();

        t = clock.getCurrentTime();
        Suggestion sug4 = new Suggestion(4, t, D, suspects.get(2), weapons.get(0), rooms.get(2));
        ks.insert(sug4);
        ks.insert(new NoShow(E, sug4, t));
        ks.insert(new Reveal(ja, rooms.get(2), t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        int f4 = ks.fireAllRules();

        t = clock.getCurrentTime();
        Suggestion sug5 = new Suggestion(5, t, E, suspects.get(3), weapons.get(0), rooms.get(3));
        ks.insert(sug5);
        ks.insert(new NoShow(ja, sug5, t));
        ks.insert(new PrivateShow(A, E, sug5, t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        int f5 = ks.fireAllRules();

        t = clock.getCurrentTime();
        Suggestion sug6 = new Suggestion(6, t, ja, suspects.get(0), weapons.get(0), rooms.get(7));
        ks.insert(sug6);
        ks.insert(new NoShow(A, sug6, t));
        ks.insert(new NoShow(B, sug6, t));
        ks.insert(new NoShow(C, sug6, t));
        ks.insert(new Reveal(D, weapons.get(0)));
        clock.advanceTime(1, TimeUnit.MINUTES);
        int f6 = ks.fireAllRules();

        t = clock.getCurrentTime();
        Suggestion sug7 = new Suggestion(7, t, A, suspects.get(0), weapons.get(0), rooms.get(1));
        ks.insert(sug7);
        ks.insert(new NoShow(B, sug7, t));
        ks.insert(new NoShow(C, sug7, t));
        ks.insert(new PrivateShow(D, A, sug7, t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        int f7 = ks.fireAllRules();

        t = clock.getCurrentTime();
        Suggestion sug8 = new Suggestion(8, t, B, suspects.get(2), weapons.get(1), rooms.get(1));
        ks.insert(sug8);
        ks.insert(new NoShow(C, sug8, t));
        ks.insert(new NoShow(D, sug8, t));
        ks.insert(new NoShow(E, sug8, t));
        ks.insert(new Reveal(ja, weapons.get(1)));
        clock.advanceTime(1, TimeUnit.MINUTES);
        int f8 = ks.fireAllRules();

        t = clock.getCurrentTime();
        Suggestion sug9 = new Suggestion(9, t, C, suspects.get(1), weapons.get(3), rooms.get(0));
        ks.insert(sug9);
        ks.insert(new NoShow(D, sug9, t));
        ks.insert(new NoShow(E, sug9, t));
        ks.insert(new NoShow(ja, sug9, t));
        ks.insert(new NoShow(A, sug9, t));
        ks.insert(new PrivateShow(B, C, sug9, t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        int f9 = ks.fireAllRules();

        t = clock.getCurrentTime();
        Suggestion sug10 = new Suggestion(10, t, D, suspects.get(3), weapons.get(4), rooms.get(6));
        ks.insert(sug10);
        ks.insert(new PrivateShow(E, D, sug10, t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        int f10 = ks.fireAllRules();

        t = clock.getCurrentTime();
        Suggestion sug11 = new Suggestion(11, t, E, suspects.get(2), weapons.get(4), rooms.get(8));
        ks.insert(sug11);
        ks.insert(new NoShow(ja, sug11, t));
        ks.insert(new PrivateShow(A, E, sug11, t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        int f11 = ks.fireAllRules();
//
//        t = clock.getCurrentTime();
//        Suggestion sug12 = new Suggestion(12, t, ja, suspects.get(0), weapons.get(1), rooms.get(2));
//        ks.insert(sug12);
//        ks.insert(new NoShow(A, sug12, t));
//        ks.insert(new NoShow(B, sug12, t));
//        ks.insert(new NoShow(C, sug12, t));
//        ks.insert(new NoShow(D, sug12, t));
//        ks.insert(new NoShow(E, sug12, t));
//        clock.advanceTime(1, TimeUnit.MINUTES);
//        int f12= ks.fireAllRules();
//
//        t = clock.getCurrentTime();
//        Suggestion sug13 = new Suggestion(13, t, A, suspects.get(0), weapons.get(2), rooms.get(8));
//// A: Green, Revolver, BilijarSoba
//        ks.insert(sug13);
//        ks.insert(new PrivateShow(B, A, sug13, t));   // B pokazuje BilijarSoba
//        clock.advanceTime(1, TimeUnit.MINUTES);
//        int f13 = ks.fireAllRules();
//
//        t = clock.getCurrentTime();
//        Suggestion sug14 = new Suggestion(14, t, B, suspects.get(0), weapons.get(2), rooms.get(5));
//// B: Green, Revolver, Hodnik
//        ks.insert(sug14);
//        ks.insert(new NoShow(C, sug14, t));
//        ks.insert(new PrivateShow(D, B, sug14, t));   // D pokazuje Hodnik
//        clock.advanceTime(1, TimeUnit.MINUTES);
//        int f14 = ks.fireAllRules();
//
//        t = clock.getCurrentTime();
//        Suggestion sug15 = new Suggestion(15, t, C, suspects.get(0), weapons.get(2), rooms.get(3));
//// C: Green, Revolver, Trpezarija
//        ks.insert(sug15);
//        ks.insert(new PrivateShow(A, C, sug15, t));   // A pokazuje Trpezarija
//        clock.advanceTime(1, TimeUnit.MINUTES);
//        int f15 = ks.fireAllRules();
//
//        t = clock.getCurrentTime();
//        Suggestion sug16 = new Suggestion(16, t, D, suspects.get(0), weapons.get(2), rooms.get(7));
//// D: Green, Revolver, Biblioteka
//        ks.insert(sug16);
//        ks.insert(new PrivateShow(E, D, sug16, t));   // E pokazuje Biblioteka
//        clock.advanceTime(1, TimeUnit.MINUTES);
//        int f16 = ks.fireAllRules();
//
//        t = clock.getCurrentTime();
//        Suggestion sug17 = new Suggestion(17, t, E, suspects.get(0), weapons.get(2), rooms.get(6));
//// E: Green, Revolver, RadnaSoba — svi NoShow jer E ima RadnaSoba u rukama
//        ks.insert(sug17);
//        ks.insert(new NoShow(ja, sug17, t));
//        ks.insert(new NoShow(A, sug17, t));
//        ks.insert(new NoShow(B, sug17, t));
//        ks.insert(new NoShow(C, sug17, t));
//        ks.insert(new NoShow(D, sug17, t));
//        clock.advanceTime(1, TimeUnit.MINUTES);
//        int f17 = ks.fireAllRules();

        boolean backwardSolved = runBackwardQuery(ks);

        Map<String, Object> result = new HashMap<>();

        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("scores", ks.getObjects(o -> o instanceof CardScore).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));
        snapshot.put("owns", ks.getObjects(o -> o instanceof Owns).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));
        snapshot.put("notOwns", ks.getObjects(o -> o instanceof NotOwns).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));
        snapshot.put("solutions", ks.getObjects(o -> o instanceof Solution).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));
        snapshot.put("gameResult", ks.getObjects(o -> o instanceof GameResult).stream()
                .map(Object::toString).collect(Collectors.toList()));
        snapshot.put("backwardSolved", backwardSolved);
        result.put("knowledgeSnapshot", snapshot);

        Map<String, Object> strategic = new HashMap<>();
        strategic.put("gameStates", ks.getObjects(o -> o instanceof GameState).stream()
                .map(Object::toString).collect(Collectors.toList()));
        strategic.put("profiles", ks.getObjects(o -> o instanceof Profile).stream()
                .map(Object::toString).collect(Collectors.toList()));
        strategic.put("roomFocuses", ks.getObjects(o -> o instanceof RoomFocus).stream()
                .map(Object::toString).collect(Collectors.toList()));
        strategic.put("recommendations", ks.getObjects(o -> o instanceof StrategicRecommendation).stream()
                .map(Object::toString).collect(Collectors.toList()));
        strategic.put("riskWarnings", ks.getObjects(o -> o instanceof RiskWarning).stream()
                .map(Object::toString).collect(Collectors.toList()));
        strategic.put("showRecommendations", ks.getObjects(o -> o instanceof ShowRecommendation).stream()
                .map(Object::toString).collect(Collectors.toList()));
        strategic.put("suggestCards", ks.getObjects(o -> o instanceof SuggestCard).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));
        strategic.put("highSuspicions", ks.getObjects(o -> o instanceof HighSuspicion).stream()
                .map(Object::toString).collect(Collectors.toList()));
        strategic.put("knowsCards", ks.getObjects(o -> o instanceof KnowsCard).stream()
                .map(Object::toString).collect(Collectors.toList()));
        strategic.put("optimalActions", ks.getObjects(o -> o instanceof OptimalAction).stream()
                .map(Object::toString).collect(Collectors.toList()));
        result.put("strategicOutput", strategic);

        result.put("table", buildTable(ks));

        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("totalRulesFired", firedInit + f1 + f2 + f3 + f4 + f5 );
        diagnostics.put("rulesPerPhase", Arrays.asList(firedInit, f1, f2, f3, f4, f5));
        diagnostics.put("backwardChainingTriggered", backwardSolved);
        result.put("diagnostics", diagnostics);

        System.out.println("========== /api/full ZAVRŠEN ==========\n");

        ks.dispose();
        return result;
    }

    /**
     * Eksplicitan poziv backward query "resenje" iz java koda.
     * Drools query-ji se ne pokreću automatski sa fireAllRules() -
     * moramo ih pozvati ručno kroz API.
     *
     * Ako query vrati rešenje, insertujemo GameResult i Solution činjenice
     * u radnu memoriju (kako bi ih dohvatili i prikazali u response-u),
     * pa pokrećemo fireAllRules() još jednom (za propagaciju).
     *
     * @return true ako je backward chaining našao rešenje
     */
    private boolean runBackwardQuery(KieSession ks) {
        try {
            QueryResults results = ks.getQueryResults(
                    "resenje",
                    org.kie.api.runtime.rule.Variable.v,
                    org.kie.api.runtime.rule.Variable.v,
                    org.kie.api.runtime.rule.Variable.v
            );
            if (results.size() == 0) {
                System.out.println("    Query 'resenje' nije našao rešenje "
                        + "(verovatno nedovoljno karata eliminisano).");
                return false;
            }
            QueryResultsRow row = results.iterator().next();
            Card suspect = (Card) row.get("$suspect");
            Card weapon = (Card) row.get("$weapon");
            Card room = (Card) row.get("$room");

            System.out.println("    Query 'resenje' našao: ("
                    + suspect.getName() + ", " + weapon.getName() + ", " + room.getName() + ")");

            // Insertuj rezultat u memoriju da bi se prikazao kasnije
            ks.insert(new GameResult(suspect, weapon, room, true));
            ks.insert(new Solution(suspect));
            ks.insert(new Solution(weapon));
            ks.insert(new Solution(room));

            // Propagiraj kroz forward chain (možda neka strateška pravila reaguju)
            int extraFired = ks.fireAllRules();
            System.out.println("    Posle backward propagacije: " + extraFired + " dodatnih pravila");

            return true;
        } catch (Exception e) {
            // Query možda ne postoji - to znači da backward_deduct.drl nije učitan
            System.err.println("    GREŠKA pri pokretanju 'resenje' query: " + e.getMessage());
            return false;
        }
    }

    // ========================================================================
    // Builder za PossibilityTable
    // ========================================================================
    private PossibilityTable buildTable(KieSession ks) {
        List<Card> allCardsList = ks.getObjects(o -> o instanceof Card).stream()
                .map(o -> (Card) o).collect(Collectors.toList());

        List<Player> allPlayersList = ks.getObjects(o -> o instanceof Player).stream()
                .map(o -> (Player) o).collect(Collectors.toList());
        allPlayersList.sort((p1, p2) -> {
            if (p1.isSelf()) return -1;
            if (p2.isSelf()) return 1;
            return p1.getName().compareTo(p2.getName());
        });

        Map<String, Integer> scoreMap = new HashMap<>();
        for (Object o : ks.getObjects(o -> o instanceof CardScore)) {
            CardScore cs = (CardScore) o;
            scoreMap.put(cs.getCard().getName(), cs.getScore());
        }

        Map<String, String> ownershipMap = new HashMap<>();
        for (Object o : ks.getObjects(o -> o instanceof Owns)) {
            Owns ow = (Owns) o;
            ownershipMap.put(ow.getPlayer().getName() + ":" + ow.getCard().getName(), "O");
        }
        for (Object o : ks.getObjects(o -> o instanceof NotOwns)) {
            NotOwns no = (NotOwns) o;
            String key = no.getPlayer().getName() + ":" + no.getCard().getName();
            ownershipMap.putIfAbsent(key, "X");
        }

        Set<String> solutionCards = new HashSet<>();
        for (Object o : ks.getObjects(o -> o instanceof Solution)) {
            solutionCards.add(((Solution) o).getCard().getName());
        }

        Map<CardType, List<Card>> cardsByType = new LinkedHashMap<>();
        cardsByType.put(CardType.SUSPECT, new ArrayList<>());
        cardsByType.put(CardType.WEAPON, new ArrayList<>());
        cardsByType.put(CardType.ROOM, new ArrayList<>());
        for (Card c : allCardsList) {
            cardsByType.get(c.getType()).add(c);
        }
        for (List<Card> cs : cardsByType.values()) {
            cs.sort((c1, c2) -> c1.getName().compareTo(c2.getName()));
        }

        PossibilityTable table = new PossibilityTable();
        table.players = allPlayersList.stream()
                .map(Player::getName).collect(Collectors.toList());
        table.scores = scoreMap;
        table.groups = new ArrayList<>();

        for (Map.Entry<CardType, List<Card>> entry : cardsByType.entrySet()) {
            PossibilityTable.RowGroup group = new PossibilityTable.RowGroup();
            group.category = entry.getKey().name();
            group.rows = new ArrayList<>();

            for (Card card : entry.getValue()) {
                PossibilityTable.Row row = new PossibilityTable.Row();
                row.cardName = card.getName();
                row.score = scoreMap.getOrDefault(card.getName(), 0);
                row.cells = new ArrayList<>();

                for (Player p : allPlayersList) {
                    String key = p.getName() + ":" + card.getName();
                    String cell = ownershipMap.get(key);
                    if (cell == null) {
                        cell = solutionCards.contains(card.getName()) ? "✓" : "?";
                    }
                    row.cells.add(cell);
                }
                group.rows.add(row);
            }
            table.groups.add(group);
        }

        // Tok + hints
        List<Suggestion> allSuggestions = ks.getObjects(o -> o instanceof Suggestion).stream()
                .map(o -> (Suggestion) o)
                .sorted((s1, s2) -> Integer.compare(s1.getTurnNumber(), s2.getTurnNumber()))
                .collect(Collectors.toList());
        List<NoShow> allNoShows = ks.getObjects(o -> o instanceof NoShow).stream()
                .map(o -> (NoShow) o).collect(Collectors.toList());
        List<PrivateShow> allPrivateShows = ks.getObjects(o -> o instanceof PrivateShow).stream()
                .map(o -> (PrivateShow) o).collect(Collectors.toList());
        List<Reveal> allReveals = ks.getObjects(o -> o instanceof Reveal).stream()
                .map(o -> (Reveal) o).collect(Collectors.toList());

        table.turns = new ArrayList<>();
        table.hints = new ArrayList<>();

        for (Suggestion sug : allSuggestions) {
            PossibilityTable.TurnEntry t = new PossibilityTable.TurnEntry();
            t.number = sug.getTurnNumber();
            t.suggester = sug.getSuggester().getName();
            t.suspect = sug.getSuspect().getName();
            t.weapon = sug.getWeapon().getName();
            t.room = sug.getRoom().getName();
            t.noShowPlayers = allNoShows.stream()
                    .filter(ns -> ns.getSuggestion() == sug)
                    .map(ns -> ns.getPlayer().getName())
                    .collect(Collectors.toList());

            if (sug.getSuggester().isSelf()) {
                for (Reveal r : allReveals) {
                    Card c = r.getCard();
                    if (c.equals(sug.getSuspect()) || c.equals(sug.getWeapon()) || c.equals(sug.getRoom())) {
                        t.shower = r.getRevealer().getName();
                        t.revealedCard = c.getName();
                        t.isPrivate = false;
                        break;
                    }
                }
            } else {
                for (PrivateShow ps : allPrivateShows) {
                    if (ps.getSuggestion() == sug) {
                        t.shower = ps.getShower().getName();
                        t.shownTo = ps.getRecipient().getName();
                        t.isPrivate = true;

                        PossibilityTable.DisjunctionEntry d = new PossibilityTable.DisjunctionEntry();
                        d.player = ps.getShower().getName();
                        d.candidateCards = Arrays.asList(
                                sug.getSuspect().getName(),
                                sug.getWeapon().getName(),
                                sug.getRoom().getName());
                        d.turnNumber = sug.getTurnNumber();
                        table.hints.add(d);
                        break;
                    }
                }
            }
            table.turns.add(t);
        }

        return table;
    }
}