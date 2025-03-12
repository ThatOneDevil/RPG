package org.frizzlenpop.rPGSkillsPlugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.rPGSkillsPlugin.commands.*;
import org.frizzlenpop.rPGSkillsPlugin.data.PlayerDataManager;
import org.frizzlenpop.rPGSkillsPlugin.gui.SkillsGUI;
import org.frizzlenpop.rPGSkillsPlugin.items.CustomEnchantScroll;
import org.frizzlenpop.rPGSkillsPlugin.listeners.*;
import org.frizzlenpop.rPGSkillsPlugin.skills.PassiveSkillManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.XPManager;
import org.frizzlenpop.rPGSkillsPlugin.skills.SkillAbilityManager;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillTreeManager;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillTreeGUI;
import org.frizzlenpop.rPGSkillsPlugin.skilltree.SkillXPListener;
import org.frizzlenpop.rPGSkillsPlugin.listeners.ExcavationListener;
import org.frizzlenpop.rPGSkillsPlugin.listeners.RepairListener;
import org.frizzlenpop.rPGSkillsPlugin.data.PartyManager;
import org.frizzlenpop.rPGSkillsPlugin.commands.PartyCommand;
import org.frizzlenpop.rPGSkillsPlugin.gui.RPGScoreboardManager;

public class RPGSkillsPlugin extends JavaPlugin {
    private PlayerDataManager playerDataManager;
    private XPManager xpManager;
    private SkillsGUI skillsGUI;
    private SkillAbilityManager abilityManager;
    private PassiveSkillManager passiveSkillManager;
    private CustomEnchantScroll customEnchantScroll;
    private SkillTreeManager skillTreeManager;
    private SkillTreeGUI skillTreeGUI;
    private SkillXPListener skillXPListener;
    private PartyManager partyManager;
    private RPGScoreboardManager scoreboardManager;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        this.config = getConfig();

        // Add default passive configuration
        addDefaultPassiveConfig();
        saveConfig();

        // Initialize managers in the correct order
        this.playerDataManager = new PlayerDataManager(this);
        this.xpManager = new XPManager(playerDataManager);
        this.abilityManager = new SkillAbilityManager(this);
        this.passiveSkillManager = new PassiveSkillManager(xpManager, this);
        this.skillsGUI = new SkillsGUI(playerDataManager, xpManager, abilityManager, passiveSkillManager);
        this.customEnchantScroll = new CustomEnchantScroll(this);
        
        // Initialize party manager for XP sharing
        this.partyManager = new PartyManager(this);
        
        // Initialize skill tree system
        this.skillTreeManager = new SkillTreeManager(this, playerDataManager, xpManager);
        this.skillTreeGUI = new SkillTreeGUI(this, skillTreeManager);
        this.skillXPListener = new SkillXPListener(this, skillTreeManager);
        
        // Initialize scoreboard manager
        this.scoreboardManager = new RPGScoreboardManager(this);

        // Set the passive skill manager after initialization
        xpManager.setPassiveSkillManager(passiveSkillManager);
        
        // Set the party manager for XP sharing
        xpManager.setPartyManager(partyManager);

        // Register commands
        getCommand("skills").setExecutor(new SkillsCommand(skillsGUI));
        getCommand("abilities").setExecutor(new AbilitiesCommand(this, playerDataManager));
        getCommand("skillsadmin").setExecutor(new SkillsAdminCommand(playerDataManager, xpManager));
        getCommand("toggleskillmessages").setExecutor(new ToggleSkillMessagesCommand(playerDataManager));
        getCommand("passives").setExecutor(new PassivesCommand(passiveSkillManager));
        getCommand("skilltree").setExecutor(new SkillTreeCommand(this, skillTreeGUI, skillTreeManager));
        getCommand("rstat").setExecutor(new RStatCommand(this));
        
        // Register party command
        PartyCommand partyCommand = new PartyCommand(this, partyManager);
        getCommand("rparty").setExecutor(partyCommand);
        getCommand("rparty").setTabCompleter(partyCommand);
        
        // Register scoreboard command
        getCommand("rscoreboard").setExecutor(new RScoreboardCommand(this, scoreboardManager));

        // Register all listeners
        registerListeners();

        getLogger().info("RPGSkills plugin has been enabled!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerDataListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new MiningListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FishingListener(xpManager, this, passiveSkillManager), this);
        getServer().getPluginManager().registerEvents(new EnchantingListener(xpManager, this), this);
        getServer().getPluginManager().registerEvents(skillsGUI, this);
        getServer().getPluginManager().registerEvents(new LoggingListener(xpManager, this, passiveSkillManager), this);
        getServer().getPluginManager().registerEvents(new FarmingListener(xpManager), this);
        getServer().getPluginManager().registerEvents(new FightingListener(xpManager, this, passiveSkillManager), this);
        getServer().getPluginManager().registerEvents(abilityManager, this);
        getServer().getPluginManager().registerEvents(new ExcavationListener(xpManager, this, passiveSkillManager), this);
        getServer().getPluginManager().registerEvents(new RepairListener(xpManager), this);
        
        // Skill tree listeners are registered in their respective classes
    }

