package Server.Pokemon;

public class PokemonAttributePoints {

    private int pokemonId;
    private String attribute;
    private int points;

    public PokemonAttributePoints(
        int pokemonId,
        String attribute,
        int points
    ) {

        setPokemonId(pokemonId);
        setAttribute(attribute);
        setPoints(points);

    }

    //============GETTERS=============

    public int getPokemonId() {
        return pokemonId;
    }

    public String getAttribute() {
        return attribute;
    }

    public int getPoints() {
        return points;
    }

    //============SETTERS=============

    public void setPokemonId(int pokemonId) {
        this.pokemonId = pokemonId;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setPoints(int points) {
        this.points = points;
    }

}