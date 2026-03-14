package com.hotel.gestion_hotelera.service;

import com.hotel.gestion_hotelera.dto.ReservaCalendarioDTO;
import com.hotel.gestion_hotelera.model.DetalleReserva;
import com.hotel.gestion_hotelera.model.Habitacion;
import com.hotel.gestion_hotelera.repository.DetalleReservaRepository;
import com.hotel.gestion_hotelera.repository.HabitacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final DetalleReservaRepository detalleReservaRepository;
    private final HabitacionRepository habitacionRepository;

    public DashboardService(DetalleReservaRepository detalleReservaRepository,
                            HabitacionRepository habitacionRepository) {
        this.detalleReservaRepository = detalleReservaRepository;
        this.habitacionRepository = habitacionRepository;
    }

    public List<Habitacion> obtenerTodasHabitaciones() {
        return habitacionRepository.findAllByOrderByPisoAscNumeroHabitacionAsc();
    }

    public List<ReservaCalendarioDTO> obtenerCalendario(int mes, int anio) {
        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDate inicioMes = yearMonth.atDay(1);
        LocalDate finMes = yearMonth.atEndOfMonth();

        List<DetalleReserva> detalles =
            detalleReservaRepository.findReservasPorMes(inicioMes, finMes);

        return detalles.stream().map(d -> new ReservaCalendarioDTO(
            d.getIdDetalle(),
            d.getReserva().getIdReserva(),
            d.getHabitacion().getNumeroHabitacion(),
            d.getHabitacion().getIdHabitacion(),
            d.getHabitacion().getTipoHabitacion().getNombre(),
            d.getHabitacion().getPiso(),
            d.getReserva().getIdCliente().getNombre(),
            d.getReserva().getIdCliente().getApellidos(),
            d.getReserva().getIdCliente().getVip(),
            d.getFechaEntrada(),
            d.getFechaSalida(),
            d.getPrecioNoche(),
            d.getSubtotal()
        )).collect(Collectors.toList());
    }
}