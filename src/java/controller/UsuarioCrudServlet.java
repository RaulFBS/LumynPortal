package controller;

import model.Usuario;
import repository.UsuarioCrudRepository;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "UsuarioCrudServlet", urlPatterns = {"/usuario-crud"})
public class UsuarioCrudServlet extends HttpServlet {

    private final UsuarioCrudRepository usuarioCrudRepo = new UsuarioCrudRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        List<Usuario> usuarios = usuarioCrudRepo.listarTodos();

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            json.append(String.format(
                "{\"id\":%d,\"username\":\"%s\",\"rol\":\"%s\",\"intentos\":%d,\"bloqueado\":%b}",
                u.getIdUsuario(), u.getNombreUsuario(), u.getRol(), u.getIntentosFallidos(), u.isBloqueado()
            ));
            if (i < usuarios.size() - 1) json.append(",");
        }
        json.append("]");

        try (PrintWriter out = response.getWriter()) {
            out.print(json.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        if ("crear".equals(accion)) {
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            int rolId = Integer.parseInt(request.getParameter("rolId"));
            usuarioCrudRepo.insertar(email, password, rolId);

        } else if ("editar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("idUsuario"));
            String email = request.getParameter("email");
            int rolId = Integer.parseInt(request.getParameter("rolId"));
            boolean bloqueado = Boolean.parseBoolean(request.getParameter("bloqueado"));
            usuarioCrudRepo.editarUsuario(id, email, rolId, bloqueado);

        } else if ("eliminar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("idUsuario"));
            usuarioCrudRepo.eliminar(id);
        }

        response.sendRedirect("admin-usuarios.html");
    }
}