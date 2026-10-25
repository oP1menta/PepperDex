package Server.Evolution;

public class Evolution {

    private int id;
    private int fromSpeciesId;
    private int fromFormId;
    private int toSpeciesId;
    private int toFormId;

    public Evolution(
        int id,
        int fromSpeciesId,
        int fromFormId,
        int toSpeciesId,
        int toFormId
    ) {

        setId(id);
        setFromSpeciesId(fromSpeciesId);
        setFromFormId(fromFormId);
        setToSpeciesId(toSpeciesId);
        setToFormId(toFormId);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getFromSpeciesId() {
        return fromSpeciesId;
    }

    public int getFromFormId() {
        return fromFormId;
    }

    public int getToSpeciesId() {
        return toSpeciesId;
    }

    public int getToFormId() {
        return toFormId;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setFromSpeciesId(int fromSpeciesId) {
        this.fromSpeciesId = fromSpeciesId;
    }

    public void setFromFormId(int fromFormId) {
        this.fromFormId = fromFormId;
    }

    public void setToSpeciesId(int toSpeciesId) {
        this.toSpeciesId = toSpeciesId;
    }

    public void setToFormId(int toFormId) {
        this.toFormId = toFormId;
    }

}