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
package me.eccentric_nz.TARDIS.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.utility.TARDISNumberParsers;

/**
 * A numerical Type designates each model of TARDIS. Every TARDIS that is
 * constructed follows the specifications of its specific "Type." For example
 * the Doctor usually operates a Type 40 TARDIS. Higher Type numbers indicated
 * later model TARDISes.
 *
 * @author eccentric_nz
 */
public class ResultSetCount {

    private final TARDISDatabaseConnection service = TARDISDatabaseConnection.getInstance();
    private final Connection connection = service.getConnection();
    private final TARDIS plugin;
    private final HashMap<String, Object> where;
    private final boolean multiple;
    private int id;
    private UUID uuid;
    private int count;
    private int grace;
    private final ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
    private final String prefix;

    /**
     * Creates a class instance that can be used to retrieve an SQL ResultSet
     * from the count table.
     *
     * @param plugin an instance of the main class.
     * @param where a HashMap<String, Object> of table fields and values to
     * refine the search.
     * @param multiple a boolean indicating whether multiple rows should be
     * fetched
     */
    public ResultSetCount(TARDIS plugin, HashMap<String, Object> where, boolean multiple) {
        this.plugin = plugin;
        this.where = where;
        this.multiple = multiple;
        this.prefix = this.plugin.getPrefix();
    }

    /**
     * Retrieves an SQL ResultSet from the lamps table. This method builds an
     * SQL query string from the parameters supplied and then executes the
     * query. Use the getters to retrieve the results.
     *
     * @return true or false depending on whether any data matches the query
     */
    public boolean resultSet() {
        PreparedStatement statement = null;
        ResultSet rs = null;
        String wheres = "";
        if (where != null) {
            StringBuilder sbw = new StringBuilder();
            for (Map.Entry<String, Object> entry : where.entrySet()) {
                sbw.append(entry.getKey()).append(" = ? AND ");
            }
            wheres = " WHERE " + sbw.toString().substring(0, sbw.length() - 5);
        }
        String query = "SELECT * FROM " + prefix + "t_count" + wheres;
        try {
            service.testConnection(connection);
            statement = connection.prepareStatement(query);
            if (where != null) {
                int s = 1;
                for (Map.Entry<String, Object> entry : where.entrySet()) {
                    if (entry.getValue().getClass().equals(String.class) || entry.getValue().getClass().equals(UUID.class)) {
                        statement.setString(s, entry.getValue().toString());
                    } else {
                        statement.setInt(s, TARDISNumberParsers.parseInt(entry.getValue().toString()));
                    }
                    s++;
                }
                where.clear();
            }
            rs = statement.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    if (multiple) {
                        HashMap<String, String> row = new HashMap<String, String>();
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int columns = rsmd.getColumnCount();
                        for (int i = 1; i < columns + 1; i++) {
                            row.put(rsmd.getColumnName(i).toLowerCase(Locale.ENGLISH), rs.getString(i));
                        }
                        data.add(row);
                    }
                    this.id = rs.getInt("t_id");
                    this.uuid = UUID.fromString(rs.getString("uuid"));
                    this.count = rs.getInt("count");
                    this.grace = rs.getInt("grace");
                }
            } else {
                return false;
            }
        } catch (SQLException e) {
            plugin.debug("ResultSet error for count table! " + e.getMessage());
            return false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                plugin.debug("Error closing count table! " + e.getMessage());
            }
        }
        return true;
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getCount() {
        return count;
    }

    public int getGrace() {
        return grace;
    }

    public ArrayList<HashMap<String, String>> getData() {
        return data;
    }
}
