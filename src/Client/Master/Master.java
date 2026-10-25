package Client.Master;

import java.util.Locale;

import Client.ClientConfiguration;

public class Master {

    public static void main(String[] args) {

        ClientConfiguration configuration =
            new ClientConfiguration(
                Locale.of("pt", "BR")
            );

        MasterController controller =
            new MasterController(
                "localhost",
                5010,
                configuration
            );

        controller.connectServer();
        controller.runConsole();

    }
}