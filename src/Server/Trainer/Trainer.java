package Server.Trainer;

public class Trainer {

    private int id;
    private String name;
    private int level;
    private int hp;
    private int money;
    private int playerId;
    
    public Trainer(
        int id,
        String name,
        int level,
        int hp,
        int money,
        int playerId
    ) {

        setId(id);
        setName(name);
        setLevel(level);
        setHp(hp);
        setMoney(money);
        setPlayerId(playerId);
    }

    //============GETTERS=============

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getHp() {
        return hp;
    }

    public int getMoney() {
        return money;
    }
    
    public int getPlayerId() {
        return playerId;
    }

    //============SETTERS=============

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setMoney(int money) {
        this.money = money;
    }
    
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

}
