package com.proyectoFoc.repository;

import com.proyectoFoc.entity.Empleado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    
    // Buscar por usuario (para login)
    Optional<Empleado> findByUsuario(String usuario);
    
    // Buscar por DNI
    Optional<Empleado> findByDni(String dni);
    
    // Buscar por email
    Optional<Empleado> findByEmail(String email);
    
    // Buscar empleados activos
    List<Empleado> findByActivoTrue();
    
    // Buscar por cargo
    List<Empleado> findByCargo(String cargo);
    
    // Buscar empleados activos por cargo
    List<Empleado> findByCargoAndActivoTrue(String cargo);
    
    // Búsqueda general (nombre, apellidos, dni, email, usuario)
    @Query("SELECT e FROM Empleado e WHERE " +
           "LOWER(e.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.dni) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.usuario) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Empleado> buscarEmpleados(@Param("query") String query, Pageable pageable);
    
    // Contar empleados activos
    long countByActivoTrue();
    
    // Contar por cargo
    long countByCargo(String cargo);
    
    // Contar empleados activos por cargo
    long countByCargoAndActivoTrue(String cargo);
}
