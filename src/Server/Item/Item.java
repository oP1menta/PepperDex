package Server.Item;

public class Item {

    private int id;
    private String name;
    private String description;
    private String category;
    private String effect;

    public Item(
        int id,
        String name,
        String description,
        String category,
        String effect
    ) {

        setId(id);
        setName(name);
        setDescription(description);
        setCategory(category);
        setEffect(effect);

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

    public String getCategory() {
        return category;
    }

    public String getEffect() {
        return effect;
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

    public void setCategory(String category) {
        this.category = category;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

}