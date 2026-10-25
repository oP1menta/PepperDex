package Server.PC;

public class PCBox {

    private int id;
    private int pcId;
    private String name;
    private int boxNumber;
    private int slotLimit;

    public PCBox(
        int id,
        int pcId,
        String name,
        int boxNumber,
        int slotLimit
    ) {

        setId(id);
        setPcId(pcId);
        setName(name);
        setBoxNumber(boxNumber);
        setSlotLimit(slotLimit);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getPcId() {
        return pcId;
    }

    public String getName() {
        return name;
    }

    public int getBoxNumber() {
        return boxNumber;
    }

    public int getSlotLimit() {
        return slotLimit;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setPcId(int pcId) {
        this.pcId = pcId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBoxNumber(int boxNumber) {
        this.boxNumber = boxNumber;
    }

    public void setSlotLimit(int slotLimit) {
        this.slotLimit = slotLimit;
    }

}