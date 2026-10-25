package Server.Encounter;

public class EncounterPokemon {

    private int id;
    private int encounterId;
    private int pokemonId;
    private String visibility; // shared or individual
    private int assignedParticipantId; // 0 = all = shared

    public EncounterPokemon(
        int id,
        int encounterId,
        int pokemonId,
        String visibility,
        int assignedParticipantId
    ) {

        setId(id);
        setEncounterId(encounterId);
        setPokemonId(pokemonId);
        setVisibility(visibility);
        setAssignedParticipantId(assignedParticipantId);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getEncounterId() {
        return encounterId;
    }

    public int getPokemonId() {
        return pokemonId;
    }

    public String getVisibility() {
        return visibility;
    }

    public int getAssignedParticipantId() {
        return assignedParticipantId;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setEncounterId(int encounterId) {
        this.encounterId = encounterId;
    }

    public void setPokemonId(int pokemonId) {
        this.pokemonId = pokemonId;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public void setAssignedParticipantId(int assignedParticipantId) {
        this.assignedParticipantId = assignedParticipantId;
    }

}