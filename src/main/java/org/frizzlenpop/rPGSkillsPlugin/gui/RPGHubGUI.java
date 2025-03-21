package org.frizzlenpop.rPGSkillsPlugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.mounts.gui.MountGUI;
import org.frizzlenpop.rPGSkillsPlugin.mounts.fusion.MountCombinationGUI;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillTreeGUI;
import org.frizzlenpop.rPGSkillsPlugin.utils.ColorUtils;

import java.util.*;

/**
 * A centralized hub GUI that serves as the main entry point for all RPGSkills features.
 * This GUI provides easy access to all subsystems: Skills, Skill Tree, Party, Mounts, etc.
 */
public class RPGHubGUI implements Listener {
    private final RPGSkillsPlugin plugin;
    private final SkillsGUI skillsGUI;
    private final SkillTreeGUI skillTreeGUI;
    private final PartyPerksGUI partyPerksGUI;
    private final MountGUI mountGUI;
    private final MountCombinationGUI mountCombinationGUI;
    private final InventoryManager inventoryManager;
    private final PlayerDataManager playerDataManager;
    
    // GUI constants
    private static final String GUI_TITLE = "✧ RPG Hub ✧";
    private static final int GUI_SIZE = 54; // 6 rows
    
    /**
     * Creates a new RPGHubGUI
     * 
     * @param plugin The plugin instance
     */
    public RPGHubGUI(RPGSkillsPlugin plugin) {
        this.plugin = plugin;
        this.skillsGUI = plugin.getSkillsGUI();
        this.skillTreeGUI = plugin.getSkillTreeGUI();
        this.partyPerksGUI = plugin.getPartyPerksGUI();
        this.mountGUI = plugin.getMountGUI();
        this.mountCombinationGUI = plugin.getMountCombinationGUI();
        this.inventoryManager = plugin.getInventoryManager();
        this.playerDataManager = plugin.getPlayerDataManager();
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Opens the RPG Hub GUI for a player
     * 
     * @param player The player
     */
    public void openGUI(Player player) {
        Inventory inventory = createHubInventory(player);
        player.openInventory(inventory);
        
        // Register this inventory with the inventory manager to prevent item theft
        inventoryManager.registerInventory(player, GUI_TITLE);
        
        // Play a nice sound effect
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1.0f);
    }
    
