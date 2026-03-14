package com.hotel.gestion_hotelera.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "tipo_habitacion")
@Data
public class TipoHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Integer idTipo;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "capacidad_personas")
    private Integer capacidadPersonas;

    @Column(name = "num_camas")
    private Integer numCamas;

    @Column(name = "precio_base", nullable = false)
    private BigDecimal precioBase;
}