package com.proyectoFoc.service;

import com.proyectoFoc.dto.EmpleadoDTO;
import com.proyectoFoc.entity.Empleado;
import com.proyectoFoc.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpleadoService {
    
    @Autowired
    private EmpleadoRepository empleadoRepository;
    
    /**
     * Obtener todos los empleados como DTOs
     */
    public List<EmpleadoDTO> obtenerTodos() {
        return empleadoRepository.findAll().stream()
                .map(EmpleadoDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Buscar empleados con paginación
     */
    public Page<EmpleadoDTO> buscarEmpleados(String query, Pageable pageable) {
        Page<Empleado> empleados;
        
        if (query == null || query.trim().isEmpty()) {
            empleados = empleadoRepository.findAll(pageable);
        } else {
            empleados = empleadoRepository.buscarEmpleados(query.trim(), pageable);
        }
        
        return empleados.map(EmpleadoDTO::new);
    }
    
    /**
     * Obtener empleado por ID
     */
    public EmpleadoDTO obtenerPorId(Integer id) {
        return empleadoRepository.findById(id)
                .map(EmpleadoDTO::new)
                .orElse(null);
    }
    
    /**
     * Obtener empleado por usuario (para login)
     */
    public Empleado obtenerPorUsuario(String usuario) {
        return empleadoRepository.findByUsuario(usuario).orElse(null);
    }
    
    /**
     * Crear nuevo empleado con validaciones
     */
    @Transactional
    public EmpleadoDTO crearEmpleado(EmpleadoDTO dto, String passwordTemporal) {
        // Validar que no exista DNI duplicado
        if (empleadoRepository.findByDni(dto.getDni()).isPresent()) {
            throw new RuntimeException("Ya existe un empleado con el DNI: " + dto.getDni());
        }
        
        // Validar que no exista email duplicado
        if (empleadoRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe un empleado con el email: " + dto.getEmail());
        }
        
        // Validar que no exista usuario duplicado
        if (empleadoRepository.findByUsuario(dto.getUsuario()).isPresent()) {
            throw new RuntimeException("Ya existe un empleado con el usuario: " + dto.getUsuario());
        }
        
        Empleado empleado = new Empleado();
        empleado.setDni(dto.getDni());
        empleado.setNombre(dto.getNombre());
        empleado.setApellidos(dto.getApellidos());
        empleado.setEmail(dto.getEmail());
        empleado.setTelefono(dto.getTelefono());
        empleado.setUsuario(dto.getUsuario());
        empleado.setPassword(passwordTemporal); // Password sin encriptar por ahora
        empleado.setCargo(dto.getCargo());
        empleado.setFechaContratacion(LocalDate.now());
        empleado.setSalario(dto.getSalario());
        empleado.setActivo(true);
        empleado.setDebeCambiarPassword(true); // Debe cambiar password en primer login
        
        Empleado guardado = empleadoRepository.save(empleado);
        return new EmpleadoDTO(guardado);
    }
    
    /**
     * Actualizar empleado existente
     */
    @Transactional
    public EmpleadoDTO actualizarEmpleado(Integer id, EmpleadoDTO dto) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + id));
        
        // Validar DNI duplicado (excepto el propio)
        empleadoRepository.findByDni(dto.getDni()).ifPresent(e -> {
            if (!e.getIdEmpleado().equals(id)) {
                throw new RuntimeException("Ya existe otro empleado con el DNI: " + dto.getDni());
            }
        });
        
        // Validar email duplicado (excepto el propio)
        empleadoRepository.findByEmail(dto.getEmail()).ifPresent(e -> {
            if (!e.getIdEmpleado().equals(id)) {
                throw new RuntimeException("Ya existe otro empleado con el email: " + dto.getEmail());
            }
        });
        
        // Validar usuario duplicado (excepto el propio)
        empleadoRepository.findByUsuario(dto.getUsuario()).ifPresent(e -> {
            if (!e.getIdEmpleado().equals(id)) {
                throw new RuntimeException("Ya existe otro empleado con el usuario: " + dto.getUsuario());
            }
        });
        
        // Actualizar solo los campos editables (NO password)
        empleado.setDni(dto.getDni());
        empleado.setNombre(dto.getNombre());
        empleado.setApellidos(dto.getApellidos());
        empleado.setEmail(dto.getEmail());
        empleado.setTelefono(dto.getTelefono());
        empleado.setUsuario(dto.getUsuario());
        empleado.setCargo(dto.getCargo());
        empleado.setSalario(dto.getSalario());
        // NO actualizar password aquí
        
        Empleado actualizado = empleadoRepository.save(empleado);
        return new EmpleadoDTO(actualizado);
    }
    
    /**
     * Cambiar contraseña del empleado
     */
    @Transactional
    public void cambiarPassword(Integer idEmpleado, String nuevaPassword) {
        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        
        empleado.setPassword(nuevaPassword);
        empleado.setDebeCambiarPassword(false); // Ya no necesita cambiar password
        empleadoRepository.save(empleado);
    }
    
    /**
     * Activar/Desactivar empleado (soft delete)
     */
    @Transactional
    public void cambiarEstado(Integer id, boolean activo) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + id));
        
        empleado.setActivo(activo);
        empleadoRepository.save(empleado);
    }
    
    /**
     * Eliminar empleado (hard delete - usar con cuidado)
     */
    @Transactional
    public void eliminarEmpleado(Integer id) {
        if (!empleadoRepository.existsById(id)) {
            throw new RuntimeException("Empleado no encontrado con ID: " + id);
        }
        empleadoRepository.deleteById(id);
    }
    
    /**
     * Estadísticas
     */
    public long contarTotal() {
        return empleadoRepository.count();
    }
    
    public long contarActivos() {
        return empleadoRepository.countByActivoTrue();
    }
    
    public long contarPorCargo(String cargo) {
        return empleadoRepository.countByCargo(cargo);
    }
    
    public long contarActivosPorCargo(String cargo) {
        return empleadoRepository.countByCargoAndActivoTrue(cargo);
    }
}
