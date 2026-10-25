package Server.Trainer;

// A character made in advance by a person (owner = his private key), outside any session.
// Placing it in a session makes an independent Trainer copy there.
public class CharacterSheet {

    private final int id;
    private final String owner;
    private final String name;

    public CharacterSheet(int id, String owner, String name) {
        this.id = id;
        this.owner = owner;
        this.name = name;
    }

    public int getId() { return id; }
    public String getOwner() { return owner; }
    public String getName() { return name; }
}
