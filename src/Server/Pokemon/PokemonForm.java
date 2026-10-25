package Server.Pokemon;

public class PokemonForm {

    private int id;
    private int speciesId;
    private String name;
    private String type1;
    private String type2;
    private int baseHp;
    private int baseAttack;
    private int baseDefense;
    private int baseSpAttack;
    private int baseSpDefense;
    private int baseSpeed;
    private int cryId;

    public PokemonForm(
        int id,
        int speciesId,
        String name,
        String type1,
        String type2,
        int baseHp,
        int baseAttack,
        int baseDefense,
        int baseSpAttack,
        int baseSpDefense,
        int baseSpeed,
        int cryId
    ) {

        setId(id);
        setSpeciesId(speciesId);
        setName(name);
        setType1(type1);
        setType2(type2);
        setBaseHp(baseHp);
        setBaseAttack(baseAttack);
        setBaseDefense(baseDefense);
        setBaseSpAttack(baseSpAttack);
        setBaseSpDefense(baseSpDefense);
        setBaseSpeed(baseSpeed);
        setCryId(cryId);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getSpeciesId() {
        return speciesId;
    }

    public String getName() {
        return name;
    }

    public String getType1() {
        return type1;
    }

    public String getType2() {
        return type2;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public int getBaseSpAttack() {
        return baseSpAttack;
    }

    public int getBaseSpDefense() {
        return baseSpDefense;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }
    
    public int getCryId() {
        return cryId;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setSpeciesId(int speciesId) {
        this.speciesId = speciesId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType1(String type1) {
        this.type1 = type1;
    }

    public void setType2(String type2) {
        this.type2 = type2;
    }

    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
    }

    public void setBaseAttack(int baseAttack) {
        this.baseAttack = baseAttack;
    }

    public void setBaseDefense(int baseDefense) {
        this.baseDefense = baseDefense;
    }

    public void setBaseSpAttack(int baseSpAttack) {
        this.baseSpAttack = baseSpAttack;
    }

    public void setBaseSpDefense(int baseSpDefense) {
        this.baseSpDefense = baseSpDefense;
    }

    public void setBaseSpeed(int baseSpeed) {
        this.baseSpeed = baseSpeed;
    }
   
    public void setCryId(int cryId) {
        this.cryId = cryId;
    }

}