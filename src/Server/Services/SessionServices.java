package Server.Services;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import Server.Master.Master;
import Server.Pokemon.Pokemon;
import Server.Repository.Repository;
import Server.Session.GameSession;
import Server.Session.SessionParticipant;
import Server.Trainer.Player;
import Server.Trainer.Trainer;
import Server.Trainer.CharacterSheet;

public class SessionServices {

    // No I, L, O, 0, 1: easy to read aloud / type on a phone
    private static final String CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 4;

    private Repository repository;

    private final SecureRandom random = new SecureRandom();

    private final java.util.Map<Integer, Long> lastSeen = new java.util.concurrent.ConcurrentHashMap<>();

    private int nextPlayerId = 1;
    private int nextMasterId = 1;
    private int nextSessionId = 1;
    private int nextParticipantId = 1;
    private int nextTrainerId = 1;
    private int nextSheetId = 1;

    public SessionServices(Repository repository) {
        setRepository(repository);
        loadCounters();
    }

    //============GETTERS=============

    public Repository getRepository() {
        return repository;
    }

    //============SETTERS=============

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    //============MASTER=============

    // Master starts a new session. The same name always reuses the same Master
    // player, so a Master with 3 campaigns is still one person. Returns the session.
    public synchronized GameSession createSession(String masterName) {

        Master master = findMasterByName(masterName);

        if (master == null) {
            Player player = new Player(nextPlayerId++, masterName);
            repository.createPlayer(player);

            master = new Master(nextMasterId++, player.getId());
            repository.createMaster(master);
        }

        GameSession session = new GameSession(nextSessionId++, master.getPlayerId(), 1, generateUniqueCode());
        repository.createGameSession(session);

        repository.createSessionParticipant(new SessionParticipant(
            nextParticipantId++, session.getId(), master.getPlayerId(), "Master", master.getId(), null
        ));

        return session;
    }

    // Every saved session, so the Master can pick one up again after closing the program
    public List<GameSession> listSessions() {
        return repository.findAllGameSessions();
    }

    public String getHostName(GameSession session) {
        Player host = repository.findPlayerById(session.getHostPlayerId());
        return host == null ? "?" : host.getName();
    }

    public int countPlayers(GameSession session) {
        int total = 0;
        for (SessionParticipant sp : repository.findAllSessionParticipants()) {
            if (sp.getGameSessionId() == session.getId() && sp.getType().equals("Player")) total++;
        }
        return total;
    }

    //============PLAYER=============

    // Player joins with the session code and gets a secret token (kept by the browser).
    // The token, not the name, is the identity: two campaigns, or two people called
    // "Ash", never collide. Same name twice in ONE session is refused (returns null).
    public synchronized SessionParticipant joinSession(GameSession session, String playerName) {

        for (Player p : playersOf(session)) {
            if (p.getName().equalsIgnoreCase(playerName)) return null;
        }

        Player player = new Player(nextPlayerId++, playerName);
        repository.createPlayer(player);

        // ponytail: participant_id = 0 until Trainers exist (it should point to the player's trainer)
        SessionParticipant sp = new SessionParticipant(
            nextParticipantId++, session.getId(), player.getId(), "Player", 0, newToken()
        );
        repository.createSessionParticipant(sp);
        return sp;
    }

    // Who is behind this token in this session? null = unknown or removed (kicked)
    public SessionParticipant findByToken(GameSession session, String token) {
        if (token == null || token.isEmpty()) return null;
        for (SessionParticipant sp : repository.findAllSessionParticipants()) {
            if (sp.getGameSessionId() == session.getId() && token.equals(sp.getToken())) return sp;
        }
        return null;
    }

    public SessionParticipant findPlayerParticipant(GameSession session, int playerId) {
        for (SessionParticipant sp : repository.findAllSessionParticipants()) {
            if (sp.getGameSessionId() == session.getId() && sp.getType().equals("Player") && sp.getPlayerId() == playerId) return sp;
        }
        return null;
    }

