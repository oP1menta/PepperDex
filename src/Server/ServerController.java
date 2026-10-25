package Server;

//======= IMPORTS =======

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import Server.Services.PokemonServices;

public class ServerController {

    private ServerSocket serverSocket;
    private int port;
    private PokemonServices pokemonServices;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public ServerController(int port, PokemonServices pokemonServices) {
        setPort(port);
        setPokemonServices(pokemonServices);
    }

    //============GETTERS=============

    public int getPort() {
        return port;
    }

    public PokemonServices getPokemonServices() {
        return pokemonServices;
    }

    //============SETTERS=============

    public void setPort(int port) {
        this.port = port;
    }

    public void setPokemonServices(PokemonServices pokemonServices) {
        this.pokemonServices = pokemonServices;
    }

    //============CLIENTS=============

    // Sends one line to every connected client (Master and Players)
    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    //============SERVER=============

    public void startServer() {

        try {

            serverSocket = new ServerSocket(port);

            System.out.println(
                "Server started on port " + serverSocket.getLocalPort()
            );

            while (true) {

                System.out.println("Waiting for client...");

                Socket client = serverSocket.accept();

                System.out.println("Client connected!");

                ClientHandler handler = new ClientHandler(client, this);

                clients.add(handler);

                Thread thread = new Thread(handler);

                thread.start();

            }

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

}
