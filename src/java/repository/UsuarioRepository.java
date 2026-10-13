package repository;

import config.DatabaseConnection;
import config.PasswordEncryptor;
import model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioRepository {

    public Usuario buscarPorUsername(String username) {
        String sql = "SELECT u.idUsuario, u.nombreUsuario, u.password_hash, r.nombre_rol, u.intentosFallidos, u.bloqueado " +
                     "FROM Usuarios u INNER JOIN rol r ON u.rol_id = r.rol_id " +
                     "WHERE u.nombreUsuario = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Usuario u = new Usuario(
                            rs.getInt("idUsuario"),
                            rs.getString("nombreUsuario"),
                            rs.getString("nombre_rol"),
                            rs.getInt("intentosFallidos"),
                            rs.getBoolean("bloqueado")
                        );
                        u.setPasswordHash(rs.getString("password_hash"));
                        return u;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean verificarPasswordEncriptada(Usuario usuario, String passwordPlana) {
        if (usuario.getPasswordHash() == null) return false;
        String hashIngresado = PasswordEncryptor.encriptarSHA256(passwordPlana);
        return hashIngresado.equalsIgnoreCase(usuario.getPasswordHash());
    }

    public int registrarIntentoFallido(int idUsuario, int intentosActuales) {
        int nuevosIntentos = intentosActuales + 1;
        boolean bloquear = (nuevosIntentos >= 3);
        String sql = "UPDATE Usuarios SET intentosFallidos = ?, bloqueado = ? WHERE idUsuario = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, nuevosIntentos);
                ps.setBoolean(2, bloquear);
                ps.setInt(3, idUsuario);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nuevosIntentos;
    }

    public void registrarAccesoExitoso(int idUsuario) {
        String sql = "UPDATE Usuarios SET intentosFallidos = 0, ultimoAcceso = NOW() WHERE idUsuario = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}