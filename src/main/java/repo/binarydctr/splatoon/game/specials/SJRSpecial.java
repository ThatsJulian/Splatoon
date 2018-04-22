package repo.binarydctr.splatoon.game.specials;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import repo.binarydctr.splatoon.game.ChatFormat;
import repo.binarydctr.splatoon.game.kits.SplattershotJr;

import java.util.ArrayList;
import java.util.List;

/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public class SJRSpecial extends Special {

    public SJRSpecial() {
        super("SplatterShot Jr. Special", new String[] {"You turn invincible for 10 seconds."}, new SplattershotJr(), 100, Material.ENDER_PEARL);
    }

    public ItemStack specialItem() {
        ItemStack stack = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "SplatterShot Jr. Special - Right-Click");
        stack.setItemMeta(meta);
        return stack;
    }
}
