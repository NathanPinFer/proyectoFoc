package com.proyectoFoc.repository;

import com.proyectoFoc.entity.TipoHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TipoHabitacionRepository extends JpaRepository<TipoHabitacion, Integer> {
    
    Optional<TipoHabitacion> findByNombre(String nombre);
}
