package Server.Trade;

public class TradeOffer {

    private int id;
    private int tradeId;
    private int participantId;
    private int pokemonId;
    private int itemId;
    private int quantity;

    public TradeOffer(
        int id,
        int tradeId,
        int participantId,
        int pokemonId,
        int itemId,
        int quantity
    ) {

        setId(id);
        setTradeId(tradeId);
        setParticipantId(participantId);
        setPokemonId(pokemonId);
        setItemId(itemId);
        setQuantity(quantity);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getTradeId() {
        return tradeId;
    }

    public int getParticipantId() {
        return participantId;
    }

    public int getPokemonId() {
        return pokemonId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setTradeId(int tradeId) {
        this.tradeId = tradeId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public void setPokemonId(int pokemonId) {
        this.pokemonId = pokemonId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}