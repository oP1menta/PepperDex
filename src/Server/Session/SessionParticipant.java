package Server.Session;

public class SessionParticipant {

    private int id;
    private int gameSessionId;
    private int playerId;
    private String type;
    private int participantId;
    private String token;  // secret the player's browser keeps; null for the Master

    public SessionParticipant(
        int id,
        int gameSessionId,
        int playerId,
        String type,
        int participantId,
        String token
    ) {

        setId(id);
        setGameSessionId(gameSessionId);
        setPlayerId(playerId);
        setType(type);
        setParticipantId(participantId);
        setToken(token);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getGameSessionId() {
        return gameSessionId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getType() {
        return type;
    }

    public int getParticipantId() {
        return participantId;
    }

    public String getToken() {
        return token;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setGameSessionId(int gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
