package com.proyectoFoc.proyectoFoc.repository;

import com.proyectoFoc.proyectoFoc.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    // Buscar empleado por usuario
    Optional<Empleado> findByUsuario(String usuario);

    // Buscar empleado por usuario y contraseña
    Optional<Empleado> findByUsuarioAndPassword(String usuario, String password);

    // Verificar si existe un usuario
    boolean existsByUsuario(String usuario);

    // Buscar empleado por DNI
    Optional<Empleado> findByDni(String dni);
}
