package com.hotel.gestion_hotelera.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservaCalendarioDTO {

    private Integer idDetalle;
    private Integer idReserva;

    // Habitación
    private String numeroHabitacion;
    private Integer idHabitacion;
    private String tipoHabitacion;
    private Integer piso;

    // Huésped
    private String nombreCliente;
    private String apellidosCliente;
    private Boolean clienteVip;

    // Fechas
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;

    // Económico
    private BigDecimal precioNoche;
    private BigDecimal subtotal;

    // Estado calculado
    private String estadoBloque;

    // Constructor vacío — necesario para Jackson
    public ReservaCalendarioDTO() {}

    // Constructor completo
    public ReservaCalendarioDTO(Integer idDetalle, Integer idReserva,
                                 String numeroHabitacion, Integer idHabitacion,
                                 String tipoHabitacion, Integer piso,
                                 String nombreCliente, String apellidosCliente,
                                 Boolean clienteVip,
                                 LocalDate fechaEntrada, LocalDate fechaSalida,
                                 BigDecimal precioNoche, BigDecimal subtotal) {
        this.idDetalle = idDetalle;
        this.idReserva = idReserva;
        this.numeroHabitacion = numeroHabitacion;
        this.idHabitacion = idHabitacion;
        this.tipoHabitacion = tipoHabitacion;
        this.piso = piso;
        this.nombreCliente = nombreCliente;
        this.apellidosCliente = apellidosCliente;
        this.clienteVip = clienteVip;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.precioNoche = precioNoche;
        this.subtotal = subtotal;

        LocalDate hoy = LocalDate.now();
        if (fechaEntrada.equals(hoy)) {
            this.estadoBloque = "CHECKIN_HOY";
        } else if (fechaSalida.equals(hoy)) {
            this.estadoBloque = "CHECKOUT_HOY";
        } else if (fechaEntrada.isBefore(hoy) && fechaSalida.isAfter(hoy)) {
            this.estadoBloque = "EN_CURSO";
        } else {
            this.estadoBloque = "FUTURO";
        }
    }

    // Getters
    public Integer getIdDetalle() { return idDetalle; }
    public Integer getIdReserva() { return idReserva; }
    public String getNumeroHabitacion() { return numeroHabitacion; }
    public Integer getIdHabitacion() { return idHabitacion; }
    public String getTipoHabitacion() { return tipoHabitacion; }
    public Integer getPiso() { return piso; }
    public String getNombreCliente() { return nombreCliente; }
    public String getApellidosCliente() { return apellidosCliente; }
    public Boolean getClienteVip() { return clienteVip; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public BigDecimal getPrecioNoche() { return precioNoche; }
    public BigDecimal getSubtotal() { return subtotal; }
    public String getEstadoBloque() { return estadoBloque; }
}