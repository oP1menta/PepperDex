package Server.Evolution;

public class EvolutionCondition {

    private int id;
    private int evolutionId;
    private String conditionType;
    private String value;
    
    public EvolutionCondition(
        int id,
        int evolutionId,
        String conditionType,
        String value
    ) {

        setId(id);
        setEvolutionId(evolutionId);
        setConditionType(conditionType);
        setValue(value);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getEvolutionId() {
        return evolutionId;
    }

    public String getConditionType() {
        return conditionType;
    }

    public String getValue() {
        return value;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setEvolutionId(int evolutionId) {
        this.evolutionId = evolutionId;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public void setValue(String value) {
        this.value = value;
    }

}