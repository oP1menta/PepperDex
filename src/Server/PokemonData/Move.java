package Server.PokemonData;

public class Move {

    private int id;
    private String name;
    private String type;
    private String frequency;
    private int accuracy;
    private String moveClass;
    private String range;
    private String damage;
    private String description;

    public Move(
        int id,
        String name,
        String type,
        String frequency,
        int accuracy,
        String moveClass,
        String range,
        String damage,
        String description
    ) {

        setId(id);
        setName(name);
        setType(type);
        setFrequency(frequency);
        setAccuracy(accuracy);
        setMoveClass(moveClass);
        setRange(range);
        setDamage(damage);
        setDescription(description);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getFrequency() {
        return frequency;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public String getMoveClass() {
        return moveClass;
    }

    public String getRange() {
        return range;
    }

    public String getDamage() {
        return damage;
    }

    public String getDescription() {
        return description;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public void setMoveClass(String moveClass) {
        this.moveClass = moveClass;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public void setDamage(String damage) {
        this.damage = damage;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}