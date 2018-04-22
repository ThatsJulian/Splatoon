package repo.binarydctr.splatoon.game;

import com.minedrixmc.gameengine.game.Game;
import com.minedrixmc.gameengine.game.GameState;
import com.minedrixmc.gameengine.game.exceptions.NoKitException;
import com.minedrixmc.gameengine.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import repo.binarydctr.splatoon.game.specials.Special;
import repo.binarydctr.splatoon.sql.SQLManager;

import java.util.HashMap;

/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public class KrakenItem {

    protected HashMap<Player, Boolean> gotSpecial;

    public KrakenItem(HashMap<Player, Boolean> gotSpecial) {
        this.gotSpecial = gotSpecial;
    }

    public void spawn(JavaPlugin plugin, final ItemStack stack, final Game game, final Kit kit, final Special special, final SQLManager sqlManager) {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        if(game.getKit(player).getName().equalsIgnoreCase(kit.getName())) {
                            if(player.getGameMode() == GameMode.SURVIVAL) {
                                if(game.getGameState() == GameState.LIVE) {
                                    if(sqlManager.specials.hasSpecial(player.getUniqueId(), special)) {
                                        if(!player.getInventory().containsAtLeast(stack, 1)) {
                                            if(!gotSpecial.containsKey(player)) {
                                                player.getInventory().setItem(8, stack);
                                                gotSpecial.put(player, true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (NoKitException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 400L, 400L);
    }

}
