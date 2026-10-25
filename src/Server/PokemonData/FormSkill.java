package Server.PokemonData;

public class FormSkill {

    private int formId;
    private int skillId;

    public FormSkill(
        int formId,
        int skillId
    ) {

        setFormId(formId);
        setSkillId(skillId);

    }

    //============GETTERS=============

    public int getFormId() {
        return formId;
    }

    public int getSkillId() {
        return skillId;
    }

    //============SETTERS=============

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

}