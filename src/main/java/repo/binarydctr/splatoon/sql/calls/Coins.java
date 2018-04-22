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
public class Coins extends DatabaseCall<SQLManager> {

    public Coins(SQLManager plugin) {
        super(plugin);
    }

    public Integer getCoins(UUID uuid) {
        plugin_.checkConnection();
        try {
            PreparedStatement ps = plugin_.connection_.prepareStatement("SELECT coins FROM Splatoon_Player WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getInt("coins");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean setCoins(UUID uuid, int amount) {
        plugin_.checkConnection();
        try {
            PreparedStatement ps = plugin_.connection_.prepareStatement("UPDATE `Splatoon_Player` SET coins=? WHERE UUID=?");
            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addCoins(UUID uuid, int amount) {
        plugin_.checkConnection();
        int currentamount = getCoins(uuid);
        if(currentamount == -1) {
            return false;
        }
        try {
            PreparedStatement ps = plugin_.connection_.prepareStatement("UPDATE `Splatoon_Player` SET coins=? WHERE UUID=?");
            ps.setInt(1, currentamount + amount);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean removeCoins(UUID uuid, int amount) {
        plugin_.checkConnection();
        int currentamount = getCoins(uuid);
        if(currentamount == -1) {
            return false;
        }
        try {
            PreparedStatement ps = plugin_.connection_.prepareStatement("UPDATE `Splatoon_Player` SET coins=? WHERE UUID=?");
            ps.setInt(1, currentamount - amount);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
