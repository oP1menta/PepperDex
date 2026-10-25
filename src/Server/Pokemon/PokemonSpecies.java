package Server.Pokemon;

public class PokemonSpecies {

    private int dexNumber;
    private String name;

    public PokemonSpecies(
        int dexNumber,
        String name
    ) {

        setDexNumber(dexNumber);
        setName(name);

    }

    //============GETTERS=============

    public int getDexNumber() {
        return dexNumber;
    }

    public String getName() {
        return name;
    }

    //============SETTERS=============

    public void setDexNumber(int dexNumber) {
        this.dexNumber = dexNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

}