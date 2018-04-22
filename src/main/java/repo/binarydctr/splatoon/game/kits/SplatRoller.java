package repo.binarydctr.splatoon.game.kits;

import com.minedrixmc.gameengine.game.Game;
import com.minedrixmc.gameengine.kit.Kit;
import com.minedrixmc.gameengine.skill.Skill;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import repo.binarydctr.splatoon.game.SpecialItems;
import repo.binarydctr.splatoon.game.SplatoonKit;
import repo.binarydctr.splatoon.game.SubItems;
import repo.binarydctr.splatoon.game.specials.SRSpecial;
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
public class SplatRoller extends Kit implements SplatoonKit {

    public SplatRoller() {
        super("SplatRoller", null, new Skill[] {}, Material.IRON_SPADE, 2000);
    }

    public void sub(Game game) {
        new SubItems(game.getPlugin(), grenades(), game, this);
    }

    public void special(HashMap<Player, Boolean> gotSpecial, Game game, SQLManager sqlManager) {
        new SpecialItems(gotSpecial).spawn(game.getPlugin(), special(), game, this, new SRSpecial(), sqlManager);
    }

    @Override
    public void apply(Player player) {
        player.getInventory().addItem(bow());
        player.setLevel(16);
        player.getInventory().setItem(1, squidItem());
    }

    public ItemStack bow() {
        ItemStack stack = new ItemStack(Material.IRON_SPADE);

        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Roller");

        stack.setItemMeta(meta);

        return stack;
    }

    public ItemStack mainItem() {
        return null;
    }


    public ItemStack grenades() {
        ItemStack stack = new ItemStack(Material.POTION);
        Potion potion = new Potion(1);
        potion.setSplash(true);
        potion.setType(PotionType.INSTANT_DAMAGE);
        potion.apply(stack);

        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Grenade - Right-Click");

        stack.setItemMeta(meta);

        return stack;
    }

    public ItemStack special() {
        ItemStack stack = new ItemStack(Material.ENDER_PEARL);

        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Kraken - Right-Click");

        stack.setItemMeta(meta);

        return stack;
    }

    public ItemStack squidItem() {
        ItemStack stack = new ItemStack(Material.INK_SACK);

        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Squid Form - Hold To Use");

        stack.setItemMeta(meta);

        return stack;
    }

    @Override
    public ItemStack getMainItem() {
        return mainItem();
    }

    @Override
    public ItemStack getSubItem() {
        return grenades();
    }

    @Override
    public ItemStack getSpecialItem() {
        return special();
    }
}