package repo.binarydctr.splatoon.game;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import repo.binarydctr.splatoon.Core;

import java.util.ArrayList;
import java.util.HashMap;
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
public class ItemBuilder implements Listener {

    private static final Plugin plugin = Core.getInstance();
    private static boolean listener	= false;
    private static final HashMap<String, PotionEffect> effects = new HashMap<String, PotionEffect>();

    private final ItemStack is;

    public ItemBuilder(final Material mat) {
        is = new ItemStack(mat);
    }

    public ItemBuilder(final ItemStack is) {
        this.is = is;
    }

    public ItemBuilder amount(final int amount) {
        is.setAmount(amount);
        return this;
    }

    public ItemBuilder name(final String name) {
        final ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        is.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(final String name) {
        final ItemMeta meta = is.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<String>();
        }
        lore.add(name);
        meta.setLore(lore);
        is.setItemMeta(meta);
        return this;
    }

    public ItemBuilder durability(final int durability) {
        is.setDurability((short) durability);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder data(final int data) {
        is.setData(new MaterialData(is.getType(), (byte) data));
        return this;
    }

    public ItemBuilder enchantment(final Enchantment enchantment, final int level) {
        is.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder enchantment(final Enchantment enchantment) {
        is.addUnsafeEnchantment(enchantment, 1);
        return this;
    }

    public ItemBuilder type(final Material material) {
        is.setType(material);
        return this;
    }

    public ItemBuilder clearLore() {
        final ItemMeta meta = is.getItemMeta();
        meta.setLore(new ArrayList<String>());
        is.setItemMeta(meta);
        return this;
    }

    public ItemBuilder clearEnchantments() {
        for (final Enchantment e : is.getEnchantments().keySet()) {
            is.removeEnchantment(e);
        }
        return this;
    }

    public ItemBuilder color(Color color) {
        if (is.getType() == Material.LEATHER_BOOTS || is.getType() == Material.LEATHER_CHESTPLATE || is.getType() == Material.LEATHER_HELMET
                || is.getType() == Material.LEATHER_LEGGINGS) {
            LeatherArmorMeta meta = (LeatherArmorMeta) is.getItemMeta();
            meta.setColor(color);
            is.setItemMeta(meta);
            return this;
        } else {
            throw new IllegalArgumentException("color() only applicable for leather armor!");
        }
    }

    public ItemBuilder effect(PotionEffectType type, int duration, int amplifier, boolean ambient) {
        effect(new PotionEffect(type, duration, amplifier, ambient));
        return this;
    }

    public ItemBuilder effect(PotionEffect effect) {
        if (!listener) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            listener = true;
        }
        String name = is.getItemMeta().getDisplayName();
        while (effects.containsKey(name)) {
            name = name + "#";
        }
        effects.put(name, effect);
        return this;
    }

    public ItemBuilder effect(PotionEffectType type, int duration, int amplifier) {
        effect(new PotionEffect(type, duration == -1 ? 1000000 : duration, amplifier));
        return this;
    }

    public ItemBuilder effect(PotionEffectType type, int duration) {
        effect(new PotionEffect(type, duration == -1 ? 1000000 : duration, 1));
        return this;
    }

    public ItemStack build() {
        return is;
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent e) {
        if (e.getItem().hasItemMeta()) {
            @SuppressWarnings("unchecked") HashMap<String, PotionEffect> copy = (HashMap<String, PotionEffect>) effects.clone();
            String name = e.getItem().getItemMeta().getDisplayName();
            while (copy.containsKey(name)) {
                e.getPlayer().addPotionEffect(copy.get(name), true);
                copy.remove(name);
                name += "#";
            }
        }
    }

    @EventHandler
    public void onItemApply(InventoryClickEvent e) {
        // TODO add effects when item is applied
    }

}
