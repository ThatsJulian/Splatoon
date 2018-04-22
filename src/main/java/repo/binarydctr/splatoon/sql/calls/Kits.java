package repo.binarydctr.splatoon.sql.calls;

import com.minedrixmc.gameengine.kit.Kit;
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
public class Kits extends DatabaseCall<SQLManager> {

    public Kits(SQLManager plugin) {
        super(plugin);
    }

    public boolean addKit(UUID uuid, Kit kit) {
        plugin_.checkConnection();
        String oldKits = getKits(uuid);
        String newKits = oldKits + ":" + kit.getName();
        try {
            PreparedStatement ps = plugin_.connection_.prepareStatement("UPDATE `GamePlayer` SET kits=? WHERE UUID=?");
            ps.setString(1, newKits);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getKits(UUID uuid) {
        plugin_.checkConnection();
        try {
            PreparedStatement ps = plugin_.connection_.prepareStatement("SELECT kits FROM GamePlayer WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getString("kits");
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasKit(UUID uuid, Kit kit) {
        plugin_.checkConnection();
        String kits = getKits(uuid);
        String[] kitArray = kits.split(":");
        for(int i = 0; i < kitArray.length; i++) {
            if (kitArray[i].equalsIgnoreCase(kit.getName())) {
                return true;
            }
        }
        return false;
    }

}
