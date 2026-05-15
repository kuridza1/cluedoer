package com.ftn.sbnz.service.controller;


import com.ftn.sbnz.model.*;
import com.ftn.sbnz.model.dto.PossibilityTable;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
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
 * Demo endpoints za CEP (Nivo 2 deduktivnog sistema).
 * GET /api/cep/forward  -> sirovi JSON sa Score-ovima nakon heurističkog skorovanja
 * GET /api/cep/table    -> tabela mogućnosti sa kumulativnim Score-ovima
 *
 * Koristi cepKSession iz kmodule.xml (stream mode + pseudo-clock).
 */
@RestController
@RequestMapping("/api/cep")
public class CepDemoController {

    private final KieContainer kieContainer;

    public CepDemoController(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    private KieSession setupAndFire() {
        KieSession ks = kieContainer.newKieSession("cepKSession");
        SessionPseudoClock clock = ks.getSessionClock();

        // ===== Karte =====
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

        // ===== Igrači =====
        Player ja = new Player("Ja", 3, true);
        Player A = new Player("A", 3, false);
        Player B = new Player("B", 3, false);
        Player C = new Player("C", 3, false);
        Player D = new Player("D", 3, false);
        Player E = new Player("E", 3, false);
        ks.insert(ja); ks.insert(A); ks.insert(B);
        ks.insert(C); ks.insert(D); ks.insert(E);

        // ===== Moje karte =====
        ks.insert(new Owns(ja, suspects.get(0))); // Scarlet
        ks.insert(new Owns(ja, weapons.get(0)));  // Bodez
        ks.insert(new Owns(ja, rooms.get(0)));    // Kuhinja
        ks.fireAllRules();

        // ========================================================================
        // 7 sugestija - dizajnirano da Score-ovi budu raznoliki
        // ========================================================================

        // Potez 1: A sugeriše (Mustard, Svecnjak, Salon)
        //          B preskočio (NoShow), C pokazao A-u (PrivateShow)
        long t = clock.getCurrentTime();
        Suggestion sug1 = new Suggestion(1, t, A,
                suspects.get(1), weapons.get(1), rooms.get(1));
        ks.insert(sug1);
        ks.insert(new NoShow(B, sug1, t));
        ks.insert(new PrivateShow(C, A, sug1, t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        ks.fireAllRules();

        // Potez 2: B sugeriše (Plum, Konopac, Biblioteka)  ← Konopac, Plum 1. pojava
        //          C preskočio, D pokazao B-u (PrivateShow)
        t = clock.getCurrentTime();
        Suggestion sug2 = new Suggestion(2, t, B,
                suspects.get(3), weapons.get(3), rooms.get(3));
        ks.insert(sug2);
        ks.insert(new NoShow(C, sug2, t));
        ks.insert(new PrivateShow(D, B, sug2, t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        ks.fireAllRules();

        // Potez 3: C sugeriše (Green, OlovnaCev, RadnaSoba)
        //          SVI preskočili (D, E, Ja) - "blizu istine" -> 5.2.3b okida
        t = clock.getCurrentTime();
        Suggestion sug3 = new Suggestion(3, t, C,
                suspects.get(2), weapons.get(4), rooms.get(4));
        ks.insert(sug3);
        ks.insert(new NoShow(D, sug3, t));
        ks.insert(new NoShow(E, sug3, t));
        ks.insert(new NoShow(ja, sug3, t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        ks.fireAllRules();

        // Potez 4: D sugeriše (Plum, Konopac, Trpezarija)  ← Konopac 2x, Plum 2x
        //          E pokazao D-u (PrivateShow)
        t = clock.getCurrentTime();
        Suggestion sug4 = new Suggestion(4, t, D,
                suspects.get(3), weapons.get(3), rooms.get(2));
        ks.insert(sug4);
        ks.insert(new PrivateShow(E, D, sug4, t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        ks.fireAllRules();

        // Potez 5: E sugeriše (Peacock, Konopac, Plesnjak)  ← Konopac 3x ✓ okida 5.2.3a
        //          Ja preskočila, A pokazao E-u (PrivateShow)
        t = clock.getCurrentTime();
        Suggestion sug5 = new Suggestion(5, t, E,
                suspects.get(4), weapons.get(3), rooms.get(6));
        ks.insert(sug5);
        ks.insert(new NoShow(ja, sug5, t));
        ks.insert(new PrivateShow(A, E, sug5, t));
        clock.advanceTime(1, TimeUnit.MINUTES);
        ks.fireAllRules();

        // Potez 6: JA sugerišem (Peacock, Revolver, Biblioteka)
        //          A preskočio, B pokazao MENI Biblioteku -> Reveal (sigurno NIJE u koverti)
        t = clock.getCurrentTime();
        Suggestion sug6 = new Suggestion(6, t, ja,
                suspects.get(4), weapons.get(2), rooms.get(3));
        ks.insert(sug6);
        ks.insert(new NoShow(A, sug6, t));
        ks.insert(new Reveal(B, rooms.get(3), t)); // Biblioteka
        clock.advanceTime(1, TimeUnit.MINUTES);
        ks.fireAllRules();

        // Potez 7: JA opet sugerišem (White, Kljuc, Hodnik)
        //          A preskočio, B preskočio, C pokazao MENI Kljuc -> Reveal (sigurno NIJE)
        t = clock.getCurrentTime();
        Suggestion sug7 = new Suggestion(7, t, ja,
                suspects.get(5), weapons.get(5), rooms.get(7));
        ks.insert(sug7);
        ks.insert(new NoShow(A, sug7, t));
        ks.insert(new NoShow(B, sug7, t));
        ks.insert(new Reveal(C, weapons.get(5), t)); // Kljuc
        clock.advanceTime(1, TimeUnit.MINUTES);
        ks.fireAllRules();

        return ks;
    }
    @GetMapping("/forward")
    public Map<String, Object> runCepDemo() {
        KieSession ks = setupAndFire();

        Map<String, Object> result = new HashMap<>();
        result.put("scores", ks.getObjects(o -> o instanceof CardScore).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));
        result.put("owns", ks.getObjects(o -> o instanceof Owns).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));
        result.put("notOwns", ks.getObjects(o -> o instanceof NotOwns).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));
        result.put("solutions", ks.getObjects(o -> o instanceof Solution).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));

        ks.dispose();
        return result;
    }

    @GetMapping("/table")
    public PossibilityTable runCepTable() {
        KieSession ks = setupAndFire();

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
// ===== Tok partije i slabe činjenice =====
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
                // Ja sugerišem -> tražim Reveal za neku od tri karte iz ove sugestije
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
                // Drugi sugeriše -> tražim PrivateShow
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
        ks.dispose();
        return table;
    }
}