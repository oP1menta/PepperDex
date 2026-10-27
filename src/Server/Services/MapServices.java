package Server.Services;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// The Master's maps: an endless grid of blocks, saved next to pokemon.db (maps.dat) and shown to a session on demand.
// A cell = a ground (BASE), something on top (TOP) and extras. value = base | top << 3 | extra << 8, where
// extra = ground variant (3 bits) | top variant (3 bits) << 3 | elevation level 0-4 (3 bits) << 6.
// The extras live in their own file (mapx.dat) so maps made before variants still load. Roads, houses and walls can stand on any level.
// A cell with no ground, nothing on top and level 0 is empty (not stored).
// ponytail: the whole file is rewritten after each stroke; fine for maps of a few thousand cells.
public class MapServices {

    public static final String[] BASE = { "", "grass", "water", "cave", "sand", "snow", "dirt" };
    public static final String[] TOP = { "", "road", "", "wall", "house", "tree" };   // 2 is a leftover of the old elevation

    public static class GameMap implements Serializable {
        int id; String name; int version = 1;
        public int getId() { return id; }
        final Map<Long, Byte> cells = new HashMap<>();
    }

    private static final java.io.File FILE = new java.io.File("maps.dat"), FILE_X = new java.io.File("mapx.dat");
    private Map<Integer, Map<Long, Integer>> extra = new HashMap<>();   // map id -> cell -> extras (only cells that have some)
    private final Map<Integer, GameMap> maps = new LinkedHashMap<>();
    private final Map<String, Integer> active = new HashMap<>();   // session code -> map shown to the players
    private int nextId = 1;

