package Server.PokemonData;

public class FormCapability {

    private int formId;
    private int capabilityId;
    private String value;

    public FormCapability(
        int formId,
        int capabilityId,
        String value
    ) {

        setFormId(formId);
        setCapabilityId(capabilityId);
        setValue(value);

    }

    //============GETTERS=============

    public int getFormId() {
        return formId;
    }

    public int getCapabilityId() {
        return capabilityId;
    }

    public String getValue() {
        return value;
    }

    //============SETTERS=============

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public void setCapabilityId(int capabilityId) {
        this.capabilityId = capabilityId;
    }

    public void setValue(String value) {
        this.value = value;
    }

}