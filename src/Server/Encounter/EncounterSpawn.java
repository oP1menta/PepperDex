package Server.Encounter;

public class EncounterSpawn {

    private int id;
    private int encounterPokemonId;
    private int level;
    private String gender;
    private boolean shiny;
    private int natureId;
    private int abilityId;

    public EncounterSpawn(
        int id,
        int encounterPokemonId,
        int level,
        String gender,
        boolean shiny,
        int natureId,
        int abilityId
    ) {

        setId(id);
        setEncounterPokemonId(encounterPokemonId);
        setLevel(level);
        setGender(gender);
        setShiny(shiny);
        setNatureId(natureId);
        setAbilityId(abilityId);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getEncounterPokemonId() {
        return encounterPokemonId;
    }

    public int getLevel() {
        return level;
    }

    public String getGender() {
        return gender;
    }

    public boolean isShiny() {
        return shiny;
    }

    public int getNatureId() {
        return natureId;
    }

    public int getAbilityId() {
        return abilityId;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setEncounterPokemonId(int encounterPokemonId) {
        this.encounterPokemonId = encounterPokemonId;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setShiny(boolean shiny) {
        this.shiny = shiny;
    }

    public void setNatureId(int natureId) {
        this.natureId = natureId;
    }

    public void setAbilityId(int abilityId) {
        this.abilityId = abilityId;
    }

}