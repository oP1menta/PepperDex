package Server.PC;

public class PC {

    private int id;
    private int trainerId;

    public PC(
        int id,
        int trainerId
    ) {

        setId(id);
        setTrainerId(trainerId);

    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public int getTrainerId() {
        return trainerId;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setTrainerId(int trainerId) {
        this.trainerId = trainerId;
    }

}