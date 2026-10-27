package Server.Services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// The pieces standing on the maps: a player's character, a Pokemon, an NPC or a wild Pokemon. One list per map, saved in tokens.dat.
// ponytail: names and sprite keys come from the browser (they are only decoration); who owns what is checked by the caller.
public class MapTokens {

    public static class Token implements Serializable {
        int id, owner, ref, x, y;
        String type, name, color, sprite;
        boolean shiny;
    }

    private static final java.io.File FILE = new java.io.File("tokens.dat");
    private final Map<Integer, List<Token>> byMap = new HashMap<>();
    private int nextId = 1;

    public MapTokens() { load(); }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!FILE.exists()) return;
        try (var in = new java.io.ObjectInputStream(new java.io.FileInputStream(FILE))) {
            byMap.putAll((Map<Integer, List<Token>>) in.readObject()); nextId = in.readInt();
        } catch (Exception e) { System.err.println("tokens.dat ignored: " + e); }
    }

    private void save() {
        try (var out = new java.io.ObjectOutputStream(new java.io.FileOutputStream(FILE))) {
            out.writeObject(byMap); out.writeInt(nextId);
        } catch (java.io.IOException e) { System.err.println("could not save tokens.dat: " + e); }
    }

    private static String clean(String s, int max) {
        if (s == null) return "";
        s = s.replace("\\", "").replace("\"", "").replace("<", "").replace(">", "");
        return s.length() > max ? s.substring(0, max) : s;
    }

    // a character or a Pokemon only stands once on a map: adding it again just moves it
    public synchronized int add(int mapId, String type, int owner, int ref, String name, String color, String sprite, boolean shiny, int x, int y) {
        List<Token> list = byMap.computeIfAbsent(mapId, k -> new ArrayList<>());
        if (ref > 0) for (Token t : list) if (t.type.equals(type) && t.ref == ref) { t.x = x; t.y = y; save(); return t.id; }
        Token t = new Token();
        t.id = nextId++; t.type = clean(type, 10); t.owner = owner; t.ref = ref; t.name = clean(name, 24);
        t.color = color != null && color.matches("#[0-9a-fA-F]{6}") ? color : "#d32f2f"; t.sprite = clean(sprite, 60); t.shiny = shiny; t.x = x; t.y = y;
        list.add(t); save();
        return t.id;
    }

    public synchronized Token find(int mapId, int id) {
        for (Token t : byMap.getOrDefault(mapId, List.of())) if (t.id == id) return t;
        return null;
    }

    public synchronized int ownerOf(int mapId, int id) { Token t = find(mapId, id); return t == null ? -1 : t.owner; }

    public synchronized boolean move(int mapId, int id, int x, int y) {
        Token t = find(mapId, id); if (t == null) return false;
        t.x = Math.max(-5000, Math.min(5000, x)); t.y = Math.max(-5000, Math.min(5000, y));
        save(); return true;
    }

    public synchronized boolean remove(int mapId, int id) {
        List<Token> list = byMap.get(mapId);
        boolean gone = list != null && list.removeIf(t -> t.id == id);
        if (gone) save();
        return gone;
    }

    public synchronized void dropMap(int mapId) { if (byMap.remove(mapId) != null) save(); }

    public synchronized String json(int mapId) {
        StringBuilder j = new StringBuilder("[");
        for (Token t : byMap.getOrDefault(mapId, List.of())) {
            if (j.length() > 1) j.append(",");
            j.append("{\"id\":").append(t.id).append(",\"t\":\"").append(t.type).append("\",\"n\":\"").append(t.name).append("\",\"c\":\"").append(t.color)
             .append("\",\"s\":\"").append(t.sprite).append("\",\"sh\":").append(t.shiny).append(",\"o\":").append(t.owner).append(",\"r\":").append(t.ref)
             .append(",\"x\":").append(t.x).append(",\"y\":").append(t.y).append("}");
        }
        return j.append("]").toString();
    }
}
