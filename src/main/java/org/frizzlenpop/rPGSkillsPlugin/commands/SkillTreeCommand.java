package org.frizzlenpop.rPGSkillsPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.frizzlenpop.rPGSkillsPlugin.RPGSkillsPlugin;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillTreeGUI;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillTreeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for interacting with the skill tree system
 */
public class SkillTreeCommand implements CommandExecutor, TabCompleter {
    private final RPGSkillsPlugin plugin;
    private final SkillTreeGUI skillTreeGUI;
    private final SkillTreeManager skillTreeManager;
    
    /**
     * Constructor for the skill tree command
     */
    public SkillTreeCommand(RPGSkillsPlugin plugin, SkillTreeGUI skillTreeGUI, SkillTreeManager skillTreeManager) {
        this.plugin = plugin;
        this.skillTreeGUI = skillTreeGUI;
        this.skillTreeManager = skillTreeManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open skill tree GUI
            skillTreeGUI.openSkillTree(player);
            return true;
        }
        
        if (args.length >= 1) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "info":
                    // Show skill tree information
                    showSkillTreeInfo(player);
                    return true;
                case "unlock":
                    // Unlock a skill tree node
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /skilltree unlock <nodeId>");
                        return true;
                    }
                    String nodeId = args[1];
                    unlockNode(player, nodeId);
                    return true;
                case "reset":
                    // Admin command to reset a player's skill tree
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /skilltree reset <player>");
                        return true;
                    }
                    if (!player.hasPermission("rpgskills.admin")) {
                        player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                        return true;
                    }
                    // Implementation for reset command would go here
                    return true;
                case "level":
                    // Show player level information
                    showLevelInfo(player);
                    return true;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /skilltree for the skill tree GUI.");
                    return true;
            }
        }
        
        return true;
    }
    
    /**
     * Show skill tree information to a player
     */
    private void showSkillTreeInfo(Player player) {
        int playerLevel = skillTreeManager.getPlayerLevel(player);
        int availablePoints = skillTreeManager.getAvailableSkillPoints(player);
        int spentPoints = skillTreeManager.getSpentSkillPoints(player);
        int totalPoints = skillTreeManager.getTotalSkillPoints(player);
        double progress = skillTreeManager.getLevelProgress(player);
        
        player.sendMessage(ChatColor.GOLD + "=== Skill Tree Information ===");
        player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + playerLevel);
        player.sendMessage(ChatColor.YELLOW + "Progress to next level: " + 
                           ChatColor.WHITE + String.format("%.1f%%", progress * 100));
        player.sendMessage(ChatColor.YELLOW + "Available Points: " + ChatColor.WHITE + availablePoints);
        player.sendMessage(ChatColor.YELLOW + "Spent Points: " + ChatColor.WHITE + spentPoints);
        player.sendMessage(ChatColor.YELLOW + "Total Points: " + ChatColor.WHITE + totalPoints);
        player.sendMessage(ChatColor.YELLOW + "Unlocked Nodes: " + ChatColor.WHITE + 
                          skillTreeManager.getPlayerUnlockedNodes(player).size());
        
        player.sendMessage(ChatColor.GOLD + "Use " + ChatColor.GREEN + "/skilltree" + 
                          ChatColor.GOLD + " to open the skill tree GUI.");
    }
    
    /**
     * Show level information to a player
     */
    private void showLevelInfo(Player player) {
        int playerLevel = skillTreeManager.getPlayerLevel(player);
        int xpUntilNext = skillTreeManager.getPlayerLevel().getXPUntilNextLevel(player);
        double progress = skillTreeManager.getLevelProgress(player);
        
        player.sendMessage(ChatColor.GOLD + "=== Player Level Information ===");
        player.sendMessage(ChatColor.YELLOW + "Current Level: " + ChatColor.WHITE + playerLevel);
        player.sendMessage(ChatColor.YELLOW + "XP until next level: " + ChatColor.WHITE + xpUntilNext);
        player.sendMessage(ChatColor.YELLOW + "Progress: " + ChatColor.WHITE + 
                          String.format("%.1f%%", progress * 100));
        
        // Calculate next milestone
        int nextMilestone = (playerLevel / 10 + 1) * 10;
        int levelsUntilMilestone = nextMilestone - playerLevel;
        
        player.sendMessage(ChatColor.YELLOW + "Next milestone (level " + nextMilestone + 
                          "): " + ChatColor.WHITE + levelsUntilMilestone + " levels away");
        player.sendMessage(ChatColor.YELLOW + "Milestone reward: " + 
                          ChatColor.GREEN + "3 skill points");
    }
    
    /**
     * Unlock a node for a player
     */
    private void unlockNode(Player player, String nodeId) {
        if (!skillTreeManager.getAllNodes().containsKey(nodeId)) {
            player.sendMessage(ChatColor.RED + "Node not found: " + nodeId);
            return;
        }
        
        if (skillTreeManager.hasUnlockedNode(player, nodeId)) {
            player.sendMessage(ChatColor.YELLOW + "You have already unlocked this node!");
            return;
        }
        
        if (!skillTreeManager.canUnlockNode(player, nodeId)) {
            player.sendMessage(ChatColor.RED + "You cannot unlock this node yet. Check the requirements!");
            return;
        }
        
        if (skillTreeManager.unlockNode(player, nodeId)) {
            player.sendMessage(ChatColor.GREEN + "Successfully unlocked node: " + 
                              skillTreeManager.getAllNodes().get(nodeId).getName());
        } else {
            player.sendMessage(ChatColor.RED + "Failed to unlock node. Please try again.");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            // Suggest subcommands
            List<String> subCommands = Arrays.asList("info", "unlock", "level");
            
            // Add admin commands if player has permission
            if (player.hasPermission("rpgskills.admin")) {
                subCommands = new ArrayList<>(subCommands);
                subCommands.add("reset");
            }
            
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("unlock")) {
                // Suggest available nodes
                return skillTreeManager.getAvailableNodes(player).stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
} 