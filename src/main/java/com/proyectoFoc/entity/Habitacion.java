package com.proyectoFoc.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "habitacion")
@Data
public class Habitacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_habitacion")
    private Integer idHabitacion;
    
    @Column(name = "numero_habitacion", nullable = false, unique = true)
    private String numeroHabitacion;
    
    @ManyToOne
    @JoinColumn(name = "id_tipo_habitacion", nullable = false)
    private TipoHabitacion tipoHabitacion;
    
    @Column(name = "estado_habitacion")
    private String estadoHabitacion; // Disponible, Ocupada, Limpieza, Mantenimiento, Fuera_servicio
    
    @Column(name = "piso")
    private Integer piso;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "accesible")
    private Boolean accesible;
}
