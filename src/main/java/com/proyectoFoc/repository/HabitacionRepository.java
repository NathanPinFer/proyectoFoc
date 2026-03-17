package com.proyectoFoc.repository;

import com.proyectoFoc.entity.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Integer> {
    
    Optional<Habitacion> findByNumeroHabitacion(String numeroHabitacion);
    
    List<Habitacion> findByEstadoHabitacion(String estado);
    
    List<Habitacion> findByPiso(Integer piso);
    
    @Query("SELECT h FROM Habitacion h ORDER BY h.numeroHabitacion ASC")
    List<Habitacion> findAllOrdenadas();
}
