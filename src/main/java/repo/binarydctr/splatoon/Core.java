package repo.binarydctr.splatoon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;
import repo.binarydctr.splatoon.commands.CommandManager;
import repo.binarydctr.splatoon.game.Splatoon;
import repo.binarydctr.splatoon.sql.SQLManager;

/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public class Core extends JavaPlugin {

    static JavaPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        for(World worlds : Bukkit.getServer().getWorlds()) {
            WorldCreator wc = new WorldCreator(worlds.getName());
            World world = wc.createWorld();
            world.setAutoSave(false);
            world.setAnimalSpawnLimit(0);
            world.setMonsterSpawnLimit(0);
        }
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        SQLManager sqlManager = new SQLManager(this);
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Splatoon created by BinaryDctr, has been enabled.");
        Splatoon splatoon = new Splatoon(this, sqlManager);
        getServer().getPluginManager().registerEvents(splatoon, this);
        getCommand("addcoins").setExecutor(new CommandManager(sqlManager, this, splatoon));
        getCommand("takecoins").setExecutor(new CommandManager(sqlManager, this, splatoon));
        getCommand("resetcoins").setExecutor(new CommandManager(sqlManager, this, splatoon));
        getCommand("setlobby").setExecutor(new CommandManager(sqlManager, this, splatoon));
        getCommand("create").setExecutor(new CommandManager(sqlManager, this, splatoon));
    }

    public static JavaPlugin getInstance() {
        return plugin;
    }
}
