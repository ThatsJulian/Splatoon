package repo.binarydctr.splatoon.sql;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import repo.binarydctr.splatoon.Core;
import repo.binarydctr.splatoon.sql.calls.Coins;
import repo.binarydctr.splatoon.sql.calls.Kills;
import repo.binarydctr.splatoon.sql.calls.Kits;
import repo.binarydctr.splatoon.sql.calls.Specials;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public class SQLManager extends Database implements Listener {

    public Connection connection_;

    public Kits kits;
    public Coins coins;
    public Kills kills;
    public Specials specials;
    public repo.binarydctr.splatoon.sql.calls.Player player;

    public SQLManager(Core core) {
        super(core.getConfig().getString("MySQL.DBUser")
                , core.getConfig().getString("MySQL.DBName")
                , core.getConfig().getString("MySQL.DBPass")
                , core.getConfig().getString("MySQL.DBPort")
                , core.getConfig().getString("MySQL.DBHost"));
        connection_ = openConnection();
        try {
            PreparedStatement ps = connection_.prepareStatement("CREATE TABLE IF NOT EXISTS `Splatoon_Player` " +
                    "(`UUID` varchar(36) NOT NULL, `name` varchar(32) NOT NULL, `kits` varchar(2000) NOT NULL" +
                    ", `specials` varchar(2000) NOT NULL, `coins` int(8) NOT NULL, `kills` int(8) NOT NULL)");
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not check if table exists, restarting server.");
            e.printStackTrace();
            Bukkit.getServer().shutdown();
        }
        this.kits = new Kits(this);
        this.coins = new Coins(this);
        this.kills = new Kills(this);
        this.specials = new Specials(this);
        this.player = new repo.binarydctr.splatoon.sql.calls.Player(this);
        core.getServer().getPluginManager().registerEvents(this, core);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.player.addPlayer(player);
    }

}
