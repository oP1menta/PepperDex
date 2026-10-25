package Server.Master;

public class Master {

    private int id;
    private int playerId;

    public Master(
        int id,
        int playerId
    ) {

        setId(id);
        setPlayerId(playerId);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getPlayerId() {
        return playerId;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

}