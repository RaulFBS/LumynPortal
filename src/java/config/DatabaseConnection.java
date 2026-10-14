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
        // Forzar lectura directa de las variables de entorno de Railway
        url = System.getenv("DB_URL");
        user = System.getenv("DB_USER");
        password = System.getenv("DB_PASSWORD");

        if (url == null || url.trim().isEmpty()) {
            // Respaldo por si corre local sin variables
            url = "jdbc:mysql://iriguchi.proxy.rlwy.net:50154/railway?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        }
        if (user == null || user.trim().isEmpty()) {
            user = "root";
        }
        if (password == null || password.trim().isEmpty()) {
            password = "TU_PASSWORD_LARGA_DE_RAILWAY";
        }

        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("🌐 URL de BD configurada en la nube: " + url);
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
