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

/**
 * The Database class provides methods to interact with a SQLite database.
 * It supports initializing tables, setting data, and retrieving data with type
 * conversion.
 * 
 * <p>
 * This class uses JDBC to connect to a SQLite database and perform SQL
 * operations.
 * It includes methods to initialize the database tables, set data with optional
 * upsert behavior,
 * and retrieve data with automatic type conversion based on predefined parsers.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * {@code
 * Database db = new Database();
 * db.setData("exampleKey", "exampleValue");
 * String value = db.getData("exampleKey", String.class);
 * }
 * </pre>
 * 
 * <p>
 * Supported types for data retrieval include Boolean, Long, Integer, Double,
 * Float, BigDecimal, String, and Date.
 * </p>
 * 
 * <p>
 * Note: This class assumes that the VanillaPlus instance and its data folder
 * are properly set up.
 * </p>
 * 
 * <p>
 * Exceptions are caught and printed to the standard error stream.
 * </p>
 * 
 * @see java.sql.Connection
 * @see java.sql.DriverManager
 * @see java.sql.PreparedStatement
 * @see java.sql.ResultSet
 * @see java.util.function.Function
 */
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

    /**
     * Initializes the database tables by executing the SQL statement defined in
     * INIT_TABLES.
     * This method uses a PreparedStatement to execute the update and handles any
     * SQLExceptions
     * that may occur during the process.
     */
    private void initTables() {
        try (PreparedStatement stmt = conn.prepareStatement(INIT_TABLES)) {
            stmt.executeUpdate();
        } catch (SQLException err) {
            err.printStackTrace();
        }
    }

    /**
     * Sets the data for the specified key with the given value.
     * This method will use the default behavior for handling the data.
     *
     * @param key   the key for which the data is to be set
     * @param value the value to be set for the specified key
     */
    public void setData(String key, Object value) {
        setData(key, value, true);
    }

    /**
     * Sets the data in the database for the given key.
     *
     * @param key     the key for which the data is to be set
     * @param value   the value to be set for the given key
     * @param initial if true, the data is set initially; otherwise, it is upserted
     */
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

    /**
     * Retrieves data from the database for the given key and converts it to the
     * specified type.
     *
     * @param <T>  the type of the data to be returned
     * @param key  the key for which data is to be retrieved
     * @param type the class of the type to which the data should be converted
     * @return the data converted to the specified type, or null if an error occurs
     * @throws IllegalArgumentException if no data is found for the given key
     * @throws ClassCastException       if the specified type is unsupported
     */
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

    /**
     * Retrieves all data from the database.
     *
     * This method executes a SQL query to fetch all key-value pairs from the
     * database
     * and stores them in a Map. The keys and values are expected to be strings.
     *
     * @return a Map containing all key-value pairs from the database.
     */
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