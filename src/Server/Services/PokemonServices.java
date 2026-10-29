package Server.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Server.PokemonData.Ability;
import Server.PokemonData.Capability;
import Server.PokemonData.FormAbility;
import Server.PokemonData.FormCapability;
import Server.PokemonData.FormSkill;
import Server.PokemonData.Skill;
import Server.Pokemon.PokemonForm;
import Server.Pokemon.PokemonSpecies;
import Server.Repository.Repository;
import Server.Util.SimpleJsonParser;
import java.util.LinkedHashSet;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.Random;

import Server.Pokemon.Pokemon;
import Server.Evolution.Evolution;
import Server.Evolution.EvolutionCondition;
import Server.Pokemon.PokemonVitamin;
import Server.PokemonData.Nature;
import Server.Pokemon.PokemonAttributePoints;

public class PokemonServices {


    private Repository repository;


    private final Map<String, Integer> abilityIdsByName = new HashMap<>();
    private final Map<String, Integer> capabilityIdsByName = new HashMap<>();
    private final Map<String, Integer> skillIdsByName = new HashMap<>();


    private final Map<String, Integer> speciesIdsByName = new HashMap<>();

    private int nextSpeciesId = 1;
    private int nextFormId = 1;
    private int nextAbilityId = 1;
    private int nextCapabilityId = 1;
    private int nextSkillId = 1;
    private int nextPokemonId = 1;
    private int nextEvolutionId = 1;
    private int nextEvolutionConditionId = 1;

    // species id -> id of its first form (evolutions point to forms)
    private final Map<Integer, Integer> firstFormIdBySpecies = new HashMap<>();

    private final Random random = new Random();

    // PTU natures: { name, raised stat, lowered stat }
    private static final String[][] NATURES = {
        {"Cuddly","HP","Attack"}, {"Distracted","HP","Defense"}, {"Proud","HP","Special Attack"},
        {"Decisive","HP","Special Defense"}, {"Patient","HP","Speed"},
        {"Desperate","Attack","HP"}, {"Lonely","Attack","Defense"}, {"Adamant","Attack","Special Attack"},
        {"Naughty","Attack","Special Defense"}, {"Brave","Attack","Speed"},
        {"Stark","Defense","HP"}, {"Bold","Defense","Attack"}, {"Impish","Defense","Special Attack"},
        {"Lax","Defense","Special Defense"}, {"Relaxed","Defense","Speed"},
        {"Curious","Special Attack","HP"}, {"Modest","Special Attack","Attack"}, {"Mild","Special Attack","Defense"},
        {"Rash","Special Attack","Special Defense"}, {"Quiet","Special Attack","Speed"},
        {"Dreamy","Special Defense","HP"}, {"Calm","Special Defense","Attack"}, {"Gentle","Special Defense","Defense"},
        {"Careful","Special Defense","Special Attack"}, {"Sassy","Special Defense","Speed"},
        {"Skittish","Speed","HP"}, {"Timid","Speed","Attack"}, {"Hasty","Speed","Defense"},
        {"Jolly","Speed","Special Attack"}, {"Naive","Speed","Special Defense"}
    };



    public PokemonServices(Repository repository) {
        setRepository(repository);
        loadLookupCaches();
        seedNaturesIfEmpty();
        loadRules();
    }

    //============GETTERS=============

    public Repository getRepository() {
        return repository;
    }

  //============GETTERS=============
    public void setRepository(Repository repository) {
    	this.repository = repository;
    }
    
    
    
    
    //============PUBLIC API=============

    public int importPokemonJson(String jsonFilePath) throws IOException {

        String content = Files.readString(Path.of(jsonFilePath));
        List<Object> entries = SimpleJsonParser.asList(SimpleJsonParser.parse(content));

        int importedForms = 0;

        // One transaction for the whole import: ~17k inserts in a single
        // disk commit instead of one commit per insert (minutes -> seconds)
        java.sql.Connection connection = repository.getConnection();

        try {
            connection.setAutoCommit(false);

            for (Object rawEntry : entries) {
                Map<String, Object> entry = SimpleJsonParser.asMap(rawEntry);
                importOneForm(entry);
                importedForms++;
            }

            importEvolutions(entries);

            connection.commit();

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }

        return importedForms;
    }

    // Rebuilds the evolution graph from pokemon.json on every start, so a corrected file reaches an
    // existing database (nothing else points at the evolution rows: Pokemon, characters and sessions are untouched).
    public void rebuildEvolutions(String jsonFilePath) {

        List<Object> entries;
        try {
            entries = SimpleJsonParser.asList(SimpleJsonParser.parse(Files.readString(Path.of(jsonFilePath))));
        } catch (IOException e) {
            System.out.println("Evolutions not rebuilt (" + jsonFilePath + " not found): " + e.getMessage());
            return;
        }

        // lowest form id of each species = its first form (this map is only filled during an import)
        for (PokemonForm form : repository.findAllPokemonForms()) {
            firstFormIdBySpecies.merge(form.getSpeciesId(), form.getId(), Math::min);
        }

        java.sql.Connection connection = repository.getConnection();
        try {
            connection.setAutoCommit(false);

            for (EvolutionCondition condition : repository.findAllEvolutionConditions()) repository.deleteEvolutionCondition(condition.getId());
            for (Evolution evolution : repository.findAllEvolutions()) repository.deleteEvolution(evolution.getId());
            nextEvolutionId = 1;
            nextEvolutionConditionId = 1;

            importEvolutions(entries);
            connection.commit();

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        } finally {
            try { connection.setAutoCommit(true); } catch (java.sql.SQLException e) { e.printStackTrace(); }
        }
    }

    //============SPAWN=============

    //============RULES (gender ratio, ability tiers) from pokemon.json=============
    // The DB keeps no gender ratio and no basic/advanced/high tiers, so they are read from
    // pokemon.json at start-up (same file the import uses; missing file = 50/50 and any ability).

    private record Rules(Double malePercent, List<String> basic, List<String> advanced, List<String> high) {}

    private final Map<String, Rules> rulesByForm = new HashMap<>();

    private void loadRules() {
        try {
            String content = Files.readString(Path.of("pokemon.json"));
            for (Object raw : SimpleJsonParser.asList(SimpleJsonParser.parse(content))) {
                Map<String, Object> e = SimpleJsonParser.asMap(raw);
                String form = SimpleJsonParser.asString(e.get("form"));
                if (form == null || form.isBlank()) form = "Normal";
                Map<String, Object> ab = SimpleJsonParser.asMap(e.get("abilities"));
                Object male = e.get("malePercent");
                rulesByForm.put(SimpleJsonParser.asString(e.get("name")) + "|" + form, new Rules(
                    male == null ? null : (Double) male,
                    names(ab == null ? null : ab.get("basic")), names(ab == null ? null : ab.get("advanced")), names(ab == null ? null : ab.get("high"))));
            }
        } catch (Exception e) {
            System.out.println("pokemon.json rules not loaded (50/50 gender, any ability): " + e.getMessage());
        }
    }

    private static List<String> names(Object list) {
        List<String> out = new java.util.ArrayList<>();
        for (Object o : SimpleJsonParser.asList(list)) out.add(SimpleJsonParser.asString(o));
        return out;
    }

    private Rules rulesOf(PokemonForm form) {
        PokemonSpecies species = repository.findPokemonSpeciesByDexNumber(form.getSpeciesId());
        return rulesByForm.get(species.getName() + "|" + form.getName());
    }

    //============SPAWN OPTIONS=============
    // null = random. Whatever the Master picks must be legal for the form and level.

    public record SpawnOptions(Integer natureId, Integer abilityId, String gender, Boolean shiny) {
        public static final SpawnOptions RANDOM = new SpawnOptions(null, null, null, null);
    }

