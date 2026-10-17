package com.example.cursedrelics.dungeon;

import com.example.cursedrelics.CursedRelicsPlugin;
import com.example.cursedrelics.loot.LootService;
import com.example.cursedrelics.party.PartyManager;
import com.example.cursedrelics.relic.RelicDraftService;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;

public final class DungeonManager implements Listener {
    private final CursedRelicsPlugin plugin;
    private final PartyManager parties;
    private final LootService loot;
    private final RelicDraftService relics;
    private final Map<UUID, DungeonInstance> instances = new LinkedHashMap<>();
    private final Map<UUID, UUID> playerInstances = new HashMap<>();
    private final Map<UUID, Location> returnLocations = new HashMap<>();
    private int nextRegion = 0;

    public DungeonManager(CursedRelicsPlugin plugin, PartyManager parties, LootService loot, RelicDraftService relics) {
        this.plugin = plugin; this.parties = parties; this.loot = loot; this.relics = relics;
    }
    public boolean join(Player requester) {
        if (!requester.hasPermission("cursedrelics.play")) { requester.sendMessage("§cBạn không có quyền vào dungeon."); return false; }
        if (playerInstances.containsKey(requester.getUniqueId())) { requester.sendMessage("§cBạn đang ở trong dungeon."); return false; }
        UUID leader = parties.leaderOf(requester.getUniqueId());
        if (!leader.equals(requester.getUniqueId())) { requester.sendMessage("§eHãy để party leader dùng /dungeon join."); return false; }
        Set<UUID> memberIds = parties.members(requester.getUniqueId());
        List<Player> members = new ArrayList<>();
        for (UUID id : memberIds) { Player p = Bukkit.getPlayer(id); if (p != null && !playerInstances.containsKey(id)) members.add(p); }
        if (members.isEmpty()) { requester.sendMessage("§cKhông có thành viên online đủ điều kiện."); return false; }
        if (members.size() > plugin.getConfig().getInt("settings.max-party-size", 5)) { requester.sendMessage("§cParty quá đông."); return false; }
        World world = requester.getWorld();
        int spacing = plugin.getConfig().getInt("settings.instance-spacing", 160);
        int index = nextRegion++;
        int x = 1000 + index * spacing;
        int z = 1000;
        Location center = new Location(world, x + 0.5, world.getHighestBlockYAt(x, z) + 2, z + 0.5);
        UUID instanceId = UUID.randomUUID();
        DungeonInstance instance = new DungeonInstance(instanceId, world, x, z, members.stream().map(Player::getUniqueId).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)), center);
        instances.put(instanceId, instance);
        buildArena(instance);
        for (Player p : members) {
            returnLocations.put(p.getUniqueId(), p.getLocation().clone());
            playerInstances.put(p.getUniqueId(), instanceId);
            p.teleport(center.clone().add((members.indexOf(p) - (members.size()-1)/2.0) * 2, 0, 4));
            p.sendMessage("§5§lCURSED RELICS §8» §dBạn đã vào Sunken Crypt.");
        }
        spawnBoss(instance);
        return true;
    }
    private void buildArena(DungeonInstance in) {
        World world = in.world(); int radius = plugin.getConfig().getInt("settings.arena-radius", 18); int floorY = in.center().getBlockY() - 1;
        for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
            int bx = in.x() + dx, bz = in.z() + dz;
            world.getBlockAt(bx, floorY, bz).setType(Material.DEEPSLATE_BRICKS, false);
            boolean wall = Math.abs(dx) == radius || Math.abs(dz) == radius;
            for (int dy = 0; dy < 5; dy++) world.getBlockAt(bx, floorY + 1 + dy, bz).setType(wall ? Material.POLISHED_DEEPSLATE : Material.AIR, false);
        }
        for (int dx=-2; dx<=2; dx++) for (int dz=-2; dz<=2; dz++) world.getBlockAt(in.x()+dx, floorY, in.z()+dz).setType(Material.CRYING_OBSIDIAN, false);
    }
    private void spawnBoss(DungeonInstance in) {
        Location spawn = in.center().clone().add(0, 1, -5);
        Zombie boss = in.world().spawn(spawn, Zombie.class, z -> {
            z.setCustomName("§5§lThe Hollow King"); z.setCustomNameVisible(true); z.setPersistent(true); z.setRemoveWhenFarAway(false);
            z.getPersistentDataContainer().set(plugin.bossKey(), PersistentDataType.STRING, in.id().toString());
            z.getPersistentDataContainer().set(plugin.instanceKey(), PersistentDataType.STRING, in.id().toString());
            if (z.getAttribute(Attribute.MAX_HEALTH) != null) z.getAttribute(Attribute.MAX_HEALTH).setBaseValue(plugin.getConfig().getDouble("settings.boss-health", 180.0));
            z.setHealth(Math.min(z.getMaxHealth(), plugin.getConfig().getDouble("settings.boss-health", 180.0)));
            if (z.getAttribute(Attribute.ATTACK_DAMAGE) != null) z.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(plugin.getConfig().getDouble("settings.boss-damage", 9.0));
            z.setGlowing(true);
        });
        in.setBossId(boss.getUniqueId());
        in.world().spawnParticle(Particle.PORTAL, spawn, 80, 1.5, 1, 1.5, 0.1);
        in.world().playSound(spawn, Sound.ENTITY_WITHER_SPAWN, 0.8f, 0.7f);
    }
    public void leave(Player player) {
        UUID id = player.getUniqueId(); UUID instanceId = playerInstances.remove(id);
        if (instanceId == null) { player.sendMessage("§eBạn không ở trong dungeon."); return; }
        Location back = returnLocations.remove(id);
        relics.clearRun(id);
        if (back != null && back.getWorld() != null) player.teleport(back);
        player.sendMessage("§7Bạn đã rời dungeon.");
        DungeonInstance instance = instances.get(instanceId);
        if (instance != null) {
            instance.members().remove(id);
            if (instance.members().isEmpty()) scheduleCleanup(instance);
        }
    }
    @EventHandler public void onBossDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        String value = entity.getPersistentDataContainer().get(plugin.bossKey(), PersistentDataType.STRING);
        if (value == null) return;
        UUID id; try { id = UUID.fromString(value); } catch (IllegalArgumentException ex) { return; }
        DungeonInstance instance = instances.get(id); if (instance == null || instance.completed()) return;
        instance.setCompleted(true);
        event.getDrops().clear();
        event.setDroppedExp(50);
        for (UUID memberId : new ArrayList<>(instance.members())) {
            Player p = Bukkit.getPlayer(memberId);
            if (p == null) continue;
            p.sendMessage("§a§lBOSS DEFEATED §7— Loot đã xuất hiện. Chọn một Relic!");
            p.getInventory().addItem(loot.createCursedLoot("ashen_oath")).values().forEach(item -> p.getWorld().dropItemNaturally(p.getLocation(), item));
            relics.openDraft(p, instance.id());
        }
        instance.world().playSound(entity.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        scheduleCleanup(instance);
    }
    private void scheduleCleanup(DungeonInstance instance) {
        if (instance.cleanupTask() != null) return;
        int delay = plugin.getConfig().getInt("settings.cleanup-delay-seconds", 180) * 20;
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> cleanup(instance), delay);
        instance.setCleanupTask(task);
    }
    private void cleanup(DungeonInstance in) {
        in.setState("CLEANING");
        for (Entity entity : in.world().getNearbyEntities(in.center(), 24, 12, 24)) {
            String iid = entity.getPersistentDataContainer().get(plugin.instanceKey(), PersistentDataType.STRING);
            if (in.id().toString().equals(iid)) entity.remove();
        }
        int radius = plugin.getConfig().getInt("settings.arena-radius", 18); int floorY = in.center().getBlockY()-1;
        for (int dx=-radius; dx<=radius; dx++) for (int dz=-radius; dz<=radius; dz++) for (int dy=0; dy<6; dy++) {
            in.world().getBlockAt(in.x()+dx, floorY+dy, in.z()+dz).setType(Material.AIR, false);
        }
        for (UUID member : new ArrayList<>(in.members())) {
            playerInstances.remove(member); relics.clearRun(member);
            Player p = Bukkit.getPlayer(member); Location back = returnLocations.remove(member);
            if (p != null && back != null && back.getWorld() != null) { p.closeInventory(); p.teleport(back); p.sendMessage("§7Dungeon instance đã đóng."); }
        }
        instances.remove(in.id());
    }
    @EventHandler public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId(); UUID instanceId = playerInstances.get(id);
        if (instanceId != null) {
            DungeonInstance in = instances.get(instanceId);
            if (in != null) { in.members().remove(id); if (in.members().isEmpty()) scheduleCleanup(in); }
            playerInstances.remove(id); returnLocations.remove(id); relics.clearRun(id);
        }
    }
    public void shutdown() { for (DungeonInstance in : new ArrayList<>(instances.values())) cleanup(in); }
    public UUID instanceOf(UUID player) { return playerInstances.get(player); }
}
