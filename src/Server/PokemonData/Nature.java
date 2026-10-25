package Server.PokemonData;

public class Nature {

    private int id;
    private String name;
    private String increasedAttribute;
    private String decreasedAttribute;

    public Nature(
        int id,
        String name,
        String increasedAttribute,
        String decreasedAttribute
    ) {

        setId(id);
        setName(name);
        setIncreasedAttribute(increasedAttribute);
        setDecreasedAttribute(decreasedAttribute);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIncreasedAttribute() {
        return increasedAttribute;
    }

    public String getDecreasedAttribute() {
        return decreasedAttribute;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIncreasedAttribute(String increasedAttribute) {
        this.increasedAttribute = increasedAttribute;
    }

    public void setDecreasedAttribute(String decreasedAttribute) {
        this.decreasedAttribute = decreasedAttribute;
    }

}