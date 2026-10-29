package Server.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Server.Item.Item;
import Server.Pokemon.Pokemon;
import Server.Repository.Repository;
import Server.Trainer.TrainerInventory;

// Balls (items), the trainer's inventory and the capture roll (PTU).
public class CaptureServices {

    public static final String BALL = "Ball";

    private Repository repository;
    private PokemonServices pokemonServices;

    private final Random random = new Random();

    public CaptureServices(Repository repository, PokemonServices pokemonServices) {
        setRepository(repository);
        setPokemonServices(pokemonServices);
        seedBalls();
        seedCatalog();
    }

    //============GETTERS=============

    public Repository getRepository() {
        return repository;
    }

    public PokemonServices getPokemonServices() {
        return pokemonServices;
    }

    //============SETTERS=============

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void setPokemonServices(PokemonServices pokemonServices) {
        this.pokemonServices = pokemonServices;
    }

    //============BALLS=============
    // item.effect = "capture:<modifier>;skin:<name>"; the modifier is added to the d100 roll
    // (negative = easier). The skin picks the look of the throw animation in the web page.

    private void seedBalls() {
        if (!balls().isEmpty()) return;
        String[][] data = {
            { "Poke Ball", "A basic ball.", "0", "poke" },
            { "Great Ball", "Better than a Poke Ball.", "-10", "great" },
            { "Ultra Ball", "A high-performance ball.", "-15", "ultra" },
            { "Master Ball", "Never fails.", "-100", "master" },
        };
        // ponytail: only the 4 basic balls; add rows here (Premier, Timer...) when the table needs them
        for (String[] d : data) {
            repository.createItem(new Item(nextItemId(), d[0], d[1], BALL, "capture:" + d[2] + ";skin:" + d[3]));
        }
    }

    //============ITEM CATALOG=============
    // Everything else the Master can hand out. item.effect = "icon:<emoji>" (drawn on the table and in the chest).
    // Written from the PTU 1.05 item lists: edit / add rows here, they are created at start-up when the name is missing.

    public static final String CURRENCY = "Currency";

