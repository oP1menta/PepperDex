package Server.Injury;

import java.time.LocalDateTime;

public class Injury {

    private int id;
    private LocalDateTime createdAt;
    private String ownerType; //Pokemon or Trainer
    private int ownerId;
    private LocalDateTime removedAt;
    
    public Injury(
    	    int id,
    	    LocalDateTime createdAt,
    	    String ownerType,
    	    int ownerId,
    	    LocalDateTime removedAt
    	    
    	) {

    	    setId(id);
    	    setCreatedAt(createdAt);
    	    setOwnerType(ownerType);
    	    setOwnerId(ownerId);
    	    setRemovedAt(removedAt);
    	    
    	}

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public String getOwnerType() {
        return ownerType;
    }
    
    public int getOwnerId() {
        return ownerId;
    }
    
    public LocalDateTime getRemovedAt() {
        return removedAt;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }
    
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
    
    public void setRemovedAt(LocalDateTime removedAt) {
        this.removedAt = removedAt;
    }
    
}