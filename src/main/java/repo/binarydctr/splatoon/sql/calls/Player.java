package repo.binarydctr.splatoon.sql.calls;

import repo.binarydctr.splatoon.sql.DatabaseCall;
import repo.binarydctr.splatoon.sql.SQLManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public class Player extends DatabaseCall<SQLManager> {

    public Player(SQLManager plugin) {
        super(plugin);
    }

    public boolean checkExists(org.bukkit.entity.Player player) {
        plugin_.checkConnection();
        try {
            PreparedStatement ps = plugin_.connection_.prepareStatement("SELECT UUID FROM Splatoon_Player WHERE UUID=?");
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getUUID(String name) {
        plugin_.checkConnection();
        try {
            PreparedStatement ps = plugin_.connection_.prepareStatement("SELECT UUID FROM Splatoon_Player WHERE name=?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getString("UUID");
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addPlayer(org.bukkit.entity.Player player) {
        plugin_.checkConnection();
        if(checkExists(player) == false) {
            try {
                PreparedStatement ps = plugin_.connection_.prepareStatement("INSERT INTO `Splatoon_Player` VALUES (?,?,?,?,?,?)");
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, player.getName());
                ps.setString(3, "SplatterShot Jr.");
                ps.setString(4, "");
                ps.setInt(5, 0);
                ps.setInt(6, 0);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

}