    private void seedCatalog() {
        String[][] data = {
            // category, name, emoji, description
            { CURRENCY, "Pokedollar", "\uD83E\uDE99", "The money of the game." },
            { "Healing", "Potion", "\uD83E\uDDEA", "Restores 20 HP." },
            { "Healing", "Super Potion", "\uD83E\uDDEA", "Restores 35 HP." },
            { "Healing", "Hyper Potion", "\uD83E\uDDEA", "Restores 70 HP." },
            { "Healing", "Max Potion", "\uD83E\uDDEA", "Restores all HP." },
            { "Healing", "Full Restore", "\uD83D\uDC9C", "Restores all HP and cures every status." },
            { "Healing", "Revive", "\uD83D\uDC8E", "Revives a fainted Pokemon with half its HP." },
            { "Healing", "Max Revive", "\uD83D\uDC8E", "Revives a fainted Pokemon with all its HP." },
            { "Healing", "Antidote", "\uD83D\uDC8A", "Cures Poison." },
            { "Healing", "Paralyze Heal", "\uD83D\uDC8A", "Cures Paralysis." },
            { "Healing", "Awakening", "\uD83D\uDC8A", "Cures Sleep." },
            { "Healing", "Burn Heal", "\uD83D\uDC8A", "Cures Burn." },
            { "Healing", "Ice Heal", "\uD83D\uDC8A", "Cures Freeze." },
            { "Healing", "Full Heal", "\uD83D\uDC8A", "Cures every status." },
            { "Healing", "Energy Powder", "\uD83C\uDF3F", "Restores HP; the Pokemon hates the taste." },
            { "Healing", "Heal Powder", "\uD83C\uDF3F", "Cures every status; the Pokemon hates the taste." },
            { "Vitamin", "HP Up", "\uD83D\uDCAA", "Raises the HP stat by 1 point." },
            { "Vitamin", "Protein", "\uD83D\uDCAA", "Raises the Attack stat by 1 point." },
            { "Vitamin", "Iron", "\uD83D\uDCAA", "Raises the Defense stat by 1 point." },
            { "Vitamin", "Calcium", "\uD83D\uDCAA", "Raises the Special Attack stat by 1 point." },
            { "Vitamin", "Zinc", "\uD83D\uDCAA", "Raises the Special Defense stat by 1 point." },
            { "Vitamin", "Carbos", "\uD83D\uDCAA", "Raises the Speed stat by 1 point." },
            { "Vitamin", "Rare Candy", "\uD83C\uDF6C", "Raises the level of a Pokemon by 1." },
            { "Battle", "X Attack", "\u2694\uFE0F", "Raises Attack for a scene." },
            { "Battle", "X Defend", "\uD83D\uDEE1\uFE0F", "Raises Defense for a scene." },
            { "Battle", "X Sp. Atk", "\u2728", "Raises Special Attack for a scene." },
            { "Battle", "X Sp. Def", "\uD83D\uDD2E", "Raises Special Defense for a scene." },
            { "Battle", "X Speed", "\uD83D\uDCA8", "Raises Speed for a scene." },
            { "Battle", "X Accuracy", "\uD83C\uDFAF", "Raises Accuracy for a scene." },
            { "Battle", "Dire Hit", "\uD83D\uDCA5", "Raises the critical range for a scene." },
            { "Battle", "Guard Spec.", "\uD83D\uDEE1\uFE0F", "Protects the team from stat drops for a scene." },
            { "Stone", "Fire Stone", "\uD83D\uDD25", "Evolves certain Pokemon." },
            { "Stone", "Water Stone", "\uD83D\uDCA7", "Evolves certain Pokemon." },
            { "Stone", "Thunder Stone", "\u26A1", "Evolves certain Pokemon." },
            { "Stone", "Leaf Stone", "\uD83C\uDF43", "Evolves certain Pokemon." },
            { "Stone", "Moon Stone", "\uD83C\uDF19", "Evolves certain Pokemon." },
            { "Stone", "Sun Stone", "\u2600\uFE0F", "Evolves certain Pokemon." },
            { "Stone", "Shiny Stone", "\uD83D\uDCAB", "Evolves certain Pokemon." },
            { "Stone", "Dusk Stone", "\uD83C\uDF11", "Evolves certain Pokemon." },
            { "Stone", "Dawn Stone", "\uD83C\uDF05", "Evolves certain Pokemon." },
            { "Stone", "Ice Stone", "\u2744\uFE0F", "Evolves certain Pokemon." },
            { "Stone", "Everstone", "\u26AA", "Stops a Pokemon from evolving." },
            { "Held", "Leftovers", "\uD83C\uDF59", "Restores a little HP every turn." },
            { "Held", "Focus Band", "\uD83C\uDF80", "Sometimes lets the holder survive a hit." },
            { "Held", "Focus Sash", "\uD83C\uDF97\uFE0F", "Lets a full-HP holder survive one hit." },
            { "Held", "Choice Band", "\uD83D\uDCAA", "Boosts Attack but locks the move." },
            { "Held", "Choice Specs", "\uD83D\uDC53", "Boosts Special Attack but locks the move." },
            { "Held", "Choice Scarf", "\uD83E\uDDE3", "Boosts Speed but locks the move." },
            { "Held", "Life Orb", "\uD83D\uDD34", "Boosts damage at a cost of HP." },
            { "Held", "Expert Belt", "\uD83E\uDD4B", "Boosts super effective moves." },
            { "Held", "Muscle Band", "\uD83D\uDCAA", "Boosts physical moves." },
            { "Held", "Wise Glasses", "\uD83E\uDD13", "Boosts special moves." },
            { "Held", "Scope Lens", "\uD83D\uDD0D", "Raises the critical range." },
            { "Held", "Quick Claw", "\uD83E\uDD85", "Sometimes lets the holder act first." },
            { "Held", "King's Rock", "\uD83D\uDC51", "May make the target flinch." },
            { "Held", "Shell Bell", "\uD83D\uDC1A", "Restores HP from damage dealt." },
            { "Held", "Light Ball", "\uD83D\uDCA1", "Boosts Pikachu." },
            { "Held", "Lucky Egg", "\uD83E\uDD5A", "Boosts experience gained." },
            { "Held", "Soothe Bell", "\uD83D\uDD14", "Makes a Pokemon friendlier." },
            { "Held", "Amulet Coin", "\uD83E\uDE99", "Doubles prize money." },
            { "Held", "Exp. Share", "\uD83D\uDCD8", "Shares experience with the holder." },
            { "Held", "Eviolite", "\uD83D\uDC8E", "Boosts the defenses of a Pokemon that can still evolve." },
            { "Held", "Assault Vest", "\uD83E\uDDBA", "Boosts Special Defense but blocks status moves." },
            { "Held", "Rocky Helmet", "\u26D1\uFE0F", "Hurts attackers that touch the holder." },
            { "Held", "Black Sludge", "\u2623\uFE0F", "Heals Poison types, hurts the others." },
            { "Held", "Toxic Orb", "\uD83D\uDFE3", "Poisons the holder." },
            { "Held", "Flame Orb", "\uD83D\uDFE0", "Burns the holder." },
            { "Berry", "Oran Berry", "\uD83E\uDED0", "Restores a little HP." },
            { "Berry", "Sitrus Berry", "\uD83C\uDF4B", "Restores HP when low." },
            { "Berry", "Cheri Berry", "\uD83C\uDF52", "Cures Paralysis." },
            { "Berry", "Chesto Berry", "\uD83C\uDF47", "Cures Sleep." },
            { "Berry", "Pecha Berry", "\uD83C\uDF51", "Cures Poison." },
            { "Berry", "Rawst Berry", "\uD83C\uDF4F", "Cures Burn." },
            { "Berry", "Aspear Berry", "\uD83C\uDF50", "Cures Freeze." },
            { "Berry", "Persim Berry", "\uD83C\uDF4E", "Cures Confusion." },
            { "Berry", "Lum Berry", "\uD83C\uDF48", "Cures any status." },
            { "Berry", "Leppa Berry", "\uD83C\uDF53", "Restores the use of a move." },
            { "Tool", "Repel", "\uD83D\uDEAB", "Keeps weak wild Pokemon away." },
            { "Tool", "Super Repel", "\uD83D\uDEAB", "Keeps wild Pokemon away for longer." },
            { "Tool", "Max Repel", "\uD83D\uDEAB", "Keeps wild Pokemon away for the longest." },
            { "Tool", "Escape Rope", "\uD83E\uDDF5", "Leads out of a cave or dungeon." },
            { "Tool", "Poke Doll", "\uD83E\uDDF8", "Lets a trainer flee from a wild Pokemon." },
            { "Tool", "Poke Toy", "\uD83E\uDDF8", "A toy Pokemon love to play with." },
            { "Tool", "Honey", "\uD83C\uDF6F", "Attracts wild Pokemon." },
            { "Tool", "Pokemon Food", "\uD83C\uDF56", "Feeds a Pokemon for a day." },
            { "Tool", "Camping Kit", "\u26FA", "A tent and supplies." },
            { "Tool", "Flashlight", "\uD83D\uDD26", "Lights up dark places." },
        };
        java.util.Set<String> have = new java.util.HashSet<>();
        for (Item item : repository.findAllItems()) have.add(item.getName());
        for (String[] d : data) {
            if (!have.contains(d[1])) repository.createItem(new Item(nextItemId(), d[1], d[3], d[0], "icon:" + d[2]));
        }
    }

