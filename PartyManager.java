package com.example.cursedrelics.party;

import com.example.cursedrelics.CursedRelicsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.*;

public final class PartyManager {
    private final CursedRelicsPlugin plugin;
    private final Map<UUID, LinkedHashSet<UUID>> parties = new HashMap<>();
    private final Map<UUID, UUID> invitations = new HashMap<>();
    public PartyManager(CursedRelicsPlugin plugin) { this.plugin = plugin; }
    public UUID leaderOf(UUID player) {
        for (var e : parties.entrySet()) if (e.getValue().contains(player)) return e.getKey();
        return player;
    }
    public Set<UUID> members(UUID player) {
        UUID leader = leaderOf(player);
        LinkedHashSet<UUID> found = parties.get(leader);
        return found == null ? new LinkedHashSet<>(Set.of(player)) : new LinkedHashSet<>(found);
    }
    public boolean invite(Player leader, Player target) {
        UUID lead = leaderOf(leader.getUniqueId());
        if (!lead.equals(leader.getUniqueId())) { leader.sendMessage("§cChỉ party leader mới được mời."); return false; }
        LinkedHashSet<UUID> group = parties.computeIfAbsent(lead, k -> new LinkedHashSet<>(Set.of(lead)));
        if (group.size() >= plugin.getConfig().getInt("settings.max-party-size", 5)) { leader.sendMessage("§cParty đã đầy."); return false; }
        if (group.contains(target.getUniqueId())) { leader.sendMessage("§eNgười chơi đã ở trong party."); return false; }
        invitations.put(target.getUniqueId(), lead);
        leader.sendMessage("§aĐã mời " + target.getName() + ".");
        target.sendMessage("§eBạn được mời vào party của " + leader.getName() + ". Dùng §f/dungeon party accept§e.");
        return true;
    }
    public boolean accept(Player player) {
        UUID lead = invitations.remove(player.getUniqueId());
        if (lead == null || Bukkit.getPlayer(lead) == null) { player.sendMessage("§cBạn không có lời mời party hợp lệ."); return false; }
        if (!lead.equals(leaderOf(player.getUniqueId())) && parties.values().stream().anyMatch(s -> s.contains(player.getUniqueId()))) {
            player.sendMessage("§cBạn đang ở trong party khác."); return false;
        }
        LinkedHashSet<UUID> group = parties.computeIfAbsent(lead, k -> new LinkedHashSet<>(Set.of(lead)));
        if (group.size() >= plugin.getConfig().getInt("settings.max-party-size", 5)) { player.sendMessage("§cParty đã đầy."); return false; }
        group.add(player.getUniqueId());
        for (UUID id : group) { Player p = Bukkit.getPlayer(id); if (p != null) p.sendMessage("§a" + player.getName() + " đã vào party."); }
        return true;
    }
    public boolean isLeader(UUID player) { return leaderOf(player).equals(player); }
    public void remove(UUID player) {
        UUID leader = leaderOf(player);
        LinkedHashSet<UUID> group = parties.get(leader);
        if (group != null) { group.remove(player); if (group.size() <= 1) parties.remove(leader); }
        invitations.remove(player);
    }
}
