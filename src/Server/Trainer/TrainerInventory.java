package Server.Trainer;

public class TrainerInventory {

    private int trainerId;
    private int itemId;
    private int quantity;

    public TrainerInventory(
        int trainerId,
        int itemId,
        int quantity
    ) {

        setTrainerId(trainerId);
        setItemId(itemId);
        setQuantity(quantity);

    }

    //============GETTERS=============

    public int getTrainerId() {
        return trainerId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    //============SETTERS=============

    public void setTrainerId(int trainerId) {
        this.trainerId = trainerId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}