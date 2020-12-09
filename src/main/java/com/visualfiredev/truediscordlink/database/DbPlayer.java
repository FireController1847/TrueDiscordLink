package com.visualfiredev.truediscordlink.database;

import com.visualfiredev.javabase.DataType;
import com.visualfiredev.javabase.Database;
import com.visualfiredev.javabase.DatabaseObject;
import com.visualfiredev.javabase.DatabaseType;
import com.visualfiredev.javabase.schema.ColumnSchema;
import com.visualfiredev.javabase.schema.TableSchema;
import com.visualfiredev.truediscordlink.TrueDiscordLink;

public class DbPlayer extends DatabaseObject {

    // Table Schemas
    // They're not final because they get modified by the DatabaseManager, but everywhere else they
    // can be considered effectively final
    public static TableSchema TABLE_SCHEMA_SQLITE = new TableSchema("players",
        new ColumnSchema("id", DataType.INTEGER).setPrimaryKey(true).setAutoIncrement(true),
        new ColumnSchema("name", DataType.TEXT),
        new ColumnSchema("minecraft_uuid", DataType.TEXT).setUniqueKey(true),
        new ColumnSchema("discord_id", DataType.TEXT),
        new ColumnSchema("linked", DataType.INTEGER)
    );
    public static TableSchema TABLE_SCHEMA_MYSQL = new TableSchema("players",
        new ColumnSchema("id", DataType.INTEGER).setPrimaryKey(true).setAutoIncrement(true),
        new ColumnSchema("name", DataType.VARCHAR, 16),
        new ColumnSchema("minecraft_uuid", DataType.VARCHAR, 36).setUniqueKey(true),
        new ColumnSchema("discord_id", DataType.VARCHAR, 25),
        new ColumnSchema("linked", DataType.TINYINT, 1)
    );

    // Variables
    private int id = -1;
    private String name;
    private String minecraft_uuid;
    private String discord_id;
    private boolean linked;

    // Constructor
    public DbPlayer(String name, String minecraft_uuid, String discord_id, boolean linked) {
        super(TrueDiscordLink.getInstance().getDatabaseManager().getDatabase().getType() == DatabaseType.SQLite ? TABLE_SCHEMA_SQLITE : TABLE_SCHEMA_MYSQL);
        this.id = id;
        this.name = name;
        this.minecraft_uuid = minecraft_uuid;
        this.discord_id = discord_id;
        this.linked = linked;
    }
    public DbPlayer() {
        super(TrueDiscordLink.getInstance().getDatabaseManager().getDatabase().getType() == DatabaseType.SQLite ? TABLE_SCHEMA_SQLITE : TABLE_SCHEMA_MYSQL);
    }

    // Getters
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getMinecraftUuid() {
        return minecraft_uuid;
    }
    public String getDiscordId() {
        return discord_id;
    }
    public boolean isLinked() {
        return linked;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }
    public void setMinecraftUuid(String minecraft_uuid) {
        this.minecraft_uuid = minecraft_uuid;
    }
    public void setDiscordId(String id) {
        this.discord_id = discord_id;
    }
    public void setLinked(boolean linked) {
        this.linked = linked;
    }

    // Utility method to get the correct table schema
    public static TableSchema getTableSchema(Database database) {
        return database.getType() == DatabaseType.SQLite ? TABLE_SCHEMA_SQLITE : TABLE_SCHEMA_MYSQL;
    }

    // ToString
    @Override
    public String toString() {
        return "DbPlayer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", minecraft_uuid='" + minecraft_uuid + '\'' +
                ", discord_id='" + discord_id + '\'' +
                ", linked=" + linked +
                '}';
    }

}
