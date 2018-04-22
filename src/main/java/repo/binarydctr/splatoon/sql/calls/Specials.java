package repo.binarydctr.splatoon.sql.calls;

import repo.binarydctr.splatoon.game.specials.Special;
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
public class Specials extends DatabaseCall<SQLManager> {

    public Specials(SQLManager plugin) {
        super(plugin);
    }

    public boolean addSpecial(UUID uuid, Special special) {
        plugin_.checkConnection();
        String oldSpecials = getSpecial(uuid);
        String newSpecials = oldSpecials + ":" + special.getName();
        try {
            PreparedStatement ps = plugin_.connection_.prepareStatement("UPDATE `Splatoon_Player` SET specials=? WHERE UUID=?");
            ps.setString(1, newSpecials);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getSpecial(UUID uuid) {
        plugin_.checkConnection();
        try {
            PreparedStatement ps = plugin_.connection_.prepareStatement("SELECT specials FROM Splatoon_Player WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getString("specials");
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasSpecial(UUID uuid, Special special) {
        plugin_.checkConnection();
        if(getSpecial(uuid).contains(special.getName())) {
            return true;
        }
        return false;
    }

}
