package repo.binarydctr.splatoon.game.teams;

import com.minedrixmc.gameengine.team.GameTeam;
import org.bukkit.ChatColor;
import java.util.List;

/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2016. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public class BadTeam extends GameTeam {

    public BadTeam(List<String> members) {
        super(members, "Bad", ChatColor.BLUE, null, null);
    }
}
