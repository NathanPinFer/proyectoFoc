package com.hotel.gestion_hotelera.service;

import com.hotel.gestion_hotelera.dto.ReservaRequestDTO;
import com.hotel.gestion_hotelera.model.*;
import com.hotel.gestion_hotelera.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ReservaService {

    private final ClienteRepository clienteRepository;
    private final HabitacionRepository habitacionRepository;
    private final ReservaRepository reservaRepository;
    private final DetalleReservaRepository detalleReservaRepository;

    public ReservaService(ClienteRepository clienteRepository,
                          HabitacionRepository habitacionRepository,
                          ReservaRepository reservaRepository,
                          DetalleReservaRepository detalleReservaRepository) {
        this.clienteRepository = clienteRepository;
        this.habitacionRepository = habitacionRepository;
        this.reservaRepository = reservaRepository;
        this.detalleReservaRepository = detalleReservaRepository;
    }

    @Transactional
    public Reserva crearReserva(ReservaRequestDTO dto) {

        // 1. Obtener o crear cliente
        Cliente cliente;
        if (dto.getIdCliente() != null) {
            cliente = clienteRepository.findById(dto.getIdCliente())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        } else {
            cliente = new Cliente();
            cliente.setNombre(dto.getNombre());
            cliente.setApellidos(dto.getApellidos());
            cliente.setDni(dto.getDni());
            cliente.setEmail(dto.getEmail() != null ? dto.getEmail() : "");
            cliente.setTelefono(dto.getTelefono() != null ? dto.getTelefono() : "");
            cliente.setVip(dto.getVip() != null ? dto.getVip() : false);
            cliente.setFechaRegistro(LocalDate.now());
            cliente.setPais("España");
            cliente = clienteRepository.save(cliente);
        }

        // 2. Obtener habitación
        Habitacion habitacion = habitacionRepository.findById(dto.getIdHabitacion())
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

        // 3. Calcular importe
        long noches = ChronoUnit.DAYS.between(dto.getFechaEntrada(), dto.getFechaSalida());
        BigDecimal subtotal = dto.getPrecioNoche().multiply(BigDecimal.valueOf(noches));

        // 4. Crear reserva
        Reserva reserva = new Reserva();
        reserva.setIdCliente(cliente);
        reserva.setEstadoReserva("Confirmada");
        reserva.setFechaEntrada(dto.getFechaEntrada());
        reserva.setFechaSalida(dto.getFechaSalida());
        reserva.setNumAdultos(dto.getNumAdultos());
        reserva.setNumNinos(dto.getNumNinos());
        reserva.setImporteTotal(subtotal);
        reserva.setObservaciones(dto.getObservaciones());
        reserva = reservaRepository.save(reserva);

        // 5. Crear detalle
        DetalleReserva detalle = new DetalleReserva();
        detalle.setReserva(reserva);
        detalle.setHabitacion(habitacion);
        detalle.setFechaEntrada(dto.getFechaEntrada());
        detalle.setFechaSalida(dto.getFechaSalida());
        detalle.setPrecioNoche(dto.getPrecioNoche());
        detalle.setSubtotal(subtotal);
        detalleReservaRepository.save(detalle);

        return reserva;
    }
}