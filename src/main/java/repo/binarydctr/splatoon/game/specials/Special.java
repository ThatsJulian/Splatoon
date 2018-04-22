package repo.binarydctr.splatoon.game.specials;

import com.minedrixmc.gameengine.kit.Kit;
import org.bukkit.Material;
import org.bukkit.event.Listener;

/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public class Special implements Listener {

    protected String name;
    protected String[] desc;
    protected Kit kit;
    protected Integer price;
    protected Material item;

    public Special(String name, String[] desc, Kit kit, int price, Material item) {
        this.name = name;
        this.desc = desc;
        this.kit = kit;
        this.price = price;
        this.item = item;
    }

    public String getName() {
        return this.name;
    }

    public String[] getDesc() {
        return this.desc;
    }

    public Kit getKit() {
        return this.kit;
    }

    public Integer getPrice() {
        return price;
    }

    public Material getItem() {
        return item;
    }
}
