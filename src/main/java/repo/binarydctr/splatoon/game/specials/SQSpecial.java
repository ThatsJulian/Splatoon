package repo.binarydctr.splatoon.game.specials;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import repo.binarydctr.splatoon.game.kits.Squiffer;

/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public class SQSpecial extends Special {

    public SQSpecial() {
        super("Squiffer Special", new String[] {}, new Squiffer(), 100, Material.BOW);
    }

    public ItemStack specialItem() {
        ItemStack stack = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Squiffer Special - Right-Click");
        stack.setItemMeta(meta);
        return stack;
    }
}
