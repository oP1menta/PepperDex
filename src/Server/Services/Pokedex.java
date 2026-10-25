package Server.Services;

import java.util.HashMap;
import java.util.Map;

// Two albums: the normal one and the shiny one. Internally a shiny entry is the form id + SHINY.
// What each player has SEEN (a wild one showed up for him) and CAUGHT (he owns or owned one), per form id.
// One file next to pokemon.db, rewritten after each change.
// ponytail: if pokemon.db is deleted, delete pokedex.dat too (player ids would be reused)
public class Pokedex {

    public static final int SEEN = 1, CAUGHT = 2;
    public static final int SHINY = 1_000_000;

    public static int key(int formId, boolean shiny) { return shiny ? formId + SHINY : formId; }

    private static final java.io.File FILE = new java.io.File("pokedex.dat");

    private final Map<Integer, Map<Integer, Integer>> byPlayer = new HashMap<>();  // playerId -> formId -> SEEN | CAUGHT

    // The first capture of each form gives a sticker (level and shine of THAT Pokemon): {level, shiny 0/1, state}
    // state: PACK (0, waiting in the unopened pack) -> LOOSE (2, pack opened, in his hand) -> STUCK (1, on the album)
    public static final int PACK = 0, STUCK = 1, LOOSE = 2;
    private final Map<Integer, Map<Integer, int[]>> stickers = new HashMap<>();  // playerId -> formId -> sticker

    private final java.util.Set<Integer> wiped = new java.util.HashSet<>();

    public Pokedex() { load(); }

    //============PUBLIC API=============

    // Never downgrades: a caught Pokemon stays caught
    public synchronized void mark(int playerId, int formId, boolean shiny, int level) {
        formId = key(formId, shiny);
        Map<Integer, Integer> dex = byPlayer.computeIfAbsent(playerId, k -> new HashMap<>());
        if (dex.getOrDefault(formId, 0) >= level) return;
        dex.put(formId, level);
        save();
    }

    // A Pokemon of this form is now his. Later ones of the same form give no new sticker (an Alolan form is another form).
    // returns true when this capture gave a new sticker
    public synchronized boolean caught(int playerId, int formId, int level, boolean shiny) {
        mark(playerId, formId, shiny, CAUGHT);
        formId = key(formId, shiny);
        boolean isNew = stickers.computeIfAbsent(playerId, k -> new HashMap<>()).putIfAbsent(formId, new int[] {level, shiny ? 1 : 0, PACK}) == null;
        if (isNew) save();
        return isNew;
    }

    // Sticks it on the album. false = he has no loose sticker of that form
    public synchronized boolean paste(int playerId, int formId, boolean shiny) {
        formId = key(formId, shiny);
        int[] sticker = stickers.getOrDefault(playerId, Map.of()).get(formId);
        if (sticker == null || sticker[2] != LOOSE) return false;
        sticker[2] = STUCK;
        save();
        return true;
    }

    // Opens the pack: every sticker waiting in it goes to his hand. Returns how many.
    public synchronized int openPack(int playerId) {
        int n = 0;
        for (int[] sticker : stickers.getOrDefault(playerId, Map.of()).values()) {
            if (sticker[2] == PACK) { sticker[2] = LOOSE; n++; }
        }
        if (n > 0) save();
        return n;
    }

    public synchronized Map<Integer, int[]> stickersOf(int playerId) {
        Map<Integer, int[]> copy = new HashMap<>();
        stickers.getOrDefault(playerId, Map.of()).forEach((form, st) -> copy.put(form, st.clone()));
        return copy;
    }

    // Master: wipes his album (seen, caught, stickers). What he owns right now stops counting automatically.
    public synchronized void reset(int playerId) {
        byPlayer.remove(playerId);
        stickers.remove(playerId);
        wiped.add(playerId);
        save();
    }

    // true after a reset: the "whatever he owns counts as caught" backfill must not bring the album back
    public synchronized boolean wiped(int playerId) { return wiped.contains(playerId); }

    public synchronized int caughtCount(int playerId) {
        int n = 0;
        for (int state : byPlayer.getOrDefault(playerId, Map.of()).values()) if (state == CAUGHT) n++;
        return n;
    }

    public synchronized Map<Integer, Integer> of(int playerId) {
        return new HashMap<>(byPlayer.getOrDefault(playerId, Map.of()));
    }

    //============PERSISTENCE=============

    @SuppressWarnings("unchecked")
    private void load() {
        if (!FILE.exists()) return;
        try (var in = new java.io.ObjectInputStream(new java.io.FileInputStream(FILE))) {
            byPlayer.putAll((Map<Integer, Map<Integer, Integer>>) in.readObject());
            try { stickers.putAll((Map<Integer, Map<Integer, int[]>>) in.readObject()); } catch (java.io.EOFException old) { /* file from before stickers */ }
            try { wiped.addAll((java.util.Set<Integer>) in.readObject()); } catch (java.io.EOFException old) { /* file from before resets */ }
        } catch (Exception e) { System.err.println("pokedex.dat ignored: " + e); }
    }

    private void save() {
        try (var out = new java.io.ObjectOutputStream(new java.io.FileOutputStream(FILE))) {
            out.writeObject(byPlayer); out.writeObject(stickers); out.writeObject(wiped);
        } catch (java.io.IOException e) { System.err.println("could not save pokedex.dat: " + e); }
    }

}
