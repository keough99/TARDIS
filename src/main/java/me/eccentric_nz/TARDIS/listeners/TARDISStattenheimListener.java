/*
 * Copyright (C) 2016 eccentric_nz
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
package me.eccentric_nz.TARDIS.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.advanced.TARDISCircuitChecker;
import me.eccentric_nz.TARDIS.api.Parameters;
import me.eccentric_nz.TARDIS.artron.TARDISBeaconToggler;
import me.eccentric_nz.TARDIS.artron.TARDISLampToggler;
import me.eccentric_nz.TARDIS.artron.TARDISPoliceBoxLampToggler;
import me.eccentric_nz.TARDIS.builders.BuildData;
import me.eccentric_nz.TARDIS.database.QueryFactory;
import me.eccentric_nz.TARDIS.database.ResultSetCurrentLocation;
import me.eccentric_nz.TARDIS.database.ResultSetPlayerPrefs;
import me.eccentric_nz.TARDIS.database.ResultSetTardis;
import me.eccentric_nz.TARDIS.database.ResultSetTravellers;
import me.eccentric_nz.TARDIS.database.data.Tardis;
import me.eccentric_nz.TARDIS.destroyers.DestroyData;
import me.eccentric_nz.TARDIS.enumeration.COMPASS;
import me.eccentric_nz.TARDIS.enumeration.DIFFICULTY;
import me.eccentric_nz.TARDIS.enumeration.FLAG;
import me.eccentric_nz.TARDIS.enumeration.PRESET;
import me.eccentric_nz.TARDIS.travel.TARDISTimeTravel;
import me.eccentric_nz.TARDIS.utility.TARDISMessage;
import me.eccentric_nz.TARDIS.utility.TARDISStaticUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yi.acru.bukkit.Lockette.Lockette;

/**
 * The handheld Recall Button on the TARDIS Stattenheim remote broadcasts a
 * Stattenheim signal through the Vortex, which summons the operator's TARDIS
 * when the operator is in the field.
 *
 * @author eccentric_nz
 */
public class TARDISStattenheimListener implements Listener {

    private final TARDIS plugin;
    List<Material> useless = new ArrayList<Material>();
    Material remote;

