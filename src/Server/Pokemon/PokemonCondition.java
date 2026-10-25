package Server.Pokemon;

public class PokemonCondition {

    private int id;
    private int pokemonId;
    private String condition;

    public PokemonCondition(
        int id,
        int pokemonId,
        String condition
    ) {

        setId(id);
        setPokemonId(pokemonId);
        setCondition(condition);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getPokemonId() {
        return pokemonId;
    }

    public String getCondition() {
        return condition;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setPokemonId(int pokemonId) {
        this.pokemonId = pokemonId;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

}