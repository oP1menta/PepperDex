package Server.Services;

import java.util.HashMap;
import java.util.Map;

// The PTU trainer sheet, only the numbers: level, XP, current HP and the six stats.
// One file next to pokemon.db, rewritten after each change.
// ponytail: if pokemon.db is deleted, delete profiles.dat too (trainer ids would be reused)
public class TrainerProfiles {

    // v = {level, xp, currentHp (-1 = full), hp, atk, def, spatk, spdef, spd}
    private static final int LEVEL = 0, XP = 1, CUR = 2, STATS = 3, N = 9, XP_PER_LEVEL = 10, MAX_LEVEL = 50;
    private static final java.io.File FILE = new java.io.File("profiles.dat");
    private final Map<Integer, int[]> byTrainer = new HashMap<>();

    // The character's description, a separate file (bios.dat) so profiles.dat keeps loading
    private static final java.io.File BIO_FILE = new java.io.File("bios.dat");
    public static final int BIO_MAX = 600;
    private final Map<Integer, String> bios = new HashMap<>();

    public TrainerProfiles() { load(); loadBios(); }

    @SuppressWarnings("unchecked")
    private void loadBios() {
        if (!BIO_FILE.exists()) return;
        try (var in = new java.io.ObjectInputStream(new java.io.FileInputStream(BIO_FILE))) {
            bios.putAll((Map<Integer, String>) in.readObject());
        } catch (Exception e) { System.err.println("bios.dat ignored: " + e); }
    }

    public synchronized void setBio(int id, String text) {
        String t = text == null ? "" : text.trim();
        bios.put(id, t.length() > BIO_MAX ? t.substring(0, BIO_MAX) : t);
        try (var out = new java.io.ObjectOutputStream(new java.io.FileOutputStream(BIO_FILE))) { out.writeObject(bios); }
        catch (java.io.IOException e) { System.err.println("could not save bios.dat: " + e); }
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!FILE.exists()) return;
        try (var in = new java.io.ObjectInputStream(new java.io.FileInputStream(FILE))) {
            byTrainer.putAll((Map<Integer, int[]>) in.readObject());
        } catch (Exception e) { System.err.println("profiles.dat ignored: " + e); }
    }

    private void save() {
        try (var out = new java.io.ObjectOutputStream(new java.io.FileOutputStream(FILE))) {
            out.writeObject(byTrainer);
        } catch (java.io.IOException e) { System.err.println("could not save profiles.dat: " + e); }
    }

    private int[] of(int id) {
        return byTrainer.computeIfAbsent(id, k -> new int[] { 1, 0, -1, 10, 10, 10, 10, 10, 10 });
    }

    private static int maxHp(int[] v) { return v[LEVEL] * 2 + v[STATS] * 3 + 10; }

    private static int clamp(int x, int lo, int hi) { return Math.max(lo, Math.min(hi, x)); }

    public synchronized String json(int id) {
        int[] v = of(id);
        int max = maxHp(v), cur = v[CUR] < 0 ? max : Math.min(v[CUR], max), spent = 0;
        for (int k = STATS; k < N; k++) spent += v[k] - 10;
        return "{\"level\":" + v[LEVEL] + ",\"xp\":" + v[XP] + ",\"xpNext\":" + XP_PER_LEVEL + ",\"hp\":" + cur + ",\"maxHp\":" + max +
            ",\"stats\":[" + v[3] + "," + v[4] + "," + v[5] + "," + v[6] + "," + v[7] + "," + v[8] + "]" +
            ",\"free\":" + (10 + v[LEVEL] - 1 - spent) +
            ",\"bio\":\"" + bios.getOrDefault(id, "").replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "\\n").replace("\t", " ") + "\"}";
    }

    // stats = 6 numbers or null; cur = current HP or null. A player may change these on his own sheet.
    public synchronized void set(int id, int[] stats, Integer cur) {
        int[] v = of(id);
        if (stats != null) for (int k = 0; k < 6; k++) v[STATS + k] = clamp(stats[k], 1, 40);
        int max = maxHp(v);
        if (cur != null) v[CUR] = clamp(cur, 0, max);
        else if (v[CUR] > max) v[CUR] = max;
        save();
    }

    // Master: every 10 XP is a level (the bank rolls over)
    public synchronized void addXp(int id, int amount) {
        int[] v = of(id);
        v[XP] += amount;
        while (v[XP] >= XP_PER_LEVEL && v[LEVEL] < MAX_LEVEL) { v[XP] -= XP_PER_LEVEL; v[LEVEL]++; }
        while (v[XP] < 0 && v[LEVEL] > 1) { v[XP] += XP_PER_LEVEL; v[LEVEL]--; }
        v[XP] = clamp(v[XP], 0, XP_PER_LEVEL - 1);
        save();
    }

    public synchronized void setLevel(int id, int level) {
        int[] v = of(id);
        v[LEVEL] = clamp(level, 1, MAX_LEVEL);
        v[XP] = 0;
        save();
    }
}
