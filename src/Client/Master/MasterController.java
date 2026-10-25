package Client.Master;

import Client.ClientConfiguration;
import Client.ClientController;

public class MasterController extends ClientController {

private MasterModel model;
	
    public MasterController(
        String serverAddress,
        int port,
        ClientConfiguration configuration
    ) {

        super(
            serverAddress,
            port,
            configuration
        );
        
        setModel(new MasterModel());
        setView(new MasterView());

    }
    
    //=========SETTERS==============
    public void setModel(MasterModel model) {
        this.model = model;
    }
    
    
    
    //==========GETTERS==============
    public MasterModel getModel() {
        return model;
    }
    
    
    //===========METHODS============

    // Console for the Master: every typed line goes to the server (e.g. "SPAWN 1 5")
    public void runConsole() {

        java.util.Scanner scanner = new java.util.Scanner(System.in);

        while (scanner.hasNextLine()) {
            getOutput().println(scanner.nextLine());
        }
    }

}