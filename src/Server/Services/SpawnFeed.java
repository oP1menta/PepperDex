package Server.Services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// In-memory list of what the Master spawned, per session code. Players poll it.
// Each spawn is for everyone (target 0) or for one player (target = player id).
// ponytail: lost on server restart and not linked to the session in the database
// (encounter_pokemon has no pokemon/form column yet); persist once encounters are modelled
public class SpawnFeed {

    // Everything below survives a server restart: one file next to pokemon.db, rewritten after each change.
    // ponytail: if pokemon.db is deleted, delete spawnfeed.dat too (ids would be reused)
    private static final java.io.File FILE = new java.io.File("spawnfeed.dat");

    public SpawnFeed() { load(); }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!FILE.exists()) return;
        try (var in = new java.io.ObjectInputStream(new java.io.FileInputStream(FILE))) {
            byCode.putAll((Map<String, List<Entry>>) in.readObject()); modes.putAll((Map<Integer, String>) in.readObject());
            caught.putAll((Map<String, java.util.Set<Integer>>) in.readObject()); effects.putAll((Map<Integer, java.util.Set<String>>) in.readObject());
            injuries.putAll((Map<Integer, Integer>) in.readObject()); bonuses.putAll((Map<Integer, Integer>) in.readObject()); tries.putAll((Map<Integer, Integer>) in.readObject());
        } catch (Exception e) { System.err.println("spawnfeed.dat ignored: " + e); }
    }

    private void save() {
        try (var out = new java.io.ObjectOutputStream(new java.io.FileOutputStream(FILE))) {
            out.writeObject(byCode); out.writeObject(modes); out.writeObject(caught); out.writeObject(effects); out.writeObject(injuries); out.writeObject(bonuses); out.writeObject(tries);
        } catch (java.io.IOException e) { System.err.println("could not save spawnfeed.dat: " + e); }
    }

    private record Entry(int target, int pokemonId, String json) implements java.io.Serializable {}

    private final Map<String, List<Entry>> byCode = new HashMap<>();
    private final Map<Integer, String> modes = new HashMap<>();  // pokemon id -> "always" | "never" (absent = normal roll)
    private final Map<String, java.util.Set<Integer>> caught = new HashMap<>();  // pokemon ids already caught, per code

    //============PUBLIC API=============

    // Stores one spawn (already JSON) and returns its sequence number (1, 2, 3...)
    public synchronized int add(String code, int target, int pokemonId, String spawnJson) {
        List<Entry> list = byCode.computeIfAbsent(code, k -> new ArrayList<>());
        list.add(new Entry(target, pokemonId, spawnJson));
        save();
        return list.size();
    }

    public synchronized int last(String code) {
        List<Entry> list = byCode.get(code);
        return list == null ? 0 : list.size();
    }

    // Spawns after "after" that this player may see (for everyone, or for him), as a JSON array
    public synchronized String since(String code, int after, int playerId) {
        List<Entry> list = byCode.getOrDefault(code, new ArrayList<>());
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (int i = Math.max(after, 0); i < list.size(); i++) {
            Entry e = list.get(i);
            if (e.target() != 0 && e.target() != playerId) continue;
            if (isCaught(code, e.pokemonId())) continue;
            if (!first) json.append(",");
            first = false;
            // add "private": true when it was sent to this player only (the page words the notification)
            json.append(e.json(), 0, e.json().length() - 1).append(",\"private\":").append(e.target() != 0).append("}");
        }
        return json.append("]").toString();
    }

    // Pokemon ids of the spawns after "after" that this player may see (same rule as since())
    public synchronized java.util.List<Integer> visibleIds(String code, int after, int playerId) {
        java.util.List<Integer> ids = new ArrayList<>();
        List<Entry> list = byCode.getOrDefault(code, new ArrayList<>());
        for (int i = Math.max(after, 0); i < list.size(); i++) {
            Entry e = list.get(i);
            if ((e.target() == 0 || e.target() == playerId) && !isCaught(code, e.pokemonId())) ids.add(e.pokemonId());
        }
        return ids;
    }

    // Can this player still try to catch this Pokemon? (it was sent to him / everyone, and nobody caught it)
    public synchronized boolean canCatch(String code, int pokemonId, int playerId) {
        if (isCaught(code, pokemonId)) return false;
        for (Entry e : byCode.getOrDefault(code, new ArrayList<>())) {
            if (e.pokemonId() == pokemonId) return e.target() == 0 || e.target() == playerId;
        }
        return false;
    }

    // Master: sends every Pokemon on the field away (nobody gets them). Returns how many.
    public synchronized int clearField(String code) {
        int n = 0;
        for (Entry e : byCode.getOrDefault(code, new ArrayList<>())) {
            if (isCaught(code, e.pokemonId())) continue;
            caught.computeIfAbsent(code, k -> new java.util.HashSet<>()).add(e.pokemonId()); n++;
        }
        if (n > 0) save();
        return n;
    }

    public synchronized void markCaught(String code, int pokemonId) {
        caught.computeIfAbsent(code, k -> new java.util.HashSet<>()).add(pokemonId); save();
    }

    //============CATCH MODE (Master override)=============

    public static final java.util.Set<String> MODES = java.util.Set.of("normal", "always", "never");

    // "normal" = the PTU roll, "always" = 100%, "never" = 0%
    public synchronized void setMode(int pokemonId, String mode) {
        if ("always".equals(mode) || "never".equals(mode)) modes.put(pokemonId, mode); else modes.remove(pokemonId);
        save();
    }

    public synchronized String getMode(int pokemonId) {
        return modes.getOrDefault(pokemonId, "normal");
    }

    //============WILD STATE (Master: status effects and bonus that make the capture easier)=============

    // fx = codes of CaptureServices.EFFECTS, injuries = count (+5 each), bonus = free number added to the capture rate
    public record WildState(java.util.Set<String> fx, int injuries, int bonus) {}

    private final Map<Integer, java.util.Set<String>> effects = new HashMap<>();
    private final Map<Integer, Integer> injuries = new HashMap<>();
    private final Map<Integer, Integer> bonuses = new HashMap<>();

    public synchronized WildState getState(int pokemonId) {
        return new WildState(new java.util.TreeSet<>(effects.getOrDefault(pokemonId, java.util.Set.of())), injuries.getOrDefault(pokemonId, 0), bonuses.getOrDefault(pokemonId, 0));
    }

    public synchronized void toggleEffect(int pokemonId, String code) {
        java.util.Set<String> set = effects.computeIfAbsent(pokemonId, k -> new java.util.HashSet<>());
        if (!set.remove(code)) set.add(code);
        save();
    }

    public synchronized void setInjuries(int pokemonId, int n) { injuries.put(pokemonId, Math.max(0, Math.min(10, n))); save(); }

    public synchronized void setBonus(int pokemonId, int n) { bonuses.put(pokemonId, Math.max(-100, Math.min(100, n))); save(); }

    //============TRIES (how many balls may still be thrown at one Pokemon, shared by all players)=============

    private final Map<Integer, Integer> tries = new HashMap<>();  // pokemon id -> balls left (absent = unlimited)

    // n <= 0 = unlimited
    public synchronized void setTries(int pokemonId, int n) {
        if (n <= 0) tries.remove(pokemonId); else tries.put(pokemonId, Math.min(99, n));
        save();
    }

    // balls left, -1 = unlimited
    public synchronized int triesLeft(int pokemonId) {
        return tries.getOrDefault(pokemonId, -1);
    }

    // spends one ball; returns the balls left (-1 = unlimited)
    public synchronized int useTry(int pokemonId) {
        Integer left = tries.get(pokemonId);
        if (left == null) return -1;
        tries.put(pokemonId, Math.max(0, left - 1));
        save();
        return Math.max(0, left - 1);
    }

    // "fx":["burn"],"inj":1,"bonus":0 (no braces); "hp" supplies the hp part, also without braces
    private String stateFields(int pokemonId, java.util.function.IntFunction<String> hp) {
        WildState w = getState(pokemonId);
        StringBuilder fx = new StringBuilder("[");
        for (String c : w.fx()) fx.append(fx.length() > 1 ? "," : "").append('"').append(c).append('"');
        return hp.apply(pokemonId) + ",\"fx\":" + fx + "],\"inj\":" + w.injuries() + ",\"bonus\":" + w.bonus() + ",\"tries\":" + triesLeft(pokemonId);
    }

    // {"12":{"hp":..,"maxHp":..,"fx":[..],"inj":0,"bonus":0}, ...} of everything still on the field (players poll it)
    public synchronized String statesJson(String code, java.util.function.IntFunction<String> hp) {
        StringBuilder json = new StringBuilder("{");
        for (Entry e : byCode.getOrDefault(code, new ArrayList<>())) {
            if (isCaught(code, e.pokemonId())) continue;
            if (json.length() > 1) json.append(",");
            json.append('"').append(e.pokemonId()).append("\":{").append(stateFields(e.pokemonId(), hp)).append("}");
        }
        return json.append("}").toString();
    }

    // What is on the field now (spawned and not caught yet), for the Master: catch mode + wild state
    public synchronized String activeJson(String code, java.util.function.IntFunction<String> hp) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Entry e : byCode.getOrDefault(code, new ArrayList<>())) {
            if (isCaught(code, e.pokemonId())) continue;
            if (!first) json.append(",");
            first = false;
            json.append(e.json(), 0, e.json().length() - 1)
                .append(",\"target\":").append(e.target()).append(",\"mode\":\"").append(getMode(e.pokemonId())).append("\",")
                .append(stateFields(e.pokemonId(), hp)).append("}");
        }
        return json.append("]").toString();
    }

    private boolean isCaught(String code, int pokemonId) {
        return caught.getOrDefault(code, java.util.Set.of()).contains(pokemonId);
    }

    // [3,7]: players drop these from the encounters they already have on screen
    public synchronized String caughtJson(String code) {
        return caught.getOrDefault(code, java.util.Set.of()).toString();
    }

    public synchronized void clear(String code) {
        byCode.remove(code);
        caught.remove(code);
        save();
    }

}
