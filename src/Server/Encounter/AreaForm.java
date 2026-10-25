package Server.Encounter;

// One Pokemon form that can appear in an area; weight is relative (2 appears twice as often as 1)
public class AreaForm {

    private int id;
    private int areaId;
    private int formId;
    private double weight;

    public AreaForm(
        int id,
        int areaId,
        int formId,
        double weight
    ) {

        setId(id);
        setAreaId(areaId);
        setFormId(formId);
        setWeight(weight);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getAreaId() {
        return areaId;
    }

    public int getFormId() {
        return formId;
    }

    public double getWeight() {
        return weight;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

}
