package Server.Encounter;

public class AreaPool {

    private int id;
    private int areaId;
    private int speciesId;
    private double probability;

    public AreaPool(
        int id,
        int areaId,
        int speciesId,
        double probability
    ) {

        setId(id);
        setAreaId(areaId);
        setSpeciesId(speciesId);
        setProbability(probability);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getAreaId() {
        return areaId;
    }

    public int getSpeciesId() {
        return speciesId;
    }

    public double getProbability() {
        return probability;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public void setSpeciesId(int speciesId) {
        this.speciesId = speciesId;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

}