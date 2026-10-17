package com.example.cursedrelics.dungeon;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import java.util.LinkedHashSet;
import java.util.UUID;

public final class DungeonInstance {
    private final UUID id; private final World world; private final int x; private final int z; private final LinkedHashSet<UUID> members; private final Location center;
    private UUID bossId; private boolean completed; private String state = "RUNNING"; private BukkitTask cleanupTask;
    public DungeonInstance(UUID id, World world, int x, int z, LinkedHashSet<UUID> members, Location center) { this.id=id; this.world=world; this.x=x; this.z=z; this.members=members; this.center=center; }
    public UUID id(){return id;} public World world(){return world;} public int x(){return x;} public int z(){return z;} public LinkedHashSet<UUID> members(){return members;} public Location center(){return center;} public UUID bossId(){return bossId;} public void setBossId(UUID v){bossId=v;} public boolean completed(){return completed;} public void setCompleted(boolean v){completed=v;} public String state(){return state;} public void setState(String v){state=v;} public BukkitTask cleanupTask(){return cleanupTask;} public void setCleanupTask(BukkitTask v){cleanupTask=v;}
}
