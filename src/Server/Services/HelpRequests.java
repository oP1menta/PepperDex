package Server.Services;

import java.util.ArrayList;
import java.util.List;

// In-memory "please adjust my Pokemon" requests players send to the Master, per session code.
// One open request per Pokemon (a new one replaces the old text). Gone when the Master opens it or the server restarts.
public class HelpRequests {

    private record Entry(int id, String code, int trainerId, int pokemonId, String who, String pokemon, String text) implements java.io.Serializable {}

    private static final java.io.File FILE = new java.io.File("helprequests.dat");  // survives a restart

    private final List<Entry> list = new ArrayList<>();
    private int nextId = 1;

    @SuppressWarnings("unchecked")
    public HelpRequests() {
        if (!FILE.exists()) return;
        try (var in = new java.io.ObjectInputStream(new java.io.FileInputStream(FILE))) {
            list.addAll((List<Entry>) in.readObject()); nextId = in.readInt();
        } catch (Exception e) { System.err.println("helprequests.dat ignored: " + e); }
    }

    private void save() {
        try (var out = new java.io.ObjectOutputStream(new java.io.FileOutputStream(FILE))) { out.writeObject(new ArrayList<>(list)); out.writeInt(nextId); }
        catch (java.io.IOException e) { System.err.println("could not save helprequests.dat: " + e); }
    }

    //============PUBLIC API=============

    public synchronized void add(String code, int trainerId, int pokemonId, String who, String pokemon, String text) {
        list.removeIf(e -> e.code().equals(code) && e.pokemonId() == pokemonId);
        list.add(new Entry(nextId++, code, trainerId, pokemonId, who, pokemon, text));
        save();
    }

    public synchronized void done(int id) {
        list.removeIf(e -> e.id() == id);
        save();
    }

    // Open requests of one session as a JSON array (strings are escaped by the caller's function)
    public synchronized String json(String code, java.util.function.UnaryOperator<String> escape) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Entry e : list) {
            if (!e.code().equals(code)) continue;
            if (!first) json.append(",");
            first = false;
            json.append("{\"id\":").append(e.id()).append(",\"trainerId\":").append(e.trainerId()).append(",\"pokemonId\":").append(e.pokemonId())
                .append(",\"who\":\"").append(escape.apply(e.who())).append("\",\"pokemon\":\"").append(escape.apply(e.pokemon()))
                .append("\",\"text\":\"").append(escape.apply(e.text())).append("\"}");
        }
        return json.append("]").toString();
    }
}
