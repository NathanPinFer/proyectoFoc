package com.proyectoFoc.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

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

    @Column(length = 200)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(length = 50)
    private String pais = "España";

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro = LocalDate.now();

    @Column(nullable = false)
    private Boolean vip = false;

    // Método auxiliar para obtener el nombre completo
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    // Método para obtener las iniciales para el avatar
    public String getIniciales() {
        String inicial1 = nombre != null && !nombre.isEmpty() ? nombre.substring(0, 1) : "";
        String inicial2 = apellidos != null && !apellidos.isEmpty() ? apellidos.substring(0, 1) : "";
        return (inicial1 + inicial2).toUpperCase();
    }
}
