package Server.PokemonData;

public class Ability {

    private int id;
    private String name;
    private String description;

    public Ability(
        int id,
        String name,
        String description
    ) {

        setId(id);
        setName(name);
        setDescription(description);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public void setDescription(String description) {
        this.description = description;
    }

}