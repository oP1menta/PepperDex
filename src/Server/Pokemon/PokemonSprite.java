package Server.Pokemon;

public class PokemonSprite {

    private int id;
    private int formId;
    private String normalSprite;
    private String shinySprite;
    
    public PokemonSprite(
        int id,
        int formId,
        String normalSprite,
        String shinySprite
    ) {

        setId(id);
        setFormId(formId);
        setNormalSprite(normalSprite);
        setShinySprite(shinySprite);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getFormId() {
        return formId;
    }

    public String getNormalSprite() {
        return normalSprite;
    }

    public String getShinySprite() {
        return shinySprite;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public void setNormalSprite(String normalSprite) {
        this.normalSprite = normalSprite;
    }

    public void setShinySprite(String shinySprite) {
        this.shinySprite = shinySprite;
    }

}