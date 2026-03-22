package com.proyectoFoc.repository;

import com.proyectoFoc.dto.ReservaCalendarioDTO;
import com.proyectoFoc.entity.DetalleReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DetalleReservaRepository extends JpaRepository<DetalleReserva, Integer> {
    
    // Obtener reservas del calendario para un mes específico
    @Query("SELECT new com.proyectoFoc.dto.ReservaCalendarioDTO(" +
           "dr.reserva.idReserva, " +
           "dr.habitacion.idHabitacion, " +
           "dr.habitacion.numeroHabitacion, " +
           "dr.habitacion.tipoHabitacion.nombre, " +
           "dr.habitacion.piso, " +
           "dr.reserva.cliente.idCliente, " +
           "dr.reserva.cliente.nombre, " +
           "dr.reserva.cliente.apellidos, " +
           "dr.reserva.cliente.vip, " +
           "dr.fechaEntrada, " +
           "dr.fechaSalida, " +
           "dr.precioNoche, " +
           "dr.subtotal, " +
           "dr.reserva.estadoReserva) " +
           "FROM DetalleReserva dr " +
           "WHERE (dr.fechaEntrada <= :finMes AND dr.fechaSalida >= :inicioMes) " +
           "AND dr.reserva.estadoReserva != 'Cancelada' " +
           "ORDER BY dr.fechaEntrada ASC")
    List<ReservaCalendarioDTO> obtenerCalendario(
            @Param("inicioMes") LocalDate inicioMes,
            @Param("finMes") LocalDate finMes);
    
    // Verificar disponibilidad de habitación
    @Query("SELECT COUNT(dr) FROM DetalleReserva dr " +
           "WHERE dr.habitacion.idHabitacion = :idHabitacion " +
           "AND dr.reserva.estadoReserva != 'Cancelada' " +
           "AND ((dr.fechaEntrada <= :fechaSalida AND dr.fechaSalida >= :fechaEntrada))")
    Long verificarDisponibilidad(
            @Param("idHabitacion") Integer idHabitacion,
            @Param("fechaEntrada") LocalDate fechaEntrada,
            @Param("fechaSalida") LocalDate fechaSalida);
    
    // Obtener detalles de reservas de un cliente
    List<DetalleReserva> findByReservaClienteIdCliente(Integer idCliente);
}
