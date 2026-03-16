package com.proyectoFoc.dto;

import com.proyectoFoc.entity.Cliente;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private Integer idCliente;
    private String dni;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String codigoPostal;
    private String pais;
    private LocalDate fechaRegistro;
    private Boolean vip;
    
    // Número de reservas del cliente (calculado)
    private Integer numeroReservas = 0;

    // Constructor para convertir de Cliente a ClienteDTO
    public ClienteDTO(Cliente cliente) {
        this.idCliente = cliente.getIdCliente();
        this.dni = cliente.getDni();
        this.nombre = cliente.getNombre();
        this.apellidos = cliente.getApellidos();
        this.email = cliente.getEmail();
        this.telefono = cliente.getTelefono();
        this.direccion = cliente.getDireccion();
        this.ciudad = cliente.getCiudad();
        this.codigoPostal = cliente.getCodigoPostal();
        this.pais = cliente.getPais();
        this.fechaRegistro = cliente.getFechaRegistro();
        this.vip = cliente.getVip();
        this.numeroReservas = 0; // Se actualizará en el servicio
    }

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
    
    // Método para obtener el estado (VIP o Regular)
    public String getEstadoTexto() {
        return vip ? "VIP" : "Regular";
    }
}
