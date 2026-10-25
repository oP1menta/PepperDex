package Server.Pokemon;

import java.time.LocalDateTime;

public class Pokemon {

    private int id;
    private Integer trainerId; //can be 0  0 = without trainer
    private int speciesId;
    private int formId;
    private String nickname; //name given from trainer
    private String gender;
    private boolean shiny; //true or false
    private int natureId; //get nature
    private int abilityId; //get ability
    private int level;
    private int xp;
    private float hp;
    private int undistributedAttributePoints;
    private int heldItemId; //0 = none
    private int partySlot; //where he is in the party; 0 = PC
    private int pcBoxId; //What box he is in the PC; 0 = Party 
    private int pcSlot; //what slot he is in the box; 0 = Party
    private LocalDateTime capturedAt;
    
    public Pokemon(
    		
        int id,
        Integer trainerId,
        int speciesId,
        int formId,
        String nickname,
        String gender,
        boolean shiny,
        int natureId,
        int abilityId,
        int level,
        int xp,
        float hp,
        int undistributedAttributePoints,
        int heldItemId,
        int partySlot,
        int pcBoxId,
        int pcSlot,
        LocalDateTime capturedAt
    ) {

        setId(id);
        setTrainerId(trainerId);
        setSpeciesId(speciesId);
        setFormId(formId);
        setNickname(nickname);
        setGender(gender);
        setShiny(shiny);
        setNatureId(natureId);
        setAbilityId(abilityId);
        setLevel(level);
        setXp(xp);
        setHp(hp);
        setUndistributedAttributePoints(undistributedAttributePoints);
        setHeldItemId(heldItemId);
        setPartySlot(partySlot);
        setPcBoxId(pcBoxId);
        setPcSlot(pcSlot);
        setCapturedAt(capturedAt);
        
    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public Integer getTrainerId() {
        return trainerId;
    }

    public int getSpeciesId() {
        return speciesId;
    }
    
    public int getFormId() {
        return formId;
    }

    public String getNickname() {
        return nickname;
    }
    
    public String getGender() {
        return gender;
    }
    
    public boolean isShiny() {
        return shiny;
    }
    
    public int getNatureId() {
        return natureId;
    }
    
    public int getAbilityId() {
        return abilityId;
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getXp() {
        return xp;
    }
    
    public float getHp() {
    	return hp;
    }
    
    public int getUndistributedAttributePoints() {
        return undistributedAttributePoints;
    }
    
    public int getHeldItemId() {
        return heldItemId;
    }
    
    public int getPartySlot() {
        return partySlot;
    }
    
    public int getPcBoxId() {
        return pcBoxId;
    }
    
    public int getPcSlot() {
        return pcSlot;
    }
    
    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }
    
    
    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setTrainerId(Integer trainerId) {
        this.trainerId = trainerId;
    }

    public void setSpeciesId(int speciesId) {
        this.speciesId = speciesId;
    }
    
    public void setFormId(int formId) {
        this.formId = formId;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public void setShiny(boolean shiny) {
        this.shiny = shiny;
    }
    
    public void setNatureId(int natureId) {
        this.natureId = natureId;
    }
    
    public void setAbilityId(int abilityId) {
        this.abilityId = abilityId;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public void setXp(int xp) {
        this.xp = xp;
    }
    
    public void setHp(float hp) {
    	this.hp = hp;
    }
    
    public void setUndistributedAttributePoints(int undistributedAttributePoints) {
        this.undistributedAttributePoints = undistributedAttributePoints;
    }
    
    public void setHeldItemId(int heldItemId) {
        this.heldItemId = heldItemId;
    }
    
    public void setPartySlot(int partySlot) {
        this.partySlot = partySlot;
    }
    
    public void setPcBoxId(int pcBoxId) {
        this.pcBoxId = pcBoxId;
    }
    
    public void setPcSlot(int pcSlot) {
        this.pcSlot = pcSlot;
    }
    
    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }
    
}
