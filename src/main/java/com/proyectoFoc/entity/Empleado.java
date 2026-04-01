package com.proyectoFoc.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "empleado")
@Data
public class Empleado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Integer idEmpleado;
    
    @Column(name = "dni", nullable = false, unique = true)
    private String dni;
    
    @Column(name = "nombre", nullable = false)
    private String nombre;
    
    @Column(name = "apellidos", nullable = false)
    private String apellidos;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "telefono", nullable = false)
    private String telefono;
    
    @Column(name = "usuario", nullable = false, unique = true)
    private String usuario;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "cargo", nullable = false)
    private String cargo; // Gerente, Recepcionista, Mantenimiento
    
    @Column(name = "fecha_contratacion", nullable = false)
    private LocalDate fechaContratacion;
    
    @Column(name = "salario")
    private BigDecimal salario;
    
    @Column(name = "activo")
    private Boolean activo;
    
    // NUEVO CAMPO para controlar cambio de contraseña obligatorio
    @Column(name = "debe_cambiar_password")
    private Boolean debeCambiarPassword = true; // Por defecto true para nuevos empleados
}
