package com.proyectoFoc.service;

import com.proyectoFoc.dto.ReservaCalendarioDTO;
import com.proyectoFoc.dto.ReservaRequestDTO;
import com.proyectoFoc.entity.*;
import com.proyectoFoc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private DetalleReservaRepository detalleReservaRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Obtener reservas del calendario para un mes específico
     */
    public List<ReservaCalendarioDTO> obtenerCalendario(int mes, int anio) {
        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDate inicioMes = yearMonth.atDay(1);
        LocalDate finMes = yearMonth.atEndOfMonth();
        
        return detalleReservaRepository.obtenerCalendario(inicioMes, finMes);
    }

    /**
     * Crear nueva reserva
     */
    @Transactional
    public Reserva crearReserva(ReservaRequestDTO dto) {
        // Validar que la habitación existe
        Habitacion habitacion = habitacionRepository.findById(dto.getIdHabitacion())
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

        // Validar que el cliente existe
        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Validar fechas
        if (dto.getFechaEntrada().isAfter(dto.getFechaSalida())) {
            throw new RuntimeException("La fecha de entrada no puede ser posterior a la de salida");
        }

        if (dto.getFechaEntrada().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de entrada no puede ser anterior a hoy");
        }

        // Verificar disponibilidad
        Long reservasExistentes = detalleReservaRepository.verificarDisponibilidad(
                dto.getIdHabitacion(),
                dto.getFechaEntrada(),
                dto.getFechaSalida());

        if (reservasExistentes > 0) {
            throw new RuntimeException("La habitación no está disponible en esas fechas");
        }

        // Calcular subtotal
        long noches = ChronoUnit.DAYS.between(dto.getFechaEntrada(), dto.getFechaSalida());
        BigDecimal subtotal = dto.getPrecioNoche().multiply(BigDecimal.valueOf(noches));
        
        // Crear la reserva
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setEstadoReserva("Confirmada");
        reserva.setFechaReserva(LocalDate.now());
        reserva.setFechaEntrada(dto.getFechaEntrada());
        reserva.setFechaSalida(dto.getFechaSalida());
        reserva.setNumAdultos(1);
        reserva.setNumNinos(0);
        reserva.setImporteTotal(subtotal);
        reserva.setObservaciones(dto.getObservaciones());
        
        Reserva reservaGuardada = reservaRepository.save(reserva);
        
        // Crear el detalle de reserva
        DetalleReserva detalle = new DetalleReserva();
        detalle.setReserva(reservaGuardada);
        detalle.setHabitacion(habitacion);
        detalle.setFechaEntrada(dto.getFechaEntrada());
        detalle.setFechaSalida(dto.getFechaSalida());
        detalle.setPrecioNoche(dto.getPrecioNoche());
        detalle.setSubtotal(subtotal);
        
        detalleReservaRepository.save(detalle);

        return reservaGuardada;
    }

    /**
     * Obtener reserva por ID
     */
    public Optional<Reserva> obtenerPorId(Integer id) {
        return reservaRepository.findById(id);
    }

    /**
     * Cancelar reserva
     */
    @Transactional
    public void cancelarReserva(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        
        reserva.setEstadoReserva("Cancelada");
        reservaRepository.save(reserva);
    }

    /**
     * Obtener número de reservas de un cliente
     */
    public Long contarReservasCliente(Integer idCliente) {
        return reservaRepository.countByClienteIdCliente(idCliente);
    }
}
