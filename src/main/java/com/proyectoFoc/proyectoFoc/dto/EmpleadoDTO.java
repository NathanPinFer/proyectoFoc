package com.proyectoFoc.proyectoFoc.dto;

import com.proyectoFoc.proyectoFoc.entity.Empleado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoDTO {
    private Integer idEmpleado;
    private String nombre;
    private String apellidos;
    private String usuario;
    private String cargo;
    private Boolean activo;

    // Constructor para convertir de Empleado a EmpleadoDTO
    public EmpleadoDTO(Empleado empleado) {
        this.idEmpleado = empleado.getIdEmpleado();
        this.nombre = empleado.getNombre();
        this.apellidos = empleado.getApellidos();
        this.usuario = empleado.getUsuario();
        this.cargo = empleado.getCargo().name();
        this.activo = empleado.getActivo();
    }

    public boolean isGerente() {
        return "Gerente".equals(this.cargo);
    }
}