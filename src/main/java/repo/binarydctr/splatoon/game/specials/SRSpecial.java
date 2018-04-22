package repo.binarydctr.splatoon.game.specials;

import org.bukkit.Material;
import repo.binarydctr.splatoon.game.kits.InkBrush;
import repo.binarydctr.splatoon.game.kits.SplatRoller;

/**
 * ******************************************************************
 * Copyright ProjectOcarina (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of ProjectOcarina. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public class SRSpecial extends Special {

    public SRSpecial() {
        super("SplatRoller Special", new String[] {}, new SplatRoller(), 100, Material.ENDER_PEARL);
    }
}