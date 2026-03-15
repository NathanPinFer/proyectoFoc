package com.hotel.gestion_hotelera.controller;

import com.hotel.gestion_hotelera.dto.ReservaCalendarioDTO;
import com.hotel.gestion_hotelera.dto.ReservaRequestDTO;
import com.hotel.gestion_hotelera.model.Habitacion;
import com.hotel.gestion_hotelera.model.Reserva;
import com.hotel.gestion_hotelera.service.DashboardService;
import com.hotel.gestion_hotelera.service.ReservaService;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    private final DashboardService dashboardService;
    private final ReservaService reservaService;

    public DashboardRestController(DashboardService dashboardService,
            ReservaService reservaService) {
        this.dashboardService = dashboardService;
        this.reservaService = reservaService;
    }

    @GetMapping("/habitaciones")
    public List<Habitacion> getHabitaciones() {
        return dashboardService.obtenerTodasHabitaciones();
    }

    @PostMapping("/reservas")
    public Reserva crearReserva(@RequestBody ReservaRequestDTO dto) {
        return reservaService.crearReserva(dto);
    }

    @GetMapping("/calendario")
    public List<ReservaCalendarioDTO> getCalendario(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) {

        // Si no se pasan parámetros, usar mes y año actuales
        if (mes == 0 || anio == 0) {
            LocalDate hoy = LocalDate.now();
            mes = hoy.getMonthValue();
            anio = hoy.getYear();
        }

        return dashboardService.obtenerCalendario(mes, anio);
    }
}
