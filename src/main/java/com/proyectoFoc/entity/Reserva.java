package com.proyectoFoc.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reserva")
@Data
public class Reserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Integer idReserva;
    
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;
    
    @ManyToOne
    @JoinColumn(name = "id_empleado")
    private Empleado empleado;
    
    @Column(name = "estado_reserva")
    private String estadoReserva; // Pendiente, Confirmada, Cancelada, Completada, No_presentado
    
    @Column(name = "fecha_reserva", nullable = false)
    private LocalDate fechaReserva;
    
    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada;
    
    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;
    
    @Column(name = "num_adultos")
    private Integer numAdultos;
    
    @Column(name = "num_ninos")
    private Integer numNinos;
    
    @Column(name = "importe_total", nullable = false)
    private BigDecimal importeTotal;
    
    @Column(name = "observaciones")
    private String observaciones;
}