    public String itemIcon(Item item) {
        return effectValue(item, "icon");
    }

    private int nextItemId() {
        int id = 1;
        for (Item item : repository.findAllItems()) id = Math.max(id, item.getId() + 1);
        return id;
    }

    public List<Item> balls() {
        List<Item> list = new ArrayList<>();
        for (Item item : repository.findAllItems()) if (BALL.equals(item.getCategory())) list.add(item);
        return list;
    }

    private static String effectValue(Item item, String key) {
        for (String part : item.getEffect().split(";")) {
            if (part.startsWith(key + ":")) return part.substring(key.length() + 1);
        }
        return "";
    }

    public int ballModifier(Item ball) {
        try { return Integer.parseInt(effectValue(ball, "capture")); } catch (NumberFormatException e) { return 0; }
    }

    public String ballSkin(Item ball) {
        return effectValue(ball, "skin");
    }

    //============INVENTORY=============

    public int quantity(int trainerId, int itemId) {
        TrainerInventory inv = repository.findTrainerInventory(trainerId, itemId);
        return inv == null ? 0 : inv.getQuantity();
    }

    public synchronized void addItem(int trainerId, int itemId, int amount) {
        TrainerInventory inv = repository.findTrainerInventory(trainerId, itemId);
        if (inv == null) {
            repository.createTrainerInventory(new TrainerInventory(trainerId, itemId, amount));
        } else {
            inv.setQuantity(inv.getQuantity() + amount);
            repository.updateTrainerInventory(inv, trainerId, itemId);
        }
    }

    // Takes n items out of a bag. false (nothing changed) when he has fewer.
    public synchronized boolean removeItems(int trainerId, int itemId, int amount) {
        TrainerInventory inv = repository.findTrainerInventory(trainerId, itemId);
        if (amount < 1 || inv == null || inv.getQuantity() < amount) return false;
        if (inv.getQuantity() == amount) repository.deleteTrainerInventory(trainerId, itemId);
        else { inv.setQuantity(inv.getQuantity() - amount); repository.updateTrainerInventory(inv, trainerId, itemId); }
        return true;
    }

