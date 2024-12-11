package org.xodium.vanillaplus;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Database {
    private Connection conn;
    private static final VanillaPlus VP = VanillaPlus.getInstance();

    private static final String DB_URL_PREFIX = "jdbc:sqlite:";
    private static final String DB_FILE = "vanillaplus.db";

    private static final String INIT_TABLES = "CREATE TABLE IF NOT EXISTS config (key TEXT PRIMARY KEY, value TEXT)";
    private static final String SET_DATA_INITIAL = "INSERT INTO config (key, value) SELECT ?, ? WHERE NOT EXISTS (SELECT 1 FROM config WHERE key = ?)";
    private static final String SET_DATA_UPSERT = "INSERT INTO config (key, value) VALUES (?, ?) "
            + "ON CONFLICT(key) DO UPDATE SET value = EXCLUDED.value";
    private static final String GET_DATA = "SELECT value FROM config WHERE key = ?";
    private static final String GET_ALL_DATA = "SELECT key, value FROM config";
    private static final String GET_DATA_COLUMN_VALUE = "value";

    private static final Map<Class<?>, Function<String, ?>> TYPE_PARSERS = new HashMap<>() {
        {
            put(Boolean.class, Boolean::parseBoolean);
            put(Long.class, Long::parseLong);
            put(Integer.class, Integer::parseInt);
            put(Double.class, Double::parseDouble);
            put(Float.class, Float::parseFloat);
            put(BigDecimal.class, BigDecimal::new);
            put(String.class, Function.identity());
            put(Date.class, Date::valueOf);
        }
    };

    {
        try {
            VP.getDataFolder().mkdirs();
            conn = DriverManager
                    .getConnection(DB_URL_PREFIX + new File(VP.getDataFolder(), DB_FILE).getAbsolutePath());
            initTables();
        } catch (SQLException err) {
            err.printStackTrace();
        }
    }

    private void initTables() {
        try (PreparedStatement stmt = conn.prepareStatement(INIT_TABLES)) {
            stmt.executeUpdate();
        } catch (SQLException err) {
            err.printStackTrace();
        }
    }

    public void setData(String key, Object value) {
        setData(key, value, true);
    }

    public void setData(String key, Object value, boolean initial) {
        String sql = initial ? SET_DATA_INITIAL : SET_DATA_UPSERT;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setObject(2, value instanceof Boolean ? ((Boolean) value ? "true" : "false") : value);
            if (initial) {
                stmt.setString(3, key);
            }
            stmt.executeUpdate();
        } catch (SQLException err) {
            err.printStackTrace();
        }
    }

    public <T> T getData(String key, Class<T> type) {
        try (PreparedStatement stmt = conn.prepareStatement(GET_DATA)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String value = rs.getString(GET_DATA_COLUMN_VALUE);
                    Function<String, ?> parser = TYPE_PARSERS.get(type);
                    if (parser != null) {
                        return type.cast(parser.apply(value));
                    }
                    throw new ClassCastException("Unsupported type: " + type.getSimpleName());
                } else {
                    throw new IllegalArgumentException("No data found for key: " + key);
                }
            }
        } catch (SQLException | IllegalArgumentException err) {
            err.printStackTrace();
            return null;
        }
    }

    public Map<String, String> getAllData() {
        Map<String, String> data = new HashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(GET_ALL_DATA);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String key = rs.getString("key");
                String value = rs.getString("value");
                data.put(key, value);
            }
        } catch (SQLException err) {
            err.printStackTrace();
        }
        return data;
    }
}