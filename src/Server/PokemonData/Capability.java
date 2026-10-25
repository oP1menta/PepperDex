package Server.PokemonData;

public class Capability {

    private int id;
    private String name;

    public Capability(
        int id,
        String name
    ) {

        setId(id);
        setName(name);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

}