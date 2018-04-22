package repo.binarydctr.splatoon.game.specials;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import repo.binarydctr.splatoon.game.kits.InkBrush;
import repo.binarydctr.splatoon.game.kits.Squiffer;

/**
 * ******************************************************************
 * Copyright ProjectOcarina (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of ProjectOcarina. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public class IBSpecial extends Special {

    public IBSpecial() {
        super("InkBrush Special", new String[] {}, new InkBrush(), 100, Material.ENDER_PEARL);
    }
}