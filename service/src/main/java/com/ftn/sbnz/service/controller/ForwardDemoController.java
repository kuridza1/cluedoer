package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.model.*;
import com.ftn.sbnz.model.dto.PossibilityTable;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
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
import java.util.stream.Collectors;

/**
 * Demo endpoints za Nivo 1 forward chaining.
 * GET /api/demo/forward  -> sirovi JSON (scores, owns, notOwns, solutions)
 * GET /api/demo/table    -> strukturirana tabela mogućnosti
 */
@RestController
@RequestMapping("/api/demo")
public class ForwardDemoController {

    private final KieContainer kieContainer;

    public ForwardDemoController(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    // ========================================================================
    // Setup logika - kreira sesiju, ubacuje sve činjenice, pokreće pravila.
    // Vraća sesiju spremnu za ispitivanje.
    // ========================================================================
    private KieSession setupAndFire() {
        KieSession ks = kieContainer.newKieSession("cluedoKSession");

        // --- 1. Karte ---
        List<Card> suspects = Arrays.asList(
                new Card(CardType.SUSPECT, "Scarlet"),
                new Card(CardType.SUSPECT, "Mustard"),
                new Card(CardType.SUSPECT, "Green"),
                new Card(CardType.SUSPECT, "Plum"),
                new Card(CardType.SUSPECT, "Peacock"),
                new Card(CardType.SUSPECT, "White")
        );
        List<Card> weapons = Arrays.asList(
                new Card(CardType.WEAPON, "Bodez"),
                new Card(CardType.WEAPON, "Svecnjak"),
                new Card(CardType.WEAPON, "Revolver"),
                new Card(CardType.WEAPON, "Konopac"),
                new Card(CardType.WEAPON, "OlovnaCev"),
                new Card(CardType.WEAPON, "Kljuc")
        );
        List<Card> rooms = Arrays.asList(
                new Card(CardType.ROOM, "Kuhinja"),
                new Card(CardType.ROOM, "Salon"),
                new Card(CardType.ROOM, "Trpezarija"),
                new Card(CardType.ROOM, "Biblioteka"),
                new Card(CardType.ROOM, "RadnaSoba"),
                new Card(CardType.ROOM, "BilijarSoba"),
                new Card(CardType.ROOM, "Plesnjak"),
                new Card(CardType.ROOM, "Hodnik"),
                new Card(CardType.ROOM, "Trem")
        );

        List<Card> allCards = new ArrayList<>();
        allCards.addAll(suspects);
        allCards.addAll(weapons);
        allCards.addAll(rooms);

        for (Card c : allCards) {
            ks.insert(c);
            ks.insert(new CardScore(c, 1));
        }

        // --- 2. Igrači ---
        Player ja = new Player("Ja", 3, true);
        Player A = new Player("A", 3, false);
        Player B = new Player("B", 3, false);
        Player C = new Player("C", 3, false);
        Player D = new Player("D", 3, false);
        Player E = new Player("E", 3, false);

        ks.insert(ja); ks.insert(A); ks.insert(B);
        ks.insert(C); ks.insert(D); ks.insert(E);

        // --- 3. Sopstvene karte ---
        ks.insert(new Owns(ja, suspects.get(0))); // Scarlet
        ks.insert(new Owns(ja, weapons.get(0)));  // Bodez
        ks.insert(new Owns(ja, rooms.get(0)));    // Kuhinja

        // --- 4. Opservacije ---
        ks.insert(new Reveal(A, suspects.get(1)));  // Mustard
        ks.insert(new Reveal(A, weapons.get(1)));   // Svecnjak
        ks.insert(new Reveal(B, suspects.get(2)));  // Green
        ks.insert(new Reveal(C, weapons.get(2)));   // Revolver
        ks.insert(new Reveal(D, rooms.get(1)));     // Salon
        ks.insert(new Reveal(E, rooms.get(2)));     // Trpezarija

        Suggestion sug1 = new Suggestion(ja,
                suspects.get(3), weapons.get(4), rooms.get(3));
        ks.insert(sug1);
        ks.insert(new NoShow(A, sug1));
        ks.insert(new NoShow(B, sug1));

        Suggestion sug2 = new Suggestion(A,
                suspects.get(4), weapons.get(3), rooms.get(4));
        ks.insert(sug2);
        ks.insert(new NoShow(C, sug2));
        ks.insert(new NoShow(D, sug2));

        Suggestion sug3 = new Suggestion(B,
                suspects.get(5), weapons.get(5), rooms.get(5));
        ks.insert(sug3);
        ks.insert(new NoShow(E, sug3));

        // --- 5. Forward chaining ---
        ks.fireAllRules();
        return ks;
    }

    // ========================================================================
    // GET /api/demo/forward - sirovi pregled činjenica
    // ========================================================================
    @GetMapping("/forward")
    public DemoResult runForwardDemo() {
        KieSession ks = setupAndFire();

        DemoResult result = new DemoResult();
        result.firedRules = -1; // setupAndFire već okinuo, broj se gubi - nebitno za demo
        result.scores = ks.getObjects(o -> o instanceof CardScore).stream()
                .map(Object::toString).sorted().collect(Collectors.toList());
        result.owns = ks.getObjects(o -> o instanceof Owns).stream()
                .map(Object::toString).sorted().collect(Collectors.toList());
        result.notOwns = ks.getObjects(o -> o instanceof NotOwns).stream()
                .map(Object::toString).sorted().collect(Collectors.toList());
        result.solutions = ks.getObjects(o -> o instanceof Solution).stream()
                .map(Object::toString).sorted().collect(Collectors.toList());

        ks.dispose();
        return result;
    }

    // ========================================================================
    // GET /api/demo/table - strukturirana tabela mogućnosti
    // ========================================================================
    @GetMapping("/table")
    public PossibilityTable runForwardTable() {
        KieSession ks = setupAndFire();

        // Karte
        List<Card> allCardsList = ks.getObjects(o -> o instanceof Card).stream()
                .map(o -> (Card) o).collect(Collectors.toList());

        // Igrači - Ja prvo, ostali abecedno
        List<Player> allPlayersList = ks.getObjects(o -> o instanceof Player).stream()
                .map(o -> (Player) o).collect(Collectors.toList());
        allPlayersList.sort((p1, p2) -> {
            if (p1.isSelf()) return -1;
            if (p2.isSelf()) return 1;
            return p1.getName().compareTo(p2.getName());
        });

        // Score mapa
        Map<String, Integer> scoreMap = new HashMap<>();
        for (Object o : ks.getObjects(o -> o instanceof CardScore)) {
            CardScore cs = (CardScore) o;
            scoreMap.put(cs.getCard().getName(), cs.getScore());
        }

        // (igrač, karta) -> "O" ili "X"
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

        // Solution
        Set<String> solutionCards = new HashSet<>();
        for (Object o : ks.getObjects(o -> o instanceof Solution)) {
            solutionCards.add(((Solution) o).getCard().getName());
        }

        // Grupiši karte po kategoriji
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

        // Popuni DTO
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

        ks.dispose();
        return table;
    }

    // ========================================================================
    // DTO za sirovi endpoint
    // ========================================================================
    public static class DemoResult {
        public int firedRules;
        public List<String> scores;
        public List<String> owns;
        public List<String> notOwns;
        public List<String> solutions;
    }
}