package Server.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private Connection connection;

    public Database() {

        connect();

    }

    private void connect() {

        try {

            setConnection(
                DriverManager.getConnection("jdbc:sqlite:pokemon.db")
            );

            Statement statement = getConnection().createStatement();

            statement.execute("PRAGMA foreign_keys = ON");

            statement.close();

            System.out.println("Database connected!");

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    //=========SETTERS==============

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    //==========GETTERS=============

    public Connection getConnection() {
        return connection;
    }

}