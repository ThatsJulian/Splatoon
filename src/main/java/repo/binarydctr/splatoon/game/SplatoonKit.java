package repo.binarydctr.splatoon.game;

import org.bukkit.inventory.ItemStack;

/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public interface SplatoonKit {

    ItemStack getMainItem();
    ItemStack getSubItem();
    ItemStack getSpecialItem();

}
