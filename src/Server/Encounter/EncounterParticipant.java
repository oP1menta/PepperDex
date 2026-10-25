package Server.Encounter;

public class EncounterParticipant {

    private int id;
    private int encounterId;
    private int sessionParticipantId;

    public EncounterParticipant(
        int id,
        int encounterId,
        int sessionParticipantId
    ) {

        setId(id);
        setEncounterId(encounterId);
        setSessionParticipantId(sessionParticipantId);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getEncounterId() {
        return encounterId;
    }

    public int getSessionParticipantId() {
        return sessionParticipantId;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setEncounterId(int encounterId) {
        this.encounterId = encounterId;
    }

    public void setSessionParticipantId(int sessionParticipantId) {
        this.sessionParticipantId = sessionParticipantId;
    }

}