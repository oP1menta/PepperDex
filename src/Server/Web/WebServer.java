package Server.Web;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import Server.Pokemon.Pokemon;
import Server.Services.AreaServices;
import Server.Services.CaptureServices;
import Server.Services.Pokedex;
import Server.Services.TableServices;
import Server.Services.TrainerProfiles;
import Server.Services.PokemonServices;
import Server.Services.SessionServices;
import Server.Services.SpawnFeed;
import Server.Services.TunnelServices;
import Server.Services.HelpRequests;
import Server.Session.GameSession;
import Server.Session.SessionParticipant;
import Server.Trainer.Player;
import Server.Trainer.Trainer;

public class WebServer {

    private static final Path WEB_FOLDER = Path.of("web").toAbsolutePath().normalize();

    private int port;
    private SessionServices sessionServices;
    private PokemonServices pokemonServices;
    private CaptureServices captureServices;
    private SpawnFeed spawnFeed;
    private final Pokedex pokedex = new Pokedex();
    private final TrainerProfiles profiles = new TrainerProfiles();
    private final TableServices table = new TableServices();
    private HelpRequests helpRequests;
    private AreaServices areaServices;
    private TunnelServices tunnelServices;
    private HttpServer httpServer;

    public WebServer(int port, SessionServices sessionServices, PokemonServices pokemonServices, CaptureServices captureServices) {
        setPort(port);
        setSessionServices(sessionServices);
        setPokemonServices(pokemonServices);
        setCaptureServices(captureServices);
        setSpawnFeed(new SpawnFeed());
        setHelpRequests(new HelpRequests());
        setAreaServices(new AreaServices(sessionServices.getRepository()));
    }

    //============GETTERS=============

    public int getPort() {
        return port;
    }

    public SessionServices getSessionServices() {
        return sessionServices;
    }

    public PokemonServices getPokemonServices() {
        return pokemonServices;
    }

    public CaptureServices getCaptureServices() {
        return captureServices;
    }

    public SpawnFeed getSpawnFeed() {
        return spawnFeed;
    }

    public HelpRequests getHelpRequests() {
        return helpRequests;
    }

    public AreaServices getAreaServices() {
        return areaServices;
    }

    //============SETTERS=============

    public void setPort(int port) {
        this.port = port;
    }

    public void setSessionServices(SessionServices sessionServices) {
        this.sessionServices = sessionServices;
    }

    public void setPokemonServices(PokemonServices pokemonServices) {
        this.pokemonServices = pokemonServices;
    }

    public void setCaptureServices(CaptureServices captureServices) {
        this.captureServices = captureServices;
    }

    public void setSpawnFeed(SpawnFeed spawnFeed) {
        this.spawnFeed = spawnFeed;
    }

    public void setHelpRequests(HelpRequests helpRequests) {
        this.helpRequests = helpRequests;
    }

    public void setAreaServices(AreaServices areaServices) {
        this.areaServices = areaServices;
    }

    public void setTunnelServices(TunnelServices tunnelServices) {
        this.tunnelServices = tunnelServices;
    }

    public TunnelServices getTunnelServices() {
        return tunnelServices;
    }

    //============START=============

