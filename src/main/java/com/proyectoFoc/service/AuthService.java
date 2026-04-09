package com.proyectoFoc.service;

import com.proyectoFoc.entity.Empleado;
import com.proyectoFoc.repository.EmpleadoRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    /**
     * -- GETTER --
     *  Obtener el empleado que ha iniciado sesión
     */
    @Getter
    private Empleado empleadoActual;

    /**
     * Iniciar sesión
     * @return true si las credenciales son correctas, false en caso contrario
     */
    public boolean login(String usuario, String password) {
        Optional<Empleado> empleadoOpt = empleadoRepository.findByUsuario(usuario);

        if (empleadoOpt.isPresent()) {
            Empleado empleado = empleadoOpt.get();

            if (passwordEncoder.matches(password, empleado.getPassword())) {
                empleadoActual = empleado;
                return true;
            }
        }

        return false;
    }

    /**
     * Cerrar sesión
     */
    public void logout() {
        empleadoActual = null;
    }

    /**
     * Verificar si hay un empleado autenticado
     */
    public boolean isAuthenticated() {
        return empleadoActual != null;
    }

    /**
     * Verificar si el empleado actual es gerente
     */
    public boolean isGerente() {
        return empleadoActual != null && "Gerente".equals(empleadoActual.getCargo());
    }

    /**
     * Obtener el cargo del empleado actual
     */
    public String getCargoActual() {
        return empleadoActual != null ? empleadoActual.getCargo() : null;
    }

    /**
     * Verificar si el empleado debe cambiar su contraseña
     * @return true si debe cambiar, false si no
     */
    public boolean debeCambiarPassword() {
        return empleadoActual != null && 
               Boolean.TRUE.equals(empleadoActual.getDebeCambiarPassword());
    }

    /**
     * Recargar los datos del empleado actual desde la BD
     * (Útil después de cambiar la contraseña)
     */
    public void recargarEmpleadoActual() {
        if (empleadoActual != null) {
            empleadoActual = empleadoRepository.findById(empleadoActual.getIdEmpleado())
                    .orElse(null);
        }
    }
}
