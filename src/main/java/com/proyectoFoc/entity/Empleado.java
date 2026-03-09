package com.proyectoFoc.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "empleado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Integer idEmpleado;

    @Column(unique = true, nullable = false, length = 20)
    private String dni;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(unique = true, nullable = false, length = 50)
    private String usuario;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Cargo cargo;

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    @Column(precision = 10, scale = 2)
    private BigDecimal salario;

    @Column(nullable = false)
    private Boolean activo = true;

    public enum Cargo {
        Gerente,
        Recepcionista,
        Mantenimiento
    }

    // Método para verificar si el empleado es gerente
    public boolean isGerente() {
        return this.cargo == Cargo.Gerente;
    }
}
