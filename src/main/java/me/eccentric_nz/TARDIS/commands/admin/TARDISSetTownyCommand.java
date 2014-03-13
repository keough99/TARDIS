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
package me.eccentric_nz.TARDIS.commands.admin;

import com.google.common.collect.ImmutableList;
import java.util.Locale;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.enumeration.MESSAGE;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author eccentric_nz
 */
public class TARDISSetTownyCommand {

    private final TARDIS plugin;
    private final ImmutableList<String> regions = ImmutableList.of("none", "wilderness", "town", "nation");

    public TARDISSetTownyCommand(TARDIS plugin) {
        this.plugin = plugin;
    }

    public boolean setRegion(CommandSender sender, String[] args) {
        String region = args[1].toLowerCase(Locale.ENGLISH);
        if (!regions.contains(region)) {
            sender.sendMessage(plugin.getPluginName() + ChatColor.RED + "The last argument must be none, wilderness, town or nation!");
            return false;
        }
        plugin.getConfig().set("preferences.respect_towny", region);
        plugin.saveConfig();
        sender.sendMessage(plugin.getPluginName() + MESSAGE.CONFIG_UPDATED.getText());
        return true;
    }
}