package com.proyectoFoc.service;

import com.proyectoFoc.entity.Habitacion;
import com.proyectoFoc.repository.HabitacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class HabitacionService {

    @Autowired
    private HabitacionRepository habitacionRepository;

    /**
     * Obtener todas las habitaciones ordenadas
     */
    public List<Habitacion> obtenerTodas() {
        return habitacionRepository.findAllOrdenadas();
    }

    /**
     * Obtener habitación por ID
     */
    public Optional<Habitacion> obtenerPorId(Integer id) {
        return habitacionRepository.findById(id);
    }

    /**
     * Obtener habitación por número
     */
    public Optional<Habitacion> obtenerPorNumero(String numero) {
        return habitacionRepository.findByNumeroHabitacion(numero);
    }

    /**
     * Obtener habitaciones por estado
     */
    public List<Habitacion> obtenerPorEstado(String estado) {
        return habitacionRepository.findByEstadoHabitacion(estado);
    }

    /**
     * Guardar habitación
     */
    @Transactional
    public Habitacion guardar(Habitacion habitacion) {
        return habitacionRepository.save(habitacion);
    }

    /**
     * Eliminar habitación
     */
    @Transactional
    public void eliminar(Integer id) {
        habitacionRepository.deleteById(id);
    }
}
