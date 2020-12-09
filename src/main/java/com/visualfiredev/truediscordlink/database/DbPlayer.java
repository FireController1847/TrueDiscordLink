package com.visualfiredev.truediscordlink.database;

import com.visualfiredev.javabase.DataType;
import com.visualfiredev.javabase.DatabaseObject;
import com.visualfiredev.javabase.DatabaseType;
import com.visualfiredev.javabase.schema.ColumnSchema;
import com.visualfiredev.javabase.schema.TableSchema;
import com.visualfiredev.truediscordlink.TrueDiscordLink;

public class DbPlayer extends DatabaseObject {

    // Table Schemas
    public static final TableSchema TABLE_SCHEMA_SQLITE = new TableSchema("players",
        new ColumnSchema("id", DataType.INTEGER).setPrimaryKey(true).setAutoIncrement(true),
        new ColumnSchema("name", DataType.TEXT),
        new ColumnSchema("uuid", DataType.TEXT),
        new ColumnSchema("linked", DataType.INTEGER).setDefaultValue(0)
    );
    public static final TableSchema TABLE_SCHEMA_MYSQL = new TableSchema("players",
        new ColumnSchema("id", DataType.INTEGER).setPrimaryKey(true).setAutoIncrement(true),
        new ColumnSchema("name", DataType.VARCHAR, 16),
        new ColumnSchema("uuid", DataType.VARCHAR, 36)
    );

    // Variables
    private int id;
    private String name;
    private String uuid;
    private boolean linked;

    // Constructor
    public DbPlayer(int id, String name, String uuid, boolean linked) {
        super(TrueDiscordLink.getInstance().getDatabaseManager().getDatabase().getType() == DatabaseType.SQLite ? TABLE_SCHEMA_SQLITE : TABLE_SCHEMA_MYSQL);
        this.id = id;
        this.name = name;
        this.uuid = uuid;
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
    public String getUuid() {
        return uuid;
    }
    public boolean isLinked() {
        return linked;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public void setLinked(boolean linked) {
        this.linked = linked;
    }

}
