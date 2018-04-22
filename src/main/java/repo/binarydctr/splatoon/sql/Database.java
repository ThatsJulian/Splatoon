package repo.binarydctr.splatoon.sql;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public class Database {

    private Connection connection_;

    private final String user_;
    private final String database_;
    private final String password_;
    private final String port_;
    private final String hostname_;

    public Database(String user, String database, String password, String port, String hostname) {
        this.user_ = user;
        this.database_ = database;
        this.password_ = password;
        this.port_ = port;
        this.hostname_ = hostname;
    }

    public String getUser() {
        return this.user_;
    }

    public String getDatabase() {
        return this.database_;
    }

    public String getPassword() {
        return this.password_;
    }

    public String getPort() {
        return this.port_;
    }

    public String getHost() {
        return this.hostname_;
    }

    public Connection openConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection_ = DriverManager.getConnection("jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getDatabase(), getUser(), getPassword());
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("Could not connect to MySQL server! because: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Bukkit.getConsoleSender().sendMessage("JDBC Driver not found!");
        }
        return connection_;
    }

    public void checkConnection() {
        if (connection_ == null) {
            this.connection_ = openConnection();
        }
    }

    public String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
