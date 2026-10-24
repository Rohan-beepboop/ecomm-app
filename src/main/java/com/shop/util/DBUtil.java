package com.shop.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central JDBC connection factory. Credentials live in db.properties (classpath) so they are never
 * hard-coded in source. DAOs call getConnection() inside try-with-resources, so every Connection is
 * closed deterministically.
 *
 * Production note: DriverManager opens a physical connection per call. For real traffic, replace the
 * body of getConnection() with a pooled javax.sql.DataSource (Tomcat JDBC pool / HikariCP via JNDI);
 * no DAO would need to change because they only depend on this method.
 */
public final class DBUtil {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) throw new IllegalStateException("db.properties was not found on the classpath");
            PROPS.load(in);
            // Explicit load: the DriverManager ServiceLoader can miss drivers in webapp class loaders.
            Class.forName(PROPS.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"), PROPS.getProperty("db.user"), PROPS.getProperty("db.password"));
    }

    public static String getProperty(String key, String defaultValue) {
        return PROPS.getProperty(key, defaultValue);
    }
}
