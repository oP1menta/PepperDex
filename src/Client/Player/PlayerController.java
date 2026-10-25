package Client.Player;

import Client.ClientConfiguration;
import Client.ClientController;

public class PlayerController extends ClientController {

    private PlayerModel model;

    public PlayerController(
        String serverAddress,
        int port,
        ClientConfiguration configuration
    ) {

        super(
            serverAddress,
            port,
            configuration
        );

        setModel(new PlayerModel());
        setView(new PlayerView());

    }

    //=========SETTERS==============

    public void setModel(PlayerModel model) {

        this.model = model;

    }

    //==========GETTERS==============

    public PlayerModel getModel() {

        return model;

    }
    
    //==========METHODS=============


}