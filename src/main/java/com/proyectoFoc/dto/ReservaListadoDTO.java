package com.proyectoFoc.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReservaListadoDTO {

    private Integer idReserva;

    // Cliente
    private Integer idCliente;
    private String nombreCliente;
    private String apellidosCliente;
    private Boolean clienteVip;

    // Habitación
    private Integer idHabitacion;
    private String numeroHabitacion;
    private String tipoHabitacion;

    // Fechas
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;

    // Huéspedes
    private Integer numAdultos;
    private Integer numNinos;

    // Económico
    private BigDecimal importeTotal;

    // Estado
    private String estadoReserva;

    // Métodos auxiliares

    public String getNombreClienteCompleto() {
        String n = nombreCliente != null ? nombreCliente : "";
        String a = apellidosCliente != null ? apellidosCliente : "";
        return (n + " " + a).trim();
    }

    public String getHabitacionDescripcion() {
        String num  = numeroHabitacion != null ? numeroHabitacion : "?";
        String tipo = tipoHabitacion   != null ? tipoHabitacion   : "";
        return tipo.isEmpty() ? num : num + " - " + tipo;
    }

    public String getHuespedesTexto() {
        int adultos = numAdultos != null ? numAdultos : 1;
        int ninos   = numNinos   != null ? numNinos   : 0;
        StringBuilder sb = new StringBuilder();
        sb.append(adultos).append(adultos == 1 ? " Adulto" : " Adultos");
        if (ninos > 0) {
            sb.append(", ").append(ninos).append(ninos == 1 ? " Niño" : " Niños");
        }
        return sb.toString();
    }

    public String getFechaEntradaFormateada() {
        if (fechaEntrada == null) return "-";
        return String.format("%02d/%02d/%d - 14:00",
                fechaEntrada.getDayOfMonth(),
                fechaEntrada.getMonthValue(),
                fechaEntrada.getYear());
    }

    public String getFechaSalidaFormateada() {
        if (fechaSalida == null) return "-";
        return String.format("%02d/%02d/%d - 12:00",
                fechaSalida.getDayOfMonth(),
                fechaSalida.getMonthValue(),
                fechaSalida.getYear());
    }

    public String getImporteTotalFormateado() {
        return importeTotal != null
                ? String.format("%.2f €", importeTotal.doubleValue())
                : "0.00 €";
    }
}
