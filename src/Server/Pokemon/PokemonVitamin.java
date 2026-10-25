package Server.Pokemon;

public class PokemonVitamin {

    private int pokemonId;
    private int vitaminId;
    private int quantity;

    public PokemonVitamin(
        int pokemonId,
        int vitaminId,
        int quantity
    ) {

        setPokemonId(pokemonId);
        setVitaminId(vitaminId);
        setQuantity(quantity);

    }

    //============GETTERS=============

    public int getPokemonId() {
        return pokemonId;
    }

    public int getVitaminId() {
        return vitaminId;
    }

    public int getQuantity() {
        return quantity;
    }

    //============SETTERS=============

    public void setPokemonId(int pokemonId) {
        this.pokemonId = pokemonId;
    }

    public void setVitaminId(int vitaminId) {
        this.vitaminId = vitaminId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}