    // Genders the form can have: genderless, female only (0% male), male only (100%) or both
    public List<String> allowedGenders(PokemonForm form) {
        Rules r = rulesOf(form);
        Double male = r == null ? Double.valueOf(50) : r.malePercent();
        if (male == null) return List.of("Genderless");
        if (male <= 0) return List.of("Female");
        if (male >= 100) return List.of("Male");
        return List.of("Male", "Female");
    }

    // PTU: Basic ability from the start, Advanced from level 20, High from level 40.
    // Only one ability is stored per Pokemon, so it is picked from the ones unlocked at its level.
    public List<Ability> allowedAbilities(PokemonForm form, int level) {
        Rules r = rulesOf(form);
        List<Ability> out = new java.util.ArrayList<>();
        if (r == null || r.basic().isEmpty()) {  // no rules: every ability the form has
            for (FormAbility fa : repository.findAllFormAbilities()) {
                if (fa.getFormId() == form.getId()) out.add(repository.findAbilityById(fa.getAbilityId()));
            }
            return out;
        }
        List<String> names = new java.util.ArrayList<>(r.basic());
        if (level >= 20) names.addAll(r.advanced());
        if (level >= 40) names.addAll(r.high());
        for (String n : names) {
            Integer id = abilityIdsByName.get(n);
            if (id != null) out.add(repository.findAbilityById(id));
        }
        return out;
    }

    // Returns an error text, or null when the options are legal
    public String checkOptions(int formId, int level, SpawnOptions o) {
        PokemonForm form = repository.findPokemonFormById(formId);
        if (form == null) return "Form not found";
        if (o.gender() != null && !allowedGenders(form).contains(o.gender())) return "This Pokémon can't be " + o.gender();
        if (o.abilityId() != null && allowedAbilities(form, level).stream().noneMatch(a -> a.getId() == o.abilityId()))
            return "That ability is not available at level " + level;
        if (o.natureId() != null && repository.findNatureById(o.natureId()) == null) return "Unknown nature";
        return null;
    }

    //============SPAWN=============

    public synchronized Pokemon spawnPokemon(int formId, int level) {
        return spawnPokemon(formId, level, SpawnOptions.RANDOM);
    }

    // Creates a wild Pokemon (no trainer). Random: nature, ability, gender (by the species' ratio) and
    // shiny (1 in 100); the Master may fix any of them. Stats follow PTU: base stats + nature,
    // then (level + 10) points spread at random while keeping the Base Relation order.
    // Returns null if the form doesn't exist or the options are illegal (see checkOptions).
    public synchronized Pokemon spawnPokemon(int formId, int level, SpawnOptions o) {

        PokemonForm form = repository.findPokemonFormById(formId);
        if (form == null || checkOptions(formId, level, o) != null) return null;

        List<Ability> abilities = allowedAbilities(form, level);
        if (abilities.isEmpty()) return null;
        int abilityId = o.abilityId() != null ? o.abilityId() : abilities.get(random.nextInt(abilities.size())).getId();

        List<Nature> natures = repository.findAllNatures();
        Nature nature = o.natureId() != null ? repository.findNatureById(o.natureId()) : natures.get(random.nextInt(natures.size()));

        List<String> genders = allowedGenders(form);
        String gender = o.gender();
        if (gender == null) {
            Rules r = rulesOf(form);
            double male = r == null || r.malePercent() == null ? 50 : r.malePercent();
            gender = genders.size() == 1 ? genders.get(0) : (random.nextDouble() * 100 < male ? "Male" : "Female");
        }

        boolean shiny = o.shiny() != null ? o.shiny() : random.nextInt(100) == 0;

        // stats: [HP, Attack, Defense, Sp.Atk, Sp.Def, Speed]
        int[] base = {form.getBaseHp(), form.getBaseAttack(), form.getBaseDefense(), form.getBaseSpAttack(), form.getBaseSpDefense(), form.getBaseSpeed()};
        int[] points = distributePoints(base, nature, level + 10);
        int hpStat = Math.max(1, base[0] + natureBonus(nature, 0)) + points[0];

        // PTU max HP = level + (HP stat * 3) + 10
        float hp = level + hpStat * 3 + 10;

        Pokemon pokemon = new Pokemon(
            nextPokemonId++,
            null,
            form.getSpeciesId(),
            formId,
            null,
            gender,
            shiny,
            nature.getId(),
            abilityId,
            level,
            XP_TABLE[level],
            hp,
            0,
            0,
            0,
            0,
            0,
            LocalDateTime.now()
        );

        repository.createPokemon(pokemon);
        for (int i = 0; i < STATS.length; i++) {
            repository.createPokemonAttributePoints(new PokemonAttributePoints(pokemon.getId(), STATS[i], points[i]));
        }
        return pokemon;
    }

    private static final String[] STATS = {"HP", "Attack", "Defense", "Special Attack", "Special Defense", "Speed"};

    // PTU nature: +2 on one stat and -2 on another; HP moves by 1 instead of 2
    private static int natureBonus(Nature n, int stat) {
        int step = stat == 0 ? 1 : 2;
        return (STATS[stat].equals(n.getIncreasedAttribute()) ? step : 0) - (STATS[stat].equals(n.getDecreasedAttribute()) ? step : 0);
    }

    // Base Relation rule: a stat with a lower (base + nature) can never pass one with a higher value.
    // Each point goes to a random stat that still obeys that.
    private int[] distributePoints(int[] base, Nature nature, int total) {
        int[] adjusted = new int[6], points = new int[6];
        for (int i = 0; i < 6; i++) adjusted[i] = Math.max(1, base[i] + natureBonus(nature, i));
        for (int p = 0; p < total; p++) {
            List<Integer> eligible = new java.util.ArrayList<>();
            for (int i = 0; i < 6; i++) if (canAdd(adjusted, points, null, i)) eligible.add(i);
            points[eligible.get(random.nextInt(eligible.size()))]++;
        }
        return points;
    }

    // Base Relation check for one more point on stat i (adjusted = base + nature, points = points already spent)
    // free[i] = 1: the Master lets this stat ignore the rule (a Feature / Edge that breaks it); pass null for none
    private static boolean canAdd(int[] adjusted, int[] points, int[] free, int i) {
        if (free != null && free[i] == 1) return true;
        for (int j = 0; j < 6; j++) {
            if (adjusted[j] > adjusted[i] && adjusted[i] + points[i] + 1 > adjusted[j] + points[j]) return false;
        }
        return true;
    }

    // What the Master sees after a spawn: the public description plus the hidden details
    public String masterJson(Pokemon pokemon) {
        String json = describePokemonJson(pokemon);
        Nature nature = repository.findNatureById(pokemon.getNatureId());
        Ability ability = repository.findAbilityById(pokemon.getAbilityId());
        return json.substring(0, json.length() - 1)
            + ",\"nature\":\"" + nature.getName() + "\",\"ability\":\"" + ability.getName().replace("\"", "") + "\"}";
    }

    // Master gives a new Pokemon to a character. First 6 go to the party (slots 1-6),
    // the rest to PC box 1.
    public synchronized Pokemon givePokemon(int formId, int level, int trainerId, SpawnOptions options) {

        if (!hasRoom(trainerId)) return null;  // callers check hasRoom first to tell this apart from "form not found"
        Pokemon pokemon = spawnPokemon(formId, level, options);
        if (pokemon == null) return null;

        pokemon.setTrainerId(trainerId);
        int[] at = freeSlot(pokemonOf(trainerId), trainerId);
        pokemon.setPartySlot(at[0]);
        pokemon.setPcBoxId(at[1]);
        pokemon.setPcSlot(at[2]);
        freePoints(pokemon);
        return pokemon;
    }

