package com.hotel.gestion_hotelera.repository;

import com.hotel.gestion_hotelera.model.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Integer> {
    List<Habitacion> findAllByOrderByPisoAscNumeroHabitacionAsc();
}