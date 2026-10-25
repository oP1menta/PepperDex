package Server;

import java.io.IOException;

import Server.Database.Database;
import Server.Database.DatabaseSchema;
import Server.Repository.Repository;
import Server.Services.CaptureServices;
import Server.Services.PokemonServices;
import Server.Services.SessionServices;
import Server.Services.TunnelServices;
import Server.Util.Browser;
import Server.Web.WebServer;

public class Server {

    public static void main(String[] args) {

        Database database = new Database();

        DatabaseSchema schema = new DatabaseSchema(database);

        schema.createSchema();

        
        PokemonServices pokemonServices = importPokemonDataIfNeeded(database);
        pokemonServices.rebuildEvolutions("pokemon.json");

        SessionServices sessionServices = new SessionServices(new Repository(database));

        // The packaged launchers start with -Dptu.release=true: open the Master page and put the table on the internet
        boolean release = Boolean.getBoolean("ptu.release");
        try {
            WebServer web = new WebServer(8080, sessionServices, pokemonServices, new CaptureServices(new Repository(database), pokemonServices));
            TunnelServices tunnel = new TunnelServices(8080);
            web.setTunnelServices(tunnel);
            web.start();
            if (release) {
                tunnel.start();
                Browser.open("http://localhost:8080/master");
            }
        } catch (IOException e) {
            System.out.println("Could not start the web server: " + e.getMessage());
        }

        ServerController controller = new ServerController(5004, pokemonServices);

        controller.startServer();

    }

    private static PokemonServices importPokemonDataIfNeeded(Database database) {

        Repository repository = new Repository(database);

        PokemonServices pokemonServices = new PokemonServices(repository);

        // Already imported on a previous run: don't duplicate the forms
        if (!repository.findAllPokemonForms().isEmpty()) return pokemonServices;

        try {
            int total = pokemonServices.importPokemonJson("pokemon.json");
            System.out.println("Importados " + total + " forms de pokemon.json!");
        } catch (IOException e) {
            System.out.println(
                "Não consegui ler pokemon.json (ele precisa estar na " +
                "raiz do projeto, do lado do pokemon.db). Detalhe: " +
                e.getMessage()
            );
        }

        return pokemonServices;

    }

}