package com.proyectoFoc.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReservaRequestDTO {
    
    private Integer idHabitacion;
    private Integer idCliente;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private BigDecimal precioNoche;
    private String observaciones;
}
