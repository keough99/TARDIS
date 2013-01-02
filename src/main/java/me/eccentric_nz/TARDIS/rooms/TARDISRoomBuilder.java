/*
 * Copyright (C) 2012 eccentric_nz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.eccentric_nz.TARDIS.rooms;

import java.util.HashMap;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.TARDISConstants.COMPASS;
import me.eccentric_nz.TARDIS.TARDISConstants.ROOM;
import me.eccentric_nz.TARDIS.database.QueryFactory;
import me.eccentric_nz.TARDIS.database.ResultSetTardis;
import me.eccentric_nz.TARDIS.database.TARDISDatabase;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author eccentric_nz
 */
public class TARDISRoomBuilder {

    private final TARDIS plugin;
    TARDISDatabase service = TARDISDatabase.getInstance();
    private ROOM r;
    private Location l;
    private COMPASS d;
    private Player p;

    public TARDISRoomBuilder(TARDIS plugin, ROOM r, Location l, COMPASS d, Player p) {
        this.plugin = plugin;
        this.r = r;
        this.l = l;
        this.d = d;
        this.p = p;
    }

    public boolean build() {
        HashMap<String, Object> where = new HashMap<String, Object>();
        where.put("player", p.getName());
        ResultSetTardis rs = new ResultSetTardis(plugin, where, "", false);
        if (rs.resultSet()) {
            int id = rs.getTardis_id();
            plugin.debug(rs.getMiddle_id());
            // get middle data, default to orange wool if not set
            int middle_id = (rs.getMiddle_id() != 0) ? rs.getMiddle_id() : 35;
            byte middle_data = (rs.getMiddle_data() != 0) ? rs.getMiddle_data() : 1;
            switch (r) {
                case PASSAGE:
                    // todo
                    TARDISPassage newPassage = new TARDISPassage(plugin, l, middle_id, middle_data);
                    newPassage.passage();
                    break;
                default:
                    // ROOM
                    break;
            }
            QueryFactory qf = new QueryFactory(plugin);
            HashMap<String, Object> set = new HashMap<String, Object>();
            set.put("tardis_id", id);
            set.put("world", l.getWorld().getName());
            set.put("startx", l.getBlockX());
            set.put("starty", l.getBlockY());
            set.put("startz", l.getBlockZ());
            set.put("endx", l.getBlockX());
            set.put("endy", l.getBlockY());
            set.put("endz", l.getBlockZ());
            set.put("room_type", r.toString());
            set.put("room_direction", d.toString());
            qf.doInsert("rooms", set);
        }
        return true;
    }
}
