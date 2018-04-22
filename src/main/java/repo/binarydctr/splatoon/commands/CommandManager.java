package repo.binarydctr.splatoon.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import repo.binarydctr.splatoon.game.ChatFormat;
import repo.binarydctr.splatoon.game.Splatoon;
import repo.binarydctr.splatoon.sql.SQLManager;

import java.util.HashMap;
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
public class CommandManager implements CommandExecutor, Listener {

    SQLManager sqlManager;
    JavaPlugin plugin;
    Splatoon splatoon;

    public CommandManager(SQLManager sqlManager, JavaPlugin plugin, Splatoon splatoon) {
        this.sqlManager = sqlManager;
        this.plugin = plugin;
        this.splatoon = splatoon;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    HashMap<Player, String> setup = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (player.hasPermission("splatoon.op") == true || player.isOp()) {
                if (command.getName().equalsIgnoreCase("addcoins")) {
                    if (args.length == 2) {
                        UUID uuid = UUID.fromString(sqlManager.player.getUUID(args[0]));
                        Integer amount = Integer.parseInt(args[1]);
                        if (playerisOnline(uuid) == true) {
                            Player target = Bukkit.getPlayer(uuid);
                            splatoon.addCoins(target, amount);
                        } else {
                            sqlManager.coins.addCoins(uuid, amount);
                        }
                        player.sendMessage(ChatFormat.info("You have gave " + amount + " coins to " + args[0] + "."));
                    } else {
                        player.sendMessage(ChatFormat.info("/addcoins <player> <amount>"));
                        return true;
                    }
                }
                if (command.getName().equalsIgnoreCase("takecoins")) {
                    if (args.length == 2) {
                        UUID uuid = UUID.fromString(sqlManager.player.getUUID(args[0]));
                        Integer amount = Integer.parseInt(args[1]);
                        if (playerisOnline(uuid) == true) {
                            Player target = Bukkit.getPlayer(uuid);
                            splatoon.removeCoins(target, amount);
                        } else {
                            sqlManager.coins.removeCoins(uuid, amount);
                        }
                        sqlManager.coins.removeCoins(uuid, amount);
                        player.sendMessage(ChatFormat.info("You have taken " + amount + " coins from " + args[0] + "."));
                    } else {
                        player.sendMessage(ChatFormat.info("/takecoins <player> <amount>"));
                        return true;
                    }
                }
                if (command.getName().equalsIgnoreCase("resetcoins")) {
                    if (args.length == 1) {
                        UUID uuid = UUID.fromString(sqlManager.player.getUUID(args[0]));
                        if (playerisOnline(uuid) == true) {
                            Player target = Bukkit.getPlayer(uuid);
                            splatoon.resetCoins(target);
                        } else {
                            sqlManager.coins.setCoins(uuid, 0);
                        }
                        sqlManager.coins.setCoins(uuid, 0);
                        player.sendMessage(ChatFormat.info("You have reset " + args[0] + "'s coins."));
                    } else {
                        player.sendMessage(ChatFormat.info("/resetcoins <player>"));
                        return true;
                    }
                }
                if (command.getName().equalsIgnoreCase("setlobby")) {
                    if (args.length == 0) {
                        setLobbySpawn(player);
                        player.sendMessage(ChatFormat.info("Lobby spawn set."));
                    } else {
                        player.sendMessage(ChatFormat.info("/setlobby"));
                        return true;
                    }
                }
                if (command.getName().equalsIgnoreCase("create")) {
                    if (args.length == 1) {
                        String name = args[0];
                        setup.put(player, name);
                        player.sendMessage(ChatFormat.info("Type good, bad, and spec to setup the map."));
                        player.sendMessage(ChatFormat.info("good, sets good team spawn."));
                        player.sendMessage(ChatFormat.info("bad, sets bad team spawn."));
                        player.sendMessage(ChatFormat.info("spec, sets spectator spawn."));
                    } else {
                        player.sendMessage(ChatFormat.info("/create <mapname>"));
                        return true;
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean playerisOnline(UUID uuid) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.getUniqueId().toString().equalsIgnoreCase(uuid.toString())) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private Location goodspawn,badspawn,specspawn;

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        if (setup.containsKey(event.getPlayer())) {
            Player player = event.getPlayer();
            event.setCancelled(true);
            if (goodspawn == null || badspawn == null || specspawn == null) {
                if (event.getMessage().equalsIgnoreCase("good")) {
                    this.goodspawn = player.getLocation();
                    player.sendMessage(ChatFormat.info("Set good spawn."));
                }
                if (event.getMessage().equalsIgnoreCase("bad")) {
                    this.badspawn = player.getLocation();
                    player.sendMessage(ChatFormat.info("Set bad spawn."));
                }
                if (event.getMessage().equalsIgnoreCase("spec")) {
                    this.specspawn = player.getLocation();
                    player.sendMessage(ChatFormat.info("Set spectator spawn."));
                }
            } else {
                if (event.getMessage().equalsIgnoreCase("done")) {
                    createMap(player, this.goodspawn, this.badspawn, this.specspawn);
                    setup.remove(player);
                }
            }
        }
    }

    public void createMap(Player player, Location good, Location bad, Location spec) {
        plugin.getConfig().set("Maps." + setup.get(player) + ".goodx", good.getX());
        plugin.getConfig().set("Maps." + setup.get(player) + ".goody", good.getY());
        plugin.getConfig().set("Maps." + setup.get(player) + ".goodz", good.getZ());

        plugin.getConfig().set("Maps." + setup.get(player) + ".badx", bad.getX());
        plugin.getConfig().set("Maps." + setup.get(player) + ".bady", bad.getY());
        plugin.getConfig().set("Maps." + setup.get(player) + ".badz", bad.getZ());

        plugin.getConfig().set("Maps." + setup.get(player) + ".specx", spec.getX());
        plugin.getConfig().set("Maps." + setup.get(player) + ".specy", spec.getY());
        plugin.getConfig().set("Maps." + setup.get(player) + ".specz", spec.getZ());

        plugin.getConfig().set("Maps." + setup.get(player) + ".world", player.getWorld().getName());
        plugin.saveConfig();

        player.sendMessage(ChatFormat.info("Successfully created a new map called "+setup.get(player)));
    }

    public void setLobbySpawn(Player player) {
        plugin.getConfig().set("Lobby.x", player.getLocation().getX());
        plugin.getConfig().set("Lobby.y", player.getLocation().getY());
        plugin.getConfig().set("Lobby.z", player.getLocation().getZ());
        plugin.getConfig().set("Lobby.yaw", player.getLocation().getYaw());
        plugin.getConfig().set("Lobby.pitch", player.getLocation().getPitch());
        plugin.getConfig().set("Lobby.world", player.getLocation().getWorld().getName());
        plugin.saveConfig();
    }
}
