package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.model.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Smoke test endpoint za Nivo 1 forward chaining.
 * Simulira mali deo partije i vraća izvedene zaključke.
 *
 * Test: GET /api/demo/forward
 */
@RestController
@RequestMapping("/api/demo")
public class ForwardDemoController {

    private final KieContainer kieContainer;

    public ForwardDemoController(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @GetMapping("/forward")
    public DemoResult runForwardDemo() {
        KieSession ks = kieContainer.newKieSession("cluedoKSession");

        // --- 1. Inicijalni skup karata (minimalni demo - 3 po kategoriji) ---
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
        allCards.addAll(suspects); allCards.addAll(weapons); allCards.addAll(rooms);

        // Inicijalni Score = 1 za svaku kartu
        for (Card c : allCards) {
            ks.insert(c);
            ks.insert(new CardScore(c, 1));
        }

        // --- 2. Igrači ---
        // 9 karata ukupno, 3 u koverti, 6 u rukama -> npr. 3-3 podela kod dva protivnika
        // ali stavićemo 3 igrača (ja + 2 protivnika) sa po 2 karte = 6 ukupno u rukama
        Player ja = new Player("Ja", 2, true);
        Player A = new Player("A", 2, false);
        Player B = new Player("B", 2, false);
        ks.insert(ja); ks.insert(A); ks.insert(B);

        // --- 3. Sopstvene karte ---
        // Pravilo 5.1.1: Score(Scarlet) i Score(Bodez) treba da postanu 0
        ks.insert(new Owns(ja, suspects.get(0))); // ja imam Scarlet
        ks.insert(new Owns(ja, weapons.get(0)));  // ja imam Bodez

        // --- 4. Opservacije iz toka igre ---
        // Pravilo 5.1.2b: A pokazao Mustard -> Owns(A, Mustard), Score=0
        ks.insert(new Reveal(A, suspects.get(1)));

        // Pravilo 5.1.2d: B nije pokazao na sugestiju (Green, Konopac, Kuhinja)
        // -> NotOwns(B, Green), NotOwns(B, Konopac), NotOwns(B, Kuhinja)
        Suggestion sug1 = new Suggestion(ja,
                suspects.get(2), weapons.get(1), rooms.get(0));
        ks.insert(sug1);
        ks.insert(new NoShow(B, sug1));

        // --- 5. Pokreni forward chaining ---
        int fired = ks.fireAllRules();

        // --- 6. Skupi rezultate ---
        DemoResult result = new DemoResult();
        result.firedRules = fired;
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

    public static class DemoResult {
        public int firedRules;
        public List<String> scores;
        public List<String> owns;
        public List<String> notOwns;
        public List<String> solutions;
    }
}