    public TARDISStattenheimListener(TARDIS plugin) {
        this.plugin = plugin;
        // add useless blocks
        useless.add(Material.BROWN_MUSHROOM);
        useless.add(Material.CARPET);
        useless.add(Material.DEAD_BUSH);
        useless.add(Material.LONG_GRASS);
        useless.add(Material.RED_MUSHROOM);
        useless.add(Material.RED_ROSE);
        useless.add(Material.SAPLING);
        useless.add(Material.SNOW);
        useless.add(Material.YELLOW_FLOWER);
        remote = Material.valueOf(plugin.getRecipesConfig().getString("shaped.Stattenheim Remote.result"));
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onStattenheimInteract(PlayerInteractEvent event) {
        if (event.getHand() == null || event.getHand().equals(EquipmentSlot.OFF_HAND)) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack is = player.getInventory().getItemInMainHand();
        if (is.getType().equals(remote) && is.hasItemMeta()) {
            ItemMeta im = is.getItemMeta();
            if (im.getDisplayName().equals("Stattenheim Remote")) {
                Action action = event.getAction();
                // check they are a Time Lord
                UUID uuid = player.getUniqueId();
                HashMap<String, Object> where = new HashMap<String, Object>();
                where.put("uuid", uuid.toString());
                ResultSetTardis rs = new ResultSetTardis(plugin, where, "", false, 0);
                if (!rs.resultSet()) {
                    TARDISMessage.send(player, "NO_TARDIS");
                    return;
                }
                Tardis tardis = rs.getTardis();
                final int id = tardis.getTardis_id();
                if (plugin.getTrackerKeeper().getInSiegeMode().contains(id)) {
                    TARDISMessage.send(player, "SIEGE_NO_CONTROL");
                    return;
                }
                if (plugin.getTrackerKeeper().getDispersedTARDII().contains(id)) {
                    TARDISMessage.send(player.getPlayer(), "NOT_WHILE_DISPERSED");
                    return;
                }
                boolean power = tardis.isPowered_on();
                final QueryFactory qf = new QueryFactory(plugin);
                if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
                    Block b = event.getClickedBlock();
                    Material m = b.getType();
                    if (b.getState() instanceof InventoryHolder || plugin.getGeneralKeeper().getDoors().contains(m)) {
                        return;
                    }
                    if (player.hasPermission("tardis.timetravel")) {
                        Location remoteLocation = b.getLocation();
                        if (!plugin.getConfig().getBoolean("travel.include_default_world") && plugin.getConfig().getBoolean("creation.default_world") && remoteLocation.getWorld().getName().equals(plugin.getConfig().getString("creation.default_world_name"))) {
                            TARDISMessage.send(player, "NO_WORLD_TRAVEL");
                            return;
                        }
                        if (!plugin.getPluginRespect().getRespect(remoteLocation, new Parameters(player, FLAG.getDefaultFlags()))) {
                            return;
                        }
                        if (player.hasPermission("tardis.exile") && plugin.getConfig().getBoolean("travel.exile")) {
                            String areaPerm = plugin.getTardisArea().getExileArea(player);
                            if (plugin.getTardisArea().areaCheckInExile(areaPerm, remoteLocation)) {
                                TARDISMessage.send(player, "EXILE_NO_TRAVEL");
                                return;
                            }
                        }
                        if (!plugin.getTardisArea().areaCheckInExisting(remoteLocation)) {
                            TARDISMessage.send(player, "AREA_NO_STAT", ChatColor.AQUA + "/tardistravel area [area name]");
                            return;
                        }
                        if (!useless.contains(m)) {
                            int yplusone = remoteLocation.getBlockY();
                            remoteLocation.setY(yplusone + 1);
                        }
                        // check the world is not excluded
                        String world = remoteLocation.getWorld().getName();
                        if (!plugin.getConfig().getBoolean("worlds." + world)) {
                            TARDISMessage.send(player, "NO_PB_IN_WORLD");
                            return;
                        }
                        if (plugin.getConfig().getBoolean("allow.power_down") && !power) {
                            TARDISMessage.send(player, "POWER_DOWN");
                            return;
                        }
                        TARDISCircuitChecker tcc = null;
                        if (!plugin.getDifficulty().equals(DIFFICULTY.EASY) && !plugin.getUtils().inGracePeriod(player, true)) {
                            tcc = new TARDISCircuitChecker(plugin, id);
                            tcc.getCircuits();
                        }
                        if (tcc != null && !tcc.hasMaterialisation()) {
                            TARDISMessage.send(player, "NO_MAT_CIRCUIT");
                            return;
                        }
                        boolean hidden = tardis.isHidden();
                        int level = tardis.getArtron_level();
                        boolean cham = (plugin.getConfig().getBoolean("travel.chameleon") && tardis.isChamele_on());
                        // check they are not in the tardis
                        HashMap<String, Object> wherettrav = new HashMap<String, Object>();
                        wherettrav.put("uuid", uuid.toString());
                        wherettrav.put("tardis_id", id);
                        ResultSetTravellers rst = new ResultSetTravellers(plugin, wherettrav, false);
                        if (rst.resultSet()) {
                            TARDISMessage.send(player, "NO_PB_IN_TARDIS");
                            return;
                        }
                        if (plugin.getTrackerKeeper().getInVortex().contains(id)) {
                            TARDISMessage.send(player, "NOT_WHILE_MAT");
                            return;
                        }
                        // get TARDIS's current location
                        HashMap<String, Object> wherecl = new HashMap<String, Object>();
                        wherecl.put("tardis_id", tardis.getTardis_id());
                        ResultSetCurrentLocation rsc = new ResultSetCurrentLocation(plugin, wherecl);
                        if (!rsc.resultSet()) {
                            hidden = true;
                        }
                        COMPASS d = rsc.getDirection();
                        COMPASS player_d = COMPASS.valueOf(TARDISStaticUtils.getPlayersDirection(player, false));
                        TARDISTimeTravel tt = new TARDISTimeTravel(plugin);
                        int count;
                        boolean sub = false;
                        if (b.getRelative(BlockFace.UP).getType().equals(Material.WATER) || b.getRelative(BlockFace.UP).getType().equals(Material.STATIONARY_WATER)) {
                            count = (tt.isSafeSubmarine(remoteLocation, player_d)) ? 0 : 1;
                            if (count == 0) {
                                sub = true;
                            }
                        } else {
                            int[] start_loc = tt.getStartLocation(remoteLocation, player_d);
                            // safeLocation(int startx, int starty, int startz, int resetx, int resetz, World w, COMPASS player_d)
                            count = tt.safeLocation(start_loc[0], remoteLocation.getBlockY(), start_loc[2], start_loc[1], start_loc[3], remoteLocation.getWorld(), player_d);
                        }
                        if (plugin.getPM().isPluginEnabled("Lockette")) {
                            Lockette Lockette = (Lockette) plugin.getPM().getPlugin("Lockette");
                            if (Lockette.isProtected(remoteLocation.getBlock())) {
                                count = 1;
                            }
                        }
                        if (count > 0) {
                            TARDISMessage.send(player, "WOULD_GRIEF_BLOCKS");
                            return;
                        }
                        int ch = plugin.getArtronConfig().getInt("comehere");
                        if (level < ch) {
                            TARDISMessage.send(player, "NOT_ENOUGH_ENERGY");
                            return;
                        }
                        Location oldSave = null;
                        HashMap<String, Object> bid = new HashMap<String, Object>();
                        bid.put("tardis_id", id);
                        HashMap<String, Object> bset = new HashMap<String, Object>();
                        if (rsc.getWorld() != null) {
                            oldSave = new Location(rsc.getWorld(), rsc.getX(), rsc.getY(), rsc.getZ());
                            // set fast return location
                            bset.put("world", rsc.getWorld().getName());
                            bset.put("x", rsc.getX());
                            bset.put("y", rsc.getY());
                            bset.put("z", rsc.getZ());
                            bset.put("direction", d.toString());
                            bset.put("submarine", rsc.isSubmarine());
                        } else {
                            // set fast return location
                            bset.put("world", remoteLocation.getWorld().getName());
                            bset.put("x", remoteLocation.getX());
                            bset.put("y", remoteLocation.getY());
                            bset.put("z", remoteLocation.getZ());
                            bset.put("submarine", (sub) ? 1 : 0);
                        }
                        qf.doUpdate("back", bset, bid);
                        // set current location
                        HashMap<String, Object> cid = new HashMap<String, Object>();
                        cid.put("tardis_id", id);
                        HashMap<String, Object> cset = new HashMap<String, Object>();
                        cset.put("world", remoteLocation.getWorld().getName());
                        cset.put("x", remoteLocation.getBlockX());
                        cset.put("y", remoteLocation.getBlockY());
                        cset.put("z", remoteLocation.getBlockZ());
                        cset.put("direction", player_d.toString());
                        cset.put("submarine", (sub) ? 1 : 0);
                        qf.doUpdate("current", cset, cid);
                        // update tardis
                        if (hidden) {
                            HashMap<String, Object> tid = new HashMap<String, Object>();
                            HashMap<String, Object> set = new HashMap<String, Object>();
                            set.put("hidden", 0);
                            tid.put("tardis_id", id);
                            qf.doUpdate("tardis", set, tid);
                            // restore biome
                            plugin.getUtils().restoreBiome(oldSave, rsc.getBiome());
                        }
                        TARDISMessage.send(player, "TARDIS_COMING");
//                        boolean mat = plugin.getConfig().getBoolean("police_box.materialise");
//                        long delay = (mat) ? 10L : 180L;
                        long delay = 10L;
                        plugin.getTrackerKeeper().getInVortex().add(id);
                        final boolean hid = hidden;
                        if (!plugin.getTrackerKeeper().getDestinationVortex().containsKey(id)) {
                            final DestroyData dd = new DestroyData(plugin, uuid.toString());
                            dd.setChameleon(cham);
                            dd.setDirection(d);
                            dd.setLocation(oldSave);
                            dd.setPlayer(player);
                            dd.setHide(false);
                            dd.setOutside(true);
                            dd.setSubmarine(rsc.isSubmarine());
                            dd.setTardisID(id);
                            dd.setBiome(rsc.getBiome());
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    if (!hid) {
                                        plugin.getTrackerKeeper().getDematerialising().add(id);
                                        plugin.getPresetDestroyer().destroyPreset(dd);
                                    } else {
                                        plugin.getPresetDestroyer().removeBlockProtection(id, qf);
                                    }
                                }
                            }, delay);
                        }
                        final BuildData bd = new BuildData(plugin, uuid.toString());
                        bd.setChameleon(cham);
                        bd.setDirection(player_d);
                        bd.setLocation(remoteLocation);
                        bd.setMalfunction(false);
                        bd.setOutside(true);
                        bd.setPlayer(player);
                        bd.setRebuild(false);
                        bd.setSubmarine(sub);
                        bd.setTardisID(id);
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                plugin.getPresetBuilder().buildPreset(bd);
                            }
                        }, delay * 2);
                        // remove energy from TARDIS
                        HashMap<String, Object> wheret = new HashMap<String, Object>();
                        wheret.put("tardis_id", id);
                        qf.alterEnergyLevel("tardis", -ch, wheret, player);
                        plugin.getTrackerKeeper().getHasDestination().remove(id);
                        if (plugin.getTrackerKeeper().getRescue().containsKey(id)) {
                            plugin.getTrackerKeeper().getRescue().remove(id);
                        }
                    } else {
                        TARDISMessage.send(player, "NO_PERMS");
                    }
                } else if (action.equals(Action.RIGHT_CLICK_AIR) && plugin.getConfig().getBoolean("allow.power_down")) {
                    // is the power off?
                    if (!power) {
                        HashMap<String, Object> wherek = new HashMap<String, Object>();
                        wherek.put("uuid", uuid.toString());
                        ResultSetPlayerPrefs rsp = new ResultSetPlayerPrefs(plugin, wherek);
                        boolean beacon_on = true;
                        if (rsp.resultSet()) {
                            beacon_on = rsp.isBeaconOn();
                        }
                        // power up
                        PRESET preset = tardis.getPreset();
                        HashMap<String, Object> wherep = new HashMap<String, Object>();
                        wherep.put("tardis_id", id);
                        HashMap<String, Object> setp = new HashMap<String, Object>();
                        setp.put("powered_on", 1);
                        TARDISMessage.send(player, "POWER_ON");
                        // if lights are off, turn them on
                        if (tardis.isLights_on()) {
                            new TARDISLampToggler(plugin).flickSwitch(id, uuid, false, tardis.getSchematic().hasLanterns());
                        }
                        // if beacon is off turn it on
                        if (beacon_on) {
                            new TARDISBeaconToggler(plugin).flickSwitch(uuid, id, true);
                        }
                        // police box lamp
                        if (preset.equals(PRESET.NEW) || preset.equals(PRESET.OLD)) {
                            new TARDISPoliceBoxLampToggler(plugin).toggleLamp(id, true);
                        }
                        qf.doUpdate("tardis", setp, wherep);
                    }
                }
            }
        }
    }
}
