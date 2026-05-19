package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.model.*;
import com.ftn.sbnz.model.dto.PossibilityTable;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.api.runtime.rule.Variable;
import org.kie.api.time.SessionPseudoClock;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Stateful Cluedo controller — drži jednu KieSession u memoriji.
 *
 * Flow:
 *   1. POST /api/game/start      - inicijalizuj karte, igrače, moje karte
 *   2. POST /api/game/suggestion - unesi potez (sugestija + odgovori)
 *   3. GET  /api/game/state      - snapshot stanja (tabela + strateški + backward)
 *   4. POST /api/game/reset      - obriši sesiju
 *
 * Napomena: ovaj kontroler radi sa jednom globalnom igrom — nije
 * multi-user. Za potrebe demonstracije projekta to je sasvim dovoljno.
 */
@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final KieContainer kieContainer;

    private KieSession ks;
    private SessionPseudoClock clock;
    private int turnCounter = 0;

    // Lookup mape za brže pronalaženje fact-ova po imenu
    private final Map<String, Card> cardsByName = new LinkedHashMap<>();
    private final Map<String, Player> playersByName = new LinkedHashMap<>();

    public GameController(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    // ========================================================================
    // DTO klase za ulaz
    // ========================================================================
    public static class StartRequest {
        public List<String> suspects;
        public List<String> weapons;
        public List<String> rooms;
        public List<PlayerInput> players;
        public List<String> myCards;
        public String myName;
    }

    public static class PlayerInput {
        public String name;
        public int handSize;
        public boolean self;
    }

    public static class SuggestionRequest {
        public String suggester;
        public String suspect;
        public String weapon;
        public String room;
        public List<String> noShowPlayers;
        public String shower;       // ako je private show ili reveal
        public String revealedCard; // null ako je private (ja nisam ni predlagac ni pokazivac)
    }

    // ========================================================================
    // ENDPOINTS
    // ========================================================================
    @PostMapping("/start")
    public Map<String, Object> start(@RequestBody StartRequest req) {
        if (ks != null) {
            ks.dispose();
        }
        cardsByName.clear();
        playersByName.clear();
        turnCounter = 0;

        ks = kieContainer.newKieSession("cluedoKSession");
        clock = ks.getSessionClock();

        // Karte
        for (String s : req.suspects) {
            Card c = new Card(CardType.SUSPECT, s);
            cardsByName.put(s, c);
            ks.insert(c);
            ks.insert(new CardScore(c, 1));
        }
        for (String w : req.weapons) {
            Card c = new Card(CardType.WEAPON, w);
            cardsByName.put(w, c);
            ks.insert(c);
            ks.insert(new CardScore(c, 1));
        }
        for (String r : req.rooms) {
            Card c = new Card(CardType.ROOM, r);
            cardsByName.put(r, c);
            ks.insert(c);
            ks.insert(new CardScore(c, 1));
        }

        // Igrači
        Player me = null;
        for (PlayerInput pi : req.players) {
            Player p = new Player(pi.name, pi.handSize, pi.self);
            playersByName.put(pi.name, p);
            ks.insert(p);
            if (pi.self) me = p;
        }

        // Moje karte
        if (me != null) {
            for (String cardName : req.myCards) {
                Card c = cardsByName.get(cardName);
                if (c != null) {
                    ks.insert(new Owns(me, c));
                }
            }
        }

        int fired = ks.fireAllRules();
        System.out.println("[/start] inicijalna pravila: " + fired);

        return snapshot();
    }

    @PostMapping("/suggestion")
    public Map<String, Object> suggestion(@RequestBody SuggestionRequest req) {
        if (ks == null) {
            throw new IllegalStateException("Igra nije pokrenuta. Pozovi /api/game/start prvo.");
        }

        turnCounter++;
        long t = clock.getCurrentTime();

        Player suggester = playersByName.get(req.suggester);
        Card suspect = cardsByName.get(req.suspect);
        Card weapon = cardsByName.get(req.weapon);
        Card room = cardsByName.get(req.room);

        if (suggester == null || suspect == null || weapon == null || room == null) {
            throw new IllegalArgumentException("Nepoznat igrač ili karta.");
        }

        Suggestion sug = new Suggestion(turnCounter, t, suggester, suspect, weapon, room);
        ks.insert(sug);

        // NoShow — zastita: shower nikad ne sme biti i u NoShow listi
        if (req.noShowPlayers != null) {
            for (String pname : req.noShowPlayers) {
                if (pname.equals(req.shower)) {
                    System.out.println("[/suggestion] preskacem NoShow za " + pname
                            + " jer je on pokazivac");
                    continue;
                }
                Player p = playersByName.get(pname);
                if (p != null) ks.insert(new NoShow(p, sug, t));
            }
        }

        if (req.shower != null) {
            Player shower = playersByName.get(req.shower);
            if (shower != null) {
                if (req.revealedCard != null) {
                    // Bilo ja predlagač, bilo ja pokazivač - znam tačno koja karta
                    Card revealed = cardsByName.get(req.revealedCard);
                    if (revealed != null) {
                        ks.insert(new Reveal(shower, revealed, t));
                    }
                } else {
                    // Niti predlažem niti pokazujem - vidim samo da je nešto pokazano
                    ks.insert(new PrivateShow(shower, suggester, sug, t));
                }
            }
        }
        clock.advanceTime(1, TimeUnit.MINUTES);
        int fired = ks.fireAllRules();
        System.out.println("[/suggestion] potez " + turnCounter + ", pravila: " + fired);

        return snapshot();
    }

    @GetMapping("/state")
    public Map<String, Object> state() {
        if (ks == null) {
            return Map.of("error", "Igra nije pokrenuta.");
        }
        return snapshot();
    }

    @PostMapping("/reset")
    public Map<String, String> reset() {
        if (ks != null) {
            ks.dispose();
            ks = null;
        }
        cardsByName.clear();
        playersByName.clear();
        turnCounter = 0;
        return Map.of("status", "reset");
    }

    // ========================================================================
    // SNAPSHOT (kombinuje knowledge + strategic + tabelu + backward chaining)
    // ========================================================================
    private Map<String, Object> snapshot() {
        Map<String, Object> result = new LinkedHashMap<>();

        // Knowledge snapshot (Sistem 1)
        Map<String, Object> knowledge = new LinkedHashMap<>();
        knowledge.put("scores", ks.getObjects(o -> o instanceof CardScore).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));
        knowledge.put("owns", ks.getObjects(o -> o instanceof Owns).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));
        knowledge.put("notOwns", ks.getObjects(o -> o instanceof NotOwns).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));
        knowledge.put("solutions", ks.getObjects(o -> o instanceof Solution).stream()
                .map(Object::toString).sorted().collect(Collectors.toList()));
        knowledge.put("gameResult", ks.getObjects(o -> o instanceof GameResult).stream()
                .map(Object::toString).collect(Collectors.toList()));
        knowledge.put("backwardChaining", runBackwardChaining());
        result.put("knowledgeSnapshot", knowledge);

        // Strateški snapshot (Sistem 2)
        Map<String, Object> strategic = new LinkedHashMap<>();
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

        // Tabela mogućnosti
        result.put("table", buildTable());

        // Meta za UI (lista karata i igrača za dropdown-ove)
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("players", playersByName.keySet());
        Map<String, List<String>> cards = new LinkedHashMap<>();
        cards.put("SUSPECT", cardsByName.values().stream()
                .filter(c -> c.getType() == CardType.SUSPECT)
                .map(Card::getName).collect(Collectors.toList()));
        cards.put("WEAPON", cardsByName.values().stream()
                .filter(c -> c.getType() == CardType.WEAPON)
                .map(Card::getName).collect(Collectors.toList()));
        cards.put("ROOM", cardsByName.values().stream()
                .filter(c -> c.getType() == CardType.ROOM)
                .map(Card::getName).collect(Collectors.toList()));
        meta.put("cards", cards);
        meta.put("turnCounter", turnCounter);
        result.put("meta", meta);

        return result;
    }

    // ========================================================================
    // BACKWARD CHAINING
    // ========================================================================
    private Map<String, Object> runBackwardChaining() {
        Map<String, Object> bc = new LinkedHashMap<>();
        bc.put("solutionFound", false);

        try {
            QueryResults results = ks.getQueryResults(
                    "solutionKnown", Variable.v, Variable.v, Variable.v);
            if (results.size() > 0) {
                QueryResultsRow row = results.iterator().next();
                Card suspect = (Card) row.get("$suspect");
                Card weapon = (Card) row.get("$weapon");
                Card room = (Card) row.get("$room");

                ks.insert(new GameResult(suspect, weapon, room, true));
                ks.fireAllRules();

                bc.put("solutionFound", true);
                Map<String, String> sol = new LinkedHashMap<>();
                sol.put("suspect", suspect.getName());
                sol.put("weapon", weapon.getName());
                sol.put("room", room.getName());
                bc.put("solution", sol);
            }
        } catch (Exception e) {
            bc.put("solutionKnownError", e.getMessage());
        }

        List<String> envelopeCards = new ArrayList<>();
        try {
            for (Object o : ks.getObjects(obj -> obj instanceof Card)) {
                Card c = (Card) o;
                QueryResults qr = ks.getQueryResults("isInEnvelope", c);
                if (qr.size() > 0) envelopeCards.add(c.getName());
            }
        } catch (Exception ignored) {}
        bc.put("cardsInEnvelope", envelopeCards);

        List<String> onlyCandidates = new ArrayList<>();
        try {
            for (Object o : ks.getObjects(obj -> obj instanceof Card)) {
                Card c = (Card) o;
                QueryResults qr = ks.getQueryResults("isOnlyCandidateInCategory", c);
                if (qr.size() > 0) onlyCandidates.add(c.getName());
            }
        } catch (Exception ignored) {}
        bc.put("onlyCandidatesInCategory", onlyCandidates);

        Map<String, List<String>> ownersMap = new LinkedHashMap<>();
        try {
            for (Object o : ks.getObjects(obj -> obj instanceof Card)) {
                Card c = (Card) o;
                QueryResults qr = ks.getQueryResults("possibleOwners", c, Variable.v);
                List<String> owners = new ArrayList<>();
                for (QueryResultsRow row : qr) {
                    Player p = (Player) row.get("$p");
                    owners.add(p.getName());
                }
                if (!owners.isEmpty()) ownersMap.put(c.getName(), owners);
            }
        } catch (Exception ignored) {}
        bc.put("possibleOwnersPerCard", ownersMap);

        return bc;
    }

    // ========================================================================
    // PossibilityTable builder (preuzeto iz FullDemoController)
    // ========================================================================
    private PossibilityTable buildTable() {
        List<Card> allCardsList = new ArrayList<>(cardsByName.values());
        List<Player> allPlayersList = new ArrayList<>(playersByName.values());
        allPlayersList.sort((p1, p2) -> {
            if (p1.isSelf()) return -1;
            if (p2.isSelf()) return 1;
            return p1.getName().compareTo(p2.getName());
        });

        Map<String, Integer> scoreMap = new HashMap<>();
        for (Object o : ks.getObjects(obj -> obj instanceof CardScore)) {
            CardScore cs = (CardScore) o;
            scoreMap.put(cs.getCard().getName(), cs.getScore());
        }

        Map<String, String> ownershipMap = new HashMap<>();
        for (Object o : ks.getObjects(obj -> obj instanceof Owns)) {
            Owns ow = (Owns) o;
            ownershipMap.put(ow.getPlayer().getName() + ":" + ow.getCard().getName(), "O");
        }
        for (Object o : ks.getObjects(obj -> obj instanceof NotOwns)) {
            NotOwns no = (NotOwns) o;
            String key = no.getPlayer().getName() + ":" + no.getCard().getName();
            ownershipMap.putIfAbsent(key, "X");
        }

        Set<String> solutionCards = new HashSet<>();
        for (Object o : ks.getObjects(obj -> obj instanceof Solution)) {
            solutionCards.add(((Solution) o).getCard().getName());
        }

        Map<CardType, List<Card>> cardsByType = new LinkedHashMap<>();
        cardsByType.put(CardType.SUSPECT, new ArrayList<>());
        cardsByType.put(CardType.WEAPON, new ArrayList<>());
        cardsByType.put(CardType.ROOM, new ArrayList<>());
        for (Card c : allCardsList) {
            cardsByType.get(c.getType()).add(c);
        }

        PossibilityTable table = new PossibilityTable();
        table.players = allPlayersList.stream().map(Player::getName).collect(Collectors.toList());
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
            PossibilityTable.TurnEntry te = new PossibilityTable.TurnEntry();
            te.number = sug.getTurnNumber();
            te.suggester = sug.getSuggester().getName();
            te.suspect = sug.getSuspect().getName();
            te.weapon = sug.getWeapon().getName();
            te.room = sug.getRoom().getName();
            te.noShowPlayers = allNoShows.stream()
                    .filter(ns -> ns.getSuggestion() == sug)
                    .map(ns -> ns.getPlayer().getName())
                    .collect(Collectors.toList());

            // -----------------------------------------------------------------
            // POPRAVKA: Reveal se trazi UVEK (ne samo kad sam ja predlagac),
            // jer Reveal nastaje i kad sam ja pokazivac. Vezivanje za sugestiju
            // ide preko (1) karta je jedna od tri u sugestiji i (2) timestamp
            // se poklapa sa timestamp-om sugestije.
            // -----------------------------------------------------------------
            boolean foundReveal = false;
            for (Reveal r : allReveals) {
                Card c = r.getCard();
                boolean cardMatches = c.equals(sug.getSuspect())
                        || c.equals(sug.getWeapon())
                        || c.equals(sug.getRoom());
                if (cardMatches && r.getTimestamp() == sug.getTimestamp()) {
                    te.shower = r.getRevealer().getName();
                    te.revealedCard = c.getName();
                    te.isPrivate = false;
                    foundReveal = true;
                    break;
                }
            }

            // Ako nema Reveal-a, trazi PrivateShow (niti sam predlagac niti pokazivac)
            if (!foundReveal) {
                for (PrivateShow ps : allPrivateShows) {
                    if (ps.getSuggestion() == sug) {
                        te.shower = ps.getShower().getName();
                        te.shownTo = ps.getRecipient().getName();
                        te.isPrivate = true;

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
            table.turns.add(te);
        }

        return table;
    }
}