    public MapServices() { load(); }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!FILE.exists()) return;
        try (var in = new java.io.ObjectInputStream(new java.io.FileInputStream(FILE))) {
            maps.putAll((Map<Integer, GameMap>) in.readObject()); migrate(); active.putAll((Map<String, Integer>) in.readObject()); nextId = in.readInt();
        } catch (Exception e) { System.err.println("maps.dat ignored: " + e); }
        if (FILE_X.exists()) try (var in = new java.io.ObjectInputStream(new java.io.FileInputStream(FILE_X))) {
            extra = (Map<Integer, Map<Long, Integer>>) in.readObject();
        } catch (Exception e) { System.err.println("mapx.dat ignored: " + e); }
        migrateLevels();
    }

    // maps from before levels: the old elevation flag (bit 6 of the byte) becomes level 1
    private void migrateLevels() {
        for (GameMap m : maps.values()) for (Map.Entry<Long, Byte> e : m.cells.entrySet()) {
            int v = e.getValue() & 255;
            if ((v & 64) != 0) { e.setValue((byte) (v & 63)); Map<Long, Integer> x = extra.computeIfAbsent(m.id, k -> new HashMap<>()); x.put(e.getKey(), (x.getOrDefault(e.getKey(), 0) & ~(7 << 6)) | (Math.max(1, (x.getOrDefault(e.getKey(), 0) >> 6) & 7) << 6)); }
        }
    }

    // old maps kept the elevation in the "top" slot (2): it is a flag of its own now
    private void migrate() {
        for (GameMap m : maps.values()) for (Map.Entry<Long, Byte> e : m.cells.entrySet()) { int v = e.getValue(); if (((v >> 3) & 7) == 2) e.setValue((byte) ((v & 7) | 64)); }
    }

    private void save() {
        try (var out = new java.io.ObjectOutputStream(new java.io.FileOutputStream(FILE))) {
            out.writeObject(maps); out.writeObject(active); out.writeInt(nextId);
        } catch (java.io.IOException e) { System.err.println("could not save maps.dat: " + e); }
        try (var out = new java.io.ObjectOutputStream(new java.io.FileOutputStream(FILE_X))) { out.writeObject(extra); }
        catch (java.io.IOException e) { System.err.println("could not save mapx.dat: " + e); }
    }

    private static long key(int x, int y) { return ((long) x << 32) | (y & 0xffffffffL); }

    public synchronized GameMap create(String name) {
        GameMap m = new GameMap(); m.id = nextId++; m.name = name;
        // a small meadow to start on, so the first click already shows something
        for (int x = 0; x < 12; x++) for (int y = 0; y < 8; y++) m.cells.put(key(x, y), (byte) 1);
        maps.put(m.id, m); save();
        return m;
    }

    public synchronized boolean delete(int id) {
        if (maps.remove(id) == null) return false;
        active.values().removeIf(v -> v == id); extra.remove(id);
        save(); return true;
    }

    public synchronized boolean rename(int id, String name) {
        GameMap m = maps.get(id); if (m == null) return false;
        m.name = name; m.version++; save(); return true;
    }

    // full value of a cell (0 = empty) and back
    private int get(GameMap m, long c) { return (m.cells.getOrDefault(c, (byte) 0) & 255) | (extra.getOrDefault(m.id, Map.of()).getOrDefault(c, 0) << 8); }
    private void put(GameMap m, long c, int full) {
        int b = full & 7, t = (full >> 3) & 7, x = full >> 8, lv = (x >> 6) & 7;
        Map<Long, Integer> ex = extra.computeIfAbsent(m.id, k -> new HashMap<>());
        if (b == 0 && t == 0 && lv == 0) { m.cells.remove(c); ex.remove(c); return; }
        m.cells.put(c, (byte) (full & 63));
        if (x != 0) ex.put(c, x); else ex.remove(c);
    }

    // tool = name or name:variant. Ground: grass water cave sand snow dirt. On top: road wall house. elev:N = level N (1-4), flat = level 0,
    // bare = nothing on top, erase = the cell is gone. Every tool sets a value (never "adds one"), so dragging over a cell twice changes nothing.
    public synchronized int paint(int id, String tool, int[] xy) {
        GameMap m = maps.get(id); if (m == null) return -1;
        String name = tool.contains(":") ? tool.substring(0, tool.indexOf(':')) : tool;
        int var = 0; try { var = tool.contains(":") ? Integer.parseInt(tool.substring(tool.indexOf(':') + 1)) : 0; } catch (NumberFormatException e) { /* variant 0 */ }
        var = Math.max(0, Math.min(7, var));
        int base = indexOf(BASE, name), top = indexOf(TOP, name);
        for (int k = 0; k + 1 < xy.length; k += 2) {
            long c = key(Math.max(-5000, Math.min(5000, xy[k])), Math.max(-5000, Math.min(5000, xy[k + 1])));
            int v = get(m, c), b = v & 7, t = (v >> 3) & 7, bv = (v >> 8) & 7, tv = (v >> 11) & 7, lv = (v >> 14) & 7;
            if (name.equals("erase")) { put(m, c, 0); continue; }
            if (base > 0) { b = base; bv = var; } else if (top > 0) { t = top; tv = var; } else if (name.equals("bare")) { t = 0; tv = 0; }
            else if (name.equals("elev")) lv = Math.max(1, Math.min(4, var)); else if (name.equals("flat")) lv = 0; else continue;
            put(m, c, b | t << 3 | bv << 8 | tv << 11 | lv << 14);
        }
        m.version++; save();
        return m.version;
    }

    // undo: triples x, y, full value (value -1 = the cell was empty)
    public synchronized int restore(int id, int[] xyv) {
        GameMap m = maps.get(id); if (m == null) return -1;
        for (int k = 0; k + 2 < xyv.length; k += 3) {
            long c = key(Math.max(-5000, Math.min(5000, xyv[k])), Math.max(-5000, Math.min(5000, xyv[k + 1])));
            put(m, c, xyv[k + 2] < 0 ? 0 : xyv[k + 2] & 0x1ffff);
        }
        m.version++; save();
        return m.version;
    }

    private static int indexOf(String[] a, String s) { for (int i = 1; i < a.length; i++) if (a[i].equals(s)) return i; return 0; }

    public synchronized void show(String code, int id) {
        if (id > 0 && maps.containsKey(id)) active.put(code, id); else active.remove(code);
        save();
    }

    public synchronized int activeId(String code) { return active.getOrDefault(code, 0); }

    public synchronized String listJson(String code) {
        StringBuilder j = new StringBuilder("[");
        for (GameMap m : maps.values()) {
            if (j.length() > 1) j.append(",");
            j.append("{\"id\":").append(m.id).append(",\"name\":\"").append(m.name.replace("\\", "").replace("\"", "")).append("\",\"cells\":").append(m.cells.size())
             .append(",\"shown\":").append(activeId(code) == m.id).append("}");
        }
        return j.append("]").toString();
    }

    // since = the version the caller already has: then only {"same":true,...} comes back
    public synchronized String mapJson(int id, int since) {
        GameMap m = maps.get(id);
        if (m == null) return "{\"id\":0}";
        StringBuilder j = new StringBuilder("{\"id\":" + m.id + ",\"name\":\"" + m.name.replace("\\", "").replace("\"", "") + "\",\"v\":" + m.version);
        if (since == m.version) return j.append(",\"same\":true}").toString();
        j.append(",\"cells\":\"");
        boolean first = true;
        for (long c : m.cells.keySet()) {
            if (!first) j.append(';'); first = false;
            j.append((int) (c >> 32)).append(',').append((int) c).append(',').append(get(m, c));
        }
        return j.append("\"}").toString();
    }
}
