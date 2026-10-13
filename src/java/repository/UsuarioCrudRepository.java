package repository;

import config.DatabaseConnection;
import config.PasswordEncryptor;
import model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioCrudRepository {

    // 1. READ: Listar todos los usuarios de MySQL
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.idUsuario, u.nombreUsuario, r.nombre_rol, u.intentosFallidos, u.bloqueado " +
                     "FROM Usuarios u INNER JOIN rol r ON u.rol_id = r.rol_id ORDER BY u.idUsuario ASC";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Usuario(
                        rs.getInt("idUsuario"),
                        rs.getString("nombreUsuario"),
                        rs.getString("nombre_rol"),
                        rs.getInt("intentosFallidos"),
                        rs.getBoolean("bloqueado")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 2. CREATE: Insertar nuevo usuario con contraseña encriptada SHA-256
    public boolean insertar(String email, String passwordPlana, int rolId) {
        String hash = PasswordEncryptor.encriptarSHA256(passwordPlana);
        int permisoId = (rolId == 1) ? 1 : 2;
        String sql = "INSERT INTO Usuarios (nombreUsuario, password_hash, rol_id, permiso_id, intentosFallidos, bloqueado) VALUES (?, ?, ?, ?, 0, FALSE)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, hash);
                ps.setInt(3, rolId);
                ps.setInt(4, permisoId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 3. UPDATE: Editar correo, rol y desbloquear/bloquear usuario
    public boolean editarUsuario(int idUsuario, String email, int rolId, boolean bloqueado) {
        String sql = "UPDATE Usuarios SET nombreUsuario = ?, rol_id = ?, bloqueado = ?, intentosFallidos = ? WHERE idUsuario = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setInt(2, rolId);
                ps.setBoolean(3, bloqueado);
                ps.setInt(4, bloqueado ? 3 : 0);
                ps.setInt(5, idUsuario);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 4. DELETE: Eliminar usuario de la base de datos
    public boolean eliminar(int idUsuario) {
        String sql = "DELETE FROM Usuarios WHERE idUsuario = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}