    // Player left or Master kicked: frees the name
    public synchronized void removePlayer(SessionParticipant sp) {
        for (Trainer t : trainersOf(sp.getPlayerId())) deleteTrainerAndPokemon(t.getId());
        repository.deleteSessionParticipant(sp.getId());
        repository.deletePlayer(sp.getPlayerId());
        lastSeen.remove(sp.getPlayerId());
    }

    // Master deletes a whole session. Pokemon already spawned stay in the pokemon table
    // (they have no session link yet); the Master's own player row stays (it may own other sessions).
    public synchronized void deleteSession(GameSession session) {
        for (SessionParticipant sp : repository.findAllSessionParticipants()) {
            if (sp.getGameSessionId() != session.getId()) continue;
            if (sp.getType().equals("Player")) removePlayer(sp);
            else repository.deleteSessionParticipant(sp.getId());
        }
        repository.deleteGameSession(session.getId());
    }

    //============RECONNECT=============

    private record Claim(int participantId, long expires) {}

    // sessionId:CODE -> who may be claimed. In memory on purpose: short-lived (10 min)
    private final java.util.Map<String, Claim> claims = new java.util.concurrent.ConcurrentHashMap<>();

    // The player lost his token (cleared browser, new phone): the Master hands him a
    // one-time code, which gives the SAME player (and his characters) a new token.
    public String createClaimCode(GameSession session, SessionParticipant sp) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 6; i++) b.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
        claims.put(session.getId() + ":" + b, new Claim(sp.getId(), System.currentTimeMillis() + 10 * 60_000));
        return b.toString();
    }

    // Swaps the token (the old browser, if still open, is locked out). null = bad/expired code
    public synchronized SessionParticipant claim(GameSession session, String claimCode) {
        Claim c = claims.remove(session.getId() + ":" + claimCode.trim().toUpperCase());
        if (c == null || c.expires() < System.currentTimeMillis()) return null;

        SessionParticipant sp = repository.findSessionParticipantById(c.participantId());
        if (sp == null) return null;

        sp.setToken(newToken());
        repository.updateSessionParticipant(sp);
        return sp;
    }

    //============REJOIN (player asks, Master approves with one click)=============

    // status: "pending" | "approved" | "denied". secret = only the asking browser knows it (it polls with it).
    public static final class Rejoin {
        public final int id, sessionId, participantId; public final String name, secret, code; public final long created = System.currentTimeMillis();
        public volatile String status = "pending"; public volatile String token = "";
        Rejoin(int id, int sessionId, int participantId, String name, String secret, String code) { this.code = code; this.id = id; this.sessionId = sessionId; this.participantId = participantId; this.name = name; this.secret = secret; }
    }

    private final java.util.Map<Integer, Rejoin> rejoins = new java.util.concurrent.ConcurrentHashMap<>();
    private int nextRejoinId = 1;
    private static final long REJOIN_TTL = 15 * 60_000;

    // The player "Ash" says "it is me". null = nobody called like that in this session.
    public synchronized Rejoin requestRejoin(GameSession session, String name) {
        rejoins.values().removeIf(r -> r.created + REJOIN_TTL < System.currentTimeMillis());
        for (Player p : playersOf(session)) {
            if (!p.getName().equalsIgnoreCase(name.trim())) continue;
            SessionParticipant sp = findPlayerParticipant(session, p.getId());
            if (sp == null) continue;
            rejoins.values().removeIf(r -> r.participantId == sp.getId() && r.status.equals("pending"));  // one open request per person
            Rejoin r = new Rejoin(nextRejoinId++, session.getId(), sp.getId(), p.getName(), newToken(), session.getJoinCode());
            rejoins.put(r.id, r);
            return r;
        }
        return null;
    }

    // every open request of every session (the Master sees them whatever session he is looking at)
    public java.util.List<Rejoin> pendingRejoins() {
        java.util.List<Rejoin> list = new ArrayList<>();
        for (Rejoin r : rejoins.values())
            if (r.status.equals("pending") && r.created + REJOIN_TTL > System.currentTimeMillis()) list.add(r);
        list.sort(java.util.Comparator.comparingInt(r -> r.id));
        return list;
    }

    public boolean isOnlineParticipant(int participantId) {
        SessionParticipant sp = repository.findSessionParticipantById(participantId);
        return sp != null && isOnline(sp.getPlayerId());
    }

    // Master answers. Approving swaps the token: the browser that asked gets the new one, an old browser is locked out.
    public synchronized boolean answerRejoin(int id, boolean ok) {
        Rejoin r = rejoins.get(id);
        if (r == null || !r.status.equals("pending")) return false;
        if (!ok) { r.status = "denied"; return true; }
        SessionParticipant sp = repository.findSessionParticipantById(r.participantId);
        if (sp == null) { rejoins.remove(id); return false; }
        sp.setToken(newToken());
        repository.updateSessionParticipant(sp);
        r.token = sp.getToken();
        r.status = "approved";
        return true;
    }

    // The asking browser polls. Wrong secret = null. An answered request is handed over once and forgotten.
    public synchronized Rejoin rejoinFor(int id, String secret) {
        Rejoin r = rejoins.get(id);
        if (r == null || !r.secret.equals(secret)) return null;
        if (!r.status.equals("pending")) rejoins.remove(id);
        return r;
    }

    //============TRAINERS (characters)=============

    // A Trainer is the character; the Player is the person. Names are NOT unique:
    // one person can have two "Roberto"s, and two people can each have one,
    // because everything is tied to the trainer id, never to the name.
    public synchronized Trainer createTrainer(SessionParticipant sp, String name) {
        // ponytail: default stats; real PTU trainer creation (class, skills, stats) comes later
        Trainer trainer = new Trainer(nextTrainerId++, name, 1, 10, 0, sp.getPlayerId());
        repository.createTrainer(trainer);
        return trainer;
    }

    public List<Trainer> trainersOf(int playerId) {
        List<Trainer> list = new ArrayList<>();
        for (Trainer t : repository.findAllTrainers()) {
            if (t.getPlayerId() == playerId) list.add(t);
        }
        return list;
    }

    // Only the owner can delete (the caller passes the owner's participant)
    public synchronized boolean deleteTrainer(SessionParticipant sp, int trainerId) {
        for (Trainer t : trainersOf(sp.getPlayerId())) {
            if (t.getId() == trainerId) { deleteTrainerAndPokemon(trainerId); return true; }
        }
        return false;
    }

    //============CHARACTER SHEETS=============
    // A person (owner = private key kept in his browser) makes characters in advance; placing one in a
    // session creates an independent Trainer there. What it gets in one session never reaches another.

    public static boolean validOwner(String owner) {
        return owner != null && owner.matches("[0-9a-f]{32}");
    }

    public List<CharacterSheet> sheetsOf(String owner) {
        List<CharacterSheet> list = new ArrayList<>();
        for (CharacterSheet c : repository.findAllSheets()) if (c.getOwner().equals(owner)) list.add(c);
        return list;
    }

    public synchronized CharacterSheet createSheet(String owner, String name) {
        CharacterSheet sheet = new CharacterSheet(nextSheetId++, owner, name);
        repository.createSheet(sheet);
        return sheet;
    }

    // The copies already living in sessions stay (they hold game progress); only the link is cut
    public synchronized boolean deleteSheet(String owner, int sheetId) {
        for (CharacterSheet c : sheetsOf(owner)) {
            if (c.getId() == sheetId) {
                repository.deletePlacementsOfSheet(sheetId);
                repository.deleteSheet(sheetId);
                return true;
            }
        }
        return false;
    }

    // Sheets of this player's trainers in this session (sheet id -> already placed here)
    public java.util.Set<Integer> placedSheets(int playerId) {
        java.util.Set<Integer> mine = new java.util.HashSet<>();
        for (Trainer t : trainersOf(playerId)) mine.add(t.getId());
        java.util.Set<Integer> placed = new java.util.HashSet<>();
        for (int[] link : repository.findAllPlacements()) if (mine.contains(link[0])) placed.add(link[1]);
        return placed;
    }

    // Returns an error text, or null when the character was placed
    public synchronized String placeSheet(SessionParticipant sp, String owner, int sheetId) {
        CharacterSheet sheet = null;
        for (CharacterSheet c : sheetsOf(owner)) if (c.getId() == sheetId) sheet = c;
        if (sheet == null) return "Character not found";
        if (placedSheets(sp.getPlayerId()).contains(sheetId)) return "This character is already in this session";
        Trainer copy = createTrainer(sp, sheet.getName());
        repository.createPlacement(copy.getId(), sheetId);
        return null;
    }

    // A trainer's Pokemon go with him (foreign key pokemon.trainer_id)
    private void deleteTrainerAndPokemon(int trainerId) {
        for (Pokemon p : repository.findAllPokemons()) {
            if (p.getTrainerId() != null && p.getTrainerId() == trainerId) repository.deletePokemon(p.getId());
        }
        repository.deletePlacementOfTrainer(trainerId);
        repository.deleteTrainer(trainerId);
    }

    //============PRESENCE=============

    // Players poll every 2s; "online" = seen in the last 7s
    public void touch(int playerId) {
        lastSeen.put(playerId, System.currentTimeMillis());
    }

    // seconds since his last poll, -1 = not seen since the server started
    public long lastSeenAgo(int playerId) {
        Long t = lastSeen.get(playerId);
        return t == null ? -1 : (System.currentTimeMillis() - t) / 1000;
    }

    public boolean isOnline(int playerId) {
        Long t = lastSeen.get(playerId);
        return t != null && System.currentTimeMillis() - t < 7000;
    }

    public List<Player> playersOf(GameSession session) {
        List<Player> list = new ArrayList<>();
        for (SessionParticipant sp : repository.findAllSessionParticipants()) {
            if (sp.getGameSessionId() != session.getId() || !sp.getType().equals("Player")) continue;
            Player p = repository.findPlayerById(sp.getPlayerId());
            if (p != null) list.add(p);
        }
        return list;
    }

    //============INTERNAL=============

    public GameSession findSessionByCode(String code) {
        String wanted = code.trim().toUpperCase();
        for (GameSession session : repository.findAllGameSessions()) {
            if (session.getJoinCode().equals(wanted)) return session;
        }
        return null;
    }

    private Master findMasterByName(String name) {
        for (Master master : repository.findAllMasters()) {
            Player player = repository.findPlayerById(master.getPlayerId());
            if (player != null && player.getName().equalsIgnoreCase(name)) return master;
        }
        return null;
    }

    private String newToken() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return java.util.HexFormat.of().formatHex(bytes);
    }

    private String generateUniqueCode() {
        List<String> used = new ArrayList<>();
        for (GameSession session : repository.findAllGameSessions()) used.add(session.getJoinCode());

        String code;
        do {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < CODE_LENGTH; i++) {
                builder.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            code = builder.toString();
        } while (used.contains(code));
        return code;
    }

    private void loadCounters() {
        for (Player p : repository.findAllPlayers()) nextPlayerId = Math.max(nextPlayerId, p.getId() + 1);
        for (Master m : repository.findAllMasters()) nextMasterId = Math.max(nextMasterId, m.getId() + 1);
        for (GameSession s : repository.findAllGameSessions()) nextSessionId = Math.max(nextSessionId, s.getId() + 1);
        for (Trainer t : repository.findAllTrainers()) nextTrainerId = Math.max(nextTrainerId, t.getId() + 1);
        for (CharacterSheet c : repository.findAllSheets()) nextSheetId = Math.max(nextSheetId, c.getId() + 1);
        for (SessionParticipant sp : repository.findAllSessionParticipants()) {
            nextParticipantId = Math.max(nextParticipantId, sp.getId() + 1);
        }
    }

}
