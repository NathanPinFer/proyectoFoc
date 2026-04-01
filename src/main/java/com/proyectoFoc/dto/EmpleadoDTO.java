package com.proyectoFoc.dto;

import com.proyectoFoc.entity.Empleado;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class EmpleadoDTO {
    
    private Integer idEmpleado;
    private String dni;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private String usuario;
    // NO incluir password por seguridad
    private String cargo;
    private LocalDate fechaContratacion;
    private BigDecimal salario;
    private Boolean activo;
    private Boolean debeCambiarPassword;
    
    // Constructor desde entity
    public EmpleadoDTO(Empleado empleado) {
        this.idEmpleado = empleado.getIdEmpleado();
        this.dni = empleado.getDni();
        this.nombre = empleado.getNombre();
        this.apellidos = empleado.getApellidos();
        this.email = empleado.getEmail();
        this.telefono = empleado.getTelefono();
        this.usuario = empleado.getUsuario();
        this.cargo = empleado.getCargo();
        this.fechaContratacion = empleado.getFechaContratacion();
        this.salario = empleado.getSalario();
        this.activo = empleado.getActivo();
        this.debeCambiarPassword = empleado.getDebeCambiarPassword();
    }
    
    // Método helper para obtener nombre completo
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }
    
    // Método helper para obtener estado en texto
    public String getEstadoTexto() {
        return Boolean.TRUE.equals(activo) ? "Activo" : "Inactivo";
    }
}
