package com.proyectoFoc.repository;

import com.proyectoFoc.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    // Buscar cliente por DNI
    Optional<Cliente> findByDni(String dni);

    // Buscar cliente por email
    Optional<Cliente> findByEmail(String email);

    // Verificar si existe un DNI
    boolean existsByDni(String dni);

    // Verificar si existe un email
    boolean existsByEmail(String email);

    // Buscar clientes VIP
    List<Cliente> findByVip(Boolean vip);

    // Buscar clientes por nombre o apellidos (búsqueda parcial)
    List<Cliente> findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase(String nombre, String apellidos);

    // Buscar por ciudad
    List<Cliente> findByCiudad(String ciudad);
    
    // Buscar con paginación
    Page<Cliente> findAll(Pageable pageable);
    
    // Buscar con filtro y paginación
    @Query("SELECT c FROM Cliente c WHERE " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(c.dni) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :filtro, '%'))")
    Page<Cliente> buscarConFiltro(@Param("filtro") String filtro, Pageable pageable);
    
    // Contar clientes VIP
    long countByVip(Boolean vip);
    
    // Contar clientes registrados hoy
    long countByFechaRegistro(LocalDate fecha);
}