    //============PARTY / PC=============
    // Party = slots 1-6 (pcBoxId 0). PC = table pcs + pc_boxes: every trainer starts with ONE box
    // (DEFAULT_SLOTS slots, the schema allows 1-30 per box); the player adds more when he wants.
    public static final int PARTY_SIZE = 6;
    public static final int DEFAULT_SLOTS = 30;

    // Boxes of the trainer ordered by number (creates the PC and box 1 on first use)
    public synchronized List<Server.PC.PCBox> boxesOf(int trainerId) {
        Server.PC.PC pc = null;
        for (Server.PC.PC x : repository.findAllPCs()) if (x.getTrainerId() == trainerId) pc = x;
        if (pc == null) {
            int id = 1;
            for (Server.PC.PC x : repository.findAllPCs()) id = Math.max(id, x.getId() + 1);
            pc = new Server.PC.PC(id, trainerId);
            repository.createPC(pc);
        }
        List<Server.PC.PCBox> boxes = new java.util.ArrayList<>();
        for (Server.PC.PCBox x : repository.findAllPCBoxes()) if (x.getPcId() == pc.getId()) boxes.add(x);
        if (boxes.isEmpty()) {
            boxes.add(newBox(pc.getId(), 1));
        }
        boxes.sort(java.util.Comparator.comparingInt(Server.PC.PCBox::getBoxNumber));
        return boxes;
    }

    private Server.PC.PCBox newBox(int pcId, int number) {
        int id = 1;
        for (Server.PC.PCBox x : repository.findAllPCBoxes()) id = Math.max(id, x.getId() + 1);
        Server.PC.PCBox box = new Server.PC.PCBox(id, pcId, "Box " + number, number, DEFAULT_SLOTS);
        repository.createPCBox(box);
        return box;
    }

    // The player asks for one more box
    public synchronized void addBox(int trainerId) {
        List<Server.PC.PCBox> boxes = boxesOf(trainerId);
        newBox(boxes.get(0).getPcId(), boxes.get(boxes.size() - 1).getBoxNumber() + 1);
    }

    private Pokemon pokemonAt(List<Pokemon> all, boolean party, int box, int slot) {
        for (Pokemon p : all) {
            if (party ? p.getPartySlot() == slot : (p.getPcBoxId() == box && p.getPcSlot() == slot)) return p;
        }
        return null;
    }

    // First free place as {partySlot, box, slot}: party first, then the boxes in order. null = no room.
    private int[] freeSlot(List<Pokemon> others, int trainerId) {
        for (int slot = 1; slot <= PARTY_SIZE; slot++) {
            if (pokemonAt(others, true, 0, slot) == null) return new int[] { slot, 0, 0 };
        }
        for (Server.PC.PCBox box : boxesOf(trainerId)) {
            for (int slot = 1; slot <= box.getSlotLimit(); slot++) {
                if (pokemonAt(others, false, box.getBoxNumber(), slot) == null) return new int[] { 0, box.getBoxNumber(), slot };
            }
        }
        return null;
    }

    // Is there a free place (party or any box) for one more Pokemon?
    public boolean hasRoom(int trainerId) {
        return freeSlot(pokemonOf(trainerId), trainerId) != null;
    }

    // Moves one of the trainer's Pokemon to a party slot or a box slot; swaps with whoever is there.
    // Returns an error text, or null when done.
    public synchronized String movePokemon(int trainerId, int pokemonId, boolean toParty, int box, int slot) {
        List<Pokemon> all = pokemonOf(trainerId);
        Pokemon mover = null;
        for (Pokemon p : all) if (p.getId() == pokemonId) mover = p;
        if (mover == null) return "Pokemon not found";
        int limit = PARTY_SIZE;
        if (!toParty) {
            limit = 0;
            for (Server.PC.PCBox b : boxesOf(trainerId)) if (b.getBoxNumber() == box) limit = b.getSlotLimit();
        }
        if (slot < 1 || slot > limit) return "Bad slot";

        Pokemon other = pokemonAt(all, toParty, box, slot);
        if (other != null && other.getId() == mover.getId()) return null;
        if (other != null) {  // the other one takes the mover's old place
            other.setPartySlot(mover.getPartySlot());
            other.setPcBoxId(mover.getPcBoxId());
            other.setPcSlot(mover.getPcSlot());
            repository.updatePokemon(other);
        }
        mover.setPartySlot(toParty ? slot : 0);
        mover.setPcBoxId(toParty ? 0 : box);
        mover.setPcSlot(toParty ? 0 : slot);
        repository.updatePokemon(mover);
        return null;
    }

    public static final int NICKNAME_MAX = 20;

    // The owner renames his Pokemon (empty text clears the nickname). Returns an error text, or null.
    public synchronized String setNickname(int trainerId, int pokemonId, String text) {
        for (Pokemon p : pokemonOf(trainerId)) {
            if (p.getId() != pokemonId) continue;
            // letters, digits, spaces and common punctuation only (it travels inside JSON and HTML)
            String clean = (text == null ? "" : text).replaceAll("[\"\\\\<>&\\p{Cntrl}]", "").trim();
            if (clean.length() > NICKNAME_MAX) return "Nickname must have at most " + NICKNAME_MAX + " characters";
            p.setNickname(clean.isEmpty() ? null : clean);
            repository.updatePokemon(p);
            return null;
        }
        return "Pokemon not found";
    }

    // The owner lets one of his Pokemon go (deleted for good). Returns an error text, or null.
    public synchronized String releasePokemon(int trainerId, int pokemonId) {
        for (Pokemon p : pokemonOf(trainerId)) {
            if (p.getId() != pokemonId) continue;
            repository.deletePokemon(pokemonId);
            return null;
        }
        return "Pokemon not found";
    }

    //============STATUS (HP, XP, stat points)=============

    // PTU experience chart: total XP needed to reach each level (index = level, 1..100)
    private static final int[] XP_TABLE = {0,
        0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 110, 135, 160, 190, 220, 250, 285, 320, 360, 400,
        460, 530, 600, 670, 745, 820, 900, 990, 1075, 1165, 1260, 1355, 1455, 1555, 1660, 1770, 1880, 1995, 2110, 2230,
        2355, 2480, 2610, 2740, 2875, 3015, 3155, 3300, 3445, 3645, 3850, 4060, 4270, 4485, 4705, 4930, 5160, 5390, 5625, 5865,
        6110, 6360, 6610, 6865, 7125, 7390, 7660, 7925, 8205, 8485, 8770, 9060, 9350, 9645, 9945, 10250, 10560, 10870, 11185, 11505,
        11910, 12320, 12735, 13155, 13580, 14010, 14445, 14885, 15330, 15780, 16235, 16695, 17160, 17630, 18105, 18585, 19070, 19560, 20055, 20555};

    // Pokemon made before XP existed have 0 XP at a high level: count at least the XP of their level
    private static int xpOf(Pokemon p) {
        return Math.max(p.getXp(), XP_TABLE[p.getLevel()]);
    }

    // [0] base stats, [1] nature bonus, [2] points spent; each in the order HP, Atk, Def, SpA, SpD, Spe
    private int[][] statArrays(Pokemon p) {
        PokemonForm form = repository.findPokemonFormById(p.getFormId());
        Nature nature = repository.findNatureById(p.getNatureId());
        int[] base = {form.getBaseHp(), form.getBaseAttack(), form.getBaseDefense(), form.getBaseSpAttack(), form.getBaseSpDefense(), form.getBaseSpeed()};
        int[] nat = new int[6], pts = new int[6];
        for (int i = 0; i < 6; i++) {
            nat[i] = nature == null ? 0 : natureBonus(nature, i);
            PokemonAttributePoints row = repository.findPokemonAttributePoints(p.getId(), STATS[i]);
            pts[i] = row == null ? 0 : row.getPoints();
        }
        int[] vit = vitaminsOf(p);
        for (int i = 0; i < 6; i++) base[i] += vit[i];   // each vitamin is +1 base
        int[] free = new int[6];
        List<String> exceptions = repository.findStatExceptions(p.getId());
        for (int i = 0; i < 6; i++) free[i] = exceptions.contains(STATS[i]) ? 1 : 0;
        return new int[][] {base, nat, pts, free};
    }

