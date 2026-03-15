package com.hotel.gestion_hotelera.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReservaRequestDTO {

    // Cliente — puede ser existente (idCliente != null) o nuevo
    private Integer idCliente;
    private String nombre;
    private String apellidos;
    private String dni;
    private String email;
    private String telefono;
    private Boolean vip = false;

    // Reserva
    private Integer idHabitacion;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private Integer numAdultos = 1;
    private Integer numNinos = 0;
    private BigDecimal precioNoche;
    private String observaciones;

    // Getters y setters
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Boolean getVip() { return vip; }
    public void setVip(Boolean vip) { this.vip = vip; }
    public Integer getIdHabitacion() { return idHabitacion; }
    public void setIdHabitacion(Integer idHabitacion) { this.idHabitacion = idHabitacion; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(LocalDate fechaEntrada) { this.fechaEntrada = fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }
    public Integer getNumAdultos() { return numAdultos; }
    public void setNumAdultos(Integer numAdultos) { this.numAdultos = numAdultos; }
    public Integer getNumNinos() { return numNinos; }
    public void setNumNinos(Integer numNinos) { this.numNinos = numNinos; }
    public BigDecimal getPrecioNoche() { return precioNoche; }
    public void setPrecioNoche(BigDecimal precioNoche) { this.precioNoche = precioNoche; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}