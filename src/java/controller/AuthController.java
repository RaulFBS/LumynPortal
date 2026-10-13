package controller;

import config.DatabaseConnection;
import model.Usuario;
import repository.UsuarioRepository;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AuthController", urlPatterns = {"/auth"}, loadOnStartup = 1)
public class AuthController extends HttpServlet {

    private final UsuarioRepository usuarioRepo = new UsuarioRepository();

    // Se ejecuta automáticamente cuando arranca Tomcat (antes de que hagas clic en Login)
    @Override
    public void init() throws ServletException {
        super.init();
        CompletableFuture.runAsync(() -> {
            try {
                DatabaseConnection.getInstance();
                System.out.println("🚀 Singleton conectado y precalentado al iniciar Tomcat.");
            } catch (Exception e) {
                System.err.println("Aviso precalentamiento: " + e.getMessage());
            }
        });
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long t0 = System.currentTimeMillis();

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        Usuario usuario = usuarioRepo.buscarPorUsername(email);
        System.out.println("⏱️ Búsqueda en MySQL tardó: " + (System.currentTimeMillis() - t0) + " ms");

        if (usuario == null) {
            response.sendRedirect("index.html?error=credenciales");
            return;
        }

        // Si ya está bloqueado en la BD
        if (usuario.isBloqueado()) {
            response.sendRedirect("index.html?error=bloqueado");
            return;
        }

        // Verificar contraseña encriptada en memoria RAM (0 ms)
        if (usuarioRepo.verificarPasswordEncriptada(usuario, password)) {
            
            // Crear sesión inmediatamente
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(30 * 60);
            session.setAttribute("usuarioLogueado", usuario);

            // Actualizar ultimoAcceso en segundo plano para NO frenar la redirección
            final int idUser = usuario.getIdUsuario();
            CompletableFuture.runAsync(() -> usuarioRepo.registrarAccesoExitoso(idUser));

            System.out.println("⚡ Redirigiendo al Dashboard en: " + (System.currentTimeMillis() - t0) + " ms totales");

            if ("PROJECT_MANAGER".equals(usuario.getRol())) {
                response.sendRedirect("dashboard-pm.html");
            } else {
                response.sendRedirect("portal-cliente.html");
            }
        } else {
            // Incrementar intentos fallidos en la BD
            int intentos = usuarioRepo.registrarIntentoFallido(usuario.getIdUsuario(), usuario.getIntentosFallidos());
            if (intentos >= 3) {
                response.sendRedirect("index.html?error=bloqueado");
            } else {
                response.sendRedirect("index.html?error=intento&n=" + intentos);
            }
        }
    }
}