    // Uses one item from the bag on a Pokemon (PokemonServices.applyItem has the rules). "!reason" = nothing spent.
    public synchronized String useItem(int trainerId, int pokemonId, int itemId, String arg) {
        Item item = repository.findItemById(itemId);
        if (item == null || quantity(trainerId, itemId) < 1) return "!Not in the bag";
        String r = pokemonServices.applyItem(trainerId, pokemonId, item, arg);
        if (!r.startsWith("!")) removeItems(trainerId, itemId, 1);
        return r;
    }

    // Gives an item to hold (the one it held goes back to the bag); itemId 0 = takes it off. "!reason" = nothing changed.
    public synchronized String holdItem(int trainerId, int pokemonId, int itemId) {
        if (itemId != 0) {
            Item item = repository.findItemById(itemId);
            if (item == null || quantity(trainerId, itemId) < 1) return "!Not in the bag";
            if (!("Held".equals(item.getCategory()) || "Berry".equals(item.getCategory()) || "Everstone".equals(item.getName()))) return "!This item can't be held";
        }
        int old = pokemonServices.swapHeld(trainerId, pokemonId, itemId);
        if (old < 0) return "!Pokemon not found";
        if (itemId != 0) removeItems(trainerId, itemId, 1);
        if (old > 0) addItem(trainerId, old, 1);
        return "ok";
    }

    private void useOne(int trainerId, int itemId) {
        TrainerInventory inv = repository.findTrainerInventory(trainerId, itemId);
        if (inv.getQuantity() <= 1) {
            repository.deleteTrainerInventory(trainerId, itemId);  // the table forbids quantity 0
        } else {
            inv.setQuantity(inv.getQuantity() - 1);
            repository.updateTrainerInventory(inv, trainerId, itemId);
        }
    }

