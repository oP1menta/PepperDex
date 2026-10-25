package Client.Player;

import java.util.Locale;

import Client.ClientConfiguration;

public class Player {

    public static void main(String[] args) {

        ClientConfiguration configuration =
            new ClientConfiguration(
                Locale.of("pt", "BR")
            );

        PlayerController controller =
            new PlayerController(
                "localhost",
                5010,
                configuration
            );

        controller.connectServer();
    }
}