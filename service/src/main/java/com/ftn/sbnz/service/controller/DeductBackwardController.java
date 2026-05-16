package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.model.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demo endpoint za backward chaining (Nivo 3 deduktivnog sistema).
 * GET /api/backward/verify -> pokreće verifikaciju i vraća da li je rešenje dokazano.
 *
 * Scenario je dizajniran tako da je partija SKORO rešena - dovoljno karata
 * je eliminisano da backward chaining može da potvrdi finalnu trojku.
 */
@RestController
@RequestMapping("/api/deduct/backward")
public class DeductBackwardController {

    private final KieContainer kieContainer;

    public DeductBackwardController(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @GetMapping("/verify")
    public Map<String, Object> verify() {
        KieSession ks = kieContainer.newKieSession("cluedoKSession");

        // ===== Karte: koristimo manji skup (3 po kategoriji) =====
        // Manji skup čini lakšim da partija bude "skoro rešena" za demo.
        List<Card> suspects = Arrays.asList(
                new Card(CardType.SUSPECT, "Scarlet"),
                new Card(CardType.SUSPECT, "Mustard"),
                new Card(CardType.SUSPECT, "Green")
        );
        List<Card> weapons = Arrays.asList(
                new Card(CardType.WEAPON, "Bodez"),
                new Card(CardType.WEAPON, "Konopac"),
                new Card(CardType.WEAPON, "Revolver")
        );
        List<Card> rooms = Arrays.asList(
                new Card(CardType.ROOM, "Kuhinja"),
                new Card(CardType.ROOM, "Biblioteka"),
                new Card(CardType.ROOM, "Salon")
        );

        List<Card> allCards = new ArrayList<>();
        allCards.addAll(suspects);
        allCards.addAll(weapons);
        allCards.addAll(rooms);
        for (Card c : allCards) {
            ks.insert(c);
            ks.insert(new CardScore(c, 1));
        }

        // ===== Igrači: ja + 2 protivnika, po 3 karte (9 - 3 koverte = 6 u rukama) =====
        Player ja = new Player("Ja", 3, true);
        Player A = new Player("A", 3, false);
        Player B = new Player("B", 3, false);
        ks.insert(ja); ks.insert(A); ks.insert(B);

        // ===== Moje karte: Scarlet, Bodez, Kuhinja =====
        ks.insert(new Owns(ja, suspects.get(0)));  // Scarlet
        ks.insert(new Owns(ja, weapons.get(0)));   // Bodez
        ks.insert(new Owns(ja, rooms.get(0)));     // Kuhinja

        // ===== Opservacije: pokazane karte tokom partije =====
        // A poseduje: Mustard, Revolver, Salon  (svi mi pokazani)
        ks.insert(new Reveal(A, suspects.get(1))); // Mustard
        ks.insert(new Reveal(A, weapons.get(2)));  // Revolver
        ks.insert(new Reveal(A, rooms.get(2)));    // Salon

        // Posle ovih opservacija u koverti ostaje samo:
        //   Suspect: Green (jer Scarlet=moj, Mustard=A)
        //   Weapon:  Konopac (jer Bodez=moj, Revolver=A)
        //   Room:    Biblioteka (jer Kuhinja=moja, Salon=A)
        // B-ove tri karte se izvode automatski (preostale).

        // ===== Pokreni: prvo forward (Nivo 1), pa backward (Nivo 3) =====
        int fired = ks.fireAllRules();

        // ===== Skupi rezultat =====
        Map<String, Object> result = new HashMap<>();
        result.put("firedRules", fired);

        List<GameResult> gameResults = ks.getObjects(o -> o instanceof GameResult).stream()
                .map(o -> (GameResult) o).collect(Collectors.toList());

        if (!gameResults.isEmpty()) {
            GameResult gr = gameResults.get(0);
            result.put("solved", gr.isSolved());
            Map<String, String> solution = new HashMap<>();
            solution.put("suspect", gr.getSuspect().getName());
            solution.put("weapon", gr.getWeapon().getName());
            solution.put("room", gr.getRoom().getName());
            result.put("solution", solution);
        } else {
            result.put("solved", false);
            result.put("solution", null);
        }

        result.put("owns", ks.getObjects(o -> o instanceof Owns).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));
        result.put("solutions", ks.getObjects(o -> o instanceof Solution).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));

        ks.dispose();
        return result;
    }
}