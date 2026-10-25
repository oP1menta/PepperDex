package Server.Trainer;

public class Player {

    private int id;
    private String name;

    public Player(
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

