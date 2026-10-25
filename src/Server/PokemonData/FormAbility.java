package Server.PokemonData;

public class FormAbility {

    private int formId;
    private int abilityId;
    private boolean isHidden;

    public FormAbility(
        int formId,
        int abilityId,
        boolean isHidden
    ) {

        setFormId(formId);
        setAbilityId(abilityId);
        setHidden(isHidden);

    }

    //============GETTERS=============

    public int getFormId() {
        return formId;
    }

    public int getAbilityId() {
        return abilityId;
    }

    public boolean isHidden() {
        return isHidden;
    }

    //============SETTERS=============

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public void setAbilityId(int abilityId) {
        this.abilityId = abilityId;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

}