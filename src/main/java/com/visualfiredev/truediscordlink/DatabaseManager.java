package com.visualfiredev.truediscordlink;

import com.visualfiredev.javabase.Database;
import com.visualfiredev.javabase.DatabaseType;
import com.visualfiredev.truediscordlink.database.DbPlayer;

import java.io.File;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseManager {

    // Variables
    private TrueDiscordLink discordlink;
    private Database database;
    private AtomicBoolean connected = new AtomicBoolean(false);

    // Constructor
    public DatabaseManager(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
        new Thread(this::connect).start();
    }

    // Connects to the database
    public void connect() {
        this.discordlink.getLogger().info("Logging in to Database...");

        // Create database if one doesn't already exist
        if (this.database == null) {
            String host = Objects.requireNonNull(discordlink.getConfig().getString("database.host"));
            String database = new File(discordlink.getDataFolder(), "/" + Objects.requireNonNull(discordlink.getConfig().getString("database.database")) + ".db").getAbsolutePath();
            String type = Objects.requireNonNull(discordlink.getConfig().getString("database.type"));
            this.database = new Database(host, database, DatabaseType.valueOf(type));
        }

        // Connect
        String username = discordlink.getConfig().getString("database.username");
        String password = discordlink.getConfig().getString("database.password");
        try {
            if (username == null || password == null) {
                this.database.connect();
            } else {
                this.database.connect(username, password);
            }
            connected.set(true);
        } catch (Exception e) {
            e.printStackTrace();
            connected.set(false);
        }

        // Initialize
        this.initialize();

        this.discordlink.getLogger().info("Logged in to Database!");
    }

    // Initializes the main tables of the database
    public void initialize() {
        try {
            if (!database.doesTableExist(DbPlayer.TABLE_SCHEMA_MYSQL)) {
                if (database.getType() == DatabaseType.SQLite) {
                    database.createTable(DbPlayer.TABLE_SCHEMA_SQLITE);
                } else {
                    database.createTable(DbPlayer.TABLE_SCHEMA_MYSQL);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            connected.set(false);
        }
    }

    // Disconnects from the database
    public void disconnect() {
        try {
            database.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Returns true if we're connected, false if not
    public boolean isConnected() {
        if (!this.database.isConnected()) {
            return false;
        } else {
            return connected.get();
        }
    }

    // Getters
    public Database getDatabase() {
        return database;
    }

}
