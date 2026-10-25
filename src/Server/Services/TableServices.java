package Server.Services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// The shared round table: items lie on it with a little physics (toss, bounce, slide, collide) and every
// player moves a hand to grab them. The server simulates; the pages poll the snapshot and draw it.
// Everything lives in memory (a restart empties the table).
public class TableServices {

    //============CONSTANTS=============

    public static final double R = 1.0;             // table radius (world units, centre = 0,0)
    public static final double PROP_R = 0.075;      // item radius
    public static final double REACH = 0.3;         // how close a hand has to be to grab an item
    public static final double MAX_REACH = 1.75;    // how far from the centre a hand can go (its own chest lies out there)
    private static final double MAGNET = 0.32, MAGNET_PULL = 1.6;  // reach of the magnet and its strength
    private static final double DT = 1.0 / 30, GRAVITY = 9;
    private static final long HAND_TTL = 4000;      // a hand that stopped talking disappears
    // Six seats around the table, 60 degrees apart (seat 0 = the Master's side). Players take them in this order: opposite first.
    private static final int[] SEAT_ORDER = {3, 1, 5, 2, 4, 0};
    public static final int MASTER = 0;             // hand id of the Master

    //============DATA=============

    // physics fields are plain public doubles on purpose: they change 30 times a second
    public static class Prop {
        private int id, itemId, qty;
        private String name, skin, icon = "";
        public double x, y, z, vx, vy, vz;
        public int heldBy = -1;

        public Prop(int id, int itemId, String name, String skin, int qty) {
            setId(id); setItemId(itemId); setName(name); setSkin(skin); setQty(qty);
        }

        public int getId() { return id; }
        public int getItemId() { return itemId; }
        public int getQty() { return qty; }
        public String getName() { return name; }
        public String getSkin() { return skin; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public void setId(int id) { this.id = id; }
        public void setItemId(int itemId) { this.itemId = itemId; }
        public void setQty(int qty) { this.qty = qty; }
        public void setName(String name) { this.name = name; }
        public void setSkin(String skin) { this.skin = skin; }
    }

    private static class Hand {
        String name, color = "";
        double x, y, vx, vy;
        boolean down;
        long seen;
        Prop held;
    }

    private static class Table {
        final List<Prop> props = new ArrayList<>();
        final Map<Integer, Hand> hands = new HashMap<>();
        int nextId = 1;
        final Map<Integer, Integer> seats = new HashMap<>();   // hand id -> seat (0..5, fixed for the session)
    }

    private final Map<String, Table> tables = new HashMap<>();

