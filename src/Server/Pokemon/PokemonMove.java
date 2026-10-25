package Server.Pokemon;

public class PokemonMove {

    private int pokemonId;
    private int moveId;

    public PokemonMove(
        int pokemonId,
        int moveId
    ) {

        setPokemonId(pokemonId);
        setMoveId(moveId);
    }

    //============GETTERS=============

    public int getPokemonId() {
        return pokemonId;
    }

    public int getMoveId() {
        return moveId;
    }

    //============SETTERS=============

    public void setPokemonId(int pokemonId) {
        this.pokemonId = pokemonId;
    }

    public void setMoveId(int moveId) {
        this.moveId = moveId;
    }

}