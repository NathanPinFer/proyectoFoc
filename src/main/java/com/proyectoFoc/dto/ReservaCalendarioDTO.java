package com.proyectoFoc.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReservaCalendarioDTO {
    
    private Integer idReserva;
    private Integer idHabitacion;
    private String numeroHabitacion;
    private String tipoHabitacion;
    private Integer piso;
    
    private Integer idCliente;
    private String nombreCliente;
    private String apellidosCliente;
    private Boolean clienteVip;
    
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private BigDecimal precioNoche;
    private BigDecimal subtotal;
    
    private String estadoBloque;
    private String estadoReserva;
    
    // Constructor completo
    public ReservaCalendarioDTO(
            Integer idReserva,
            Integer idHabitacion,
            String numeroHabitacion,
            String tipoHabitacion,
            Integer piso,
            Integer idCliente,
            String nombreCliente,
            String apellidosCliente,
            Boolean clienteVip,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            BigDecimal precioNoche,
            BigDecimal subtotal,
            String estadoReserva) {

        this.idReserva = idReserva;
        this.idHabitacion = idHabitacion;
        this.numeroHabitacion = numeroHabitacion;
        this.tipoHabitacion = tipoHabitacion;
        this.piso = piso;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.apellidosCliente = apellidosCliente;
        this.clienteVip = clienteVip;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.precioNoche = precioNoche;
        this.subtotal = subtotal;
        this.estadoReserva = estadoReserva;

        LocalDate hoy = LocalDate.now();
        if ("No_presentado".equals(estadoReserva)) {
            this.estadoBloque = "NO_SHOW";
        } else if ("Completada".equals(estadoReserva)) {
            this.estadoBloque = "FINALIZADA";
        } else if (fechaEntrada.equals(hoy)) {
            this.estadoBloque = "CHECKIN_HOY";
        } else if (fechaSalida.equals(hoy)) {
            this.estadoBloque = "CHECKOUT_HOY";
        } else if (fechaEntrada.isBefore(hoy) && fechaSalida.isAfter(hoy)) {
            this.estadoBloque = "EN_CURSO";
        } else if (fechaSalida.isBefore(hoy)) {
            this.estadoBloque = "FINALIZADA";
        } else {
            this.estadoBloque = "FUTURO";
        }
    }

    // Constructor vacío requerido por JPA/Spring
    public ReservaCalendarioDTO() {}
}