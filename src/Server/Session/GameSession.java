package Server.Session;

public class GameSession {

    private int id;
    private int currentDay;
    private int hostPlayerId;
    private String joinCode;
    
    public GameSession(
    	    int id,
    	    int hostPlayerId,
    	    int currentDay,
    	    String joinCode
    ){

        setId(id);
        setHostPlayerId(hostPlayerId);
        setCurrentDay(currentDay);
        setJoinCode(joinCode);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }
    
    public int getHostPlayerId() {
        return hostPlayerId;
    }
    
    public int getCurrentDay() {
        return currentDay;
    }

    public String getJoinCode() {
        return joinCode;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }
    
    public void setHostPlayerId(int hostPlayerId) {
        this.hostPlayerId = hostPlayerId;
    }
    
    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

}