    /**
     * Creates the hub inventory
     * 
     * @param player The player
     * @return The created inventory
     */
    private Inventory createHubInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);
        
        // Create a visually appealing background pattern with stained glass
        ItemStack backgroundGlass1 = createGuiItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ");
        ItemStack backgroundGlass2 = createGuiItem(Material.BLUE_STAINED_GLASS_PANE, " ");
        
        for (int i = 0; i < GUI_SIZE; i++) {
            if ((i % 9 == 0) || (i % 9 == 8) || (i < 9) || (i >= GUI_SIZE - 9) || (i % 2 == 0)) {
                inventory.setItem(i, backgroundGlass1);
            } else {
                inventory.setItem(i, backgroundGlass2);
            }
        }
        
        // Player info in the center top (slot 4) with total XP earned
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName(ChatColor.GOLD + "✧ " + player.getName() + " ✧");
        
        // Calculate total XP earned across all skills
        int totalXpEarned = calculateTotalXpEarned(player.getUniqueId());
        
        List<String> playerLore = new ArrayList<>();
        playerLore.add(ChatColor.GRAY + "Welcome to the RPG Hub!");
        playerLore.add(ChatColor.GRAY + "Select an option below to begin");
        playerLore.add("");
        playerLore.add(ChatColor.GOLD + "Total XP Earned: " + ChatColor.YELLOW + totalXpEarned);
        
        skullMeta.setLore(playerLore);
        playerHead.setItemMeta(skullMeta);
        inventory.setItem(4, playerHead);
        
        // Skills system (main skills)
        inventory.setItem(19, createGuiItem(
                Material.EXPERIENCE_BOTTLE,
                ColorUtils.colorize("&6✦ &eSkills"),
                ChatColor.GRAY + "View and manage your skills",
                ChatColor.GRAY + "Level up and earn passive bonuses",
                "",
                ChatColor.YELLOW + "Click to open Skills menu"
        ));
        
        // Skill tree system
        inventory.setItem(21, createGuiItem(
                Material.KNOWLEDGE_BOOK,
                ColorUtils.colorize("&6✦ &eSkill Tree"),
                ChatColor.GRAY + "Unlock powerful abilities",
                ChatColor.GRAY + "Customize your character progression",
                "",
                ChatColor.YELLOW + "Click to open Skill Tree menu"
        ));
        
        // Party system
        inventory.setItem(23, createGuiItem(
                Material.TOTEM_OF_UNDYING,
                ColorUtils.colorize("&6✦ &eParty System"),
                ChatColor.GRAY + "Create or join a party",
                ChatColor.GRAY + "Share XP and unlock party perks",
                "",
                ChatColor.YELLOW + "Click to open Party menu"
        ));
        
        // Mount main menu
        inventory.setItem(25, createGuiItem(
                Material.SADDLE,
                ColorUtils.colorize("&6✦ &eMounts"),
                ChatColor.GRAY + "Manage your collection of mounts",
                ChatColor.GRAY + "Level up and customize your mounts",
                "",
                ChatColor.YELLOW + "Click to open Mounts menu"
        ));
        
        // Mount fusion (moved to slot 29, replacing Mount Shop)
        inventory.setItem(29, createGuiItem(
                Material.ANVIL,
                ColorUtils.colorize("&6✦ &eMount Fusion"),
                ChatColor.GRAY + "Combine mounts to create",
                ChatColor.GRAY + "more powerful variants",
                "",
                ChatColor.YELLOW + "Click to open Mount Fusion menu"
        ));
        
        // Mount chests (moved to slot 31, where Fusion was)
        inventory.setItem(31, createGuiItem(
                Material.CHEST,
                ColorUtils.colorize("&6✦ &eMount Chests"),
                ChatColor.GRAY + "Open chests to discover",
                ChatColor.GRAY + "new and rare mounts",
                "",
                ChatColor.YELLOW + "Click to open Mount Chests menu"
        ));
        
        // Stats overview
        inventory.setItem(40, createGuiItem(
                Material.BOOK,
                ColorUtils.colorize("&6✦ &ePlayer Stats"),
                ChatColor.GRAY + "View detailed statistics",
                ChatColor.GRAY + "Track your progress and achievements",
                "",
                ChatColor.YELLOW + "Click to view your stats"
        ));
        
        return inventory;
    }
    
    /**
     * Calculate the total XP earned across all skills for a player
     * 
     * @param playerUUID The player's UUID
     * @return The total XP earned
     */
    private int calculateTotalXpEarned(UUID playerUUID) {
        int totalXp = 0;
        String[] skills = {"mining", "logging", "farming", "fighting", "fishing", "enchanting", "excavation", "repair"};
        
        for (String skill : skills) {
            totalXp += playerDataManager.getTotalSkillXPEarned(playerUUID, skill);
        }
        
        return totalXp;
    }
    
    /**
     * Helper method to create a GUI item with a name and lore
     */
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }
        meta.setLore(loreList);
        
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Handles clicks in the hub inventory
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.equals(GUI_TITLE)) {
            return;
        }
        
        // Cancel the event to prevent item theft
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null) {
            return;
        }
        
        // Handle the different menu options
        switch (event.getSlot()) {
            case 19: // Skills
                player.closeInventory();
                player.performCommand("skills");
                break;
                
            case 21: // Skill Tree
                player.closeInventory();
                player.performCommand("skilltree");
                break;
                
            case 23: // Party System
                player.closeInventory();
                player.performCommand("rparty");
                break;
                
            case 25: // Mounts main menu
                player.closeInventory();
                player.performCommand("mount");
                break;
                
            case 29: // Mount fusion (moved from slot 31)
                player.closeInventory();
                player.performCommand("mount fusion");
                break;
                
            case 31: // Mount chests (moved from slot 33)
                player.closeInventory();
                player.performCommand("mountchest");
                break;
                
            case 40: // Player stats
                player.closeInventory();
                player.performCommand("rstat");
                break;
        }
        
        // Play click sound
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Handles inventory dragging to prevent item theft
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.equals(GUI_TITLE)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Handles inventory closing to update tracking and play sound
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        
        if (title.equals(GUI_TITLE)) {
            // Unregister this inventory with the inventory manager
            inventoryManager.unregisterInventory(player, GUI_TITLE);
            
            // Play a nice sound effect
            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, 0.5f, 1.0f);
        }
    }
} 