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

        // ========================================================================
        // 1. Karte (pun Cluedo skup: 6 + 6 + 9 = 21)
        // ========================================================================
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

        // ========================================================================
        // 2. Igrači (6 × 3 = 18 u rukama + 3 u koverti)
        // ========================================================================
        Player ja = new Player("Ja", 3, true);
        Player A = new Player("A", 3, false);
        Player B = new Player("B", 3, false);
        Player C = new Player("C", 3, false);
        Player D = new Player("D", 3, false);
        Player E = new Player("E", 3, false);

        ks.insert(ja); ks.insert(A); ks.insert(B);
        ks.insert(C); ks.insert(D); ks.insert(E);

        // ========================================================================
        // 3. Moje karte u ruci (Scarlet, Bodez, Kuhinja)
        // ========================================================================
        ks.insert(new Owns(ja, suspects.get(0))); // Scarlet
        ks.insert(new Owns(ja, weapons.get(0)));  // Bodez
        ks.insert(new Owns(ja, rooms.get(0)));    // Kuhinja

        // ========================================================================
        // 4. Tok partije - 5 sugestija
        // ========================================================================

        // --- Potez 1: A sugeriše (Mustard, Svecnjak, Salon)
        //     B preskočio, C pokazao A-u (ja ne vidim šta)
        Suggestion sug1 = new Suggestion(
                A,
                suspects.get(1),   // Mustard
                weapons.get(1),    // Svecnjak
                rooms.get(1));     // Salon
        ks.insert(sug1);
        ks.insert(new NoShow(B, sug1));
        ks.insert(new PrivateShow(C, A, sug1));   // C je pokazao A-u, ja ne vidim

        // --- Potez 2: B sugeriše (Green, Konopac, Biblioteka)
        //     C pokazao B-u (ja ne vidim šta)
        Suggestion sug2 = new Suggestion(
                B,
                suspects.get(2),   // Green
                weapons.get(3),    // Konopac
                rooms.get(3));     // Biblioteka
        ks.insert(sug2);
        ks.insert(new PrivateShow(C, B, sug2));   // C je pokazao B-u

        // --- Potez 3: D sugeriše (Plum, OlovnaCev, RadnaSoba)
        //     E pokazao D-u (ja ne vidim šta)
        Suggestion sug3 = new Suggestion(
                D,
                suspects.get(3),   // Plum
                weapons.get(4),    // OlovnaCev
                rooms.get(4));     // RadnaSoba
        ks.insert(sug3);
        ks.insert(new PrivateShow(E, D, sug3));   // E je pokazao D-u

        // --- Potez 4: E sugeriše (Peacock, Revolver, BilijarSoba)
        //     Ja nemam ništa od toga → NoShow(ja)
        //     A pokazao E-u (ja ne vidim šta)
        Suggestion sug4 = new Suggestion(
                E,
                suspects.get(4),   // Peacock
                weapons.get(2),    // Revolver
                rooms.get(5));     // BilijarSoba
        ks.insert(sug4);
        ks.insert(new NoShow(ja, sug4));
        ks.insert(new PrivateShow(A, E, sug4));   // A je pokazao E-u

        // --- Potez 5: JA sugerišem (White, Kljuc, Plesnjak)
        //     A nema → NoShow
        //     B pokazao MENI Kljuc → ovde imamo PRAVI Reveal
        Suggestion sug5 = new Suggestion(
                ja,
                suspects.get(5),   // White
                weapons.get(5),    // Kljuc
                rooms.get(6));     // Plesnjak
        ks.insert(sug5);
        ks.insert(new NoShow(A, sug5));
        ks.insert(new Reveal(B, weapons.get(5)));  // B mi je pokazao Kljuc
        ks.insert(new Reveal(A, rooms.get(1)));
        ks.insert(new Reveal(A, rooms.get(1)));
        ks.insert(new Reveal(A, rooms.get(1)));

        // ========================================================================
        // 5. Forward chaining
        // ========================================================================
        ks.fireAllRules();
        return ks;
    }

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
        result.gameStates = ks.getObjects(o -> o instanceof GameState)
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        result.priorities = ks.getObjects(o -> o instanceof PriorityCard)
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        result.profiles = ks.getObjects(o -> o instanceof Profile)
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        result.roomFocuses = ks.getObjects(o -> o instanceof RoomFocus)
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        result.strategicRecommendations =
                ks.getObjects(o -> o instanceof StrategicRecommendation)
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());

        ks.dispose();
        return result;
    }

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

        public List<String> gameStates;
        public List<String> priorities;
        public List<String> profiles;
        public List<String> roomFocuses;
        public List<String> strategicRecommendations;
    }
}