    public void start() throws IOException {

        httpServer = HttpServer.create(new InetSocketAddress(port), 0);

        // Static files from the web/ folder (read from disk on every request:
        // edit, save, refresh the browser; no recompiling needed)
        httpServer.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();

            // Master-only: only requests coming from this same computer are accepted
            if (path.equals("/master")) {
                if (!isLocal(exchange)) { reply(exchange, 403, "text/plain", "Master page is local only"); return; }
                path = "/master.html";
            } else if (path.equals("/")) {
                path = "/player.html";
            } else if (path.equals("/master.html") && !isLocal(exchange)) {
                reply(exchange, 403, "text/plain", "Master page is local only");
                return;
            }

            serveFile(exchange, path);
        });

        httpServer.createContext("/api/session", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            String name = form.getOrDefault("name", "").trim();
            if (name.isEmpty()) { reply(exchange, 400, "application/json", error("Name required")); return; }

            GameSession session = sessionServices.createSession(name);
            reply(exchange, 200, "application/json",
                "{\"code\":\"" + session.getJoinCode() + "\",\"playerUrl\":\"" + playerUrl() + "\"}");
        });

        // Master-only: saved sessions, so the Master can continue one after restarting
        // Master-only: the internet link (tunnel). GET = state, POST = start it
        httpServer.createContext("/api/tunnel", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            if (tunnelServices == null) { reply(exchange, 404, "application/json", error("No tunnel")); return; }
            if (exchange.getRequestMethod().equals("POST")) tunnelServices.start();
            reply(exchange, 200, "application/json", tunnelServices.toJson("http://" + lanAddress() + ":" + port));
        });

        httpServer.createContext("/api/sessions", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }

            StringBuilder json = new StringBuilder("{\"playerUrl\":\"" + playerUrl() + "\",\"sessions\":[");
            boolean first = true;
            for (GameSession session : sessionServices.listSessions()) {
                if (!first) json.append(",");
                first = false;
                json.append("{\"code\":\"").append(session.getJoinCode())
                    .append("\",\"host\":\"").append(escape(sessionServices.getHostName(session)))
                    .append("\",\"day\":").append(session.getCurrentDay())
                    .append(",\"players\":").append(sessionServices.countPlayers(session)).append("}");
            }
            json.append("]}");
            reply(exchange, 200, "application/json", json.toString());
        });

        // Master-only: every form, for the picker
        httpServer.createContext("/api/forms", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }

            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (String[] form : pokemonServices.listFormLabels()) {
                if (!first) json.append(",");
                first = false;
                json.append("{\"id\":").append(form[0]).append(",\"label\":\"").append(escape(form[1]))
                    .append("\",\"family\":").append(form[2]).append(",\"stage\":").append(form[3])
                    .append(",\"key\":\"").append(escape(form[4])).append("\",\"types\":[\"").append(form[5].replace("/", "\",\"")).append("\"]}");
            }
            reply(exchange, 200, "application/json", json.append("]").toString());
        });

        // Master-only: what can be chosen for a form at a level: genders, abilities, natures
        httpServer.createContext("/api/options", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> query = parse(exchange.getRequestURI().getRawQuery());
            int formId, level;
            try {
                formId = Integer.parseInt(query.getOrDefault("formId", ""));
                level = Integer.parseInt(query.getOrDefault("level", "1"));
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("formId and level must be numbers")); return; }
            var form = pokemonServices.getRepository().findPokemonFormById(formId);
            if (form == null) { reply(exchange, 404, "application/json", error("Form not found")); return; }

            StringBuilder json = new StringBuilder("{\"genders\":[");
            json.append(String.join(",", pokemonServices.allowedGenders(form).stream().map(g -> "\"" + g + "\"").toList()));
            json.append("],\"abilities\":[");
            json.append(String.join(",", pokemonServices.allowedAbilities(form, Math.max(1, Math.min(100, level))).stream()
                .map(a -> "{\"id\":" + a.getId() + ",\"name\":\"" + escape(a.getName()) + "\"}").toList()));
            json.append("],\"natures\":[");
            json.append(String.join(",", pokemonServices.getRepository().findAllNatures().stream()
                .map(n -> "{\"id\":" + n.getId() + ",\"name\":\"" + n.getName() + "\",\"up\":\"" + n.getIncreasedAttribute()
                    + "\",\"down\":\"" + n.getDecreasedAttribute() + "\"}").toList()));
            reply(exchange, 200, "application/json", json.append("]}").toString());
        });

        // Master-only: spawn a wild Pokemon into a session
        httpServer.createContext("/api/spawn", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            spawnFromForm(exchange, readForm(exchange));
        });

        // Master-only: areas (name, level range, forms with weights)
        httpServer.createContext("/api/areas", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            reply(exchange, 200, "application/json", areaServices.listJson());
        });

        httpServer.createContext("/api/area", exchange -> {      // id empty = new area
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            int id = Integer.parseInt("0" + form.getOrDefault("id", "").replaceAll("\\D", ""));
            int min, max;
            try { min = Integer.parseInt(form.getOrDefault("min", "")); max = Integer.parseInt(form.getOrDefault("max", "")); }
            catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Levels must be numbers")); return; }
            String problem = areaServices.save(id, form.getOrDefault("name", ""), min, max);
            reply(exchange, problem == null ? 200 : 400, "application/json", problem == null ? areaServices.listJson() : error(problem));
        });

        httpServer.createContext("/api/area/delete", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            areaServices.delete(Integer.parseInt("0" + form.getOrDefault("id", "").replaceAll("\\D", "")));
            reply(exchange, 200, "application/json", areaServices.listJson());
        });

        // add a form to an area, or change its weight; or remove it (rowId)
        httpServer.createContext("/api/area/form", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            String problem;
            try {
                if (form.containsKey("rowId")) { areaServices.removeForm(Integer.parseInt(form.get("rowId"))); problem = null; }
                else problem = areaServices.setForm(Integer.parseInt(form.getOrDefault("areaId", "")), Integer.parseInt(form.getOrDefault("formId", "")), Double.parseDouble(form.getOrDefault("weight", "1")));
            } catch (NumberFormatException e) { problem = "Bad numbers"; }
            reply(exchange, problem == null ? 200 : 400, "application/json", problem == null ? areaServices.listJson() : error(problem));
        });

        // roll the area: a random form of it at a random level inside its range, spawned like /api/spawn
        httpServer.createContext("/api/area/roll", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            int[] rolled;
            try { rolled = areaServices.roll(Integer.parseInt(form.getOrDefault("areaId", ""))); }
            catch (NumberFormatException e) { rolled = null; }
            if (rolled == null) { reply(exchange, 400, "application/json", error("This area has no Pokemon yet")); return; }
            form.put("formId", String.valueOf(rolled[0]));
            form.put("level", String.valueOf(rolled[1]));
            spawnFromForm(exchange, form);
        });

        // what is on the field now (not caught yet) and its catch mode
        httpServer.createContext("/api/active", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> query = parse(exchange.getRequestURI().getRawQuery());
            reply(exchange, 200, "application/json", spawnFeed.activeJson(query.getOrDefault("code", "").trim().toUpperCase(), id -> wildHp(id) + ",\"rate\":" + wildRate(id) + wildDetail(id)));
        });

        // Master: catch mode of a spawned Pokemon: normal roll / always (100%) / never (0%)
        httpServer.createContext("/api/catchmode", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            String mode = form.getOrDefault("mode", "");
            int pokemonId;
            try { pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", "")); } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad id")); return; }
            if (!SpawnFeed.MODES.contains(mode)) { reply(exchange, 400, "application/json", error("Bad mode")); return; }
            spawnFeed.setMode(pokemonId, mode);
            reply(exchange, 200, "application/json", "{\"ok\":true}");
        });

        // Master: weaken a wild Pokemon and put effects on it. Any of: hp (absolute), fx (toggle one effect), inj, bonus
        httpServer.createContext("/api/wild", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            int pokemonId;
            try { pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", "")); } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad id")); return; }
            Pokemon wild = sessionServices.getRepository().findPokemonById(pokemonId);
            if (wild == null || wild.getTrainerId() != null) { reply(exchange, 404, "application/json", error("Pokemon not found")); return; }
            if ("1".equals(form.get("remove"))) {  // the Master sends it away: players stop seeing it (same as a catch, but nobody gets it)
                spawnFeed.markCaught(form.getOrDefault("code", "").trim().toUpperCase(), pokemonId);
                reply(exchange, 200, "application/json", "{\"ok\":true}"); return;
            }
            try {
                if (form.containsKey("hp")) pokemonServices.setWildHp(pokemonId, Float.parseFloat(form.get("hp")));
                if (form.containsKey("inj")) spawnFeed.setInjuries(pokemonId, Integer.parseInt(form.get("inj")));
                if (form.containsKey("bonus")) spawnFeed.setBonus(pokemonId, Integer.parseInt(form.get("bonus")));
                if (form.containsKey("tries")) spawnFeed.setTries(pokemonId, Integer.parseInt(form.get("tries")));
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            if (form.containsKey("fx")) {
                if (!CaptureServices.EFFECTS.containsKey(form.get("fx"))) { reply(exchange, 400, "application/json", error("Bad effect")); return; }
                spawnFeed.toggleEffect(pokemonId, form.get("fx"));
            }
            reply(exchange, 200, "application/json", "{\"ok\":true}");
        });

        // Master-only: give a new Pokemon to a character (trainerId)
        httpServer.createContext("/api/give", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            GameSession session = sessionServices.findSessionByCode(form.getOrDefault("code", ""));
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }

            int formId, level, trainerId;
            try {
                formId = Integer.parseInt(form.getOrDefault("formId", ""));
                level = Integer.parseInt(form.getOrDefault("level", ""));
                trainerId = Integer.parseInt(form.getOrDefault("trainerId", ""));
            } catch (NumberFormatException e) {
                reply(exchange, 400, "application/json", error("formId, level and trainerId must be numbers")); return;
            }
            if (level < 1 || level > 100) { reply(exchange, 400, "application/json", error("Level must be 1-100")); return; }

            // the character must belong to a player of THIS session
            Trainer trainer = sessionServices.getRepository().findTrainerById(trainerId);
            if (trainer == null || sessionServices.findPlayerParticipant(session, trainer.getPlayerId()) == null) {
                reply(exchange, 404, "application/json", error("Character not in this session")); return;
            }

            if (!pokemonServices.hasRoom(trainerId)) { reply(exchange, 409, "application/json", error("No room: party and every PC box are full")); return; }
            PokemonServices.SpawnOptions options = spawnOptions(form);
            String problem = pokemonServices.checkOptions(formId, level, options);
            if (problem != null) { reply(exchange, 400, "application/json", error(problem)); return; }
            Pokemon pokemon = pokemonServices.givePokemon(formId, level, trainerId, options);
            if (pokemon == null) { reply(exchange, 404, "application/json", error("Form not found")); return; }
            reply(exchange, 200, "application/json", pokemonServices.masterJson(pokemon));
        });

        // Players poll this (with their token: it also marks them online). Without "after"
        // (first call, or a page reload) it returns the last 10 spawns they may see.
        // 403 "removed" = kicked; 404 = session deleted.
        httpServer.createContext("/api/spawns", exchange -> {
            Map<String, String> query = parse(exchange.getRequestURI().getRawQuery());
            String code = query.getOrDefault("code", "").trim().toUpperCase();

            GameSession session = sessionServices.findSessionByCode(code);
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }

            SessionParticipant me = sessionServices.findByToken(session, query.get("token"));
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            sessionServices.touch(me.getPlayerId());

            int after;
            try { after = Integer.parseInt(query.get("after")); } catch (NumberFormatException e) { after = 0; }  // page just opened: everything still on the field
            for (int pid : spawnFeed.visibleIds(code, after, me.getPlayerId())) {  // the Pokedex records what he has been shown
                Pokemon seen = sessionServices.getRepository().findPokemonById(pid);
                if (seen != null) pokedex.mark(me.getPlayerId(), seen.getFormId(), seen.isShiny(), Pokedex.SEEN);
            }

            reply(exchange, 200, "application/json",
                "{\"last\":" + spawnFeed.last(code) + ",\"gone\":" + spawnFeed.caughtJson(code) + ",\"spawns\":" + spawnFeed.since(code, after, me.getPlayerId()) + ",\"states\":" + spawnFeed.statesJson(code, this::wildHp) + "}");
        });

        // New player in a session. Returns the token the browser must keep.
        httpServer.createContext("/api/join", exchange -> {
            Map<String, String> form = readForm(exchange);
            String name = form.getOrDefault("name", "").trim();
            String code = form.getOrDefault("code", "").trim();
            if (name.isEmpty() || code.isEmpty()) { reply(exchange, 400, "application/json", error("Code and name required")); return; }

            GameSession session = sessionServices.findSessionByCode(code);
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown code")); return; }

            SessionParticipant sp = sessionServices.joinSession(session, name);
            if (sp == null) { reply(exchange, 409, "application/json", "{\"error\":\"That name is already used in this session. If it is you, ask the Master for a reconnect code.\",\"rejoin\":true}"); return; }

            sessionServices.touch(sp.getPlayerId());
            reply(exchange, 200, "application/json", playerJson(session, sp));
        });

        // My characters in this session. GET = list, POST = create (name).
        httpServer.createContext("/api/trainers", exchange -> {
            Map<String, String> data = exchange.getRequestMethod().equals("POST")
                ? readForm(exchange) : parse(exchange.getRequestURI().getRawQuery());
            GameSession session = sessionServices.findSessionByCode(data.getOrDefault("code", ""));
            SessionParticipant me = session == null ? null : sessionServices.findByToken(session, data.get("token"));
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }

            if (exchange.getRequestMethod().equals("POST")) {
                String name = data.getOrDefault("name", "").trim();
                if (name.isEmpty() || name.length() > 30) { reply(exchange, 400, "application/json", error("Name must have 1-30 characters")); return; }
                sessionServices.createTrainer(me, name);
            }
            reply(exchange, 200, "application/json", trainersJson(me.getPlayerId()));
        });

        // My characters, made before any session (person = the private key kept in the browser).
        // GET = list (with code + token: also says which are already in that session), POST = create (name).
        httpServer.createContext("/api/sheets", exchange -> {
            Map<String, String> data = exchange.getRequestMethod().equals("POST")
                ? readForm(exchange) : parse(exchange.getRequestURI().getRawQuery());
            String person = data.get("person");
            if (!SessionServices.validOwner(person)) { reply(exchange, 400, "application/json", error("Bad person key")); return; }

            if (exchange.getRequestMethod().equals("POST")) {
                String name = data.getOrDefault("name", "").trim();
                if (name.isEmpty() || name.length() > 30) { reply(exchange, 400, "application/json", error("Name must have 1-30 characters")); return; }
                sessionServices.createSheet(person, name);
            }
            reply(exchange, 200, "application/json", sheetsJson(person, sessionPlayerOrNull(data)));
        });

        httpServer.createContext("/api/sheet/delete", exchange -> {
            Map<String, String> form = readForm(exchange);
            String person = form.get("person");
            int id;
            try { id = Integer.parseInt(form.getOrDefault("sheetId", "")); } catch (NumberFormatException e) { id = -1; }
            if (!SessionServices.validOwner(person) || !sessionServices.deleteSheet(person, id)) {
                reply(exchange, 404, "application/json", error("Not your character")); return;
            }
            reply(exchange, 200, "application/json", sheetsJson(person, null));
        });

        // Put one of my characters into this session: an independent copy
        httpServer.createContext("/api/place", exchange -> {
            Map<String, String> form = readForm(exchange);
            SessionParticipant me = sessionPlayerOrNull(form);
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int id;
            try { id = Integer.parseInt(form.getOrDefault("sheetId", "")); } catch (NumberFormatException e) { id = -1; }
            String problem = SessionServices.validOwner(form.get("person")) ? sessionServices.placeSheet(me, form.get("person"), id) : "Bad person key";
            if (problem != null) { reply(exchange, 400, "application/json", error(problem)); return; }
            reply(exchange, 200, "application/json", trainersJson(me.getPlayerId()));
        });

        // Move a Pokemon of mine between party and PC boxes (swaps if the target is taken)
        httpServer.createContext("/api/pc/move", exchange -> {
            Map<String, String> form = readForm(exchange);
            SessionParticipant me = sessionPlayerOrNull(form);
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int trainerId, pokemonId, box, slot;
            try {
                trainerId = Integer.parseInt(form.getOrDefault("trainerId", ""));
                pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", ""));
                box = Integer.parseInt(form.getOrDefault("box", "0"));
                slot = Integer.parseInt(form.getOrDefault("slot", ""));
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            boolean mine = false;
            for (Trainer t : sessionServices.trainersOf(me.getPlayerId())) if (t.getId() == trainerId) mine = true;
            if (!mine) { reply(exchange, 404, "application/json", error("Not your trainer")); return; }
            String problem = pokemonServices.movePokemon(trainerId, pokemonId, "party".equals(form.get("zone")), box, slot);
            if (problem != null) { reply(exchange, 400, "application/json", error(problem)); return; }
            reply(exchange, 200, "application/json", trainersJson(me.getPlayerId()));
        });

        // Add one more PC box to a character of mine
        httpServer.createContext("/api/pc/box", exchange -> {
            Map<String, String> form = readForm(exchange);
            SessionParticipant me = sessionPlayerOrNull(form);
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int trainerId;
            try { trainerId = Integer.parseInt(form.getOrDefault("trainerId", "")); } catch (NumberFormatException e) { trainerId = -1; }
            boolean mine = false;
            for (Trainer t : sessionServices.trainersOf(me.getPlayerId())) if (t.getId() == trainerId) mine = true;
            if (!mine) { reply(exchange, 404, "application/json", error("Not your trainer")); return; }
            pokemonServices.addBox(trainerId);
            reply(exchange, 200, "application/json", trainersJson(me.getPlayerId()));
        });

        // Player throws a ball at a wild Pokemon: the server rolls, spends the ball, and says what happened
        httpServer.createContext("/api/capture", exchange -> {
            Map<String, String> form = readForm(exchange);
            SessionParticipant me = sessionPlayerOrNull(form);
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            String code = form.getOrDefault("code", "").trim().toUpperCase();
            int trainerId, pokemonId, ballId;
            try {
                trainerId = Integer.parseInt(form.getOrDefault("trainerId", ""));
                pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", ""));
                ballId = Integer.parseInt(form.getOrDefault("ballId", ""));
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }

            boolean mine = false;
            for (Trainer t : sessionServices.trainersOf(me.getPlayerId())) if (t.getId() == trainerId) mine = true;
            if (!mine) { reply(exchange, 404, "application/json", error("Not your trainer")); return; }

            // one capture at a time: two players throwing at the same Pokemon -> first one wins
            synchronized (spawnFeed) {
                if (!spawnFeed.canCatch(code, pokemonId, me.getPlayerId())) { reply(exchange, 409, "application/json", error("gone")); return; }
                Pokemon wild = sessionServices.getRepository().findPokemonById(pokemonId);
                if (wild == null || wild.getTrainerId() != null) { reply(exchange, 409, "application/json", error("gone")); return; }
                Server.Item.Item ball = sessionServices.getRepository().findItemById(ballId);
                if (ball == null || !CaptureServices.BALL.equals(ball.getCategory()) || captureServices.quantity(trainerId, ballId) < 1) {
                    reply(exchange, 400, "application/json", error("noball")); return;
                }
                if (!pokemonServices.hasRoom(trainerId)) { reply(exchange, 409, "application/json", error("noroom")); return; }

                CaptureServices.Result r = captureServices.throwBall(trainerId, wild, ball, spawnFeed.getMode(pokemonId), spawnFeed.getState(pokemonId));
                // balls left for this Pokemon (shared by every player); a miss with none left = it runs away
                int left = spawnFeed.useTry(pokemonId);
                boolean fled = !r.caught() && left == 0;
                if (r.caught() || fled) spawnFeed.markCaught(code, pokemonId);
                boolean newSticker = r.caught() && pokedex.caught(me.getPlayerId(), wild.getFormId(), wild.getLevel(), wild.isShiny());
                String json = r.toJson(captureServices.ballSkin(ball));
                reply(exchange, 200, "application/json", json.substring(0, json.length() - 1) + ",\"left\":" + left + ",\"fled\":" + fled + ",\"sticker\":" + newSticker + "}");
            }
        });

        // Pokedex of one player: only what he has seen/caught is described (the rest is just counted).
        // Players read their own; the Master (this computer) may add &player=<id> to read anyone's.
        httpServer.createContext("/api/dex", exchange -> {
            Map<String, String> query = parse(exchange.getRequestURI().getRawQuery());
            GameSession session = sessionServices.findSessionByCode(query.getOrDefault("code", "").trim());
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }
            int playerId;
            if (isLocal(exchange) && query.containsKey("player")) {
                try { playerId = Integer.parseInt(query.get("player")); } catch (NumberFormatException e) { playerId = -1; }
                if (sessionServices.findPlayerParticipant(session, playerId) == null) { reply(exchange, 404, "application/json", error("Player not in this session")); return; }
            } else {
                SessionParticipant me = sessionServices.findByToken(session, query.get("token"));
                if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
                playerId = me.getPlayerId();
            }

            if (!pokedex.wiped(playerId)) for (Trainer t : sessionServices.trainersOf(playerId))  // whatever he owns counts as caught (older captures, gifts)
                for (Pokemon p : pokemonServices.pokemonOf(t.getId())) pokedex.caught(playerId, p.getFormId(), p.getLevel(), p.isShiny());
            Map<Integer, Integer> dex = pokedex.of(playerId);
            Map<Integer, int[]> stickers = pokedex.stickersOf(playerId);

            StringBuilder json = new StringBuilder();
            int total = 0, caught = 0, shinyCaught = 0;
            for (String[] form : pokemonServices.listFormLabels()) {
                total++;
                int id = Integer.parseInt(form[0]);
                for (boolean shiny : new boolean[] {false, true}) {
                    int key = Pokedex.key(id, shiny);
                    Integer state = dex.get(key);
                    if (state == null) continue;
                    if (state == Pokedex.CAUGHT) { if (shiny) shinyCaught++; else caught++; }
                    int[] st = stickers.get(key);  // sticker: 0 none, 1 loose (in his hand), 2 stuck on the album
                    json.append(json.length() > 0 ? "," : "").append("{\"id\":").append(form[0]).append(",\"label\":\"").append(escape(form[1]))
                        .append("\",\"family\":").append(form[2]).append(",\"stage\":").append(form[3]).append(",\"key\":\"").append(escape(form[4]))
                        .append("\",\"types\":[\"").append(form[5].replace("/", "\",\"")).append("\"],\"caught\":").append(state == Pokedex.CAUGHT)
                        .append(",\"sticker\":").append(st == null ? 0 : st[2] == Pokedex.STUCK ? 2 : st[2] == Pokedex.LOOSE ? 1 : 3).append(",\"lvl\":").append(st == null ? 0 : st[0]).append(",\"shiny\":").append(shiny).append("}");
                }
            }
            reply(exchange, 200, "application/json", "{\"total\":" + total + ",\"caught\":" + caught + ",\"shinyCaught\":" + shinyCaught + ",\"entries\":[" + json + "]}");
        });

        // Master-only: wipe a player's Pokedex (album, stickers)
        httpServer.createContext("/api/dex/reset", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            GameSession session = sessionServices.findSessionByCode(form.getOrDefault("code", "").trim().toUpperCase());
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }
            int playerId;
            try { playerId = Integer.parseInt(form.getOrDefault("playerId", "")); } catch (NumberFormatException e) { playerId = -1; }
            if (sessionServices.findPlayerParticipant(session, playerId) == null) { reply(exchange, 404, "application/json", error("Player not in this session")); return; }
            pokedex.reset(playerId);
            reply(exchange, 200, "application/json", "{}");
        });

        // My character's numbers (the six stats and the current HP). Level and XP are the Master's.
        httpServer.createContext("/api/profile", exchange -> {
            Map<String, String> form = readForm(exchange);
            SessionParticipant me = sessionPlayerOrNull(form);
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int trainerId = intOr(form.get("trainerId"), -1);
            boolean mine = false;
            for (Trainer t : sessionServices.trainersOf(me.getPlayerId())) if (t.getId() == trainerId) mine = true;
            if (!mine) { reply(exchange, 404, "application/json", error("Not your trainer")); return; }
            applyProfile(trainerId, form);
            reply(exchange, 200, "application/json", trainersJson(me.getPlayerId()));
        });

        // Master-only: same plus XP (+/-), level
        httpServer.createContext("/api/profile/master", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            GameSession session = sessionServices.findSessionByCode(form.getOrDefault("code", "").trim().toUpperCase());
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }
            int trainerId = intOr(form.get("trainerId"), -1);
            boolean found = false;
            for (Player p : sessionServices.playersOf(session)) for (Trainer t : sessionServices.trainersOf(p.getId())) if (t.getId() == trainerId) found = true;
            if (!found) { reply(exchange, 404, "application/json", error("Trainer not in this session")); return; }
            if (form.containsKey("level")) profiles.setLevel(trainerId, intOr(form.get("level"), 1));
            if (form.containsKey("xp")) profiles.addXp(trainerId, intOr(form.get("xp"), 0));
            applyProfile(trainerId, form);
            reply(exchange, 200, "application/json", profiles.json(trainerId));
        });

        // Master-only: send every wild Pokemon of the session away
        httpServer.createContext("/api/field/clear", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            reply(exchange, 200, "application/json", "{\"removed\":" + spawnFeed.clearField(form.getOrDefault("code", "").trim().toUpperCase()) + "}");
        });

        // Open the pack: the waiting stickers go to his hand
        httpServer.createContext("/api/dex/open", exchange -> {
            SessionParticipant me = sessionPlayerOrNull(readForm(exchange));
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            reply(exchange, 200, "application/json", "{\"opened\":" + pokedex.openPack(me.getPlayerId()) + "}");
        });

        // Stick a loose sticker on the album (the page already checked it is the right slot)
        httpServer.createContext("/api/dex/paste", exchange -> {
            Map<String, String> form = readForm(exchange);
            SessionParticipant me = sessionPlayerOrNull(form);
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int formId; try { formId = Integer.parseInt(form.getOrDefault("formId", "")); } catch (NumberFormatException e) { formId = -1; }
            boolean ok = pokedex.paste(me.getPlayerId(), formId, "1".equals(form.get("shiny")));
            reply(exchange, ok ? 200 : 409, "application/json", "{\"ok\":" + ok + "}");
        });

        // The shared table: a snapshot of items and hands (players with their token; the Master from this computer with master=1)
        httpServer.createContext("/api/table", exchange -> {
            Map<String, String> query = parse(exchange.getRequestURI().getRawQuery());
            String code = query.getOrDefault("code", "").trim().toUpperCase();
            GameSession session = sessionServices.findSessionByCode(code);
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }
            boolean master = isLocal(exchange) && "1".equals(query.get("master"));
            if (!master && sessionServices.findByToken(session, query.get("token")) == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int meId = master ? TableServices.MASTER : sessionServices.findByToken(session, query.get("token")).getPlayerId();
            String snap = table.snapshot(code);
            reply(exchange, 200, "application/json", snap.substring(0, snap.length() - 1) + ",\"me\":" + meId + ",\"ms\":" + table.seatOf(code, meId) + "}");
        });

        // A hand reports its place; releasing over the chest puts the stack in the character's bag
        httpServer.createContext("/api/table/hand", exchange -> {
            Map<String, String> form = readForm(exchange);
            String code = form.getOrDefault("code", "").trim().toUpperCase();
            GameSession session = sessionServices.findSessionByCode(code);
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }
            boolean master = isLocal(exchange) && "1".equals(form.get("master"));
            SessionParticipant me = master ? null : sessionServices.findByToken(session, form.get("token"));
            if (!master && me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int trainerId = -1;
            try { trainerId = Integer.parseInt(form.getOrDefault("trainerId", "")); } catch (NumberFormatException e) { /* no chest */ }
            boolean ownTrainer = false;
            if (me != null) for (Trainer t : sessionServices.trainersOf(me.getPlayerId())) if (t.getId() == trainerId) ownTrainer = true;
            double x, y; int take;
            try { x = Double.parseDouble(form.getOrDefault("x", "0")); y = Double.parseDouble(form.getOrDefault("y", "0")); take = Integer.parseInt(form.getOrDefault("take", "1")); }
            catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            String name = master ? "Master" : sessionServices.getRepository().findPlayerById(me.getPlayerId()).getName();
            int handId = master ? TableServices.MASTER : me.getPlayerId();
            boolean toChest = "1".equals(form.get("chest")) && ownTrainer;
            TableServices.Prop dropped = table.hand(code, handId, name, form.get("color"), x, y, "1".equals(form.get("down")), take, toChest);
            if (dropped != null) {
                captureServices.addItem(trainerId, dropped.getItemId(), dropped.getQty());
                reply(exchange, 200, "application/json", "{\"deposited\":" + dropped.getQty() + ",\"name\":\"" + escape(dropped.getName()) + "\"}"); return;
            }
            reply(exchange, 200, "application/json", "{}");
        });

        // Master-only: toss a stack of an item onto the table
        httpServer.createContext("/api/table/spawn", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            int itemId, qty;
            try { itemId = Integer.parseInt(form.getOrDefault("itemId", "")); qty = Integer.parseInt(form.getOrDefault("qty", "")); }
            catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            Server.Item.Item item = sessionServices.getRepository().findItemById(itemId);
            if (item == null || qty < 1 || qty > 99999) { reply(exchange, 400, "application/json", error("Bad item")); return; }
            table.toss(form.getOrDefault("code", "").trim().toUpperCase(), itemId, item.getName(), captureServices.ballSkin(item), captureServices.itemIcon(item), qty, (Math.random() - 0.5) * 0.5, (Math.random() - 0.5) * 0.5);
            reply(exchange, 200, "application/json", "{}");
        });

        // Master-only: toss a whole list ("itemId:qty,itemId:qty") at random places of the table
        httpServer.createContext("/api/table/spawnlist", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            String code = form.getOrDefault("code", "").trim().toUpperCase();
            int n = 0;
            for (String pair : form.getOrDefault("list", "").split(",")) {
                String[] part = pair.split(":");
                if (part.length != 2) continue;
                try {
                    int itemId = Integer.parseInt(part[0].trim()), qty = Integer.parseInt(part[1].trim());
                    Server.Item.Item item = sessionServices.getRepository().findItemById(itemId);
                    if (item == null || qty < 1 || qty > 99999) continue;
                    double a = Math.random() * Math.PI * 2, r = Math.sqrt(Math.random()) * 0.55;   // anywhere on the middle of the table
                    table.toss(code, itemId, item.getName(), captureServices.ballSkin(item), captureServices.itemIcon(item), qty, Math.cos(a) * r, Math.sin(a) * r);
                    n++;
                } catch (NumberFormatException e) { /* skip the bad pair */ }
            }
            reply(exchange, 200, "application/json", "{\"tossed\":" + n + "}");
        });

        // A stack comes out of the player's chest (taken from his bag) or off the Master's list (unlimited) into the hand
        httpServer.createContext("/api/table/pull", exchange -> {
            Map<String, String> form = readForm(exchange);
            String code = form.getOrDefault("code", "").trim().toUpperCase();
            GameSession session = sessionServices.findSessionByCode(code);
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }
            boolean master = isLocal(exchange) && "1".equals(form.get("master"));
            SessionParticipant me = master ? null : sessionServices.findByToken(session, form.get("token"));
            if (!master && me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int itemId, trainerId = -1, take;
            double x, y;
            try {
                itemId = Integer.parseInt(form.getOrDefault("itemId", ""));
                take = Integer.parseInt(form.getOrDefault("take", "1"));
                x = Double.parseDouble(form.getOrDefault("x", "0")); y = Double.parseDouble(form.getOrDefault("y", "0"));
                if (!master) trainerId = Integer.parseInt(form.getOrDefault("trainerId", ""));
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            Server.Item.Item item = sessionServices.getRepository().findItemById(itemId);
            if (item == null) { reply(exchange, 404, "application/json", error("Item not found")); return; }
            int handId = master ? TableServices.MASTER : me.getPlayerId();
            String name = master ? "Master" : sessionServices.getRepository().findPlayerById(me.getPlayerId()).getName();
            int qty;
            if (master) qty = Math.max(1, Math.min(take, 99999));
            else {
                boolean own = false;
                for (Trainer t : sessionServices.trainersOf(me.getPlayerId())) if (t.getId() == trainerId) own = true;
                if (!own) { reply(exchange, 403, "application/json", error("Not your character")); return; }
                int have = captureServices.quantity(trainerId, itemId);
                qty = take == -1 ? (have + 1) / 2 : Math.min(take, have);
                if (qty < 1 || !captureServices.removeItems(trainerId, itemId, qty)) { reply(exchange, 409, "application/json", error("Not enough")); return; }
            }
            table.hand(code, handId, name, form.get("color"), x, y, false, 1, false);   // makes sure his hand exists at that place
            table.holdNew(code, handId, itemId, item.getName(), captureServices.ballSkin(item), captureServices.itemIcon(item), qty);
            reply(exchange, 200, "application/json", "{\"qty\":" + qty + "}");
        });

        // Master-only: sweep the table
        httpServer.createContext("/api/table/clear", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            table.clear(readForm(exchange).getOrDefault("code", "").trim().toUpperCase());
            reply(exchange, 200, "application/json", "{}");
        });

        // Master-only: every item that exists (the Master's list for the table)
        httpServer.createContext("/api/items", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (Server.Item.Item item : sessionServices.getRepository().findAllItems()) {
                json.append(first ? "" : ",").append("{\"id\":").append(item.getId()).append(",\"name\":\"").append(escape(item.getName()))
                    .append("\",\"cat\":\"").append(escape(item.getCategory())).append("\",\"skin\":\"").append(captureServices.ballSkin(item))
                    .append("\",\"icon\":\"").append(captureServices.itemIcon(item)).append("\",\"info\":\"").append(escape(item.getDescription())).append("\"}");
                first = false;
            }
            reply(exchange, 200, "application/json", json.append("]").toString());
        });

        // Master-only: give items (balls) to a character of this session
        httpServer.createContext("/api/giveitem", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            GameSession session = sessionServices.findSessionByCode(form.getOrDefault("code", ""));
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }
            int trainerId, itemId, amount;
            try {
                trainerId = Integer.parseInt(form.getOrDefault("trainerId", ""));
                itemId = Integer.parseInt(form.getOrDefault("itemId", ""));
                amount = Integer.parseInt(form.getOrDefault("amount", ""));
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            if (amount < 1 || amount > 99999) { reply(exchange, 400, "application/json", error("Amount must be 1-99999")); return; }
            Trainer trainer = sessionServices.getRepository().findTrainerById(trainerId);
            if (trainer == null || sessionServices.findPlayerParticipant(session, trainer.getPlayerId()) == null) {
                reply(exchange, 404, "application/json", error("Character not in this session")); return;
            }
            Server.Item.Item item = sessionServices.getRepository().findItemById(itemId);
            if (item == null) { reply(exchange, 404, "application/json", error("Item not found")); return; }
            captureServices.addItem(trainerId, itemId, amount);
            reply(exchange, 200, "application/json", "{\"name\":\"" + escape(item.getName()) + "\",\"qty\":" + captureServices.quantity(trainerId, itemId) + "}");
        });

        // Rename one of my Pokemon (empty = back to the species name)
        httpServer.createContext("/api/pc/nickname", exchange -> {
            Map<String, String> form = readForm(exchange);
            SessionParticipant me = sessionPlayerOrNull(form);
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int trainerId, pokemonId;
            try {
                trainerId = Integer.parseInt(form.getOrDefault("trainerId", ""));
                pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", ""));
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            boolean mine = false;
            for (Trainer t : sessionServices.trainersOf(me.getPlayerId())) if (t.getId() == trainerId) mine = true;
            if (!mine) { reply(exchange, 404, "application/json", error("Not your trainer")); return; }
            String problem = pokemonServices.setNickname(trainerId, pokemonId, form.get("nickname"));
            if (problem != null) { reply(exchange, 400, "application/json", error(problem)); return; }
            reply(exchange, 200, "application/json", trainersJson(me.getPlayerId()));
        });

        httpServer.createContext("/api/pc/release", exchange -> {
            Map<String, String> form = readForm(exchange);
            SessionParticipant me = sessionPlayerOrNull(form);
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int trainerId, pokemonId;
            try {
                trainerId = Integer.parseInt(form.getOrDefault("trainerId", ""));
                pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", ""));
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            boolean mine = false;
            for (Trainer t : sessionServices.trainersOf(me.getPlayerId())) if (t.getId() == trainerId) mine = true;
            if (!mine) { reply(exchange, 404, "application/json", error("Not your trainer")); return; }
            String problem = pokemonServices.releasePokemon(trainerId, pokemonId);
            if (problem != null) { reply(exchange, 400, "application/json", error(problem)); return; }
            reply(exchange, 200, "application/json", trainersJson(me.getPlayerId()));
        });

        // Spend free stat points on one of my Pokemon: add = "0,1,0,0,2,0" (HP, Atk, Def, SpA, SpD, Spe)
        httpServer.createContext("/api/pc/stats", exchange -> {
            Map<String, String> form = readForm(exchange);
            SessionParticipant me = sessionPlayerOrNull(form);
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int trainerId, pokemonId;
            int[] add = new int[6];
            try {
                trainerId = Integer.parseInt(form.getOrDefault("trainerId", ""));
                pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", ""));
                String[] parts = form.getOrDefault("add", "").split(",");
                if (parts.length != 6) throw new NumberFormatException();
                for (int i = 0; i < 6; i++) add[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            if (!isMine(me, trainerId)) { reply(exchange, 404, "application/json", error("Not your trainer")); return; }
            String problem = pokemonServices.spendPoints(trainerId, pokemonId, add);
            if (problem != null) { reply(exchange, 400, "application/json", error(problem)); return; }
            reply(exchange, 200, "application/json", trainersJson(me.getPlayerId()));
        });

        // Set the current HP of one of my Pokemon (kept between 0 and its maximum)
        httpServer.createContext("/api/pc/hp", exchange -> {
            Map<String, String> form = readForm(exchange);
            SessionParticipant me = sessionPlayerOrNull(form);
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int trainerId, pokemonId;
            float hp;
            try {
                trainerId = Integer.parseInt(form.getOrDefault("trainerId", ""));
                pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", ""));
                hp = Float.parseFloat(form.getOrDefault("hp", ""));
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            if (!isMine(me, trainerId)) { reply(exchange, 404, "application/json", error("Not your trainer")); return; }
            String problem = pokemonServices.setHp(trainerId, pokemonId, hp);
            if (problem != null) { reply(exchange, 400, "application/json", error(problem)); return; }
            reply(exchange, 200, "application/json", trainersJson(me.getPlayerId()));
        });

        // Master-only: give XP to a Pokemon (levels up on its own) -> {"from":old level,"to":new level}
        httpServer.createContext("/api/xp", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            int pokemonId, amount;
            try {
                pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", ""));
                amount = Integer.parseInt(form.getOrDefault("amount", ""));
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            int[] levels = pokemonServices.addXp(pokemonId, amount);
            if (levels == null) { reply(exchange, 400, "application/json", error("XP must be 1-100000")); return; }
            reply(exchange, 200, "application/json", "{\"from\":" + levels[0] + ",\"to\":" + levels[1] + "}");
        });

        // Master-only: set the current HP of any trainer's Pokemon
        httpServer.createContext("/api/hp", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            int pokemonId;
            float hp;
            try {
                pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", ""));
                hp = Float.parseFloat(form.getOrDefault("hp", ""));
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            String problem = pokemonServices.setHp(null, pokemonId, hp);
            if (problem != null) { reply(exchange, 400, "application/json", error(problem)); return; }
            reply(exchange, 200, "application/json", "{}");
        });

        // Player: evolve one of my Pokemon (only the plain level evolutions; the rest the Master does)
        httpServer.createContext("/api/pc/evolve", exchange -> {
            Map<String, String> form = readForm(exchange);
            SessionParticipant me = sessionPlayerOrNull(form);
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int trainerId, pokemonId;
            try {
                trainerId = Integer.parseInt(form.getOrDefault("trainerId", ""));
                pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", ""));
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            if (!isMine(me, trainerId)) { reply(exchange, 404, "application/json", error("Not your trainer")); return; }
            String problem = pokemonServices.evolve(trainerId, pokemonId, form.getOrDefault("to", ""));
            if (problem != null) { reply(exchange, 400, "application/json", error(problem)); return; }
            reply(exchange, 200, "application/json", trainersJson(me.getPlayerId()));
        });

        // Master-only: evolve any trainer's Pokemon into a species it evolves into, whatever the requirement
        httpServer.createContext("/api/evolve", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            int pokemonId;
            try { pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", "")); }
            catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            String problem = pokemonServices.evolve(null, pokemonId, form.getOrDefault("to", ""));
            if (problem != null) { reply(exchange, 400, "application/json", error(problem)); return; }
            reply(exchange, 200, "application/json", "{}");
        });

        // Player: ask the Master to adjust one of my Pokemon (reset points, change nature...)
        httpServer.createContext("/api/pc/request", exchange -> {
            Map<String, String> form = readForm(exchange);
            SessionParticipant me = sessionPlayerOrNull(form);
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }
            int trainerId, pokemonId;
            try {
                trainerId = Integer.parseInt(form.getOrDefault("trainerId", ""));
                pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", ""));
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            if (!isMine(me, trainerId)) { reply(exchange, 404, "application/json", error("Not your trainer")); return; }
            Pokemon target = null;
            for (Pokemon p : pokemonServices.pokemonOf(trainerId)) if (p.getId() == pokemonId) target = p;
            if (target == null) { reply(exchange, 404, "application/json", error("Pokemon not found")); return; }
            String text = form.getOrDefault("text", "").replaceAll("[\\p{Cntrl}]", " ").trim();
            if (text.length() > 120) text = text.substring(0, 120);
            Trainer trainer = null;
            for (Trainer t : sessionServices.trainersOf(me.getPlayerId())) if (t.getId() == trainerId) trainer = t;
            String name = target.getNickname() != null ? target.getNickname() : pokemonServices.getRepository().findPokemonSpeciesByDexNumber(target.getSpeciesId()).getName();
            helpRequests.add(form.getOrDefault("code", "").trim().toUpperCase(), trainerId, pokemonId, trainer.getName(), name, text);
            reply(exchange, 200, "application/json", trainersJson(me.getPlayerId()));
        });

        // Master-only: open requests of a session, and mark one as handled
        httpServer.createContext("/api/requests", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> query = parse(exchange.getRequestURI().getRawQuery());
            reply(exchange, 200, "application/json", helpRequests.json(query.getOrDefault("code", "").trim().toUpperCase(), this::escape));
        });
        httpServer.createContext("/api/request/done", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            try { helpRequests.done(Integer.parseInt(form.getOrDefault("id", ""))); } catch (NumberFormatException e) { /* nothing to close */ }
            reply(exchange, 200, "application/json", "{}");
        });

        // Master-only: edit one Pokemon (nature, ability, gender, shiny, level) or give back its spent points
        httpServer.createContext("/api/edit", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            int pokemonId;
            Integer natureId = null, abilityId = null, level = null;
            Boolean shiny = null;
            try {
                pokemonId = Integer.parseInt(form.getOrDefault("pokemonId", ""));
                if (!form.getOrDefault("natureId", "").isEmpty()) natureId = Integer.parseInt(form.get("natureId"));
                if (!form.getOrDefault("abilityId", "").isEmpty()) abilityId = Integer.parseInt(form.get("abilityId"));
                if (!form.getOrDefault("level", "").isEmpty()) level = Integer.parseInt(form.get("level"));
                if (!form.getOrDefault("shiny", "").isEmpty()) shiny = form.get("shiny").equals("1");
            } catch (NumberFormatException e) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
            String gender = form.getOrDefault("gender", "").isEmpty() ? null : form.get("gender");
            int[] free = null;
            String freeText = form.getOrDefault("free", "");
            if (!freeText.isEmpty()) {
                String[] parts = freeText.split(",");
                if (parts.length != 6) { reply(exchange, 400, "application/json", error("Bad numbers")); return; }
                free = new int[6];
                for (int i = 0; i < 6; i++) free[i] = parts[i].trim().equals("1") ? 1 : 0;
            }
            String problem = pokemonServices.editPokemon(pokemonId, natureId, abilityId, gender, shiny, level, "1".equals(form.get("reset")), free);
            if (problem != null) { reply(exchange, 400, "application/json", error(problem)); return; }
            reply(exchange, 200, "application/json", "{}");
        });

        httpServer.createContext("/api/trainer/delete", exchange -> {
            Map<String, String> form = readForm(exchange);
            GameSession session = sessionServices.findSessionByCode(form.getOrDefault("code", ""));
            SessionParticipant me = session == null ? null : sessionServices.findByToken(session, form.get("token"));
            if (me == null) { reply(exchange, 403, "application/json", error("removed")); return; }

            int id;
            try { id = Integer.parseInt(form.getOrDefault("trainerId", "")); } catch (NumberFormatException e) { id = -1; }
            if (!sessionServices.deleteTrainer(me, id)) { reply(exchange, 404, "application/json", error("Not your trainer")); return; }
            reply(exchange, 200, "application/json", trainersJson(me.getPlayerId()));
        });

        // Master-only: one-time code so a player who lost his browser data can get back in
        httpServer.createContext("/api/claimcode", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            GameSession session = sessionServices.findSessionByCode(form.getOrDefault("code", ""));
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }

            int playerId;
            try { playerId = Integer.parseInt(form.getOrDefault("playerId", "")); } catch (NumberFormatException e) { playerId = -1; }
            SessionParticipant sp = sessionServices.findPlayerParticipant(session, playerId);
            if (sp == null) { reply(exchange, 404, "application/json", error("Player not in this session")); return; }

            reply(exchange, 200, "application/json", "{\"claim\":\"" + sessionServices.createClaimCode(session, sp) + "\"}");
        });

        // Player types the Master's code: same player, new token
        httpServer.createContext("/api/claim", exchange -> {
            Map<String, String> form = readForm(exchange);
            GameSession session = sessionServices.findSessionByCode(form.getOrDefault("code", ""));
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }

            SessionParticipant sp = sessionServices.claim(session, form.getOrDefault("claim", ""));
            if (sp == null) { reply(exchange, 404, "application/json", error("Invalid or expired reconnect code")); return; }

            sessionServices.touch(sp.getPlayerId());
            reply(exchange, 200, "application/json", playerJson(session, sp));
        });

        // "It is me": a player whose name is already in the session asks to come back
        httpServer.createContext("/api/rejoin/request", exchange -> {
            Map<String, String> form = readForm(exchange);
            GameSession session = sessionServices.findSessionByCode(form.getOrDefault("code", "").trim());
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown code")); return; }
            SessionServices.Rejoin r = sessionServices.requestRejoin(session, form.getOrDefault("name", ""));
            if (r == null) { reply(exchange, 404, "application/json", error("No player with that name in this session")); return; }
            reply(exchange, 200, "application/json", "{\"id\":" + r.id + ",\"secret\":\"" + r.secret + "\"}");
        });

        // The asking browser polls until the Master answers; "approved" carries the new login (same as /api/join)
        httpServer.createContext("/api/rejoin/status", exchange -> {
            Map<String, String> query = parse(exchange.getRequestURI().getRawQuery());
            int id; try { id = Integer.parseInt(query.getOrDefault("id", "")); } catch (NumberFormatException e) { id = -1; }
            SessionServices.Rejoin r = sessionServices.rejoinFor(id, query.getOrDefault("secret", ""));
            if (r == null) { reply(exchange, 200, "application/json", "{\"status\":\"gone\"}"); return; }
            if (r.status.equals("approved")) {
                GameSession session = sessionServices.getRepository().findGameSessionById(r.sessionId);
                SessionParticipant sp = session == null ? null : sessionServices.findByToken(session, r.token);
                if (sp != null) { sessionServices.touch(sp.getPlayerId()); reply(exchange, 200, "application/json", "{\"status\":\"approved\",\"player\":" + playerJson(session, sp) + "}"); return; }
                reply(exchange, 200, "application/json", "{\"status\":\"gone\"}"); return;
            }
            reply(exchange, 200, "application/json", "{\"status\":\"" + r.status + "\"}");
        });

        // Master-only: who wants back in, and the answer
        httpServer.createContext("/api/rejoin/pending", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            StringBuilder json = new StringBuilder("[");
            for (SessionServices.Rejoin r : sessionServices.pendingRejoins())
                json.append(json.length() > 1 ? "," : "").append("{\"id\":").append(r.id).append(",\"code\":\"").append(r.code).append("\",\"name\":\"").append(escape(r.name)).append("\",\"online\":").append(sessionServices.isOnlineParticipant(r.participantId)).append("}");
            reply(exchange, 200, "application/json", json.append("]").toString());
        });
        httpServer.createContext("/api/rejoin/answer", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            try { sessionServices.answerRejoin(Integer.parseInt(form.getOrDefault("id", "")), "1".equals(form.get("ok"))); } catch (NumberFormatException e) { /* nothing to answer */ }
            reply(exchange, 200, "application/json", "{}");
        });

        // Is my saved token still valid? (page reload / switching campaign)
        httpServer.createContext("/api/me", exchange -> {
            Map<String, String> query = parse(exchange.getRequestURI().getRawQuery());
            GameSession session = sessionServices.findSessionByCode(query.getOrDefault("code", ""));
            if (session == null) { reply(exchange, 404, "application/json", error("Session no longer exists")); return; }

            SessionParticipant sp = sessionServices.findByToken(session, query.get("token"));
            if (sp == null) { reply(exchange, 403, "application/json", error("You were removed from this session")); return; }

            sessionServices.touch(sp.getPlayerId());
            reply(exchange, 200, "application/json", playerJson(session, sp));
        });

        // Player leaves on purpose: frees the name
        httpServer.createContext("/api/leave", exchange -> {
            Map<String, String> form = readForm(exchange);
            GameSession session = sessionServices.findSessionByCode(form.getOrDefault("code", ""));
            if (session != null) {
                SessionParticipant sp = sessionServices.findByToken(session, form.get("token"));
                if (sp != null) sessionServices.removePlayer(sp);
            }
            reply(exchange, 200, "application/json", "{}");
        });

        // Master-only: players of a session, with online status
        httpServer.createContext("/api/players", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> query = parse(exchange.getRequestURI().getRawQuery());
            GameSession session = sessionServices.findSessionByCode(query.getOrDefault("code", ""));
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }

            StringBuilder json = new StringBuilder("{\"players\":[");
            boolean first = true;
            for (Player p : sessionServices.playersOf(session)) {
                if (!first) json.append(",");
                first = false;
                json.append("{\"id\":").append(p.getId()).append(",\"name\":\"").append(escape(p.getName()))
                    .append("\",\"online\":").append(sessionServices.isOnline(p.getId()))
                    .append(",\"ago\":").append(sessionServices.lastSeenAgo(p.getId()))
                    .append(",\"dex\":").append(pokedex.caughtCount(p.getId()))
                    .append(",\"trainers\":").append(trainersJson(p.getId())).append("}");
            }
            reply(exchange, 200, "application/json", json.append("]}").toString());
        });

        // Master-only: kick a player (his browser finds out on the next poll)
        httpServer.createContext("/api/kick", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            GameSession session = sessionServices.findSessionByCode(form.getOrDefault("code", ""));
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }

            int playerId;
            try { playerId = Integer.parseInt(form.getOrDefault("playerId", "")); } catch (NumberFormatException e) { playerId = -1; }
            SessionParticipant sp = sessionServices.findPlayerParticipant(session, playerId);
            if (sp == null) { reply(exchange, 404, "application/json", error("Player not in this session")); return; }

            sessionServices.removePlayer(sp);
            reply(exchange, 200, "application/json", "{}");
        });

        // Master-only: delete a session and everything attached to it
        // (longer path than /api/session, so the server picks this one)
        httpServer.createContext("/api/session/delete", exchange -> {
            if (!isLocal(exchange)) { reply(exchange, 403, "application/json", error("Master only")); return; }
            Map<String, String> form = readForm(exchange);
            String code = form.getOrDefault("code", "").trim().toUpperCase();
            GameSession session = sessionServices.findSessionByCode(code);
            if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }

            sessionServices.deleteSession(session);
            spawnFeed.clear(code);
            reply(exchange, 200, "application/json", "{}");
        });

        httpServer.start();

        System.out.println("Master page:  http://localhost:" + port + "/master");
        System.out.println("Players open: http://" + lanAddress() + ":" + port);
    }

    //============HELPERS=============

    // The player of a request with code + token, or null
    private boolean isMine(SessionParticipant me, int trainerId) {
        for (Trainer t : sessionServices.trainersOf(me.getPlayerId())) if (t.getId() == trainerId) return true;
        return false;
    }

    private SessionParticipant sessionPlayerOrNull(Map<String, String> data) {
        GameSession session = sessionServices.findSessionByCode(data.getOrDefault("code", "").trim().toUpperCase());
        return session == null ? null : sessionServices.findByToken(session, data.get("token"));
    }

    // [{"id":1,"name":"Roberto","placed":false}, ...]; "placed" = already in the session of "me"
    private String sheetsJson(String person, SessionParticipant me) {
        java.util.Set<Integer> placed = me == null ? java.util.Set.of() : sessionServices.placedSheets(me.getPlayerId());
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (var sheet : sessionServices.sheetsOf(person)) {
            if (!first) json.append(",");
            first = false;
            json.append("{\"id\":").append(sheet.getId()).append(",\"name\":\"").append(escape(sheet.getName()))
                .append("\",\"placed\":").append(placed.contains(sheet.getId())).append("}");
        }
        return json.append("]").toString();
    }

    // [{"id":1,"name":"Roberto"}, ...]
    private static int intOr(String v, int def) { try { return Integer.parseInt(v.trim()); } catch (Exception e) { return def; } }

    // stats = hp,atk,def,spatk,spdef,spd (comma separated); hp = current HP. Either may be missing.
    private void applyProfile(int trainerId, Map<String, String> form) {
        int[] stats = null;
        if (form.get("stats") != null) {
            String[] parts = form.get("stats").split(",");
            if (parts.length == 6) { stats = new int[6]; for (int k = 0; k < 6; k++) stats[k] = intOr(parts[k], 10); }
        }
        Integer cur = form.get("hp") == null ? null : Integer.valueOf(intOr(form.get("hp"), 0));
        if (stats != null || cur != null) profiles.set(trainerId, stats, cur);
    }

    private String trainersJson(int playerId) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Trainer t : sessionServices.trainersOf(playerId)) {
            if (!first) json.append(",");
            first = false;
            json.append("{\"id\":").append(t.getId()).append(",\"name\":\"").append(escape(t.getName()))
                .append("\",\"profile\":").append(profiles.json(t.getId())).append(",\"boxes\":[");
            boolean firstBox = true;
            for (Server.PC.PCBox box : pokemonServices.boxesOf(t.getId())) {
                if (!firstBox) json.append(",");
                firstBox = false;
                json.append(box.getSlotLimit());
            }
            json.append("],\"team\":[");
            boolean firstMon = true;
            for (Pokemon p : pokemonServices.pokemonOf(t.getId())) {
                if (!firstMon) json.append(",");
                firstMon = false;
                json.append(pokemonServices.ownJson(p));
            }
            json.append("],\"items\":").append(captureServices.inventoryJson(t.getId())).append("}");
        }
        return json.append("]").toString();
    }

    private String playerJson(GameSession session, SessionParticipant sp) {
        Player player = sessionServices.getRepository().findPlayerById(sp.getPlayerId());
        return "{\"playerId\":" + player.getId() + ",\"name\":\"" + escape(player.getName())
            + "\",\"code\":\"" + session.getJoinCode() + "\",\"token\":\"" + sp.getToken() + "\"}";
    }

    // Spawn a wild Pokemon into a session from form fields (shared by /api/spawn and /api/area/roll)
    private void spawnFromForm(HttpExchange exchange, Map<String, String> form) throws IOException {

        String code = form.getOrDefault("code", "").trim().toUpperCase();
        GameSession session = sessionServices.findSessionByCode(code);
        if (session == null) { reply(exchange, 404, "application/json", error("Unknown session")); return; }

        // target: "all" (or empty) = everyone, otherwise a player id of THIS session
        int target = 0;
        String targetText = form.getOrDefault("target", "all");
        if (!targetText.equals("all") && !targetText.isEmpty()) {
            try { target = Integer.parseInt(targetText); } catch (NumberFormatException e) { target = -1; }
            if (target <= 0 || sessionServices.findPlayerParticipant(session, target) == null) {
                reply(exchange, 404, "application/json", error("Player not in this session")); return;
            }
        }

        int formId, level;
        try {
            formId = Integer.parseInt(form.getOrDefault("formId", ""));
            level = Integer.parseInt(form.getOrDefault("level", ""));
        } catch (NumberFormatException e) {
            reply(exchange, 400, "application/json", error("formId and level must be numbers"));
            return;
        }
        if (level < 1 || level > 100) { reply(exchange, 400, "application/json", error("Level must be 1-100")); return; }

        PokemonServices.SpawnOptions options = spawnOptions(form);
        String problem = pokemonServices.checkOptions(formId, level, options);
        if (problem != null) { reply(exchange, 400, "application/json", error(problem)); return; }
        Pokemon pokemon = pokemonServices.spawnPokemon(formId, level, options);
        if (pokemon == null) { reply(exchange, 404, "application/json", error("Form not found")); return; }

        String catchMode = form.getOrDefault("catch", "normal");
        if (SpawnFeed.MODES.contains(catchMode)) spawnFeed.setMode(pokemon.getId(), catchMode);
        try { spawnFeed.setTries(pokemon.getId(), Integer.parseInt(form.getOrDefault("tries", "3"))); } catch (NumberFormatException e) { spawnFeed.setTries(pokemon.getId(), 3); }
        spawnFeed.add(code, target, pokemon.getId(), pokemonServices.describePokemonJson(pokemon));  // players: no nature/ability
        reply(exchange, 200, "application/json", pokemonServices.masterJson(pokemon));
    }

    // "hp":12,"maxHp":30 of a wild Pokemon (part of a JSON object)
    private String wildHp(int id) {
        Pokemon p = sessionServices.getRepository().findPokemonById(id);
        return p == null ? "\"hp\":0,\"maxHp\":1" : "\"hp\":" + p.getHp() + ",\"maxHp\":" + pokemonServices.maxHpOf(p);
    }

    // ,"nature":..,"base":[..],"caps":[..] of a wild Pokemon (Master's eyes only)
    private String wildDetail(int id) {
        Pokemon p = sessionServices.getRepository().findPokemonById(id);
        return p == null ? "" : "," + pokemonServices.wildDetailJson(p);
    }

    // capture rate right now (Master's eyes only)
    private int wildRate(int id) {
        Pokemon p = sessionServices.getRepository().findPokemonById(id);
        return p == null ? 0 : captureServices.captureRate(p, spawnFeed.getState(id));
    }

    // Master = a request from THIS computer's own browser. A tunnel (Cloudflare) also arrives from this computer, so
    // a request that carries proxy headers or a public Host name is a player on the internet, never the Master.
    private boolean isLocal(HttpExchange exchange) {
        if (!exchange.getRemoteAddress().getAddress().isLoopbackAddress()) return false;
        com.sun.net.httpserver.Headers headers = exchange.getRequestHeaders();
        for (String name : new String[] {"Cf-Ray", "Cf-Connecting-Ip", "Cf-Visitor", "Cdn-Loop", "X-Forwarded-For", "X-Forwarded-Host", "X-Real-Ip", "Forwarded"}) {
            if (headers.containsKey(name)) return false;
        }
        String host = headers.getFirst("Host");
        return host == null || host.matches("(?i)(localhost|127\\.0\\.0\\.1|\\[::1\\])(:\\d+)?");
    }

    // Where the players should go: the internet link when the tunnel is up, otherwise this computer's address on the Wi-Fi
    private String playerUrl() {
        return tunnelServices != null && tunnelServices.isReady() ? tunnelServices.getUrl() : "http://" + lanAddress() + ":" + port;
    }

    private Map<String, String> readForm(HttpExchange exchange) throws IOException {
        return parse(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
    }

    // "a=1&b=2" (form body or URL query) -> map
    private Map<String, String> parse(String text) {
        Map<String, String> form = new HashMap<>();
        if (text == null) return form;
        for (String pair : text.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                form.put(
                    URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                );
            }
        }
        return form;
    }

    private void reply(HttpExchange exchange, int status, String type, String body) throws IOException {
        replyBytes(exchange, status, type + "; charset=utf-8", body.getBytes(StandardCharsets.UTF_8));
    }

    private void replyBytes(HttpExchange exchange, int status, String type, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private void serveFile(HttpExchange exchange, String path) throws IOException {

        Path file = WEB_FOLDER.resolve(path.substring(1)).normalize();

        // Block "../" tricks: the file must stay inside web/
        if (!file.startsWith(WEB_FOLDER) || !Files.isRegularFile(file)) {
            reply(exchange, 404, "text/plain", "Not found");
            return;
        }

        String type = path.endsWith(".html") ? "text/html; charset=utf-8"
            : path.endsWith(".css") ? "text/css; charset=utf-8"
            : path.endsWith(".js") ? "application/javascript; charset=utf-8"
            : path.endsWith(".json") ? "application/json; charset=utf-8"
            : path.endsWith(".png") ? "image/png"
            : path.endsWith(".mp3") ? "audio/mpeg"
            : "application/octet-stream";

        // sprites and cries never change: let the browser keep them for a day
        if (path.startsWith("/assets/") && !path.endsWith(".json")) {
            exchange.getResponseHeaders().set("Cache-Control", "max-age=86400");
        } else {
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");   // pages and scripts: always the newest after an update
        }
        replyBytes(exchange, 200, type, Files.readAllBytes(file));
    }

    private String error(String message) {
        return "{\"error\":\"" + escape(message) + "\"}";
    }

    // Spawn / give form fields: nature, ability (ids), gender, shiny ("1" / "0"); empty = random
    private PokemonServices.SpawnOptions spawnOptions(Map<String, String> form) {
        return new PokemonServices.SpawnOptions(
            intOrNull(form.get("nature")), intOrNull(form.get("ability")),
            form.get("gender") != null && Set.of("Male", "Female", "Genderless").contains(form.get("gender")) ? form.get("gender") : null,
            "1".equals(form.get("shiny")) ? Boolean.TRUE : "0".equals(form.get("shiny")) ? Boolean.FALSE : null);
    }

    private static Integer intOrNull(String text) {
        try { return Integer.valueOf(text); } catch (NumberFormatException e) { return null; }
    }

    private String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Address other devices on the same network can use. Connecting a UDP socket
    // sends nothing; it only makes the OS pick the outgoing interface.
    private String lanAddress() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 80);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            return "localhost";
        }
    }

}
