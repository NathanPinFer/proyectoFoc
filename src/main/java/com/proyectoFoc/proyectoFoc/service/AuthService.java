package com.proyectoFoc.proyectoFoc.service;

import com.proyectoFoc.proyectoFoc.dto.EmpleadoDTO;
import com.proyectoFoc.proyectoFoc.entity.Empleado;
import com.proyectoFoc.proyectoFoc.repository.EmpleadoRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    /**
     * -- GETTER --
     *  Obtener usuario actual autenticado
     */
    // Sesión actual
    @Getter
    private EmpleadoDTO usuarioActual;

    /**
     * Intenta autenticar un usuario
     * @param usuario nombre de usuario
     * @param password contraseña
     * @return EmpleadoDTO si login exitoso, null si falla
     */
    public EmpleadoDTO login(String usuario, String password) {
        // Buscar empleado por usuario y contraseña
        Optional<Empleado> empleadoOpt = empleadoRepository.findByUsuarioAndPassword(usuario, password);

        // Si se encuentra el empleado, verificar que esté activo
        if (empleadoOpt.isPresent()) {
            Empleado empleado = empleadoOpt.get();

            // Verificar que esté activo
            if (!empleado.getActivo()) {
                return null; // Usuario desactivado
            }

            // Crear DTO y guardar sesión
            usuarioActual = new EmpleadoDTO(empleado);
            return usuarioActual;
        }

        return null; // Login fallido
    }

    /**
     * Cerrar sesión
     */
    public void logout() {
        usuarioActual = null;
    }

    /**
     * Verificar si hay una sesión activa
     */
    public boolean isAuthenticated() {
        return usuarioActual != null;
    }

    /**
     * Verificar si el usuario actual es gerente
     */
    public boolean isGerente() {
        return usuarioActual != null && usuarioActual.isGerente();
    }
}