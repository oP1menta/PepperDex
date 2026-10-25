package Server.Trade;

public class Trade {

    private int id;
    private int gameSessionId;
    private int initiatorParticipantId;//who started the trade?
    private int receiverParticipantId;//who accepting?
    private boolean initiatorConfirmed;
    private boolean receiverConfirmed;
    private String status;

    public Trade(
        int id,
        int gameSessionId,
        int initiatorParticipantId,
        int receiverParticipantId,
        boolean initiatorConfirmed,
        boolean receiverConfirmed,
        String status
    ) {

        setId(id);
        setGameSessionId(gameSessionId);
        setInitiatorParticipantId(initiatorParticipantId);
        setReceiverParticipantId(receiverParticipantId);
        setInitiatorConfirmed(initiatorConfirmed);
        setReceiverConfirmed(receiverConfirmed);
        setStatus(status);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getGameSessionId() {
        return gameSessionId;
    }

    public int getInitiatorParticipantId() {
        return initiatorParticipantId;
    }

    public int getReceiverParticipantId() {
        return receiverParticipantId;
    }

    public boolean isInitiatorConfirmed() {
        return initiatorConfirmed;
    }

    public boolean isReceiverConfirmed() {
        return receiverConfirmed;
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

    public void setInitiatorParticipantId(int initiatorParticipantId) {
        this.initiatorParticipantId = initiatorParticipantId;
    }

    public void setReceiverParticipantId(int receiverParticipantId) {
        this.receiverParticipantId = receiverParticipantId;
    }

    public void setInitiatorConfirmed(boolean initiatorConfirmed) {
        this.initiatorConfirmed = initiatorConfirmed;
    }

    public void setReceiverConfirmed(boolean receiverConfirmed) {
        this.receiverConfirmed = receiverConfirmed;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}