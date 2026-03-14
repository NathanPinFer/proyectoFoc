package com.hotel.gestion_hotelera.repository;

import com.hotel.gestion_hotelera.model.DetalleReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DetalleReservaRepository extends JpaRepository<DetalleReserva, Integer> {

    @Query("""
        SELECT d FROM DetalleReserva d
        WHERE d.fechaEntrada <= :finMes
          AND d.fechaSalida >= :inicioMes
          AND d.reserva.estadoReserva IN ('Confirmada', 'Pendiente')
        ORDER BY d.habitacion.piso ASC, d.habitacion.numeroHabitacion ASC
    """)
    List<DetalleReserva> findReservasPorMes(
        @Param("inicioMes") LocalDate inicioMes,
        @Param("finMes") LocalDate finMes
    );
}