    private static String arr(int[] a) {
        return java.util.Arrays.toString(a).replace(" ", "");
    }

    // PTU max HP = level + (HP stat * 3) + 10
    private static int maxHp(int[][] s, int level) {
        return level + (Math.max(1, s[0][0] + s[1][0]) + s[2][0]) * 3 + 10;
    }

    // The status screen's numbers (added to ownJson): max HP, XP bar, free stat points, stat table
    private String statusJson(Pokemon p) {
        int[][] s = statArrays(p);
        int level = p.getLevel();
        return ",\"maxHp\":" + maxHp(s, level)
            + ",\"xp\":" + xpOf(p) + ",\"xpFloor\":" + XP_TABLE[level] + ",\"xpNext\":" + (level < 100 ? XP_TABLE[level + 1] : -1)
            + ",\"points\":" + p.getUndistributedAttributePoints()
            + ",\"base\":" + arr(s[0]) + ",\"nat\":" + arr(s[1]) + ",\"pts\":" + arr(s[2]) + ",\"free\":" + arr(s[3])
            + ",\"evo\":" + evolutionJson(p.getSpeciesId())
            + ",\"caps\":" + capsJson(p.getFormId());
    }

    //============CAPABILITIES=============

    // form id -> [["Overland","5"],["Jump","1/1"]...]; built once (the data never changes after import)
    private Map<Integer, String> capsByForm;

    private synchronized String capsJson(int formId) {
        if (capsByForm == null) {
            Map<Integer, String> names = new HashMap<>();
            for (Capability c : repository.findAllCapabilities()) names.put(c.getId(), c.getName());
            Map<Integer, StringBuilder> rows = new HashMap<>();
            for (FormCapability fc : repository.findAllFormCapabilities()) {
                StringBuilder b = rows.computeIfAbsent(fc.getFormId(), k -> new StringBuilder());
                b.append(b.length() > 0 ? "," : "").append("[\"").append(String.valueOf(names.get(fc.getCapabilityId())).replace("\"", ""))
                    .append("\",\"").append(String.valueOf(fc.getValue()).replace("\"", "")).append("\"]");
            }
            capsByForm = new HashMap<>();
            rows.forEach((k, v) -> capsByForm.put(k, "[" + v + "]"));
        }
        return capsByForm.getOrDefault(formId, "[]");
    }

    // The Master's view of a wild Pokemon: nature, ability, stats (base / nature / spent) and capabilities
    public String wildDetailJson(Pokemon p) {
        Nature nature = repository.findNatureById(p.getNatureId());
        Ability ability = repository.findAbilityById(p.getAbilityId());
        int[][] s = statArrays(p);
        return "\"nature\":\"" + (nature == null ? "" : nature.getName()) + "\",\"ability\":\"" + (ability == null ? "" : ability.getName().replace("\"", "")) + "\""
            + ",\"base\":" + arr(s[0]) + ",\"nat\":" + arr(s[1]) + ",\"pts\":" + arr(s[2]) + ",\"caps\":" + capsJson(p.getFormId());
    }

    //============ITEMS ON A POKEMON=============

    // ponytail: PTU's cap on vitamins per Pokemon, edit here if the table plays another number
    public static final int VITAMIN_MAX = 5;
    private static final String[] VITAMINS = {"HP Up", "Protein", "Iron", "Calcium", "Zinc", "Carbos"};   // same order as STATS
    private int[] vitaminIds;

    private synchronized int[] vitaminIds() {
        if (vitaminIds == null) {
            int[] ids = new int[6];
            for (Server.Item.Item it : repository.findAllItems()) for (int i = 0; i < 6; i++) if (it.getName().equals(VITAMINS[i])) ids[i] = it.getId();
            vitaminIds = ids;
        }
        return vitaminIds;
    }

    // vitamins taken, per stat
    private int[] vitaminsOf(Pokemon p) {
        int[] ids = vitaminIds(), n = new int[6];
        for (int i = 0; i < 6; i++) {
            PokemonVitamin row = ids[i] == 0 ? null : repository.findPokemonVitamin(p.getId(), ids[i]);
            n[i] = row == null ? 0 : row.getQuantity();
        }
        return n;
    }

    private static final java.util.regex.Pattern MIN_LEVEL = java.util.regex.Pattern.compile("(?i)min+imum\\s*(\\d+)");

    // Uses one item on one of the trainer's Pokemon. Returns "!reason" when it can't (nothing is spent), else a short message.
    // arg = the species to evolve into (stones). The caller takes the item out of the bag when the answer isn't "!...".
    public synchronized String applyItem(int trainerId, int pokemonId, Server.Item.Item item, String arg) {
        Pokemon p = findOwned(trainerId, pokemonId);
        if (p == null) return "!Pokemon not found";
        String n = item.getName();
        int max = maxHpOf(p);
        int heal = switch (n) {
            case "Potion" -> 20; case "Super Potion" -> 35; case "Hyper Potion" -> 70; case "Energy Powder" -> 25;
            case "Max Potion", "Full Restore" -> max;
            default -> 0;
        };
        if (heal > 0) {
            if (p.getHp() <= 0) return "!Fainted: needs a Revive";
            if (p.getHp() >= max) return "!HP is already full";
            float before = p.getHp();
            p.setHp(Math.min(max, before + heal));
            repository.updatePokemon(p);
            return "+" + Math.round(p.getHp() - before) + " HP";
        }
        if (n.equals("Revive") || n.equals("Max Revive")) {
            if (p.getHp() > 0) return "!Not fainted";
            p.setHp(n.equals("Revive") ? Math.max(1, max / 2) : max);
            repository.updatePokemon(p);
            return "HP " + Math.round(p.getHp());
        }
        int v = java.util.Arrays.asList(VITAMINS).indexOf(n);
        if (v >= 0) {
            int[] have = vitaminsOf(p);
            if (java.util.Arrays.stream(have).sum() >= VITAMIN_MAX) return "!Already took " + VITAMIN_MAX + " vitamins";
            if (have[v] == 0 && repository.findPokemonVitamin(pokemonId, item.getId()) == null) repository.createPokemonVitamin(new PokemonVitamin(pokemonId, item.getId(), 1));
            else repository.updatePokemonVitamin(new PokemonVitamin(pokemonId, item.getId(), have[v] + 1), pokemonId, item.getId());
            if (v == 0 && p.getHp() > 0) { p.setHp(p.getHp() + 3); repository.updatePokemon(p); }   // max HP grows by 3
            return "+1 " + STATS[v];
        }
        if (n.equals("Rare Candy")) {
            if (p.getLevel() >= 100) return "!Already level 100";
            int[] r = addXp(pokemonId, XP_TABLE[p.getLevel() + 1] - xpOf(p));
            return r == null ? "!Pokemon not found" : "Level " + r[1];
        }
        if ("Stone".equals(item.getCategory()) && !n.equals("Everstone")) {
            loadEvolutionInfo();
            String key = n.replace(" ", "").toLowerCase();
            List<String[]> match = new java.util.ArrayList<>();
            for (String[] r : evolvesTo.getOrDefault(p.getSpeciesId(), List.of())) if (r[1].replace(" ", "").toLowerCase().contains(key)) match.add(r);
            if (match.isEmpty()) return "!Nothing happens";
            String[] pick = null;
            for (String[] r : match) if (r[0].equals(arg) || (match.size() == 1 && (arg == null || arg.isEmpty()))) pick = r;
            if (pick == null) return "!Choose what to evolve into";
            java.util.regex.Matcher m = MIN_LEVEL.matcher(pick[1]);
            if (m.find() && p.getLevel() < Integer.parseInt(m.group(1))) return "!Level " + m.group(1) + " needed";
            String problem = evolve(null, pokemonId, pick[0]);
            return problem != null ? "!" + problem : "Evolved into " + pick[0];
        }
        return "!This item can't be used on a Pokemon";
    }