    public TableServices() {
        var timer = Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r, "table-physics"); t.setDaemon(true); return t; });
        timer.scheduleAtFixedRate(this::tick, 100, 33, TimeUnit.MILLISECONDS);
    }

    private Table of(String code) { return tables.computeIfAbsent(code, k -> new Table()); }

    //============PUBLIC API=============

    // An item stack appears above the table and is tossed a little
    // lands near the centre; every seat reaches it
    public synchronized void toss(String code, int itemId, String name, String skin, String icon, int qty, double fromX, double fromY) {
        Table t = of(code);
        Prop p = new Prop(t.nextId++, itemId, name, skin, qty);
        p.setIcon(icon);
        double angle = Math.random() * Math.PI * 2;
        p.x = fromX; p.y = fromY; p.z = 0.45;  // lands in the front half, close to the players' hands
        p.vx = Math.cos(angle) * (0.3 + Math.random() * 0.5); p.vy = Math.sin(angle) * (0.3 + Math.random() * 0.5); p.vz = 2.2;
        t.props.add(p);
    }

    // A stack that comes out of a chest (or the Master's list) straight into the hand
    public synchronized void holdNew(String code, int handId, int itemId, String name, String skin, String icon, int qty) {
        Table t = of(code);
        Hand h = t.hands.get(handId);
        if (h == null) return;
        if (h.held != null) { h.held.heldBy = -1; h.held.vz = 1; }   // whatever it carried is let go
        Prop p = new Prop(t.nextId++, itemId, name, skin, qty);
        p.setIcon(icon);
        p.x = h.x; p.y = h.y; p.z = 0.12; p.heldBy = handId;
        t.props.add(p);
        h.held = p; h.down = true;
    }

    public synchronized void clear(String code) {
        Table t = tables.get(code);
        if (t == null) return;
        t.props.clear();
        t.hands.values().forEach(h -> h.held = null);
    }

    // The seat of a hand / viewer (fixed for the session). The Master always sits at seat 0 and takes none from the players.
    public synchronized int seatOf(String code, int handId) {
        Table t = of(code);
        if (handId == MASTER) return 0;
        return t.seats.computeIfAbsent(handId, k -> SEAT_ORDER[(int) t.seats.values().stream().count() % SEAT_ORDER.length]);
    }

    // A hand reports where it is. down = finger / button pressed. take = how many to take from a stack.
    // toChest = it was released over the chest. Returns the stack to deposit in the chest, or null.
    public synchronized Prop hand(String code, int handId, String name, String color, double x, double y, boolean down, int take, boolean toChest) {
        Table t = of(code);
        seatOf(code, handId);
        Hand h = t.hands.computeIfAbsent(handId, k -> new Hand());
        h.name = name;
        if (color != null && color.matches("#[0-9a-fA-F]{6}")) h.color = color;   // his frame colour: the sleeve of his arm
        h.seen = System.currentTimeMillis();
        double len = Math.hypot(x, y), k = len > MAX_REACH ? MAX_REACH / len : 1;
        h.x = x * k; h.y = y * k;
        if (down && !h.down && h.held == null) grab(t, handId, h, take);
        Prop deposited = null;
        if (!down && h.held != null) {
            Prop p = h.held;
            h.held = null;
            p.heldBy = -1;
            if (toChest) { t.props.remove(p); deposited = p; }
            else { p.vx = clampSpeed(h.vx); p.vy = clampSpeed(h.vy); p.vz = 1.5; }
        }
        h.down = down;
        return deposited;
    }

    // JSON snapshot for the pages: {"t":ms,"items":[...],"hands":[...]}
    public synchronized String snapshot(String code) {
        Table t = of(code);
        StringBuilder json = new StringBuilder("{\"t\":").append(System.currentTimeMillis()).append(",\"items\":[");
        boolean first = true;
        for (Prop p : t.props) {
            json.append(first ? "" : ",").append("{\"id\":").append(p.id).append(",\"i\":").append(p.itemId)
                .append(",\"n\":\"").append(p.name.replace("\"", "")).append("\",\"s\":\"").append(p.skin).append("\",\"e\":\"").append(p.icon)
                .append("\",\"q\":").append(p.qty).append(",\"x\":").append(r3(p.x)).append(",\"y\":").append(r3(p.y))
                .append(",\"z\":").append(r3(p.z)).append(",\"h\":").append(p.heldBy).append("}");
            first = false;
        }
        json.append("],\"hands\":[");
        first = true;
        for (Map.Entry<Integer, Hand> e : t.hands.entrySet()) {
            Hand h = e.getValue();
            json.append(first ? "" : ",").append("{\"p\":").append(e.getKey()).append(",\"n\":\"").append(h.name.replace("\"", ""))
                .append("\",\"c\":\"").append(h.color).append("\",\"s\":").append(e.getKey() == MASTER ? 0 : t.seats.get(e.getKey())).append(",\"x\":").append(r3(h.x)).append(",\"y\":").append(r3(h.y)).append(",\"d\":").append(h.down).append("}");
            first = false;
        }
        return json.append("]}").toString();
    }

    //============PHYSICS=============

    private void grab(Table t, int handId, Hand h, int take) {
        Prop best = null;
        double bestD = REACH;
        for (Prop p : t.props) {
            if (p.heldBy >= 0) continue;
            double d = Math.hypot(p.x - h.x, p.y - h.y);
            if (d < bestD) { best = p; bestD = d; }
        }
        if (best == null) return;
        int n = take == -1 ? (best.qty + 1) / 2 : Math.max(1, Math.min(take, best.qty));   // -1 = half (rounded up)
        Prop got = best;
        if (n < best.qty) {  // split the stack: a new one with n in the hand
            best.setQty(best.qty - n);
            got = new Prop(t.nextId++, best.itemId, best.name, best.skin, n);
            got.setIcon(best.icon);
            got.x = best.x; got.y = best.y;
            t.props.add(got);
        }
        got.heldBy = handId; got.vx = got.vy = got.vz = 0;
        h.held = got;
    }

    private static double clampSpeed(double v) { return Math.max(-3, Math.min(3, v)); }
    private static double r3(double v) { return Math.round(v * 1000) / 1000.0; }

    private synchronized void tick() {
        long now = System.currentTimeMillis();
        for (Table t : tables.values()) {
            t.hands.values().removeIf(h -> {
                boolean stale = now - h.seen > HAND_TTL;
                if (stale && h.held != null) { h.held.heldBy = -1; h.held.vz = 1; }
                return stale;
            });
            for (Hand h : t.hands.values()) {  // the held item follows the hand; the hand's speed is what a throw keeps
                if (h.held == null) continue;
                double nx = h.held.x + (h.x - h.held.x) * 0.6, ny = h.held.y + (h.y - h.held.y) * 0.6;
                h.vx = h.vx * 0.5 + (nx - h.held.x) / DT * 0.5; h.vy = h.vy * 0.5 + (ny - h.held.y) / DT * 0.5;
                h.held.x = nx; h.held.y = ny; h.held.z = 0.12;
            }
            for (Prop p : t.props) if (p.heldBy < 0) step(p);
            collide(t);
        }
    }

    private void step(Prop p) {
        if (p.z > 0 || p.vz != 0) {
            p.vz -= GRAVITY * DT;
            p.z += p.vz * DT;
            if (p.z <= 0) { p.z = 0; p.vz = p.vz < -0.6 ? -p.vz * 0.45 : 0; }
        }
        p.x += p.vx * DT; p.y += p.vy * DT;
        if (p.z == 0) { double f = Math.pow(0.12, DT); p.vx *= f; p.vy *= f; }  // sliding friction
        double r = Math.hypot(p.x, p.y), lim = R - PROP_R;
        if (r > lim) {  // the table edge: back inside, bounce
            double nx = p.x / r, ny = p.y / r, dot = p.vx * nx + p.vy * ny;
            p.x = nx * lim; p.y = ny * lim;
            if (dot > 0) { p.vx -= 1.6 * dot * nx; p.vy -= 1.6 * dot * ny; }
        }
    }

    // Items push each other apart and bounce; two stacks of the same item that touch merge.
    // A light magnet pulls the same item together when they lie close, so loose balls end up in one stack.
    private void collide(Table t) {
        List<Prop> list = t.props;
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Prop a = list.get(i), b = list.get(j);
                if (a.heldBy >= 0 || b.heldBy >= 0 || a.itemId != b.itemId || a.z > 0.05 || b.z > 0.05) continue;
                double dx = b.x - a.x, dy = b.y - a.y, d = Math.hypot(dx, dy);
                if (d > MAGNET || d < 1e-6) continue;
                double pull = MAGNET_PULL * (1 - d / MAGNET) * DT;
                a.vx += dx / d * pull; a.vy += dy / d * pull; b.vx -= dx / d * pull; b.vy -= dy / d * pull;
            }
        }
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Prop a = list.get(i), b = list.get(j);
                if (a.heldBy >= 0 || b.heldBy >= 0 || Math.abs(a.z - b.z) > 0.1) continue;
                double dx = b.x - a.x, dy = b.y - a.y, d = Math.hypot(dx, dy);
                if (d >= PROP_R * 2) continue;
                double speed = Math.hypot(a.vx - b.vx, a.vy - b.vy);
                if (a.itemId == b.itemId && (speed < 0.6 || d < PROP_R * 1.2)) { a.setQty(a.qty + b.qty); list.remove(j); j--; continue; }
                if (d < 1e-6) { dx = 1; dy = 0; d = 1; }
                double nx = dx / d, ny = dy / d, push = (PROP_R * 2 - d) / 2;
                a.x -= nx * push; a.y -= ny * push; b.x += nx * push; b.y += ny * push;
                double rel = (b.vx - a.vx) * nx + (b.vy - a.vy) * ny;
                if (rel < 0) { a.vx += rel * nx; a.vy += rel * ny; b.vx -= rel * nx; b.vy -= rel * ny; }
            }
        }
    }

}
