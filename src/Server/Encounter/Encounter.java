package Server.Encounter;

public class Encounter {

    private int id;
    private int gameSessionId;
    private String status;

    public Encounter(
        int id,
        int gameSessionId,
        String status
    ) {

        setId(id);
        setGameSessionId(gameSessionId);
        setStatus(status);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getGameSessionId() {
        return gameSessionId;
    }

    public String getStatus() {
        return status;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setGameSessionId(int gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}