    // Puts an item in the Pokemon's hand (0 = takes it off). Returns the item it was holding before (0 = none), or -1 when not his.
    public synchronized int swapHeld(int trainerId, int pokemonId, int itemId) {
        Pokemon p = findOwned(trainerId, pokemonId);
        if (p == null) return -1;
        int old = p.getHeldItemId();
        p.setHeldItemId(itemId);
        repository.updatePokemon(p);
        return old;
    }

    private String heldJson(Pokemon p) {
        Server.Item.Item it = p.getHeldItemId() > 0 ? repository.findItemById(p.getHeldItemId()) : null;
        if (it == null) return "null";
        String eff = it.getEffect() == null ? "" : it.getEffect();
        return "{\"id\":" + it.getId() + ",\"name\":\"" + it.getName().replace("\"", "") + "\",\"icon\":\"" + (eff.startsWith("icon:") ? eff.substring(5).replace("\"", "") : "") + "\",\"info\":\"" + it.getDescription().replace("\\", "").replace("\"", "") + "\"}";
    }

    //============EVOLUTION INFO=============

    // species id -> rows { other species name, requirement text from the book }; built once (the data never changes after import)
    private Map<Integer, List<String[]>> evolvesTo, evolvesFrom;

    private synchronized void loadEvolutionInfo() {
        if (evolvesTo != null) return;
        Map<Integer, String> requirements = new HashMap<>();
        for (EvolutionCondition c : repository.findAllEvolutionConditions()) requirements.put(c.getEvolutionId(), c.getValue());
        Map<Integer, List<String[]>> to = new HashMap<>(), from = new HashMap<>();
        for (Evolution e : repository.findAllEvolutions()) {
            PokemonSpecies a = repository.findPokemonSpeciesByDexNumber(e.getFromSpeciesId());
            PokemonSpecies b = repository.findPokemonSpeciesByDexNumber(e.getToSpeciesId());
            if (a == null || b == null) continue;
            String req = requirements.getOrDefault(e.getId(), "");
            to.computeIfAbsent(a.getDexNumber(), k -> new java.util.ArrayList<>()).add(new String[] { b.getName(), req });
            from.computeIfAbsent(b.getDexNumber(), k -> new java.util.ArrayList<>()).add(new String[] { a.getName(), req });
        }
        evolvesFrom = from;
        evolvesTo = to;
    }

    // {"from":[["Ivysaur","Minimum 30"]],"to":[...]}: what this species comes from and what it becomes
    private String evolutionJson(int speciesId) {
        loadEvolutionInfo();
        return "{\"from\":" + evoRows(evolvesFrom.get(speciesId)) + ",\"to\":" + evoRows(evolvesTo.get(speciesId)) + "}";
    }

    private String evoRows(List<String[]> rows) {
        if (rows == null) return "[]";
        StringBuilder out = new StringBuilder("[");
        for (String[] r : rows) {
            if (out.length() > 1) out.append(',');
            out.append("[\"").append(r[0].replace("\"", "")).append("\",\"").append(r[1].replace("\\", "").replace("\"", "")).append("\"]");
        }
        return out.append(']').toString();
    }

    //============EVOLVE=============

    private static final java.util.regex.Pattern PLAIN_LEVEL = java.util.regex.Pattern.compile("^\\s*Minimum (\\d+)\\s*$");

    // Level of an evolution that asks for nothing else ("Minimum 30"); -1 when the book also asks for an item, move, time...
    public static int plainLevel(String requirement) {
        java.util.regex.Matcher m = PLAIN_LEVEL.matcher(requirement == null ? "" : requirement);
        return m.matches() ? Integer.parseInt(m.group(1)) : -1;
    }