    private void addDefaultPassiveConfig() {
        // Mining passives
        addDefaultPassiveIfNotExists("mining", "doubleOreChance", "chance", 0.1);
        addDefaultPassiveIfNotExists("mining", "doubleOreChance", "enabled", true);

        // Fishing passives
        addDefaultPassiveIfNotExists("fishing", "treasureHunter", "chance", 0.15);
        addDefaultPassiveIfNotExists("fishing", "treasureHunter", "enabled", true);

        // Farming passives
        addDefaultPassiveIfNotExists("farming", "doubleCropChance", "chance", 0.2);
        addDefaultPassiveIfNotExists("farming", "doubleCropChance", "enabled", true);

        // Logging passives
        addDefaultPassiveIfNotExists("logging", "doubleLogChance", "chance", 0.15);
        addDefaultPassiveIfNotExists("logging", "doubleLogChance", "enabled", true);

        // Fighting passives
        addDefaultPassiveIfNotExists("fighting", "damageReduction", "reduction", 0.15);
        addDefaultPassiveIfNotExists("fighting", "damageReduction", "enabled", true);

        // Enchanting passives
        addDefaultPassiveIfNotExists("enchanting", "doubleEnchantChance", "chance", 0.1);
        addDefaultPassiveIfNotExists("enchanting", "doubleEnchantChance", "enabled", true);
        
        // Excavation passives
        addDefaultPassiveIfNotExists("excavation", "doubleDrops", "chance", 0.15);
        addDefaultPassiveIfNotExists("excavation", "doubleDrops", "enabled", true);
        addDefaultPassiveIfNotExists("excavation", "archaeologyBasics", "chance", 0.12);
        addDefaultPassiveIfNotExists("excavation", "archaeologyBasics", "enabled", true);
        addDefaultPassiveIfNotExists("excavation", "treasureFinder", "chance", 0.1);
        addDefaultPassiveIfNotExists("excavation", "treasureFinder", "enabled", true);
        addDefaultPassiveIfNotExists("excavation", "rareFind", "chance", 0.05);
        addDefaultPassiveIfNotExists("excavation", "rareFind", "enabled", true);
        addDefaultPassiveIfNotExists("excavation", "multiBlock", "chance", 0.08);
        addDefaultPassiveIfNotExists("excavation", "multiBlock", "enabled", true);
        addDefaultPassiveIfNotExists("excavation", "ancientArtifacts", "chance", 0.02);
        addDefaultPassiveIfNotExists("excavation", "ancientArtifacts", "enabled", true);
        
        // Repair passives
        addDefaultPassiveIfNotExists("repair", "materialSaver", "chance", 0.10);
        addDefaultPassiveIfNotExists("repair", "materialSaver", "enabled", true);
        addDefaultPassiveIfNotExists("repair", "experienceSaver", "reduction", 0.15);
        addDefaultPassiveIfNotExists("repair", "experienceSaver", "enabled", true);
        addDefaultPassiveIfNotExists("repair", "qualityRepair", "bonusDurability", 0.10);
        addDefaultPassiveIfNotExists("repair", "qualityRepair", "enabled", true);
        addDefaultPassiveIfNotExists("repair", "masterSmith", "efficiency", 0.20);
        addDefaultPassiveIfNotExists("repair", "masterSmith", "enabled", true);
    }

    private void addDefaultPassiveIfNotExists(String skill, String passive, String property, Object value) {
        String path = "passives." + skill + "." + passive + "." + property;
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    public double getPassiveValue(String skill, String passive) {
        String path = "passives." + skill + "." + passive + "." + "chance";
        return config.getDouble(path);
    }

    public boolean isPassiveEnabled(String skill, String passive) {
        String path = "passives." + skill + "." + passive + ".enabled";
        return config.getBoolean(path, true);
    }

    @Override
    public void onDisable() {
        // Save player data
        if (playerDataManager != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                FileConfiguration playerData = playerDataManager.getPlayerData(player.getUniqueId());
                playerDataManager.savePlayerData(player.getUniqueId(), playerData);
            }
        }
        
        // Clean up scoreboard
        if (scoreboardManager != null) {
            scoreboardManager.cleanup();
        }

        // Save configuration
        saveConfig();

        getLogger().info("RPGSkills plugin has been disabled!");
    }

    // Getters for accessing managers from other classes
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public XPManager getXpManager() {
        return xpManager;
    }

    public SkillAbilityManager getAbilityManager() {
        return abilityManager;
    }

    public PassiveSkillManager getPassiveSkillManager() {
        return passiveSkillManager;
    }
    
    public CustomEnchantScroll getCustomEnchantScroll() {
        return customEnchantScroll;
    }

    /**
     * Get the skill tree manager
     */
    public SkillTreeManager getSkillTreeManager() {
        return skillTreeManager;
    }
    
    /**
     * Get the skill tree GUI
     */
    public SkillTreeGUI getSkillTreeGUI() {
        return skillTreeGUI;
    }

    /**
     * Get the party manager for XP sharing
     */
    public PartyManager getPartyManager() {
        return partyManager;
    }
    
    /**
     * Get the scoreboard manager
     */
    public RPGScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    /**
     * Reload the configuration and refresh skill tree
     */
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.config = getConfig();
        
        // Reinitialize skill tree with new config
        if (skillTreeManager != null) {
            // Refresh skill tree nodes
            skillTreeManager.reloadSkillTreeConfig();
        }
    }
}