    // [{"id":1,"name":"Poke Ball","qty":3,"skin":"poke"}, ...] for the trainer's bag
    public String inventoryJson(int trainerId) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (TrainerInventory inv : repository.findAllTrainerInventories()) {
            if (inv.getTrainerId() != trainerId) continue;
            Item item = repository.findItemById(inv.getItemId());
            if (item == null) continue;
            if (!first) json.append(",");
            first = false;
            json.append("{\"id\":").append(item.getId())
                .append(",\"name\":\"").append(item.getName().replace("\"", ""))
                .append("\",\"qty\":").append(inv.getQuantity())
                .append(",\"skin\":\"").append(ballSkin(item)).append("\",\"icon\":\"").append(itemIcon(item)).append("\",\"cat\":\"").append(item.getCategory()).append("\",\"info\":\"").append(item.getDescription().replace("\\", "").replace("\"", "")).append("\"}");
        }
        return json.append("]").toString();
    }

    //============CAPTURE=============

    // Status effects the Master can put on a wild Pokemon, and what each adds to the capture rate (PTU:
    // persistent +10, volatile +5, stuck +10, slow +5, injury +5 each). Edit the numbers here.
    public static final java.util.Map<String, Integer> EFFECTS = new java.util.LinkedHashMap<>();
    static {
        EFFECTS.put("burn", 10); EFFECTS.put("freeze", 10); EFFECTS.put("paralysis", 10); EFFECTS.put("poison", 10);
        EFFECTS.put("sleep", 5); EFFECTS.put("confusion", 5); EFFECTS.put("stuck", 10); EFFECTS.put("slow", 5);
    }
    public static final int INJURY = 5;

    // One line of the calculation shown to the players: k = kind (the page translates it), a = extra text, v = value
    public record Mod(String k, String a, int v) {}

    // roll = the d100, pre = what built the capture rate, post = what changed the roll (the ball),
    // forced = why the result was not decided by the numbers ("always", "never", "master", "nat100", or "")
    public record Result(boolean caught, int shakes, int roll, int rate, int total, String forced, List<Mod> pre, List<Mod> post) {
        public String toJson(String skin) {
            return "{\"caught\":" + caught + ",\"shakes\":" + shakes + ",\"skin\":\"" + skin + "\",\"roll\":" + roll + ",\"rate\":" + rate
                + ",\"total\":" + total + ",\"forced\":\"" + forced + "\",\"pre\":" + modsJson(pre) + ",\"post\":" + modsJson(post) + "}";
        }
        private static String modsJson(List<Mod> mods) {
            StringBuilder json = new StringBuilder("[");
            for (Mod m : mods) json.append(json.length() > 1 ? "," : "").append("{\"k\":\"").append(m.k()).append("\",\"a\":\"").append(m.a().replace("\"", "")).append("\",\"v\":").append(m.v()).append("}");
            return json.append("]").toString();
        }
    }

    // PTU HP modifier: above 75% -30, up to 75% -15, up to 50% 0, up to 25% +15, 1 HP or less +30
    static int hpModifier(float hp, int max) {
        if (hp <= 1) return 30;
        double f = hp / max;
        return f > .75 ? -30 : f > .5 ? -15 : f > .25 ? 0 : 15;
    }

    // The capture rate, line by line: 100, level x2, HP, evolution stages left, shiny, effects, injuries, Master bonus.
    // ponytail: legendary (-30) is not modelled; the Master bonus covers it
    public List<Mod> rateMods(Pokemon wild, SpawnFeed.WildState state) {
        List<Mod> mods = new ArrayList<>();
        mods.add(new Mod("base", "", 100));
        mods.add(new Mod("level", String.valueOf(wild.getLevel()), -wild.getLevel() * 2));
        int max = pokemonServices.maxHpOf(wild);
        mods.add(new Mod("hp", Math.round(100f * wild.getHp() / max) + "%", hpModifier(wild.getHp(), max)));
        int left = pokemonServices.evolutionsLeft(wild.getSpeciesId());
        mods.add(new Mod("evo", String.valueOf(left), left >= 2 ? 10 : left == 1 ? 0 : -10));
        if (wild.isShiny()) mods.add(new Mod("shiny", "", -10));
        for (String code : state.fx()) mods.add(new Mod("fx", code, EFFECTS.getOrDefault(code, 0)));
        if (state.injuries() > 0) mods.add(new Mod("injury", String.valueOf(state.injuries()), state.injuries() * INJURY));
        if (state.bonus() != 0) mods.add(new Mod("bonus", "", state.bonus()));
        mods.removeIf(m -> m.v() == 0);
        return mods;
    }

    public int captureRate(Pokemon wild, SpawnFeed.WildState state) {
        int rate = 0;
        for (Mod m : rateMods(wild, state)) rate += m.v();
        return rate;
    }

    // Spends one ball and rolls. The caller already checked: trainer is the player's, the wild
    // Pokemon is still free, there is room, and the trainer owns this ball (quantity > 0).
    public synchronized Result throwBall(int trainerId, Pokemon wild, Item ball, String mode, SpawnFeed.WildState state) {
        useOne(trainerId, ball.getId());

        List<Mod> pre = rateMods(wild, state);
        int rate = 0;
        for (Mod m : pre) rate += m.v();
        int modifier = ballModifier(ball);
        List<Mod> post = new ArrayList<>();
        if (modifier != 0) post.add(new Mod("ball", ball.getName(), modifier));
        int roll = 1 + random.nextInt(100);

        // The Master can override the result ("always" = 100%, "never" = 0%). Instead of showing a roll that disagrees
        // with the result, the roll itself is chosen to agree with it; "forced" is only set when no honest roll could.
        // success = roll + modifier <= rate, i.e. roll <= rate - modifier; a natural 100 always catches
        int limit = rate - modifier;
        String forced = "";
        if ("always".equals(mode)) {
            if (limit >= 1) roll = 1 + random.nextInt(Math.min(99, limit)); else forced = "always";
        } else if ("never".equals(mode)) {
            int min = Math.max(1, limit + 1);
            if (min <= 99) roll = min + random.nextInt(100 - min); else forced = "never";
        } else {
            forced = modifier <= -100 ? "master" : roll == 100 ? "nat100" : "";
        }
        int total = roll + modifier;
        boolean caught = forced.isEmpty() ? total <= rate : !forced.equals("never");
        if (caught) pokemonServices.claimWild(wild, trainerId);

        // wobbles before the result: a catch always shakes 3 times; a miss can break out on the 1st, 2nd or 3rd
        // (the closer the roll was, the later it tends to break out)
        int gap = total - rate;
        int shakes = 3;
        if (!caught) {
            int[] w = gap <= 10 ? new int[] {10, 30} : gap <= 30 ? new int[] {30, 40} : new int[] {60, 30};  // chance of breaking on shake 1 / 2; the rest = 3
            int pick = random.nextInt(100);
            shakes = pick < w[0] ? 1 : pick < w[0] + w[1] ? 2 : 3;
        }
        return new Result(caught, shakes, roll, rate, total, forced, pre, post);
    }

}
