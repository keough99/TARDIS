/*
 * Copyright (C) 2015 eccentric_nz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package me.eccentric_nz.TARDIS.junk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.database.QueryFactory;
import me.eccentric_nz.TARDIS.database.ResultSetBlocks;
import me.eccentric_nz.TARDIS.database.ResultSetTardis;
import me.eccentric_nz.TARDIS.database.data.ReplacedBlock;
import me.eccentric_nz.TARDIS.destroyers.DestroyData;
import me.eccentric_nz.TARDIS.utility.TARDISBlockSetters;
import me.eccentric_nz.TARDIS.utility.TARDISJunkParticles;
import me.eccentric_nz.TARDIS.utility.TARDISSounds;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 *
 * @author eccentric_nz
 */
public class TARDISJunkDestroyer implements Runnable {

    private final TARDIS plugin;
    private final DestroyData pdd;
    private int task;
    private int i = 0;
    private final int sx, ex, sy, ey, sz, ez;
    private final Location junkLoc;
    private final Location effectsLoc;
    private Location vortexJunkLoc;
    World world;
    Biome biome;
    private int fryTask;

    public TARDISJunkDestroyer(TARDIS plugin, DestroyData pdd) {
        this.plugin = plugin;
        this.pdd = pdd;
        this.junkLoc = this.pdd.getLocation();
        this.effectsLoc = this.junkLoc.clone().add(0.5d, 0, 0.5d);
        this.ex = this.junkLoc.getBlockX() + 2;
        this.sx = this.junkLoc.getBlockX() - 3;
        this.sy = this.junkLoc.getBlockY();
        this.ey = this.junkLoc.getBlockY() + 5;
        this.ez = this.junkLoc.getBlockZ() + 3;
        this.sz = this.junkLoc.getBlockZ() - 2;
        this.world = this.junkLoc.getWorld();
        this.biome = this.pdd.getBiome();
    }

    @Override
    public void run() {
        // get relative locations
        if (i < 25) {
            i++;
            if (i == 1) {
                for (Entity e : getJunkTravellers(4.0d)) {
                    if (e instanceof Player) {
                        Player p = (Player) e;
                        plugin.getGeneralKeeper().getJunkTravellers().add(p.getUniqueId());
                    }
                }
                TARDISSounds.playTARDISSound(junkLoc, "junk_takeoff");
                fryTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new TARDISJunkItsDangerousRunnable(plugin, junkLoc), 0, 1L);
            }
            if (i == 25) {
                // get junk vortex location
                HashMap<String, Object> where = new HashMap<String, Object>();
                where.put("uuid", "00000000-aaaa-bbbb-cccc-000000000000");
                ResultSetTardis rs = new ResultSetTardis(plugin, where, "", false);
                if (rs.resultSet()) {
                    // teleport players to vortex
                    vortexJunkLoc = plugin.getLocationUtils().getLocationFromBukkitString(rs.getTardis().getCreeper()).add(3.0d, 0.0d, 2.0d);
                    for (Entity e : getJunkTravellers(4.0d)) {
                        if (e instanceof Player) {
                            final Player p = (Player) e;
                            final Location relativeLoc = getRelativeLocation(p);
                            p.teleport(relativeLoc);
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    p.teleport(relativeLoc);
                                }
                            }, 2L);
                        }
                    }
                    TARDISJunkVortexRunnable runnable = new TARDISJunkVortexRunnable(plugin, vortexJunkLoc, pdd.getPlayer(), pdd.getTardisID());
                    int jvrtask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, runnable, 1L, 20L);
                    runnable.setTask(jvrtask);
                }
                List<Chunk> chunks = new ArrayList<Chunk>();
                // remove blocks
                for (int level = ey; level >= sy; level--) {
                    for (int row = ex; row >= sx; row--) {
                        for (int col = sz; col <= ez; col++) {
                            Block b = world.getBlockAt(row, level, col);
                            b.setType(Material.AIR);
                            if (level == sy && ((b.getBiome().equals(Biome.SKY) && !junkLoc.getWorld().getEnvironment().equals(Environment.THE_END)) || b.getBiome().equals(Biome.VOID)) && biome != null) {
                                if (!chunks.contains(b.getChunk())) {
                                    chunks.add(b.getChunk());
                                }
                                // reset the biome
                                try {
                                    world.setBiome(row, col, biome);
                                } catch (NullPointerException e) {
                                    // remove TARDIS from tracker
                                    plugin.getTrackerKeeper().getDematerialising().remove(Integer.valueOf(pdd.getTardisID()));
                                }
                            }
                        }
                        // refresh the chunks
                        for (Chunk chink : chunks) {
                            //world.refreshChunk(chink.getX(), chink.getZ());
                            plugin.getTardisHelper().refreshChunk(chink);
                        }
                        chunks.clear();
                    }
                }
                plugin.getTrackerKeeper().getDematerialising().remove(Integer.valueOf(pdd.getTardisID()));
                plugin.getTrackerKeeper().getInVortex().remove(Integer.valueOf(pdd.getTardisID()));
                // check protected blocks if has block id and data stored then put the block back!
                HashMap<String, Object> tid = new HashMap<String, Object>();
                tid.put("tardis_id", pdd.getTardisID());
                ResultSetBlocks rsb = new ResultSetBlocks(plugin, tid, true);
                if (rsb.resultSet()) {
                    for (ReplacedBlock rp : rsb.getData()) {
                        int rx = rp.getLocation().getBlockX();
                        int ry = rp.getLocation().getBlockY();
                        int rz = rp.getLocation().getBlockZ();
                        TARDISBlockSetters.setBlock(world, rx, ry, rz, rp.getBlock(), rp.getData());
                    }
                }
                // remove block protection
                plugin.getPresetDestroyer().removeBlockProtection(pdd.getTardisID(), new QueryFactory(plugin));
                plugin.getServer().getScheduler().cancelTask(fryTask);
                plugin.getServer().getScheduler().cancelTask(task);
                task = 0;
            } else if (plugin.getConfig().getBoolean("junk.particles")) {
                // just animate particles
                for (Entity e : plugin.getUtils().getJunkTravellers(junkLoc)) {
                    if (e instanceof Player) {
                        Player p = (Player) e;
                        TARDISJunkParticles.sendVortexParticles(effectsLoc, p);
                    }
                }
            }
        }
    }

    private Location getRelativeLocation(Player p) {
        Location playerLoc = p.getLocation();
        double x = vortexJunkLoc.getX() + (playerLoc.getX() - junkLoc.getX());
        double y = vortexJunkLoc.getY() + (playerLoc.getY() - junkLoc.getY()) + 0.5d;
        double z = vortexJunkLoc.getZ() + (playerLoc.getZ() - junkLoc.getZ());
        Location l = new Location(vortexJunkLoc.getWorld(), x, y, z, playerLoc.getYaw(), playerLoc.getPitch());
        while (!l.getChunk().isLoaded()) {
            l.getChunk().load();
        }
        return l;
    }

    private List<Entity> getJunkTravellers(double d) {
        // spawn an entity
        Entity orb = junkLoc.getWorld().spawnEntity(junkLoc, EntityType.EXPERIENCE_ORB);
        List<Entity> ents = orb.getNearbyEntities(d, d, d);
        orb.remove();
        return ents;
    }

    public void setTask(int task) {
        this.task = task;
    }
}
