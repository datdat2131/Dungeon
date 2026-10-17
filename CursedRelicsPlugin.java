package com.example.cursedrelics;

import com.example.cursedrelics.dungeon.DungeonManager;
import com.example.cursedrelics.DungeonCommand;
import com.example.cursedrelics.loot.LootService;
import com.example.cursedrelics.party.PartyManager;
import com.example.cursedrelics.relic.RelicDraftService;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CursedRelicsPlugin extends JavaPlugin {
    private NamespacedKey customItemKey;
    private NamespacedKey bossKey;
    private NamespacedKey instanceKey;
    private PartyManager partyManager;
    private DungeonManager dungeonManager;
    private LootService lootService;
    private RelicDraftService relicDraftService;

    @Override public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("dungeons.yml");
        saveResourceIfMissing("relics.yml");
        customItemKey = new NamespacedKey(this, "custom_item_id");
        bossKey = new NamespacedKey(this, "dungeon_boss");
        instanceKey = new NamespacedKey(this, "instance_id");
        partyManager = new PartyManager(this);
        lootService = new LootService(this);
        relicDraftService = new RelicDraftService(this);
        dungeonManager = new DungeonManager(this, partyManager, lootService, relicDraftService);
        DungeonCommand dungeonCommand = new DungeonCommand(dungeonManager, partyManager, relicDraftService);
        PluginCommand dc = getCommand("dungeon");
        if (dc != null) { dc.setExecutor(dungeonCommand); dc.setTabCompleter(dungeonCommand); }
        PluginCommand cr = getCommand("cr");
        if (cr != null) cr.setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("cursedrelics.admin")) { sender.sendMessage("§cNo permission."); return true; }
            if (args.length == 2 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("reload")) {
                reloadConfig(); sender.sendMessage("§aCursedRelics config reloaded. YAML content files are loaded at startup in this MVP."); return true;
            }
            sender.sendMessage("§eUsage: /cr admin reload"); return true;
        });
        getServer().getPluginManager().registerEvents(dungeonManager, this);
        getServer().getPluginManager().registerEvents(relicDraftService, this);
        getLogger().info("CursedRelics 0.1.0 enabled for Paper 1.21.4.");
    }

    private void saveResourceIfMissing(String name) { if (!getDataFolder().toPath().resolve(name).toFile().exists()) saveResource(name, false); }
    @Override public void onDisable() { if (dungeonManager != null) dungeonManager.shutdown(); }
    public NamespacedKey customItemKey() { return customItemKey; }
    public NamespacedKey bossKey() { return bossKey; }
    public NamespacedKey instanceKey() { return instanceKey; }
    public PartyManager partyManager() { return partyManager; }
    public DungeonManager dungeonManager() { return dungeonManager; }
    public LootService lootService() { return lootService; }
    public RelicDraftService relicDraftService() { return relicDraftService; }
}
