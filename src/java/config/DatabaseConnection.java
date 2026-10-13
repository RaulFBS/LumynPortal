package config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private Connection connection;

    private static String url;
    private static String user;
    private static String password;

    // Bloque estático: lee db.properties y carga el Driver 1 SOLA VEZ en memoria RAM
    static {
        try {
            Properties props = new Properties();
            try (InputStream input = DatabaseConnection.class.getResourceAsStream("db.properties")) {
                if (input != null) {
                    props.load(input);
                    System.out.println("✅ Archivo db.properties cargado en memoria.");
                } else {
                    System.err.println("❌ ERROR: No se encontró db.properties dentro del paquete 'config'");
                }
            } catch (IOException e) {
                System.err.println("❌ Error leyendo db.properties: " + e.getMessage());
            }

            url = props.getProperty("db.url", System.getenv("DB_URL"));
            user = props.getProperty("db.user", System.getenv("DB_USER"));
            password = props.getProperty("db.password", System.getenv("DB_PASSWORD"));

            if (url != null) {
                // Reemplaza localhost por 127.0.0.1 automáticamente para eliminar los 3 seg de demora IPv6 en Windows
                url = url.trim().replace("localhost", "127.0.0.1");
            }
            if (user != null) {
                user = user.trim();
            }
            if (password != null) {
                password = password.trim().replace("\"", "");
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            System.err.println("❌ Error al cargar el driver de MySQL: " + ex.getMessage());
        }
    }

    private DatabaseConnection() throws SQLException {
        long t0 = System.currentTimeMillis();
        this.connection = DriverManager.getConnection(url, user, password);
        System.out.println("⚡ Conexión Singleton abierta en " + (System.currentTimeMillis() - t0) + " ms");
    }

    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.connection == null || instance.connection.isClosed()) {
            synchronized (DatabaseConnection.class) {
                if (instance == null || instance.connection == null || instance.connection.isClosed()) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}