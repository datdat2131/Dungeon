package com.example.cursedrelics;

import com.example.cursedrelics.dungeon.DungeonManager;
import com.example.cursedrelics.party.PartyManager;
import com.example.cursedrelics.relic.RelicDraftService;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public final class DungeonCommand implements CommandExecutor, TabCompleter {
    private final DungeonManager dungeons; private final PartyManager parties; private final RelicDraftService relics;
    public DungeonCommand(DungeonManager dungeons, PartyManager parties, RelicDraftService relics) { this.dungeons=dungeons; this.parties=parties; this.relics=relics; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("This command is player-only."); return true; }
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage("§5§lCURSED RELICS §7Commands:"); player.sendMessage("§d/dungeon join §7- Start Sunken Crypt"); player.sendMessage("§d/dungeon leave §7- Leave dungeon"); player.sendMessage("§d/dungeon party invite <player>"); player.sendMessage("§d/dungeon party accept"); player.sendMessage("§d/dungeon relics §7- View run relics"); return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "join" -> dungeons.join(player);
            case "leave" -> dungeons.leave(player);
            case "party" -> {
                if (args.length >= 2 && args[1].equalsIgnoreCase("invite") && args.length >= 3) {
                    Player target = Bukkit.getPlayerExact(args[2]); if (target == null) player.sendMessage("§cNgười chơi không online."); else parties.invite(player, target);
                } else if (args.length >= 2 && args[1].equalsIgnoreCase("accept")) parties.accept(player);
                else player.sendMessage("§eDùng: /dungeon party invite <player> hoặc /dungeon party accept");
            }
            case "relics" -> {
                List<String> list = relics.getActiveRelics(player.getUniqueId());
                player.sendMessage("§5§lRelics §7(" + list.size() + "): " + (list.isEmpty() ? "§7Chưa có Relic trong lượt này." : "§d" + String.join("§7, §d", list)));
            }
            default -> player.sendMessage("§cLệnh không hợp lệ. Dùng /dungeon help.");
        }
        return true;
    }
    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("join", "leave", "party", "relics", "help");
        if (args.length == 2 && args[0].equalsIgnoreCase("party")) return List.of("invite", "accept");
        if (args.length == 3 && args[0].equalsIgnoreCase("party") && args[1].equalsIgnoreCase("invite")) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        return Collections.emptyList();
    }
}
