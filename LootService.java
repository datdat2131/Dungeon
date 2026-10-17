package com.example.cursedrelics.loot;

import com.example.cursedrelics.CursedRelicsPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class LootService {
    private final CursedRelicsPlugin plugin;
    private final NamespacedKey curseKey;
    public LootService(CursedRelicsPlugin plugin) { this.plugin = plugin; this.curseKey = new NamespacedKey(plugin, "curse_id"); }
    public ItemStack createCursedLoot(String id) {
        Material material = switch (id) {
            case "gravekeepers_aegis" -> Material.SHIELD;
            case "last_pulse" -> Material.TOTEM_OF_UNDYING;
            default -> Material.DIAMOND_SWORD;
        };
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        String name = switch (id) { case "gravekeepers_aegis" -> "§5Gravekeeper's Aegis"; case "last_pulse" -> "§dThe Last Pulse"; default -> "§cAshen Oath"; };
        meta.setDisplayName(name);
        meta.setLore(List.of("§7Cursed Loot", "§8A relic of the Sunken Crypt", "§cCurse: Hollow Vitality", "§7Prototype effect: healing reduced while burdened."));
        meta.getPersistentDataContainer().set(plugin.customItemKey(), PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(curseKey, PersistentDataType.STRING, "hollow_vitality");
        item.setItemMeta(meta);
        return item;
    }
}
