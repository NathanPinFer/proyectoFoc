package com.proyectoFoc.repository;

import com.proyectoFoc.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
    
    // Obtener reservas de un cliente
    List<Reserva> findByClienteIdCliente(Integer idCliente);
    
    // Contar reservas de un cliente
    Long countByClienteIdCliente(Integer idCliente);
}
