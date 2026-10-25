package Server;

import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import Server.Pokemon.Pokemon;

public class ClientHandler implements Runnable {

    private Socket client;
    private ServerController controller;
    private PrintWriter output;

    public ClientHandler(Socket client, ServerController controller) {
        setClient(client);
        setController(controller);
    }

    //============GETTERS=============

    public Socket getClient() {
        return client;
    }

    public ServerController getController() {
        return controller;
    }

    //============SETTERS=============

    public void setClient(Socket client) {
        this.client = client;
    }

    public void setController(ServerController controller) {
        this.controller = controller;
    }

    //============SENDING=============

    public synchronized void send(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    //============RUN=============

    @Override
    public void run() {

        try {

            BufferedReader input = new BufferedReader(
                new InputStreamReader(client.getInputStream())
            );

            output = new PrintWriter(
                client.getOutputStream(),
                true
            );

            while (true) {

                String message = input.readLine();

                if (message == null) {

                    System.out.println("Client disconnected.");
                    break;
                }

                System.out.println("Message received: " + message);

                handleCommand(message);
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            controller.removeClient(this);

        }

    }

    //============COMMANDS=============

    // Protocol (one command per line):
    //   SPAWN <formId> <level>  -> creates a wild Pokemon and broadcasts it
    // ponytail: no Master/Player check yet, any client can SPAWN; add roles with the session system
    private void handleCommand(String message) {

        String[] parts = message.trim().split("\\s+");

        if (parts[0].equals("SPAWN") && parts.length == 3) {
            handleSpawn(parts[1], parts[2]);
            return;
        }

        send("ERROR unknown command: " + message);
    }

    private void handleSpawn(String formText, String levelText) {

        int formId;
        int level;

        try {
            formId = Integer.parseInt(formText);
            level = Integer.parseInt(levelText);
        } catch (NumberFormatException e) {
            send("ERROR usage: SPAWN <formId> <level>");
            return;
        }

        if (level < 1 || level > 100) {
            send("ERROR level must be between 1 and 100");
            return;
        }

        Pokemon pokemon = controller.getPokemonServices().spawnPokemon(formId, level);

        if (pokemon == null) {
            send("ERROR form not found: " + formId);
            return;
        }

        controller.broadcast(controller.getPokemonServices().describePokemon(pokemon));
    }

}
