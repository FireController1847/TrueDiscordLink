package com.firecontroller1847.truediscordlink;

import com.firecontroller1847.truediscordlink.database.DbPlayer;
import com.visualfiredev.javabase.Database;
import com.visualfiredev.javabase.DatabaseType;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseManager {

    // Variables
    private TrueDiscordLink discordlink;
    private Database database;
    private AtomicBoolean connected = new AtomicBoolean(false);
    private boolean initialized = false;

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
            DatabaseType type = DatabaseType.valueOf(Objects.requireNonNull(discordlink.getConfig().getString("database.type")));
            String database;
            if (type == DatabaseType.SQLite) {
                database = new File(discordlink.getDataFolder(), "/" + Objects.requireNonNull(discordlink.getConfig().getString("database.database"))).getAbsolutePath();
            } else {
                database = Objects.requireNonNull(discordlink.getConfig().getString("database.database"));
            }

            // Add SQLite ".db"
            if (type == DatabaseType.SQLite) {
                database += ".db";
            }

            this.database = new Database(host, database, type);
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
            if (!initialized) {
                // Handle Prefixes
                String prefix = discordlink.getConfig().getString("database.table_prefix");
                if (prefix != null) {
                    DbPlayer.TABLE_SCHEMA_MYSQL = DbPlayer.TABLE_SCHEMA_MYSQL.clone().setName(prefix + DbPlayer.TABLE_SCHEMA_MYSQL.getName());
                    DbPlayer.TABLE_SCHEMA_SQLITE = DbPlayer.TABLE_SCHEMA_SQLITE.clone().setName(prefix + DbPlayer.TABLE_SCHEMA_SQLITE.getName());
                }
            }

            // Create Tables
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

        initialized = true;
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
