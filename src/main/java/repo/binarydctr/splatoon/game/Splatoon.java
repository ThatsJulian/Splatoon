package repo.binarydctr.splatoon.game;

import com.minedrixmc.gameengine.game.Game;
import com.minedrixmc.gameengine.game.GameState;
import com.minedrixmc.gameengine.game.exceptions.NoKitException;
import com.minedrixmc.gameengine.kit.Kit;
import com.minedrixmc.gameengine.team.GameTeam;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.minecraft.server.v1_9_R2.EnumParticle;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.PacketPlayOutTitle;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import repo.binarydctr.splatoon.game.kits.*;
import repo.binarydctr.splatoon.game.specials.*;
import repo.binarydctr.splatoon.game.teams.BadTeam;
import repo.binarydctr.splatoon.game.teams.GoodTeam;
import repo.binarydctr.splatoon.sql.SQLManager;

import java.util.*;


/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public class Splatoon extends Game implements Listener {

    SQLManager sqlManager;

    Special[] specials;

    HashMap<Player, Boolean> gotSpecial = new HashMap<>();

    public Splatoon(JavaPlugin plugin, SQLManager sqlManager) {
        super(plugin //JavaPlugin instance so the game can register as a Listener
                , "Splatoon" //Game Name
                , new Kit[]{new SplattershotJr(), new SplatterShot(), new Squiffer(), new InkBrush(), new SplatCharger(), new SplatRoller()} //All Kits
                , new GameTeam[]{new BadTeam(new ArrayList<String>()), new GoodTeam(new ArrayList<String>())}
                , 2 //Min Players
                , 24 //Max Players
                , new SplattershotJr()); //Default Kit
        this.sqlManager = sqlManager;
        this.specials = new Special[]{new SJRSpecial(), new SSSpecial(), new SQSpecial(), new IBSpecial(), new SCSpecial(), new SRSpecial()};
        registerSpecials();
        setGameState(GameState.LOBBY);
        addAllMaps();
        setMap();
        registerKitsandSkills();
    }

    public GameTeam getGoodTeam() {
        for(GameTeam team : getTeams()) {
            if(team.getName().equalsIgnoreCase("good")) {
                return team;
            }
        }
        return null;
    }

    public GameTeam getBadTeam() {
        for(GameTeam team : getTeams()) {
            if(team.getName().equalsIgnoreCase("bas")) {
                return team;
            }
        }
        return null;
    }

    List<String> badMembers = getBadTeam().getMembers();
    List<String> goodMembers = getGoodTeam().getMembers();

    @Override
    public void startGame() {
        setGameState(GameState.LIVE);
        /*
        Setup Game Managers
         */
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                getKit(player).apply(player);
            } catch (NoKitException e) {
                e.printStackTrace();
            }
            checkHand(player);
            checkLand(player);
            checkALand(player);
            clearItem(player);
            jumpSquid(player);
            infDura(player);
            if (goodMembers.contains(player.getName())) {
                player.getInventory().setHelmet(new ItemBuilder(Material.LEATHER_HELMET).color(Color.ORANGE).build());
                player.getInventory().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.ORANGE).build());
                player.getInventory().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.ORANGE).build());
                player.getInventory().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).color(Color.ORANGE).build());
            } else {
                player.getInventory().setHelmet(new ItemBuilder(Material.LEATHER_HELMET).color(Color.BLUE).build());
                player.getInventory().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.BLUE).build());
                player.getInventory().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.BLUE).build());
                player.getInventory().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).color(Color.BLUE).build());
            }
        }
        new SplattershotJr().sub(this);
        new SplattershotJr().special(gotSpecial, this, sqlManager);
        new SplatterShot().sub(this);
        new SplatterShot().special(gotSpecial, this, sqlManager);
        new Squiffer().sub(this);
        new Squiffer().special(gotSpecial, this, sqlManager);
        new InkBrush().sub(this);
        new InkBrush().special(gotSpecial, this, sqlManager);
        new SplatCharger().sub(this);
        new SplatCharger().special(gotSpecial, this, sqlManager);
        new SplatRoller().sub(this);
        new SplatRoller().special(gotSpecial, this, sqlManager);
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
            @Override
            public void run() {
                endGame();
            }
        }, 20 * getPlugin().getConfig().getInt("GameTime"));
    }

    @Override
    public void endGame() {
        Bukkit.getServer().broadcastMessage(ChatFormat.info("Fetching ink data..."));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(getSpecSpawn());
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    GoodTeam goodTeam = new GoodTeam(goodMembers);
                    BadTeam badTeam = new BadTeam(badMembers);
                    if (badBlocks.size() > goodBlocks.size()) {
                        if (badTeam.getMembers().contains(player.getName())) {
                            sendTitle(player, badTeam.getColor() + "You Win");
                            sendSubTitle(player, ChatColor.WHITE + "Congrats");
                            addCoins(player, 50);
                        } else {
                            sendTitle(player, badTeam.getColor() + "You Lose");
                            sendSubTitle(player, ChatColor.WHITE + "Sorry");
                        }
                        setGameState(GameState.ENDED);
                    } else if (badBlocks.size() < goodBlocks.size()) {
                        if (goodTeam.getMembers().contains(player.getName())) {
                            sendTitle(player, goodTeam.getColor() + "You Win");
                            sendSubTitle(player, ChatColor.WHITE + "Congrats");
                            addCoins(player, 50);
                        } else {
                            sendTitle(player, goodTeam.getColor() + "You Lose");
                            sendSubTitle(player, ChatColor.WHITE + "Sorry");
                        }
                        setGameState(GameState.ENDED);
                    } else if (badBlocks.size() == goodBlocks.size()) {
                        sendTitle(player, ChatColor.WHITE + "TIE");
                        sendSubTitle(player, ChatColor.WHITE + "Sorry...");
                        setGameState(GameState.ENDED);
                    }
                }
            }
        }, 100);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.kickPlayer(ChatFormat.info("Game restarting..."));
                }
            }
        }, 300);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getServer().getWorlds()) {
                    Bukkit.unloadWorld(world.getName(), false);
                    Bukkit.shutdown();
                }
            }
        }, 350);
    }

    int task;

    int countdown = 60;

    @Override
    public void startCountdown() {
        setGameState(GameState.COUNTINGDOWN);
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                countdown--;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.setLevel(countdown);
                    if (countdown == 10) {
                        player.sendMessage(ChatFormat.info("Game starting in 10 seconds."));
                    }
                }
                if (countdown == 0) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (goodMembers.contains(player.getName())) {
                            player.teleport(getGoodSpawn());
                        }
                        if (badMembers.contains(player.getName())) {
                            player.teleport(getBadSpawn());
                        }
                        player.getInventory().clear();
                        player.setExp(0);
                        player.setLevel(0);
                    }
                    Bukkit.broadcastMessage(ChatFormat.info("The game has begun."));
                    endCountdown();
                    startGame();
                }
            }
        }, 20L, 20L);
    }

    @Override
    public void endCountdown() {
        Bukkit.getScheduler().cancelTask(task);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setExp(0);
            player.setLevel(0);
        }
        countdown = 60;
    }

    public void registerSpecials() {
        for (Special special : this.specials) {
            getPlugin().getServer().getPluginManager().registerEvents(special, getPlugin());
        }
    }

    public static void sendTitle(Player p, String title) {
        IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a("{\"text\": \"\"}").a(title);
        PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, message);

        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public static void sendSubTitle(Player p, String subtitle) {
        IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a("{\"text\": \"\"}").a(subtitle);
        PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, message);

        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (getLobbySpawn() == null) {
            return;
        }
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        player.teleport(getLobbySpawn());
        player.getInventory().clear();
        player.getInventory().setHelmet(new ItemStack(Material.AIR));
        player.getInventory().setChestplate(new ItemStack(Material.AIR));
        player.getInventory().setLeggings(new ItemStack(Material.AIR));
        player.getInventory().setBoots(new ItemStack(Material.AIR));
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);
        player.getInventory().setItem(0, kitItem());
        player.getInventory().setItem(2, specialsItem());
        player.setGameMode(GameMode.SURVIVAL);
        setKit(player, new SplattershotJr());
        joinTeam(player);
        Gameboard(player);
        player.sendMessage(ChatFormat.info("You have the Splattershot Jr. kit."));
        if (Bukkit.getOnlinePlayers().size() >= getMin()) {
            if (getGameState() != GameState.COUNTINGDOWN) {
                startCountdown();
                countdown = 60;
            }
        }
    }

    @EventHandler
    public void onPlayerPreLogin(PlayerPreLoginEvent event) {
        if (getGameState() == GameState.LIVE) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, ChatFormat.info("Game is in progress."));
        } else if (getGameState() == GameState.ENDED) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, ChatFormat.info("Game is restarting."));
        }
    }

    @EventHandler
    public void pickup(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (goodMembers.contains(player.getName())) {
            goodMembers.remove(player.getName());
        }
        if (badMembers.contains(player.getName())) {
            badMembers.remove(player.getName());
        }
        if (Bukkit.getOnlinePlayers().size() <= getMin()) {
            if (getGameState() == GameState.COUNTINGDOWN) {
                endCountdown();
                setGameState(GameState.LOBBY);
            }
            if (getGameState() == GameState.LIVE) {
                endGame();
            }
        }
    }

    @EventHandler
    public void foodChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    public void joinTeam(Player player) {
        if (goodMembers.size() > badMembers.size()) {
            badMembers.add(player.getName());
            player.sendMessage(ChatFormat.info("You have joined the bad team."));
        } else {
            goodMembers.add(player.getName());
            player.sendMessage(ChatFormat.info("You have joined the good team."));
        }
    }

    List<Block> goodBlocks = new ArrayList<>();
    List<Block> badBlocks = new ArrayList<>();

    public void ChangeBlockfTeam(Player p, int radius, Location ml) {
        double radhalf = radius / 2;
        int xmin = (int) (ml.getBlockX() - radhalf);
        int zmin = (int) (ml.getBlockZ() - radhalf);
        int ymin = (int) (ml.getBlockY() - radhalf);
        int xm = (int) (ml.getBlockX() + radhalf + 1.0D);
        int zm = (int) (ml.getBlockZ() + radhalf + 1.0D);
        int ym = (int) (ml.getBlockY() + radhalf + 1.0D);

        for (int x = xmin; x != xm; x++) {
            for (int z = zmin; z != zm; z++) {
                for (int y = ymin; y != ym; y++) {
                    Location loc = new Location(p.getWorld(), x, y, z);
                    Block bl = loc.getBlock();
                    if ((!bl.getType().equals(Material.AIR) && !bl.getType().equals(Material.STONE_PLATE))) {
                        if (isBlockBlacklisted(bl) == false) {
                            if (badMembers.contains(p.getName())) {
                                if (!badBlocks.contains(bl)) {
                                    if (goodBlocks.contains(bl)) {
                                        goodBlocks.remove(bl);
                                        badBlocks.add(bl);
                                        bl.setType(Material.STAINED_CLAY);
                                        bl.setData(getColor(ChatColor.BLUE).getData());
                                    } else {
                                        badBlocks.add(bl);
                                        bl.setType(Material.STAINED_CLAY);
                                        bl.setData(getColor(ChatColor.BLUE).getData());
                                    }
                                }
                            } else {
                                if (!goodBlocks.contains(bl)) {
                                    if (badBlocks.contains(bl)) {
                                        badBlocks.remove(bl);
                                        goodBlocks.add(bl);
                                        bl.setType(Material.STAINED_CLAY);
                                        bl.setData(getColor(ChatColor.GOLD).getData());
                                    } else {
                                        goodBlocks.add(bl);
                                        bl.setType(Material.STAINED_CLAY);
                                        bl.setData(getColor(ChatColor.GOLD).getData());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public DyeColor getColor(ChatColor color) {
        switch (color) {
            case GOLD:
                return DyeColor.ORANGE;
            case BLUE:
                return DyeColor.BLUE;
        }
        return null;
    }

    /*
    Maps
     */
    String map;

    List<String> maps = new ArrayList<>();

    public void addAllMaps() {
        if (plugin.getConfig().getConfigurationSection("Maps") == null) {
            return;
        }
        for (String string : plugin.getConfig().getConfigurationSection("Maps").getKeys(false)) {
            if (plugin.getConfig().getString("LastMap") == null) {
                maps.add(string);
                return;
            }
            if (plugin.getConfig().getConfigurationSection("Maps").getKeys(false).size() > 1) {
                if (!plugin.getConfig().getString("LastMap").equalsIgnoreCase(string)) {
                    maps.add(string);
                }
            } else {
                maps.add(string);
            }
        }
    }

    public void setMap() {
        if (maps.isEmpty()) {
            return;
        }
        if (maps.size() > 1) {
            Random random = new Random();
            String randomMap = maps.get(random.nextInt(maps.size()));
            if (!plugin.getConfig().getString("LastMap").equalsIgnoreCase(randomMap)) {
                this.map = randomMap;
                plugin.getConfig().set("LastMap", randomMap);
                plugin.saveConfig();
            }
        } else {
            this.map = maps.get(0);
            plugin.getConfig().set("LastMap", maps.get(0));
            plugin.saveConfig();
        }
    }

    public Location getGoodSpawn() {
        double x = plugin.getConfig().getDouble("Maps." + map + ".goodx");
        double y = plugin.getConfig().getDouble("Maps." + map + ".goody");
        double z = plugin.getConfig().getDouble("Maps." + map + ".goodz");
        World world = Bukkit.getWorld(plugin.getConfig().getString("Maps." + map + ".world"));
        return new Location(world, x, y, z);
    }

    public Location getBadSpawn() {
        double x = plugin.getConfig().getDouble("Maps." + map + ".badx");
        double y = plugin.getConfig().getDouble("Maps." + map + ".bady");
        double z = plugin.getConfig().getDouble("Maps." + map + ".badz");
        World world = Bukkit.getWorld(plugin.getConfig().getString("Maps." + map + ".world"));
        return new Location(world, x, y, z);
    }

    public Location getSpecSpawn() {
        double x = plugin.getConfig().getDouble("Maps." + map + ".specx");
        double y = plugin.getConfig().getDouble("Maps." + map + ".specy");
        double z = plugin.getConfig().getDouble("Maps." + map + ".specz");
        World world = Bukkit.getWorld(plugin.getConfig().getString("Maps." + map + ".world"));
        return new Location(world, x, y, z);
    }

    public Location getLobbySpawn() {
        if (plugin.getConfig().getString("Lobby") != null) {
            double x = plugin.getConfig().getDouble("Lobby.x");
            double y = plugin.getConfig().getDouble("Lobby.y");
            double z = plugin.getConfig().getDouble("Lobby.z");
            int yaw = plugin.getConfig().getInt("Lobby.yaw");
            int pitch = plugin.getConfig().getInt("Lobby.pitch");
            World world = Bukkit.getWorld(plugin.getConfig().getString("Lobby.world"));
            return new Location(world, x, y, z, yaw, pitch);
        } else {
            return null;
        }
    }

    public boolean isBlockBlacklisted(Block block) {
        List<String> blacklisted = plugin.getConfig().getStringList("BlockBlacklist");
        for (int i = 0; i < blacklisted.size(); i++) {
            if (blacklisted.get(i).contains("/")) {
                String[] array = blacklisted.get(i).split("/");
                if (block.getTypeId() == Integer.parseInt(array[0])) {
                    if (block.getData() == Integer.parseInt(array[1])) {
                        return true;
                    }
                }
            } else {
                if (block.getTypeId() == Integer.parseInt(blacklisted.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void inWater(PlayerMoveEvent event) {
        Block block = event.getPlayer().getLocation().getBlock();
        if(block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) {
            event.getPlayer().damage(6);
        }
    }

    /*
    Kit Inventory
     */

    public ItemStack kitItem() {
        ItemStack stack = new ItemStack(Material.COMPASS);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Right-Click - Kit Menu");
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack specialsItem() {
        ItemStack stack = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Right-Click - Specials Menu");
        stack.setItemMeta(meta);
        return stack;
    }

    public Inventory specialsInv(Player player) {
        Inventory inv = Bukkit.createInventory(player, 9, ChatFormat.info("Specials"));
        for (Special special : this.specials) {
            if (sqlManager.specials.hasSpecial(player.getUniqueId(), special) == true) {
                ItemStack stack = new ItemStack(special.getItem());
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + special.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "You currently own this kit.");
                lore.add(ChatColor.GREEN + "Click to select.");
                meta.setLore(lore);
                stack.setItemMeta(meta);
                inv.addItem(stack);
            } else {
                ItemStack stack = new ItemStack(Material.REDSTONE);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(ChatColor.RED + special.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "You don't own this special.");
                lore.add(ChatColor.RED + "Click to buy.");
                lore.add(ChatColor.YELLOW + "Price: " + special.getPrice());
                meta.setLore(lore);
                stack.setItemMeta(meta);
                inv.addItem(stack);
            }
        }
        player.openInventory(inv);
        return inv;
    }

    public Inventory kitInv(Player player) {
        Inventory inv = Bukkit.createInventory(player, 9, ChatFormat.info("Kits"));
        for (Kit kit : getKits()) {
            if (sqlManager.kits.hasKit(player.getUniqueId(), kit) == true) {
                ItemStack stack = new ItemStack(kit.getItem());
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + kit.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "You currently own this kit.");
                lore.add(ChatColor.GREEN + "Click to select.");
                meta.setLore(lore);
                stack.setItemMeta(meta);
                inv.addItem(stack);
            } else if (sqlManager.kits.hasKit(player.getUniqueId(), kit) == false) {
                ItemStack stack = new ItemStack(Material.REDSTONE);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(ChatColor.RED + kit.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "You don't own this kit.");
                lore.add(ChatColor.RED + "Click to buy.");
                lore.add(ChatColor.YELLOW + "Price: " + kit.getAmount());
                meta.setLore(lore);
                stack.setItemMeta(meta);
                inv.addItem(stack);
            } else try {
                if (getKit(player).getName().equalsIgnoreCase(kit.getName())) {
                    ItemStack stack = new ItemStack(kit.getItem());
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(ChatColor.GREEN + kit.getName() + ChatColor.GREEN + " (Selected)");
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.YELLOW + "You currently own this kit.");
                    lore.add(ChatColor.GREEN + "Click to select.");
                    meta.setLore(lore);
                    stack.setItemMeta(meta);
                    inv.addItem(stack);
                }
            } catch (NoKitException e) {
                e.printStackTrace();
            }
        }
        player.openInventory(inv);
        return inv;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getTitle().equalsIgnoreCase(ChatFormat.info("Kits"))) {

            event.setCancelled(true);

            if (event.getCurrentItem().getType() == Material.REDSTONE) {
                player.closeInventory();
                for (Kit kit : getKits()) {
                    String kitName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                    if (kit.getName().equalsIgnoreCase(kitName)) {
                        confirmBuy(player, kit);
                    }
                }
            }

            for (Kit kit : getKits()) {
                if (event.getCurrentItem().getType() == kit.getItem()) {
                    String kitName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                    if (kit.getName().equalsIgnoreCase(kitName)) {
                        player.closeInventory();
                        setPlayerKit(player, kit);
                        player.sendMessage(ChatFormat.info("You just equipped the " + kit.getName() + " Kit."));
                    }
                }
            }
        }

        if (event.getInventory().getTitle().equalsIgnoreCase(ChatFormat.info("Specials"))) {

            event.setCancelled(true);

            if (event.getCurrentItem().getType() == Material.REDSTONE) {
                player.closeInventory();
                for (Special special : this.specials) {
                    String specialName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                    if (special.getName().equalsIgnoreCase(specialName)) {
                        confirmBuySpecial(player, special);
                    }
                }
            }

            for (Special special : this.specials) {
                if (event.getCurrentItem().getType() == special.getItem()) {
                    String specialName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                    if (special.getName().equalsIgnoreCase(specialName)) {
                        player.closeInventory();
                        player.sendMessage(ChatFormat.info("If you want to use this special please select the " + special.getKit().getName() + " Kit."));
                    }
                }
            }
        }

        if (event.getInventory().getTitle().equalsIgnoreCase("Kit Confirm")) {
            Kit kit = confirmingKit.get(player);
            event.setCancelled(true);

            if (event.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
                if (sqlManager.coins.getCoins(player.getUniqueId()) >= kit.getAmount()) {
                    removeCoins(player, kit.getAmount());
                    sqlManager.kits.addKit(player.getUniqueId(), kit);
                    player.sendMessage(ChatFormat.info("You have successfully bought the " + kit.getName() + " Kit."));
                    player.closeInventory();
                    confirmingKit.remove(player);
                } else {
                    player.sendMessage(ChatFormat.info("Sorry you don't have enough coins to buy this."));
                    player.closeInventory();
                    confirmingKit.remove(player);
                }
                player.closeInventory();
            }

            if (event.getCurrentItem().getType() == Material.REDSTONE_BLOCK) {
                player.sendMessage(ChatFormat.info("You have declined the purchase of the " + kit.getName()) + " Kit.");
                player.closeInventory();
                confirmingKit.remove(player);
            }
        }

        if (event.getInventory().getTitle().equalsIgnoreCase("Special Confirm")) {
            Special special = confirmingSpecial.get(player);
            event.setCancelled(true);

            if (event.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
                if (sqlManager.coins.getCoins(player.getUniqueId()) >= special.getPrice()) {
                    removeCoins(player, special.getPrice());
                    sqlManager.specials.addSpecial(player.getUniqueId(), special);
                    player.sendMessage(ChatFormat.info("You have successfully bought the " + special.getName()));
                    player.closeInventory();
                    confirmingSpecial.remove(player);
                } else {
                    player.sendMessage(ChatFormat.info("Sorry you don't have enough coins to buy this."));
                    player.closeInventory();
                    confirmingSpecial.remove(player);
                }
                player.closeInventory();
            }

            if (event.getCurrentItem().getType() == Material.REDSTONE_BLOCK) {
                player.sendMessage(ChatFormat.info("You have declined the purchase of the " + special.getName()));
                player.closeInventory();
                confirmingSpecial.remove(player);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action a = event.getAction();
        ItemStack stack = player.getItemInHand();

        if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
            if (stack.equals(kitItem())) {
                kitInv(player);
            } else if (stack.equals(specialsItem())) {
                specialsInv(player);
            }
        }
    }

    HashMap<Player, Kit> confirmingKit = new HashMap<>();

    public Inventory confirmBuy(Player player, Kit kit) {
        Inventory inv = Bukkit.createInventory(player, 9, "Kit Confirm");

        ItemStack yes = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta yesMeta = yes.getItemMeta();
        yesMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "YES");
        yes.setItemMeta(yesMeta);

        ItemStack no = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta noMeta = no.getItemMeta();
        noMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "NO");
        no.setItemMeta(noMeta);

        ItemStack kitItem = new ItemStack(kit.getItem());
        ItemMeta kitMeta = kitItem.getItemMeta();
        kitMeta.setDisplayName(ChatColor.YELLOW + kit.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Click Yes or No if you want");
        lore.add(ChatColor.GREEN + "to purchase this kit.");
        kitMeta.setLore(lore);
        kitItem.setItemMeta(kitMeta);

        inv.setItem(0, yes);
        inv.setItem(1, yes);

        inv.setItem(4, kitItem);

        inv.setItem(7, no);
        inv.setItem(8, no);

        confirmingKit.put(player, kit);
        player.openInventory(inv);
        return inv;
    }

    HashMap<Player, Special> confirmingSpecial = new HashMap<>();

    public Inventory confirmBuySpecial(Player player, Special special) {
        Inventory inv = Bukkit.createInventory(player, 9, "Special Confirm");

        ItemStack yes = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta yesMeta = yes.getItemMeta();
        yesMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "YES");
        yes.setItemMeta(yesMeta);

        ItemStack no = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta noMeta = no.getItemMeta();
        noMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "NO");
        no.setItemMeta(noMeta);

        ItemStack kitItem = new ItemStack(special.getItem());
        ItemMeta kitMeta = kitItem.getItemMeta();
        kitMeta.setDisplayName(ChatColor.YELLOW + special.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Click Yes or No if you want");
        lore.add(ChatColor.GREEN + "to purchase this special.");
        kitMeta.setLore(lore);
        kitItem.setItemMeta(kitMeta);

        inv.setItem(0, yes);
        inv.setItem(1, yes);

        inv.setItem(4, kitItem);

        inv.setItem(7, no);
        inv.setItem(8, no);

        confirmingSpecial.put(player, special);
        player.openInventory(inv);
        return inv;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (confirmingKit.containsKey(player)) {
            confirmingKit.remove(player);
        }
        if (confirmingSpecial.containsKey(player)) {
            confirmingSpecial.remove(player);
        }
    }

    /*
    Scoreboard
     */

    public Scoreboard Gameboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = scoreboard.registerNewObjective("Splatoon", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Mineskill.uk");

        obj.getScore("       ").setScore(8);
        obj.getScore(ChatColor.YELLOW + "Current Class:").setScore(7);
        try {
            obj.getScore(ChatColor.AQUA + "  " + getKit(player).getName()).setScore(6);
        } catch (NoKitException e) {
            e.printStackTrace();
        }
        obj.getScore("       ").setScore(5);
        obj.getScore(ChatColor.YELLOW + "Coins:").setScore(4);
        obj.getScore(ChatColor.GOLD +"  $" + Integer.toString(sqlManager.coins.getCoins(player.getUniqueId()))).setScore(3);
        obj.getScore("     ").setScore(2);
        obj.getScore(ChatColor.YELLOW + "Kills:").setScore(1);
        obj.getScore(ChatColor.AQUA + "  " + sqlManager.kills.getKills(player.getUniqueId())).setScore(0);

        player.setScoreboard(scoreboard);
        return scoreboard;
    }

    public void setKills(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        scoreboard.resetScores(ChatColor.AQUA + "  " + sqlManager.kills.getKills(player.getUniqueId()));
        sqlManager.kills.addKills(player.getUniqueId(), 1);
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.AQUA + "  " + sqlManager.kills.getKills(player.getUniqueId())).setScore(0);
    }

    public void setPlayerKit(Player player, Kit kit) {
        Scoreboard scoreboard = player.getScoreboard();
        try {
            scoreboard.resetScores(ChatColor.AQUA + "  " + getKit(player).getName());
        } catch (NoKitException e) {
            e.printStackTrace();
        }
        setKit(player, kit);
        try {
            scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.AQUA + "  " + getKit(player).getName()).setScore(6);
        } catch (NoKitException e) {
            e.printStackTrace();
        }
    }

    public void addCoins(Player player, int amount) {
        Scoreboard scoreboard = player.getScoreboard();
        scoreboard.resetScores(ChatColor.GOLD +"  $" + Integer.toString(sqlManager.coins.getCoins(player.getUniqueId())));
        sqlManager.coins.addCoins(player.getUniqueId(), amount);
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.GOLD +"  $" + Integer.toString(sqlManager.coins.getCoins(player.getUniqueId()))).setScore(3);
    }

    public void removeCoins(Player player, int amount) {
        Scoreboard scoreboard = player.getScoreboard();
        scoreboard.resetScores(ChatColor.GOLD +"  $" + Integer.toString(sqlManager.coins.getCoins(player.getUniqueId())));
        sqlManager.coins.removeCoins(player.getUniqueId(), amount);
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.GOLD +"  $" + Integer.toString(sqlManager.coins.getCoins(player.getUniqueId()))).setScore(3);
    }

    public void resetCoins(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        scoreboard.resetScores(ChatColor.GOLD +"  $" + Integer.toString(sqlManager.coins.getCoins(player.getUniqueId())));
        sqlManager.coins.setCoins(player.getUniqueId(), 0);
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.GOLD +"  $" + Integer.toString(sqlManager.coins.getCoins(player.getUniqueId()))).setScore(3);
    }

    /*
    Paint
     */

    @EventHandler
    public void paint(ProjectileHitEvent event) {
        if(event.getEntity() instanceof Arrow) {
            if(sarrow.contains(event.getEntity())) {
                Arrow arrow = (Arrow) event.getEntity();
                sarrow.remove(arrow);
                arrow.remove();
            } else if(ssarrow.contains(event.getEntity())) {
                Arrow arrow = (Arrow) event.getEntity();
                arrow.getWorld().createExplosion(arrow.getLocation().getX(), arrow.getLocation().getY(), arrow.getLocation().getZ(), 1F);
                sarrow.remove(arrow);
                arrow.remove();
            }
        }

        if (event.getEntity().getShooter() instanceof Player) {
            if (getGameState() == GameState.LIVE) {
                Player player = (Player) event.getEntity().getShooter();
                try {
                    if (getKit(player).getName().equalsIgnoreCase(new SplattershotJr().getName())) {
                        if (event.getEntity() instanceof Snowball) {
                            ChangeBlockfTeam(player, 3, event.getEntity().getLocation());
                            event.getEntity().remove();
                        }
                    } else if (getKit(player).getName().equalsIgnoreCase(new SplatterShot().getName())) {
                        if (event.getEntity() instanceof Egg) {
                            ChangeBlockfTeam(player, 3, event.getEntity().getLocation());
                            event.getEntity().remove();
                        }
                    } else if (getKit(player).getName().equalsIgnoreCase(new SplatCharger().getName())) {
                        if (event.getEntity() instanceof Arrow) {
                            ChangeBlockfTeam(player, 4, event.getEntity().getLocation());
                            event.getEntity().remove();
                        }
                    } else if (getKit(player).getName().equalsIgnoreCase(new InkBrush().getName())) {
                        if (event.getEntity() instanceof EnderPearl) {
                            ChangeBlockfTeam(player, 10, event.getEntity().getLocation());
                            event.getEntity().getLocation().getWorld().strikeLightning(event.getEntity().getLocation());
                            for (Entity entity : event.getEntity().getNearbyEntities(3, 3, 3)) {
                                if (entity instanceof Player) {
                                    if (event.getEntity().getShooter() instanceof Player) {
                                        if (!getPlayerTeam((Player) entity).getName().equalsIgnoreCase(getPlayerTeam((Player) event.getEntity().getShooter()).getName())) {
                                            killplayer(player, (Player) entity, "Ink Strike");
                                        }
                                    }
                                }
                            }
                            event.getEntity().remove();
                        }
                    }
                } catch (NoKitException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void damagePlayer(Player player, Player damager, double damage) {
        if(isPlayeronSameTeam(player, damager) == true) {
            return;
        }

        player.damage(damage);

    }

    public void killplayer(Player killer, final Player killed, String reason) {
        if (getPlayerTeam(killer).getName().equalsIgnoreCase(getPlayerTeam(killed).getName())) {
            return;
        }

        killed.setHealth(20);

        Bukkit.getServer().broadcastMessage(ChatFormat.info(killer.getName() + " killed " + killed.getName()));

        addCoins(killer, 10);
        setKills(killer);
        killer.sendMessage(ChatFormat.info("You have been awarded 10 coins for killing, " + killed.getName()));

        killed.setHealth(20);
        killed.setGameMode(GameMode.SPECTATOR);
        killed.teleport(getSpecSpawn());
        if (gotSpecial.containsKey(killed)) {
            gotSpecial.remove(killed);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                killed.getInventory().clear();
                if (goodMembers.contains(killed.getName())) {
                    killed.teleport(getGoodSpawn());
                    killed.getInventory().setHelmet(new ItemBuilder(Material.LEATHER_HELMET).color(Color.ORANGE).build());
                    killed.getInventory().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.ORANGE).build());
                    killed.getInventory().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.ORANGE).build());
                    killed.getInventory().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).color(Color.ORANGE).build());
                } else {
                    killed.teleport(getBadSpawn());
                    killed.getInventory().setHelmet(new ItemBuilder(Material.LEATHER_HELMET).color(Color.BLUE).build());
                    killed.getInventory().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.BLUE).build());
                    killed.getInventory().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.BLUE).build());
                    killed.getInventory().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).color(Color.BLUE).build());
                }
                spawn.add(killed.getName());
                killed.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 1));
                killed.setGameMode(GameMode.SURVIVAL);
                try {
                    getKit(killed).apply(killed);
                } catch (NoKitException e) {
                    e.printStackTrace();
                }
            }
        }, 100);
    }

    public void killPlayer(final Player killed) {
        killed.setHealth(20);

        Bukkit.getServer().broadcastMessage(ChatFormat.info(killed.getName() + " has died"));

        killed.setHealth(20);
        killed.setGameMode(GameMode.SPECTATOR);
        killed.teleport(getSpecSpawn());
        if (gotSpecial.containsKey(killed)) {
            gotSpecial.remove(killed);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                killed.getInventory().clear();
                if (goodMembers.contains(killed.getName())) {
                    killed.teleport(getGoodSpawn());
                    killed.getInventory().setHelmet(new ItemBuilder(Material.LEATHER_HELMET).color(Color.ORANGE).build());
                    killed.getInventory().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.ORANGE).build());
                    killed.getInventory().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.ORANGE).build());
                    killed.getInventory().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).color(Color.ORANGE).build());
                } else {
                    killed.teleport(getBadSpawn());
                    killed.getInventory().setHelmet(new ItemBuilder(Material.LEATHER_HELMET).color(Color.BLUE).build());
                    killed.getInventory().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.BLUE).build());
                    killed.getInventory().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.BLUE).build());
                    killed.getInventory().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).color(Color.BLUE).build());
                }
                spawn.add(killed.getName());
                killed.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 1));
                killed.setGameMode(GameMode.SURVIVAL);
                try {
                    getKit(killed).apply(killed);
                } catch (NoKitException e) {
                    e.printStackTrace();
                }
            }
        }, 100);
    }

    /*
    Move Events
     */

    @EventHandler
    public void paintMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (getGameState() != GameState.LIVE) {
            return;
        }
        if (!player.isOnGround()) {
            return;
        }
        try {
            if (getKit(player).getName().equalsIgnoreCase(new InkBrush().getName())) {
                if (!player.getItemInHand().equals(new InkBrush().bow())) {
                    return;
                }
                if (player.getLevel() > 0) {
                    if (isPlayerOnTeamBlock(player) == false) {
                        ChangeBlockfTeam(player, 3, player.getLocation());
                        player.setLevel(player.getLevel() - 1);
                    }
                }
            } else if (getKit(player).getName().equalsIgnoreCase(new SplatRoller().getName())) {
                if (!player.getItemInHand().equals(new SplatRoller().bow())) {
                    return;
                }
                if (player.getLevel() > 0) {
                    if (isPlayerOnTeamBlock(player) == false) {
                        ChangeBlockfTeam(player, 4, player.getLocation());
                        player.setLevel(player.getLevel() - 1);
                    }
                }
            }
        } catch (NoKitException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Block block = event.getPlayer().getLocation().subtract(0, 1, 0).getBlock();
        if (getGameState() == GameState.LIVE) {
            if (kraken.contains(event.getPlayer().getName())) {
                for (Entity players : event.getPlayer().getNearbyEntities(2, 2, 2)) {
                    if (players instanceof Player) {
                        Player playerz = (Player) players;
                        if (getPlayerTeam(event.getPlayer()).getName().equalsIgnoreCase(getPlayerTeam(playerz).getName())) {
                            return;
                        } else {
                            killplayer(event.getPlayer(), playerz, "Kraken");
                        }
                    }
                }
                ChangeBlockfTeam(event.getPlayer(), 3, event.getPlayer().getLocation());
                new ParticleEffectUtil().playAt(EnumParticle.FIREWORKS_SPARK, event.getFrom());
                new ParticleEffectUtil().playAt(EnumParticle.FIREWORKS_SPARK, event.getTo());
                new ParticleEffectUtil().playAt(EnumParticle.FIREWORKS_SPARK, event.getPlayer().getLocation());
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000, 4));
                return;
            }
            if (!squidForm.contains(event.getPlayer().getName())) {
                if (goodMembers.contains(event.getPlayer().getName())) {
                    if (block.getType() == Material.STAINED_CLAY) {
                        if (block.getData() == getColor(ChatColor.BLUE).getData()) {
                            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 2));
                        } else {
                            for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
                                event.getPlayer().removePotionEffect(potionEffect.getType());
                            }
                        }
                    } else {
                        for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
                            event.getPlayer().removePotionEffect(potionEffect.getType());
                        }
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 1));
                    }
                }
                if (badMembers.contains(event.getPlayer().getName())) {
                    if (block.getType() == Material.STAINED_CLAY) {
                        if (block.getData() == getColor(ChatColor.GOLD).getData()) {
                            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 2));
                        } else {
                            for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
                                event.getPlayer().removePotionEffect(potionEffect.getType());
                            }
                            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 1));
                        }
                    } else {
                        for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
                            event.getPlayer().removePotionEffect(potionEffect.getType());
                        }
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 1));
                    }
                }
            } else {
                if (goodMembers.contains(event.getPlayer().getName())) {
                    if (block.getType() == Material.STAINED_CLAY) {
                        if (block.getData() == getColor(ChatColor.BLUE).getData()) {
                            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 2));
                        } else if (block.getData() == getColor(ChatColor.GOLD).getData()) {
                            for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
                                event.getPlayer().removePotionEffect(potionEffect.getType());
                            }
                            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 2));
                            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1000000, 2));
                        } else {
                            for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
                                event.getPlayer().removePotionEffect(potionEffect.getType());
                            }
                            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 1));
                        }
                    } else {
                        for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
                            event.getPlayer().removePotionEffect(potionEffect.getType());
                        }
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 1));
                    }
                }
                if (badMembers.contains(event.getPlayer().getName())) {
                    if (block.getType() == Material.STAINED_CLAY) {
                        if (block.getData() == getColor(ChatColor.GOLD).getData()) {
                            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 2));
                        } else if (block.getData() == getColor(ChatColor.BLUE).getData()) {
                            for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
                                event.getPlayer().removePotionEffect(potionEffect.getType());
                            }
                            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 2));
                            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100000, 2));
                        } else {
                            for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
                                event.getPlayer().removePotionEffect(potionEffect.getType());
                            }
                            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 1));
                        }
                    } else {
                        for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
                            event.getPlayer().removePotionEffect(potionEffect.getType());
                        }
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 1));
                    }
                }
            }
        }
    }

    /*
    Player Damaging
     */

    List<String> spawn = new ArrayList<>();

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Projectile)) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!(((Projectile) event.getDamager()).getShooter() instanceof Player)) {
            return;
        }
        if (getGameState() != GameState.LIVE) {
            return;
        }

        Player killer = (Player) ((Projectile) event.getDamager()).getShooter();
        final Player killed = (Player) event.getEntity();

        if (ssjrs.contains(killed.getName())) {
            return;
        }
        if (killer == killed) {
            return;
        }
        if (getPlayerTeam(killer).getName().equalsIgnoreCase(getPlayerTeam(killed).getName())) {
            return;
        }
        if (spawn.contains(killed.getName())) {
            killer.sendMessage(ChatFormat.info("Player still has spawn protection."));
            return;
        }

        try {
            if (getKit(killer).getName().equalsIgnoreCase(new SplattershotJr().getName())) {
                damagePlayer(killed, killer, 8);
            } else if (getKit(killer).getName().equalsIgnoreCase(new SplatterShot().getName())) {
                damagePlayer(killed, killer, getRandom(8, 10));
            } else if (getKit(killer).getName().equalsIgnoreCase(new Squiffer().getName())) {
                damagePlayer(killed, killer, 10);
            } else if (getKit(killer).getName().equalsIgnoreCase(new SplatCharger().getName())) {
                damagePlayer(killed, killer, 10);
            }
        } catch (NoKitException e) {
            e.printStackTrace();
        }

    }

    public int getRandom(int lower, int upper) {
        return new Random().nextInt((upper - lower) + 1) + lower;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player killer = event.getEntity().getKiller();
            if (getGameState() == GameState.LIVE) {
                event.setDeathMessage("");
                event.setKeepInventory(true);
                event.setKeepLevel(true);
                killplayer(killer, event.getEntity(), "Ink");
            }
        } else if(event.getEntity() instanceof Projectile) {
            Player killer = (Player) ((Projectile) event.getEntity()).getShooter();
            if (getGameState() == GameState.LIVE) {
                event.setDeathMessage("");
                event.setKeepInventory(true);
                event.setKeepLevel(true);
                killplayer(killer, event.getEntity(), "Ink");
            }
        } else {
            event.setDeathMessage("");
            killPlayer(event.getEntity());
        }
    }

    @EventHandler
    public void spawn(PlayerMoveEvent event) {
        if (spawn.contains(event.getPlayer().getName())) {
            spawn.remove(event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (getGameState() == GameState.LIVE) {
            if (event.getDamager() instanceof Player) {
                if (event.getEntity() instanceof Player) {
                    Player damager = (Player) event.getDamager();
                    Player damaged = (Player) event.getEntity();
                    if(isPlayeronSameTeam(damaged, damager) == true) {
                        return;
                    }
                    try {
                        if (getKit((Player) event.getDamager()).getName().equalsIgnoreCase(new InkBrush().getName())) {
                            if(((Player) event.getDamager()).getItemInHand().equals(new InkBrush().bow())) {
                                damagePlayer(damaged, damager, 14);
                            }
                        } else if (getKit((Player) event.getDamager()).getName().equalsIgnoreCase(new SplatRoller().getName())) {
                            if(((Player) event.getDamager()).getItemInHand().equals(new SplatRoller().bow())) {
                                damagePlayer(damaged, damager, 20);
                            }
                        } else {
                            damagePlayer(damaged, damager, 0.5);
                        }
                    } catch (NoKitException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            event.setCancelled(true);
        }
    }

    public boolean isPlayeronSameTeam(Player player, Player p) {
        if(getPlayerTeam(player).getName().equalsIgnoreCase(getPlayerTeam(p).getName())) {
            return true;
        } else {
            return false;
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (getGameState() != GameState.LIVE) {
            event.setCancelled(true);
        } else if(event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    /*
    KIT SUB ITEM MANAGERS
     */

    @EventHandler
    public void onDelayedGrenades(PotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity().getShooter();
        try {
            if (getKit(player).getName().equalsIgnoreCase(new SplattershotJr().getName())) {
                event.setCancelled(true);
                for (Entity ent : event.getEntity().getNearbyEntities(3, 3, 3)) {
                    if (ent instanceof Player) {
                        if(!ssjrs.contains(ent)) {
                            killplayer(player, (Player) ent, "Grenade");
                        }
                    }
                }
                startDelay(player, event.getEntity().getLocation());
            }
        } catch (NoKitException e) {
            e.printStackTrace();
        }
        try {
            if (getKit(player).getName().equalsIgnoreCase(new SplatterShot().getName())) {
                event.setCancelled(true);
                for (Entity ent : event.getEntity().getNearbyEntities(3, 3, 3)) {
                    if (ent instanceof Player) {
                        if(!ssjrs.contains(ent)) {
                            killplayer(player, (Player) ent, "Grenade");
                        }
                    }
                }
                grenade(player, event.getEntity().getLocation());
            }
        } catch (NoKitException e) {
            e.printStackTrace();
        }
        try {
            if (getKit(player).getName().equalsIgnoreCase(new SplatCharger().getName())) {
                event.setCancelled(true);
                for (Entity ent : event.getEntity().getNearbyEntities(3, 3, 3)) {
                    if (ent instanceof Player) {
                        if(!ssjrs.contains(ent)) {
                            killplayer(player, (Player) ent, "Grenade");
                        }
                    }
                }
                startDelay(player, event.getEntity().getLocation());
            }
        } catch (NoKitException e) {
            e.printStackTrace();
        }
        try {
            if (getKit(player).getName().equalsIgnoreCase(new SplatRoller().getName())) {
                event.setCancelled(true);
                for (Entity ent : event.getEntity().getNearbyEntities(3, 3, 3)) {
                    if (ent instanceof Player) {
                        if(!ssjrs.contains(ent)) {
                            killplayer(player, (Player) ent, "Grenade");
                        }
                    }
                }
                grenade(player, event.getEntity().getLocation());
            }
        } catch (NoKitException e) {
            e.printStackTrace();
        }
    }

    public void startDelay(final Player player, final Location location) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
            @Override
            public void run() {
                ChangeBlockfTeam(player, 5, location);
                location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 1F, false, false);
            }
        }, 60L);
    }

    public void grenade(final Player player, final Location location) {
        ChangeBlockfTeam(player, 4, location);
        location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 1F, false, false);
    }
    /*
    SQUID FORM
     */

    List<String> squidForm = new ArrayList<>();

    public void checkHand(final Player player) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (player.getItemInHand().equals(new SplattershotJr().squidItem())) {
                    if (!squidForm.contains(player.getName())) {
                        squidForm.add(player.getName());
                        MobDisguise mobDisguise = new MobDisguise(DisguiseType.SQUID);
                        DisguiseAPI.disguiseToAll(player, mobDisguise);
                    }
                } else {
                    if (squidForm.contains(player.getName())) {
                        squidForm.remove(player.getName());
                        DisguiseAPI.undisguiseToAll(player);
                    }
                }
            }
        }, 0L, 0L);
    }

    public void checkLand(final Player player) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (squidForm.contains(player.getName())) {
                    if (isPlayerOnTeamBlock(player) == true) {
                        try {
                            if (getKit(player).getName().equalsIgnoreCase(new SplattershotJr().getName())) {
                                replenishAmmo(player, new SplattershotJr());
                            } else if (getKit(player).getName().equalsIgnoreCase(new SplatterShot().getName())) {
                                replenishAmmo(player, new SplatterShot());
                            } else if (getKit(player).getName().equalsIgnoreCase(new Squiffer().getName())) {
                                replenishAAmmo(player, new Squiffer());
                            } else if (getKit(player).getName().equalsIgnoreCase(new SplatCharger().getName())) {
                                replenishAmmo(player, new SplatCharger());
                            } else if (getKit(player).getName().equalsIgnoreCase(new InkBrush().getName())) {
                                replenishLevels(player, new InkBrush());
                            } else if (getKit(player).getName().equalsIgnoreCase(new SplatRoller().getName())) {
                                replenishLevels(player, new SplatRoller());
                            }
                        } catch (NoKitException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, 0L, 10L);
    }

    public void jumpSquid(final Player player) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(), new Runnable() {
            @Override
            public void run() {
                if(isPlayerOnTeamBlock(player) == true) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100000000, 2, true, false));
                } else if(isPlayerOnTeamBlock(player) == false) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000000, 2, true, false));
                } else {
                    for(PotionEffect effect : player.getActivePotionEffects()) {
                        player.removePotionEffect(effect.getType());
                    }
                }
            }
        }, 0L, 0L);
    }

    public void checkALand(final Player player) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (squidForm.contains(player.getName())) {
                    if (isPlayerOnTeamBlock(player) == true) {
                        try {
                            if (getKit(player).getName().equalsIgnoreCase(new Squiffer().getName())) {
                                replenishAAmmo(player, new Squiffer());
                            }
                        } catch (NoKitException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, 0L, 60L);
    }

    public void replenishAmmo(final Player player, final SplatoonKit splatoonKit) {
        if (squidForm.contains(player.getName())) {
            if (isPlayerOnTeamBlock(player) == true) {
                if (!player.getInventory().containsAtLeast(splatoonKit.getMainItem(), 16)) {
                    player.getInventory().addItem(splatoonKit.getMainItem());
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                }
            }
        }
    }

    public void replenishAAmmo(final Player player, final SplatoonKit splatoonKit) {
        if (squidForm.contains(player.getName())) {
            if (isPlayerOnTeamBlock(player) == true) {
                if (!player.getInventory().containsAtLeast(splatoonKit.getMainItem(), 6)) {
                    player.getInventory().addItem(splatoonKit.getMainItem());
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                }
            }
        }
    }

    public void replenishLevels(final Player player, final SplatoonKit splatoonKit) {
        if (squidForm.contains(player.getName())) {
            if (isPlayerOnTeamBlock(player) == true) {
                if (!(player.getLevel() >= 16)) {
                    player.setLevel(player.getLevel() + 1);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                }
            }
        }
    }

    public boolean isPlayerOnTeamBlock(Player player) {
        Block block = player.getLocation().subtract(0, 1, 0).getBlock();
        GoodTeam goodTeam = new GoodTeam(goodMembers);
        BadTeam badTeam = new BadTeam(badMembers);
        if (goodTeam.getMembers().contains(player.getName())) {
            if (block.getType() == Material.STAINED_CLAY) {
                if (block.getData() == getColor(goodTeam.getColor()).getData()) {
                    return true;
                }
            }
        }
        if (badTeam.getMembers().contains(player.getName())) {
            if (block.getType() == Material.STAINED_CLAY) {
                if (block.getData() == getColor(badTeam.getColor()).getData()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isTeamBlock(Player player, Block block) {
        GoodTeam goodTeam = new GoodTeam(goodMembers);
        BadTeam badTeam = new BadTeam(badMembers);
        if (goodTeam.getMembers().contains(player.getName())) {
            if (block.getType() == Material.STAINED_CLAY) {
                if (block.getData() == getColor(goodTeam.getColor()).getData()) {
                    return true;
                }
            }
        }
        if (badTeam.getMembers().contains(player.getName())) {
            if (block.getType() == Material.STAINED_CLAY) {
                if (block.getData() == getColor(badTeam.getColor()).getData()) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
    Specials Manager
     */

    List<String> ssjrs = new ArrayList<>();
    List<String> sqs = new ArrayList<>();
    List<String> minigun = new ArrayList<>();
    List<String> kraken = new ArrayList<>();

    @EventHandler
    public void onSpecial(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        try {
            if (getKit(player).getName().equalsIgnoreCase(new SplattershotJr().getName())) {
                if (player.getItemInHand().equals(new SplattershotJr().getSpecialItem())) {
                    event.setCancelled(true);
                    player.getInventory().removeItem(player.getInventory().getItemInHand());
                    ssjrs.add(player.getName());
                    sendTitle(player, ChatColor.GOLD + "SPECIAL:");
                    sendSubTitle(player, "The Shield");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            ssjrs.remove(player.getName());
                        }
                    }, 200L);
                }
            } else try {
                if (getKit(player).getName().equalsIgnoreCase(new SplatterShot().getName())) {
                    if (player.getItemInHand().equals(new SplatterShot().getSpecialItem())) {
                        event.setCancelled(true);
                        player.getInventory().removeItem(player.getInventory().getItemInHand());
                        player.getInventory().setItem(8, new SplatterShot().special());
                        sendTitle(player, ChatColor.GOLD + "SPECIAL:");
                        sendSubTitle(player, "Boom boom!");
                    }
                } else if (getKit(player).getName().equalsIgnoreCase(new Squiffer().getName())) {
                    if (player.getItemInHand().equals(new Squiffer().getSpecialItem())) {
                        event.setCancelled(true);
                        sqs.add(player.getName());
                        player.getInventory().removeItem(player.getInventory().getItemInHand());
                        player.getInventory().setItem(8, new Squiffer().special());
                        sendTitle(player, ChatColor.GOLD + "SPECIAL:");
                        sendSubTitle(player, "Ink Zooka!");
                        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
                            @Override
                            public void run() {
                                if (sqs.contains(player.getName())) {
                                    sqs.remove(player.getName());
                                    player.getInventory().setItem(8, new ItemStack(Material.AIR));
                                }
                            }
                        }, 200L);
                    }
                } else if (getKit(player).getName().equalsIgnoreCase(new InkBrush().getName())) {
                    if (player.getItemInHand().equals(new InkBrush().getSpecialItem())) {
                        player.getInventory().removeItem(player.getInventory().getItemInHand());
                        sendTitle(player, ChatColor.GOLD + "SPECIAL:");
                        sendSubTitle(player, "Ink Strike!!");
                    }
                } else if (getKit(player).getName().equalsIgnoreCase(new SplatCharger().getName())) {
                    if (player.getItemInHand().equals(new SplatCharger().getSpecialItem())) {
                        event.setCancelled(true);
                        player.getInventory().removeItem(player.getInventory().getItemInHand());
                        player.getInventory().setItem(8, new SplatCharger().special());
                        minigun.add(player.getName());
                        sendTitle(player, ChatColor.GOLD + "SPECIAL:");
                        sendSubTitle(player, "MINIGUN!!");
                        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
                            @Override
                            public void run() {
                                minigun.remove(player.getName());
                                player.getInventory().setItem(8, new ItemStack(Material.AIR));
                            }
                        }, 160L);
                    }
                } else if (getKit(player).getName().equalsIgnoreCase(new SplatRoller().getName())) {
                    if (player.getItemInHand().equals(new SplatRoller().getSpecialItem())) {
                        event.setCancelled(true);
                        player.getInventory().removeItem(player.getInventory().getItemInHand());
                        MobDisguise mobDisguise = new MobDisguise(DisguiseType.SQUID);
                        DisguiseAPI.disguiseToAll(player, mobDisguise);
                        kraken.add(player.getName());
                        sendTitle(player, ChatColor.GOLD + "SPECIAL:");
                        sendSubTitle(player, "Release thy Kraken!!");
                        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
                            @Override
                            public void run() {
                                kraken.remove(player.getName());
                                DisguiseAPI.undisguiseToAll(player);
                            }
                        }, 200L);
                    }
                }
            } catch (NoKitException e) {
                e.printStackTrace();
            }
        } catch (NoKitException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onSprinkler(final PlayerInteractEvent event) {
        try {
            if (!getKit(event.getPlayer()).getName().equalsIgnoreCase(new InkBrush().getName())) {
                return;
            }
        } catch (NoKitException e) {
            e.printStackTrace();
        }

        if (!event.getPlayer().getItemInHand().equals(new InkBrush().getSubItem())) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);
        final Player player = event.getPlayer();
        final Bat bat = (Bat) player.getWorld().spawnEntity(event.getClickedBlock().getLocation(), EntityType.BAT);
        sprinklers.add(player.getName());
        sprinklerBat.add(bat);
        player.getInventory().remove(player.getItemInHand());
        sprinklereffect(player, event.getClickedBlock().getLocation(), bat);
    }

    @EventHandler
    public void onSprinkla(final PlayerInteractEvent event) {
        try {
            if (!getKit(event.getPlayer()).getName().equalsIgnoreCase(new Squiffer().getName())) {
                return;
            }
        } catch (NoKitException e) {
            e.printStackTrace();
        }

        if (!event.getPlayer().getItemInHand().equals(new Squiffer().getSubItem())) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);
        final Player player = event.getPlayer();
        final Bat bat = (Bat) player.getWorld().spawnEntity(event.getClickedBlock().getLocation(), EntityType.BAT);
        sprinklas.add(player.getName());
        sprinklaBat.add(bat);
        player.getInventory().remove(player.getItemInHand());
        sprinklaEffect(player, event.getClickedBlock().getLocation(), bat);
    }

    @EventHandler
    public void minigun(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        if (!minigun.contains(player.getName())) {
            return;
        }

        if (player.getItemInHand().getType() != Material.BOW) {
            return;
        }

        Arrow a = player.launchProjectile(Arrow.class);
        a.setShooter(player);
        player.getInventory().removeItem(new ItemStack[]{new ItemStack(Material.ARROW, 1)});
        player.updateInventory();
    }

    @EventHandler
    public void inkZooka(EntityShootBowEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!(event.getProjectile() instanceof Arrow)) {
            return;
        }

        Player player = (Player) event.getEntity();



        if (!sqs.contains(event.getEntity().getName())) {
            if (event.getBow().equals(new Squiffer().bow())) {
                Arrow arrow = (Arrow) event.getProjectile();
                sarrow.add(arrow);
                arrowTrag(arrow, player);
                return;
            }
            return;
        }

        if (!event.getBow().equals(new Squiffer().special())) {
            return;
        }

        Arrow arrow = (Arrow) event.getProjectile();
        ssarrow.add(arrow);
        arrowTrag(arrow, player);
    }

    @EventHandler
    public void onEgg(PlayerEggThrowEvent event) {
        event.setHatching(false);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.blockList().clear();
    }

    List<String> sprinklers = new ArrayList<>();
    List<Entity> sprinklerBat = new ArrayList<>();

    public EnumParticle particle = EnumParticle.FLAME;

    public void sprinklereffect(final Player player, final Location loc, final Entity entity) {
        task = new BukkitRunnable() {
            double t = 0;

            @Override
            public void run() {
                if (sprinklers.contains(player.getName())) {
                    if (sprinklerBat.contains(entity)) {
                        t += Math.PI / 8;
                        Location location = loc;
                        for (double phi = 0; phi <= 2 * Math.PI; phi += Math.PI / 5) {
                            double x = 0.4 * (4 * Math.PI - t) * Math.cos(t + phi);
                            double y = 0.2 * t;
                            double z = 0.4 * (4 * Math.PI - t) * Math.sin(t + phi);

                            location.add(x, y, z);
                            new ParticleEffectUtil().playAt(particle, location, 0, 0, 0, 0, 1);
                            location.subtract(x, y, z);
                            if (t >= 4 * Math.PI) {
                                sprinklerBat.remove(entity);
                                sprinklers.remove(player.getName());
                                for(Entity ent : entity.getNearbyEntities(3, 3, 3)) {
                                    if(ent instanceof Player) {
                                        killplayer((Player) ent, player, "Sprinler");
                                    }
                                }
                                entity.remove();
                                ChangeBlockfTeam(player, 5, loc);
                                cancel();
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(getPlugin(), 0, 0).getTaskId();
    }


    List<String> sprinklas = new ArrayList<>();
    List<Entity> sprinklaBat = new ArrayList<>();

    public EnumParticle particle1 = EnumParticle.FIREWORKS_SPARK;

    public void sprinklaEffect(final Player player, final Location loc, final Entity entity) {
        task = new BukkitRunnable() {
            double phi = 0;
            @Override
            public void run() {
                if (sprinklas.contains(player.getName())) {
                    if (sprinklaBat.contains(entity)) {
                        phi = phi + Math.PI / 8;
                        double x, y, z;

                        Location location1 = loc;
                        for (double t = 0; t <= 2 * Math.PI; t = t + Math.PI / 16) {
                            for (double i = 0; i <= 1; i = i + 1) {
                                x = 0.4 * (2 * Math.PI - t) * 0.5 * Math.cos(t + phi + i * Math.PI);
                                y = 0.3 * t;
                                z = 0.4 * (2 * Math.PI - t) * 0.5 * Math.sin(t + phi + i * Math.PI);
                                location1.add(x, y, z);
                                new ParticleEffectUtil().playAt(particle1, location1, 0, 0, 0, 0, 1);
                                location1.subtract(x, y, z);
                            }
                        }

                        if (phi > 10 * Math.PI) {
                            sprinklaBat.remove(entity);
                            sprinklas.remove(player.getName());
                            for (Entity ent : entity.getNearbyEntities(3, 3, 3)) {
                                if (ent instanceof Player) {
                                    killplayer((Player) ent, player, "Sprinla");
                                }
                            }
                            entity.remove();
                            ChangeBlockfTeam(player, 5, loc);
                            cancel();
                        }
                    }
                }
            }
        }.runTaskTimer(getPlugin(), 0, 0).getTaskId();
    }

    @EventHandler
    public void onEnderpearl(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            event.setCancelled(true);
        }
    }

    List<Arrow> sarrow = new ArrayList<>();
    List<Arrow> ssarrow = new ArrayList<>();
    int task1;
    public void arrowTrag(final Arrow arrow, final Player player) {
        task1 = Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (!arrow.isOnGround()) {
                    int x = (int) arrow.getLocation().getX();
                    int z = (int) arrow.getLocation().getZ();
                    Block block = player.getWorld().getHighestBlockAt(x, z);
                    ChangeBlockfTeam(player, 3, block.getLocation());
                }
            }
        }, 0L, 0L);
    }

    @EventHandler
    public void heal(EntityRegainHealthEvent event) {
        event.setCancelled(true);
    }

    public void clearItem(final Player player) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(), new Runnable() {
            @Override
            public void run() {
                for (Entity ent : player.getWorld().getEntities()) {
                    if (ent instanceof Item) {
                        ent.remove();
                    }
                }
            }
        }, 0L, 0L);
    }

    public void infDura(final Player player) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(), new Runnable() {
            @Override
            public void run() {
                for(ItemStack itemStack : player.getInventory().getContents()) {
                    if (itemStack != null) {
                        if (itemStack.getType() != Material.AIR) {
                            if (itemCheck(itemStack) == true) {
                                itemStack.setDurability((short) 0);
                            }
                        }
                    }
                }
             }
        }, 0L, 0L);
    }


    private boolean itemCheck(ItemStack w) {
        if (w.getType().getId() == 256 || (w.getType().getId() == 257) ||
                (w.getType().getId() == 258) || (w.getType().getId() == 259) ||
                (w.getType().getId() == 261) || (w.getType().getId() == 267) ||
                (w.getType().getId() == 268) || (w.getType().getId() == 269) ||
                (w.getType().getId() == 270) || (w.getType().getId() == 271) ||
                (w.getType().getId() == 272) || (w.getType().getId() == 273) ||
                (w.getType().getId() == 274) || (w.getType().getId() == 275) ||
                (w.getType().getId() == 276) || (w.getType().getId() == 277) ||
                (w.getType().getId() == 278) || (w.getType().getId() == 279) ||
                (w.getType().getId() == 283) || (w.getType().getId() == 284) ||
                (w.getType().getId() == 285) || (w.getType().getId() == 286) ||
                (w.getType().getId() == 290) || (w.getType().getId() == 291) ||
                (w.getType().getId() == 292) || (w.getType().getId() == 293) ||
                (w.getType().getId() == 294) || (w.getType().getId() == 298) ||
                (w.getType().getId() == 299) || (w.getType().getId() == 300) ||
                (w.getType().getId() == 301) || (w.getType().getId() == 302) ||
                (w.getType().getId() == 303) || (w.getType().getId() == 304) ||
                (w.getType().getId() == 305) || (w.getType().getId() == 306) ||
                (w.getType().getId() == 307) || (w.getType().getId() == 308) ||
                (w.getType().getId() == 309) || (w.getType().getId() == 310) ||
                (w.getType().getId() == 311) || (w.getType().getId() == 312) ||
                (w.getType().getId() == 313) || (w.getType().getId() == 314) ||
                (w.getType().getId() == 315) || (w.getType().getId() == 316) ||
                (w.getType().getId() == 317) || (w.getType().getId() == 346) ||
                (w.getType().getId() == 359)) {
            return true;
        }
        return false;
    }
}
