package org.xodium.vanillaplus;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
    private Connection conn;
    private final VanillaPlus plugin = VanillaPlus.getInstance();

    private static final String DB_URL_PREFIX = "jdbc:sqlite:";
    private static final String DB_FILE = "vanilla.db";

    private static final String INIT_TABLES = "CREATE TABLE IF NOT EXISTS config (key TEXT PRIMARY KEY, value TEXT)";
    private static final String SET_DATA = "INSERT INTO config (key, value) SELECT ?, ? WHERE NOT EXISTS (SELECT 1 FROM config WHERE key = ?)";
    private static final String UPDATE_DATA = "INSERT OR REPLACE INTO config (key, value) VALUES (?, ?)";
    private static final String GET_DATA = "SELECT value FROM config WHERE key = ?";
    private static final String GET_DATA_COLUMN_VALUE = "value";

    public Database() {
        try {
            if (!plugin.getDataFolder().exists())
                plugin.getDataFolder().mkdirs();
            conn = DriverManager
                    .getConnection(DB_URL_PREFIX + new File(plugin.getDataFolder(), DB_FILE).getAbsolutePath());
            initTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initTables() {
        try (PreparedStatement stmt = conn.prepareStatement(
                INIT_TABLES)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setData(String key, Object value) {
        try (PreparedStatement stmt = conn.prepareStatement(SET_DATA)) {
            stmt.setString(1, key);
            stmt.setObject(2, value);
            stmt.setString(3, key);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateData(String key, Object value) {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_DATA)) {
            stmt.setString(1, key);
            stmt.setObject(2, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getData(String key) {
        try (PreparedStatement stmt = conn.prepareStatement(
                GET_DATA)) {
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(GET_DATA_COLUMN_VALUE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