    // Evolves a Pokemon into the species called toName. trainerId null = Master: any listed evolution, requirement ignored.
    // A player may only do the plain level ones; the rest are asked to the Master. Nature, level, XP, nickname, shiny and
    // the damage taken stay. The ability stays if the new form has it, else it becomes the first one allowed. The spent stat
    // points stay unless the new base stats break the Base Relation: then all of them are given back. Returns an error text, or null.
    public synchronized String evolve(Integer trainerId, int pokemonId, String toName) {
        Pokemon p = findOwned(trainerId, pokemonId);
        if (p == null) return "Pokemon not found";
        Integer toSpecies = speciesIdsByName.get(toName);
        Evolution chosen = null;
        for (Evolution e : repository.findAllEvolutions()) {
            if (e.getFromSpeciesId() == p.getSpeciesId() && toSpecies != null && e.getToSpeciesId() == toSpecies) chosen = e;
        }
        if (chosen == null) return "This Pokémon doesn't evolve into that";

        if (trainerId != null) {
            String requirement = "";
            for (EvolutionCondition c : repository.findAllEvolutionConditions()) if (c.getEvolutionId() == chosen.getId()) requirement = c.getValue();
            int need = plainLevel(requirement);
            if (need < 0) return "This evolution needs the Master";
            if (p.getLevel() < need) return "Level " + need + " needed to evolve";
        }

        // same form name (Alolan -> Alolan) when the new species has it, else the form the book points at
        PokemonForm oldForm = repository.findPokemonFormById(p.getFormId());
        int newFormId = chosen.getToFormId();
        for (PokemonForm f : repository.findAllPokemonForms()) {
            if (f.getSpeciesId() == toSpecies && f.getName().equals(oldForm.getName())) newFormId = f.getId();
        }
        PokemonForm form = repository.findPokemonFormById(newFormId);

        String gender = p.getGender();
        if (!allowedGenders(form).contains(gender)) {
            if (trainerId != null) return "This Pokémon can't evolve as a " + gender;
            gender = allowedGenders(form).get(0);
        }

        int[][] before = statArrays(p);
        int damage = Math.max(0, Math.round(maxHp(before, p.getLevel()) - p.getHp()));
        List<Ability> abilities = allowedAbilities(form, p.getLevel());
        int abilityId = p.getAbilityId();
        if (!abilities.isEmpty() && abilities.stream().noneMatch(a -> a.getId() == abilityId)) p.setAbilityId(abilities.get(0).getId());

        p.setSpeciesId(toSpecies);
        p.setFormId(newFormId);
        p.setGender(gender);

        int[][] after = statArrays(p);
        int[] adjusted = new int[6];
        for (int i = 0; i < 6; i++) adjusted[i] = Math.max(1, after[0][i] + after[1][i]);
        boolean broken = false;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (after[3][i] == 0 && adjusted[j] > adjusted[i] && adjusted[i] + after[2][i] > adjusted[j] + after[2][j]) broken = true;
            }
        }
        if (broken) {
            for (int i = 0; i < 6; i++) setPoints(pokemonId, i, 0);
            p.setUndistributedAttributePoints(p.getLevel() + 10);
        }
        p.setHp(Math.max(0, maxHp(statArrays(p), p.getLevel()) - damage));
        repository.updatePokemon(p);
        return null;
    }

    // trainerId null = any owned Pokemon (the Master); otherwise it must belong to that trainer
    private Pokemon findOwned(Integer trainerId, int pokemonId) {
        Pokemon p = repository.findPokemonById(pokemonId);
        if (p == null || p.getTrainerId() == null || (trainerId != null && !trainerId.equals(p.getTrainerId()))) return null;
        return p;
    }

    // Spends free stat points: add = points per stat. The Base Relation rule must hold. Returns an error text, or null.
    public synchronized String spendPoints(int trainerId, int pokemonId, int[] add) {
        Pokemon p = findOwned(trainerId, pokemonId);
        if (p == null) return "Pokemon not found";
        int total = 0;
        for (int a : add) { if (a < 0) return "Bad numbers"; total += a; }
        if (total == 0) return null;
        if (total > p.getUndistributedAttributePoints()) return "Not enough stat points";

        int[][] s = statArrays(p);
        int[] adjusted = new int[6], pts = s[2].clone(), left = add.clone();
        for (int i = 0; i < 6; i++) adjusted[i] = Math.max(1, s[0][i] + s[1][i]);
        // one point at a time, in any order that keeps the rule (so the order the player tapped doesn't matter)
        for (int done = 0; done < total; done++) {
            int pick = -1;
            for (int i = 0; i < 6 && pick < 0; i++) if (left[i] > 0 && canAdd(adjusted, pts, s[3], i)) pick = i;
            if (pick < 0) return "Base Relation rule: a stat can't pass one with a higher base";
            pts[pick]++;
            left[pick]--;
        }
        for (int i = 0; i < 6; i++) {
            if (add[i] == 0) continue;
            setPoints(pokemonId, i, pts[i]);
        }
        if (add[0] > 0) p.setHp(p.getHp() + 3 * add[0]);  // max HP grows by 3 per HP point; the Pokemon gets it too
        p.setUndistributedAttributePoints(p.getUndistributedAttributePoints() - total);
        repository.updatePokemon(p);
        return null;
    }

    // A Pokemon that joins a trainer comes with every stat point (level + 10) still free: the player spends them.
    // Saves the Pokemon too. Its HP is full for the stats it has now.
    private void freePoints(Pokemon p) {
        for (int i = 0; i < 6; i++) setPoints(p.getId(), i, 0);
        p.setUndistributedAttributePoints(p.getLevel() + 10);
        p.setHp(maxHp(statArrays(p), p.getLevel()));
        repository.updatePokemon(p);
    }

    private void setPoints(int pokemonId, int stat, int value) {
        PokemonAttributePoints row = new PokemonAttributePoints(pokemonId, STATS[stat], value);
        if (repository.findPokemonAttributePoints(pokemonId, STATS[stat]) == null) repository.createPokemonAttributePoints(row);
        else repository.updatePokemonAttributePoints(row, pokemonId, STATS[stat]);
    }

    // Master: edits one Pokemon following the rules. Any argument may be null (= leave as is).
    // A new nature, or a level that leaves too many points spent, gives every spent point back (level + 10 in total);
    // reset = true does that on purpose. The damage the Pokemon had taken is kept. Returns an error text, or null.
    public synchronized String editPokemon(int pokemonId, Integer natureId, Integer abilityId, String gender, Boolean shiny, Integer level, boolean reset, int[] free) {
        Pokemon p = findOwned(null, pokemonId);
        if (p == null) return "Pokemon not found";
        PokemonForm form = repository.findPokemonFormById(p.getFormId());
        int newLevel = level == null ? p.getLevel() : level;
        if (newLevel < 1 || newLevel > 100) return "Level must be 1-100";
        if (gender != null && !allowedGenders(form).contains(gender)) return "This Pokémon can't be " + gender;
        if (abilityId != null && allowedAbilities(form, newLevel).stream().noneMatch(a -> a.getId() == abilityId))
            return "That ability is not available at level " + newLevel;
        if (natureId != null && repository.findNatureById(natureId) == null) return "Unknown nature";

        int[][] before = statArrays(p);
        int damage = Math.max(0, Math.round(maxHp(before, p.getLevel()) - p.getHp()));
        boolean natureChanged = natureId != null && natureId != p.getNatureId();
        if (natureChanged) p.setNatureId(natureId);
        if (abilityId != null) p.setAbilityId(abilityId);
        if (gender != null) p.setGender(gender);
        if (shiny != null) p.setShiny(shiny);
        if (newLevel != p.getLevel()) { p.setLevel(newLevel); p.setXp(XP_TABLE[newLevel]); }

        if (free != null) for (int i = 0; i < 6; i++) repository.setStatException(pokemonId, STATS[i], free[i] == 1);

        int spent = java.util.Arrays.stream(before[2]).sum();
        if (reset || natureChanged || spent > newLevel + 10) {
            for (int i = 0; i < 6; i++) setPoints(pokemonId, i, 0);
            spent = 0;
        }
        p.setUndistributedAttributePoints(newLevel + 10 - spent);
        p.setHp(Math.max(0, maxHp(statArrays(p), newLevel) - damage));
        repository.updatePokemon(p);
        return null;
    }

    // Current HP, kept between 0 and the maximum. trainerId null = Master. Returns an error text, or null.
    public synchronized String setHp(Integer trainerId, int pokemonId, float hp) {
        Pokemon p = findOwned(trainerId, pokemonId);
        if (p == null) return "Pokemon not found";
        int max = maxHp(statArrays(p), p.getLevel());
        p.setHp(Math.max(0, Math.min(max, hp)));
        repository.updatePokemon(p);
        return null;
    }

    public int maxHpOf(Pokemon p) { return maxHp(statArrays(p), p.getLevel()); }

    // Master sets the current HP of a WILD Pokemon (the battle is played at the table: this is the "weakening")
    public synchronized String setWildHp(int pokemonId, float hp) {
        Pokemon p = repository.findPokemonById(pokemonId);
        if (p == null || p.getTrainerId() != null) return "Pokemon not found";
        p.setHp(Math.max(0, Math.min(maxHpOf(p), hp)));
        repository.updatePokemon(p);
        return null;
    }

    // Master gives XP (1..100000): returns {old level, new level}, or null if the Pokemon isn't a trainer's.
    // Each level gained = +1 free stat point and +1 HP (PTU: max HP = level + HP*3 + 10).
    public synchronized int[] addXp(int pokemonId, int amount) {
        Pokemon p = findOwned(null, pokemonId);
        if (p == null || amount < 1 || amount > 100000) return null;
        int old = p.getLevel(), level = old;
        int xp = Math.min(XP_TABLE[100], xpOf(p) + amount);
        while (level < 100 && xp >= XP_TABLE[level + 1]) level++;
        p.setXp(xp);
        p.setLevel(level);
        p.setUndistributedAttributePoints(p.getUndistributedAttributePoints() + (level - old));
        p.setHp(p.getHp() + (level - old));
        repository.updatePokemon(p);
        return new int[] {old, level};
    }

    // A wild Pokemon becomes the trainer's: first free place, party first. false = no room.
    public synchronized boolean claimWild(Pokemon wild, int trainerId) {
        int[] at = freeSlot(pokemonOf(trainerId), trainerId);
        if (at == null) return false;
        wild.setTrainerId(trainerId);
        wild.setPartySlot(at[0]);
        wild.setPcBoxId(at[1]);
        wild.setPcSlot(at[2]);
        wild.setCapturedAt(LocalDateTime.now());
        freePoints(wild);
        return true;
    }

    // How many evolutions this species still has in front of it (longest path), for the capture rate
    public int evolutionsLeft(int speciesId) {
        return evolutionsLeft(speciesId, 0);
    }

    private int evolutionsLeft(int speciesId, int depth) {
        if (depth > 5) return 0;  // guards against a loop in the data
        int best = 0;
        for (Evolution evolution : repository.findAllEvolutions()) {
            if (evolution.getFromSpeciesId() == speciesId) {
                best = Math.max(best, 1 + evolutionsLeft(evolution.getToSpeciesId(), depth + 1));
            }
        }
        return best;
    }

    // What the owner sees about his own Pokemon (nature/ability included) + where it is
    public String ownJson(Pokemon pokemon) {
        String json = describePokemonJson(pokemon);
        Nature nature = repository.findNatureById(pokemon.getNatureId());
        Ability ability = repository.findAbilityById(pokemon.getAbilityId());
        return json.substring(0, json.length() - 1)
            + ",\"nickname\":\"" + (pokemon.getNickname() == null ? "" : pokemon.getNickname()) + "\""
            + ",\"nature\":\"" + (nature == null ? "" : nature.getName()) + "\""
            + ",\"ability\":\"" + (ability == null ? "" : ability.getName().replace("\"", "")) + "\""
            + ",\"formId\":" + pokemon.getFormId() + ",\"natureId\":" + pokemon.getNatureId() + ",\"abilityId\":" + pokemon.getAbilityId()
            + ",\"party\":" + pokemon.getPartySlot()
            + ",\"box\":" + pokemon.getPcBoxId()
            + ",\"slot\":" + pokemon.getPcSlot() + ",\"held\":" + heldJson(pokemon) + statusJson(pokemon) + "}";
    }

    public List<Pokemon> pokemonOf(int trainerId) {
        List<Pokemon> list = new java.util.ArrayList<>();
        for (Pokemon p : repository.findAllPokemons()) {
            if (p.getTrainerId() != null && p.getTrainerId() == trainerId) list.add(p);
        }
        return list;
    }

    // One-line wire format sent to clients:
    // POKEMON|id|species|form|types|level|hp
    public String describePokemon(Pokemon pokemon) {

        PokemonForm form = repository.findPokemonFormById(pokemon.getFormId());
        PokemonSpecies species = repository.findPokemonSpeciesByDexNumber(pokemon.getSpeciesId());

        String types = form.getType1() + (form.getType2() != null ? "/" + form.getType2() : "");

        return "POKEMON|" + pokemon.getId()
            + "|" + species.getName()
            + "|" + form.getName()
            + "|" + types
            + "|" + pokemon.getLevel()
            + "|" + pokemon.getHp();
    }

    // Rows { formId, "Species (Form)", familyId, stage, "Species|Form" } for the Master's picker.
    // Family = every species connected by evolutions (Bulbasaur-Ivysaur-Venusaur);
    // stage = 1 for the first species of the family, 2 for what it evolves into, ...
    public List<String[]> listFormLabels() {

        Map<Integer, String> speciesNames = new HashMap<>();
        for (PokemonSpecies species : repository.findAllPokemonSpecies()) {
            speciesNames.put(species.getDexNumber(), species.getName());
        }

        // union-find over the evolution edges
        Map<Integer, Integer> parentOf = new HashMap<>();
        Map<Integer, List<Integer>> children = new HashMap<>();
        Set<Integer> hasParent = new java.util.HashSet<>();

        for (Evolution evolution : repository.findAllEvolutions()) {
            int a = findRoot(parentOf, evolution.getFromSpeciesId());
            int b = findRoot(parentOf, evolution.getToSpeciesId());
            if (a != b) parentOf.put(b, a);

            children.computeIfAbsent(evolution.getFromSpeciesId(), k -> new java.util.ArrayList<>())
                .add(evolution.getToSpeciesId());
            hasParent.add(evolution.getToSpeciesId());
        }

        // stage = depth from the species nobody evolves from
        Map<Integer, Integer> stageOf = new HashMap<>();
        java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
        for (Integer speciesId : children.keySet()) {
            if (!hasParent.contains(speciesId)) {
                stageOf.put(speciesId, 1);
                queue.add(speciesId);
            }
        }
        while (!queue.isEmpty()) {
            int current = queue.poll();
            for (int next : children.getOrDefault(current, new java.util.ArrayList<>())) {
                if (!stageOf.containsKey(next)) {
                    stageOf.put(next, stageOf.get(current) + 1);
                    queue.add(next);
                }
            }
        }

        List<String[]> labels = new java.util.ArrayList<>();
        for (PokemonForm form : repository.findAllPokemonForms()) {
            int speciesId = form.getSpeciesId();
            String label = speciesNames.get(speciesId);
            if (!form.getName().equals("Normal")) label += " (" + form.getName() + ")";

            labels.add(new String[] {
                String.valueOf(form.getId()),
                label,
                String.valueOf(findRoot(parentOf, speciesId)),
                String.valueOf(stageOf.getOrDefault(speciesId, 1)),
                speciesNames.get(speciesId) + "|" + form.getName(),   // key of web/assets/pokemon-art.json
                form.getType1() + (form.getType2() != null ? "/" + form.getType2() : "")   // "Grass/Poison"
            });
        }
        labels.sort((x, y) -> x[1].compareToIgnoreCase(y[1]));
        return labels;
    }

    private int findRoot(Map<Integer, Integer> parentOf, int id) {
        while (parentOf.containsKey(id)) id = parentOf.get(id);
        return id;
    }

    // What a player sees when a Pokemon appears
    public String describePokemonJson(Pokemon pokemon) {

        PokemonForm form = repository.findPokemonFormById(pokemon.getFormId());
        PokemonSpecies species = repository.findPokemonSpeciesByDexNumber(pokemon.getSpeciesId());

        String types = "\"" + form.getType1() + "\"" + (form.getType2() != null ? ",\"" + form.getType2() + "\"" : "");

        return "{\"id\":" + pokemon.getId()
            + ",\"species\":\"" + species.getName().replace("\"", "") + "\""
            + ",\"form\":\"" + form.getName().replace("\"", "") + "\""
            + ",\"types\":[" + types + "]"
            + ",\"level\":" + pokemon.getLevel()
            + ",\"hp\":" + pokemon.getHp()
            + ",\"gender\":\"" + pokemon.getGender() + "\""
            + ",\"shiny\":" + pokemon.isShiny() + "}";
    }

    //============EVOLUTIONS=============

    // Each entry in the JSON lists its whole family as "stage / name / requirement".
    // Every stage-N line is linked to the closest earlier stage-(N-1) line.
    // ponytail: evolutions point to each species' first form (regional forms
    // evolving into regional forms is not modelled yet)
    private void importEvolutions(List<Object> entries) {

        Set<String> done = new LinkedHashSet<>();

        for (Object rawEntry : entries) {
            Map<String, Object> entry = SimpleJsonParser.asMap(rawEntry);
            List<Object> chain = SimpleJsonParser.asList(entry.get("evolution"));

            for (int i = 0; i < chain.size(); i++) {
                Map<String, Object> child = SimpleJsonParser.asMap(chain.get(i));
                int stage = SimpleJsonParser.asInt(child.get("stage"));
                if (stage <= 1) continue;

                Map<String, Object> parent = null;
                for (int j = i - 1; j >= 0; j--) {
                    Map<String, Object> candidate = SimpleJsonParser.asMap(chain.get(j));
                    if (SimpleJsonParser.asInt(candidate.get("stage")) == stage - 1) {
                        parent = candidate;
                        break;
                    }
                }
                if (parent == null) continue;

                String fromName = SimpleJsonParser.asString(parent.get("name"));
                String toName = SimpleJsonParser.asString(child.get("name"));
                if (!done.add(fromName + ">" + toName)) continue;

                Integer fromSpecies = speciesIdsByName.get(fromName);
                Integer toSpecies = speciesIdsByName.get(toName);
                if (fromSpecies == null || toSpecies == null || fromSpecies.equals(toSpecies)) continue;

                int evolutionId = nextEvolutionId++;
                repository.createEvolution(new Evolution(
                    evolutionId,
                    fromSpecies, firstFormIdBySpecies.get(fromSpecies),
                    toSpecies, firstFormIdBySpecies.get(toSpecies)
                ));

                String requirement = SimpleJsonParser.asString(child.get("requirement"));
                if (requirement != null) {
                    repository.createEvolutionCondition(new EvolutionCondition(
                        nextEvolutionConditionId++, evolutionId, "Requirement", requirement
                    ));
                }
            }
        }
    }

    //============IMPORT=============

   
    private void loadLookupCaches() {

        for (Ability ability : repository.findAllAbilities()) {
            abilityIdsByName.put(ability.getName(), ability.getId());
            if (ability.getId() >= nextAbilityId) {
                nextAbilityId = ability.getId() + 1;
            }
        }

        for (Capability capability : repository.findAllCapabilities()) {
            capabilityIdsByName.put(capability.getName(), capability.getId());
            if (capability.getId() >= nextCapabilityId) {
                nextCapabilityId = capability.getId() + 1;
            }
        }

        for (Skill skill : repository.findAllSkills()) {
            skillIdsByName.put(skill.getName(), skill.getId());
            if (skill.getId() >= nextSkillId) {
                nextSkillId = skill.getId() + 1;
            }
        }

        for (PokemonSpecies species : repository.findAllPokemonSpecies()) {
            speciesIdsByName.put(species.getName(), species.getDexNumber());
            if (species.getDexNumber() >= nextSpeciesId) {
                nextSpeciesId = species.getDexNumber() + 1;
            }
        }

        for (PokemonForm form : repository.findAllPokemonForms()) {
            if (form.getId() >= nextFormId) {
                nextFormId = form.getId() + 1;
            }
        }

        for (Pokemon pokemon : repository.findAllPokemons()) {
            if (pokemon.getId() >= nextPokemonId) {
                nextPokemonId = pokemon.getId() + 1;
            }
        }

        for (Evolution evolution : repository.findAllEvolutions()) {
            nextEvolutionId = Math.max(nextEvolutionId, evolution.getId() + 1);
        }

        for (EvolutionCondition condition : repository.findAllEvolutionConditions()) {
            nextEvolutionConditionId = Math.max(nextEvolutionConditionId, condition.getId() + 1);
        }
    }

    private void seedNaturesIfEmpty() {
        if (!repository.findAllNatures().isEmpty()) return;

        for (int i = 0; i < NATURES.length; i++) {
            repository.createNature(new Nature(i + 1, NATURES[i][0], NATURES[i][1], NATURES[i][2]));
        }
    }

    private void importOneForm(Map<String, Object> entry) {

        String speciesName = SimpleJsonParser.asString(entry.get("name"));
        String formName = SimpleJsonParser.asString(entry.get("form"));
        if (formName == null || formName.isBlank()) {
            formName = "Normal";
        }

        int speciesId = getOrCreateSpecies(speciesName);

        List<Object> types = SimpleJsonParser.asList(entry.get("types"));
        String type1 = types.size() > 0 ? SimpleJsonParser.asString(types.get(0)) : "Normal";
        String type2 = types.size() > 1 ? SimpleJsonParser.asString(types.get(1)) : null;

        Map<String, Object> attributes = SimpleJsonParser.asMap(entry.get("attributes"));

       
        int cryId = nextFormId;
        int formId = nextFormId++;
        firstFormIdBySpecies.putIfAbsent(speciesId, formId);

        PokemonForm form = new PokemonForm(
            formId,
            speciesId,
            formName,
            type1,
            type2,
            SimpleJsonParser.asInt(attributes.get("hp")),
            SimpleJsonParser.asInt(attributes.get("attack")),
            SimpleJsonParser.asInt(attributes.get("defense")),
            SimpleJsonParser.asInt(attributes.get("specialAttack")),
            SimpleJsonParser.asInt(attributes.get("specialDefense")),
            SimpleJsonParser.asInt(attributes.get("speed")),
            cryId
        );
        repository.createPokemonForm(form);

        importAbilities(formId, SimpleJsonParser.asMap(entry.get("abilities")));
        importCapabilities(formId, SimpleJsonParser.asList(entry.get("capabilities")));
        importSkills(formId, SimpleJsonParser.asList(entry.get("skills")));
    }


