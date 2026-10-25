package Server.Encounter;

public class Area {

    private int id;
    private String name;
    private int minLevel;
    private int maxLevel;

    public Area(
        int id,
        String name,
        int minLevel,
        int maxLevel
    ) {

        setId(id);
        setName(name);
        setMinLevel(minLevel);
        setMaxLevel(maxLevel);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

}