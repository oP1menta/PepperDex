package Server.Pokemon;

public class PokemonSkill {

    private int pokemonId;
    private int skillId;
    private int modifier;

    public PokemonSkill(
        int pokemonId,
        int skillId,
        int modifier
    ) {

        setPokemonId(pokemonId);
        setSkillId(skillId);
        setModifier(modifier);

    }

    //============GETTERS=============

    public int getPokemonId() {
        return pokemonId;
    }

    public int getSkillId() {
        return skillId;
    }

    public int getModifier() {
        return modifier;
    }

    //============SETTERS=============

    public void setPokemonId(int pokemonId) {
        this.pokemonId = pokemonId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

}