private void importAbilities(int formId, Map<String, Object> abilities) {
    if (abilities == null) {
        return;
    }

    Set<String> importedNames = new LinkedHashSet<>();

    for (Object name : SimpleJsonParser.asList(abilities.get("basic"))) {
        String abilityName = SimpleJsonParser.asString(name);

        if (abilityName != null && importedNames.add(abilityName)) {
            linkAbility(formId, abilityName, false);
        }
    }

    for (Object name : SimpleJsonParser.asList(abilities.get("advanced"))) {
        String abilityName = SimpleJsonParser.asString(name);

        if (abilityName != null && importedNames.add(abilityName)) {
            linkAbility(formId, abilityName, false);
        }
    }

    for (Object name : SimpleJsonParser.asList(abilities.get("high"))) {
        String abilityName = SimpleJsonParser.asString(name);

        if (abilityName != null && importedNames.add(abilityName)) {
            linkAbility(formId, abilityName, false);
        }
    }
}


    private void linkAbility(int formId, String abilityName, boolean isHidden) {
        int abilityId = getOrCreateAbility(abilityName);
        repository.createFormAbility(new FormAbility(formId, abilityId, isHidden));
    }


private void importCapabilities(int formId, List<Object> capabilities) {
    Set<String> importedNames = new LinkedHashSet<>();

    for (Object rawCap : capabilities) {
        Map<String, Object> cap = SimpleJsonParser.asMap(rawCap);
        String name = SimpleJsonParser.asString(cap.get("name"));

        if (name == null || !importedNames.add(name)) {
            continue;
        }

        String value = SimpleJsonParser.asString(cap.get("value"));

        if (value == null) {
            value = "";
        }

        int capabilityId = getOrCreateCapability(name);

        repository.createFormCapability(
            new FormCapability(formId, capabilityId, value)
        );
    }
}


    private void importSkills(int formId, List<Object> skills) {
        for (Object rawSkill : skills) {
            Map<String, Object> skill = SimpleJsonParser.asMap(rawSkill);
            String name = SimpleJsonParser.asString(skill.get("name"));
            
            int skillId = getOrCreateSkill(name);
            repository.createFormSkill(new FormSkill(formId, skillId));
        }
    }

    //============GET-OR-CREATE HELPERS=============

    private int getOrCreateSpecies(String name) {
        Integer existing = speciesIdsByName.get(name);
        if (existing != null) return existing;

        int id = nextSpeciesId++;
        repository.createPokemonSpecies(new PokemonSpecies(id, name));
        speciesIdsByName.put(name, id);
        return id;
    }

    private int getOrCreateAbility(String name) {
        Integer existing = abilityIdsByName.get(name);
        if (existing != null) return existing;

        int id = nextAbilityId++;
   
        repository.createAbility(new Ability(id, name, "(descrição pendente)"));
        abilityIdsByName.put(name, id);
        return id;
    }

    private int getOrCreateCapability(String name) {
        Integer existing = capabilityIdsByName.get(name);
        if (existing != null) return existing;

        int id = nextCapabilityId++;
        repository.createCapability(new Capability(id, name));
        capabilityIdsByName.put(name, id);
        return id;
    }

    private int getOrCreateSkill(String name) {
        Integer existing = skillIdsByName.get(name);
        if (existing != null) return existing;

        int id = nextSkillId++;
        repository.createSkill(new Skill(id, name));
        skillIdsByName.put(name, id);
        return id;
    }

   
    
    
    
    
    
}