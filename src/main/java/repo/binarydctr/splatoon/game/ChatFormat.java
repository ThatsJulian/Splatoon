package repo.binarydctr.splatoon.game;

import org.bukkit.ChatColor;

/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public class ChatFormat {

    public static String info(String information) {
        return ChatColor.GOLD +""+ ChatColor.BOLD +
                "Splatoon > " + ChatColor.GRAY + information;
    }

}
