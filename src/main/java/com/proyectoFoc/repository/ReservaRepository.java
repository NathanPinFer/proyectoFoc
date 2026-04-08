package com.proyectoFoc.repository;

import com.proyectoFoc.entity.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    List<Reserva> findByClienteIdCliente(Integer idCliente);

    Long countByClienteIdCliente(Integer idCliente);

    // búsqueda con filtros y paginación para el listado de reservas
    @Query("SELECT r FROM Reserva r " +
            "JOIN r.cliente c " +
            "WHERE (:filtroCliente IS NULL OR " +
            "       LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filtroCliente, '%')) OR " +
            "       LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :filtroCliente, '%')) OR " +
            "       LOWER(c.dni) LIKE LOWER(CONCAT('%', :filtroCliente, '%'))) " +
            "AND (:fechaEntrada IS NULL OR r.fechaEntrada = :fechaEntrada) " +
            "AND (:estado IS NULL OR r.estadoReserva = :estado) " +
            "ORDER BY r.fechaReserva DESC, r.idReserva DESC")
    Page<Reserva> buscarConFiltros(
            @Param("filtroCliente") String filtroCliente,
            @Param("fechaEntrada") LocalDate fechaEntrada,
            @Param("estado") String estado,
            Pageable pageable);

    // contadores para estadísticas
    Long countByEstadoReserva(String estadoReserva);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.estadoReserva IN ('Confirmada', 'Pendiente')")
    Long countReservasActivas();

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.fechaEntrada = :hoy AND r.estadoReserva NOT IN ('Cancelada')")
    Long countCheckinHoy(@Param("hoy") LocalDate hoy);

    @Query("SELECT COALESCE(SUM(r.importeTotal), 0) FROM Reserva r " +
            "WHERE YEAR(r.fechaReserva) = :anio AND MONTH(r.fechaReserva) = :mes " +
            "AND r.estadoReserva NOT IN ('Cancelada')")
    BigDecimal ingresosDelMes(@Param("mes") int mes, @Param("anio") int anio);


}