package Client;

//=========IMPORTS============
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import Localization.LanguageManager;

public class ClientController {

    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private ClientView view;
    private String serverAddress;
    private int port;
    private ClientModel model;
    private LanguageManager languageManager;

    public ClientController(
    	    String serverAddress,
    	    int port,
    	    ClientConfiguration configuration
    	) {
        setServerAddress(serverAddress);
        setPort(port);
        setView(new ClientView());
        setModel(new ClientModel());
        setLanguageManager(
        	    new LanguageManager(
        	        configuration.getLocale()
        	    )
        	);
    }

    //=========SETTERS============
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    
    public void setOutput(PrintWriter output) {
        this.output = output;
    }
    
    public void setInput(BufferedReader input) {
        this.input = input;
    }
    
    public void setView(ClientView view) {
        this.view = view;
    }
    
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public void setModel(ClientModel model) {
        this.model = model;
    }
    
    public void setLanguageManager(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }
    
    //==========GETTERS===========
    public Socket getSocket() {
        return socket;
    }
    public PrintWriter getOutput() {
        return output;
    }

    public BufferedReader getInput() {
        return input;
    }

    public ClientView getView() {
        return view;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getPort() {
        return port;
    }

    public ClientModel getModel() {
        return model;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }


    //=========CONNECTION============

    public void connectServer() {

        try {

            setSocket(
                new Socket(getServerAddress(), getPort())
            );

            getModel().setConnected(true);

            showTranslatedMessage("connected");

            setOutput(
                new PrintWriter(
                    getSocket().getOutputStream(),
                    true
                )
            );

            setInput(
                new BufferedReader(
                    new InputStreamReader(
                        getSocket().getInputStream()
                    )
                )
            );

            startListening();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Prints every line the server sends, in the background
    private void startListening() {

        Thread listener = new Thread(() -> {
            try {
                String line;
                while ((line = getInput().readLine()) != null) {
                    showMessage(line);
                }
            } catch (Exception e) {
                showMessage("Disconnected from server.");
            }
        });

        listener.start();
    }

    //=========MESSAGES============

    public void sendMessage(String message) {
    	showTranslatedMessage("sending_message");
    	
        getOutput().println(message);
    }

    public String receiveMessage() {

        try {

        	showTranslatedMessage("waiting_messages");

            return getInput().readLine();

        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    public void showMessage(String message) {
        getView().showMessage(message);
    }
    
    public void showTranslatedMessage(String key) {
        getView().showMessage(
            getLanguageManager().get(key)
        );
    }

    public void showTranslatedMessage(String key, String value) {
        getView().showMessage(
            getLanguageManager().get(key) + " " + value
        );
    }
    
    public void showTranslatedInput(String key) {
        getView().showInput(
            getLanguageManager().get(key) + " "
        );
    }
}