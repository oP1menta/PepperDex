package Server.Pokemon;

public class PokemonCapability {

    private int pokemonId;
    private int capabilityId;
    private int modifier;

    public PokemonCapability(
        int pokemonId,
        int capabilityId,
        int modifier
    ) {

        setPokemonId(pokemonId);
        setCapabilityId(capabilityId);
        setModifier(modifier);

    }

    //============GETTERS=============

    public int getPokemonId() {
        return pokemonId;
    }

    public int getCapabilityId() {
        return capabilityId;
    }

    public int getModifier() {
        return modifier;
    }

    //============SETTERS=============

    public void setPokemonId(int pokemonId) {
        this.pokemonId = pokemonId;
    }

    public void setCapabilityId(int capabilityId) {
        this.capabilityId = capabilityId;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

}