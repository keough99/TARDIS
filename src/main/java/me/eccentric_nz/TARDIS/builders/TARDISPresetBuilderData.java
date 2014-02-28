/*
 * Copyright (C) 2014 eccentric_nz
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
package me.eccentric_nz.TARDIS.builders;

import me.eccentric_nz.TARDIS.enumeration.COMPASS;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Data class for building the TARDIS exterior.
 *
 * @author eccentric_nz
 */
public class TARDISPresetBuilderData {

    private COMPASS direction;
    private Location location;
    private Player player;
    private boolean chameleon;
    private boolean malfunction;
    private boolean rebuild;
    private boolean submarine;
    private int tardisID;

    public COMPASS getDirection() {
        return direction;
    }

    public void setDirection(COMPASS direction) {
        this.direction = direction;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isChameleon() {
        return chameleon;
    }

    public void setChameleon(boolean chameleon) {
        this.chameleon = chameleon;
    }

    public boolean isMalfunction() {
        return malfunction;
    }

    public void setMalfunction(boolean malfunction) {
        this.malfunction = malfunction;
    }

    public boolean isRebuild() {
        return rebuild;
    }

    public void setRebuild(boolean rebuild) {
        this.rebuild = rebuild;
    }

    public boolean isSubmarine() {
        return submarine;
    }

    public void setSubmarine(boolean submarine) {
        this.submarine = submarine;
    }

    public int getTardisID() {
        return tardisID;
    }

    public void setTardisID(int tardisID) {
        this.tardisID = tardisID;
    }
}
