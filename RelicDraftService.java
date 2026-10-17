package com.example.cursedrelics.relic;

import com.example.cursedrelics.CursedRelicsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public final class RelicDraftService implements Listener {
    private final CursedRelicsPlugin plugin;
    private final Map<UUID, List<String>> pending = new HashMap<>();
    private final Map<UUID, LinkedHashSet<String>> active = new HashMap<>();
    private final Map<UUID, UUID> playerInstance = new HashMap<>();
    private final List<Relic> pool = List.of(
        new Relic("blood_battery", "§cBlood Battery", Material.REDSTONE, "§7Every few hits, trigger a small burst. Prototype."),
        new Relic("echo_ward", "§bEcho Ward", Material.SHIELD, "§7Gain brief resistance after a kill."),
        new Relic("fractured_hourglass", "§eFractured Hourglass", Material.CLOCK, "§7Gain a short speed boost after a kill."),
        new Relic("ash_collector", "§7Ash Collector", Material.COAL, "§7A future currency bonus relic."),
        new Relic("void_heart", "§5Void Heart", Material.AMETHYST_SHARD, "§7Gain a brief strength effect after a kill."),
        new Relic("grave_charm", "§2Grave Charm", Material.PHANTOM_MEMBRANE, "§7Gain brief regeneration after the boss falls.")
    );
    public RelicDraftService(CursedRelicsPlugin plugin) { this.plugin = plugin; }
    public void openDraft(Player player, UUID instanceId) {
        List<Relic> candidates = new ArrayList<>(pool);
        Set<String> already = active.getOrDefault(player.getUniqueId(), new LinkedHashSet<>());
        candidates.removeIf(r -> already.contains(r.id()));
        Collections.shuffle(candidates);
        int count = Math.min(3, candidates.size());
        List<String> ids = candidates.subList(0, count).stream().map(Relic::id).toList();
        if (ids.isEmpty()) { player.sendMessage("§eBạn đã sở hữu toàn bộ Relic trong MVP."); return; }
        pending.put(player.getUniqueId(), ids);
        playerInstance.put(player.getUniqueId(), instanceId);
        Inventory inv = Bukkit.createInventory(null, 9, "§5Relic Draft");
        for (int i = 0; i < ids.size(); i++) inv.setItem(2 + i * 2, itemFor(find(ids.get(i))));
        ItemStack info = new ItemStack(Material.BOOK); ItemMeta meta = info.getItemMeta(); meta.setDisplayName("§eChọn một Relic"); info.setItemMeta(meta); inv.setItem(8, info);
        player.openInventory(inv);
    }
    private Relic find(String id) { return pool.stream().filter(r -> r.id().equals(id)).findFirst().orElseThrow(); }
    private ItemStack itemFor(Relic relic) { ItemStack item = new ItemStack(relic.material()); ItemMeta meta = item.getItemMeta(); meta.setDisplayName(relic.name()); meta.setLore(List.of(relic.description(), "§8ID: " + relic.id(), "§aNhấn để chọn")); item.setItemMeta(meta); return item; }
    @EventHandler public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§5Relic Draft")) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) return;
        List<String> choices = pending.get(player.getUniqueId());
        if (choices == null) { player.closeInventory(); return; }
        int index = (slot - 2) / 2;
        if (slot < 2 || slot > 6 || slot % 2 != 0 || index < 0 || index >= choices.size()) return;
        String id = choices.get(index);
        active.computeIfAbsent(player.getUniqueId(), k -> new LinkedHashSet<>()).add(id);
        pending.remove(player.getUniqueId());
        player.closeInventory();
        player.sendMessage("§aBạn đã chọn Relic: §f" + find(id).name());
    }
    public List<String> getActiveRelics(UUID player) { return List.copyOf(active.getOrDefault(player, new LinkedHashSet<>())); }
    public void clearRun(UUID player) { active.remove(player); pending.remove(player); playerInstance.remove(player); }
    private record Relic(String id, String name, Material material, String description) {}
}
