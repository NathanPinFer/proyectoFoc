package com.proyectoFoc.service;

import com.proyectoFoc.dto.ClienteDTO;
import com.proyectoFoc.entity.Cliente;
import com.proyectoFoc.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Obtener todos los clientes
     */
    public List<ClienteDTO> obtenerTodosLosClientes() {
        return clienteRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener clientes con paginación
     */
    public Page<ClienteDTO> obtenerClientesPaginados(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
        return clienteRepository.findAll(pageable)
                .map(this::convertirADTO);
    }

    /**
     * Buscar clientes con filtro y paginación
     */
    public Page<ClienteDTO> buscarClientesPaginados(String filtro, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
        if (filtro == null || filtro.trim().isEmpty()) {
            return clienteRepository.findAll(pageable).map(this::convertirADTO);
        }
        return clienteRepository.buscarConFiltro(filtro, pageable).map(this::convertirADTO);
    }

    /**
     * Obtener cliente por ID
     */
    public Optional<ClienteDTO> obtenerClientePorId(Integer id) {
        return clienteRepository.findById(id)
                .map(this::convertirADTO);
    }

    /**
     * Obtener cliente por DNI
     */
    public Optional<ClienteDTO> obtenerClientePorDni(String dni) {
        return clienteRepository.findByDni(dni)
                .map(this::convertirADTO);
    }

    /**
     * Buscar clientes por nombre o apellidos
     */
    public List<ClienteDTO> buscarClientes(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return obtenerTodosLosClientes();
        }
        return clienteRepository.findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase(texto, texto)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener clientes VIP
     */
    public List<ClienteDTO> obtenerClientesVIP() {
        return clienteRepository.findByVip(true)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * ESTADÍSTICAS
     */
    
    // Contar total de clientes
    public long contarClientes() {
        return clienteRepository.count();
    }

    // Contar clientes VIP
    public long contarClientesVIP() {
        return clienteRepository.countByVip(true);
    }

    // Contar clientes regulares
    public long contarClientesRegulares() {
        return clienteRepository.countByVip(false);
    }

    // Contar clientes nuevos hoy
    public long contarClientesNuevosHoy() {
        return clienteRepository.countByFechaRegistro(LocalDate.now());
    }

    /**
     * Crear nuevo cliente
     */
    @Transactional
    public ClienteDTO crearCliente(ClienteDTO clienteDTO) {
        // Validaciones
        if (clienteRepository.existsByDni(clienteDTO.getDni())) {
            throw new RuntimeException("Ya existe un cliente con este DNI");
        }
        if (clienteRepository.existsByEmail(clienteDTO.getEmail())) {
            throw new RuntimeException("Ya existe un cliente con este email");
        }

        // Convertir DTO a Entity
        Cliente cliente = new Cliente();
        cliente.setDni(clienteDTO.getDni().trim().toUpperCase());
        cliente.setNombre(clienteDTO.getNombre().trim());
        cliente.setApellidos(clienteDTO.getApellidos().trim());
        cliente.setEmail(clienteDTO.getEmail().trim().toLowerCase());
        cliente.setTelefono(clienteDTO.getTelefono().trim());
        cliente.setDireccion(clienteDTO.getDireccion() != null ? clienteDTO.getDireccion().trim() : null);
        cliente.setCiudad(clienteDTO.getCiudad() != null ? clienteDTO.getCiudad().trim() : null);
        cliente.setCodigoPostal(clienteDTO.getCodigoPostal() != null ? clienteDTO.getCodigoPostal().trim() : null);
        cliente.setPais(clienteDTO.getPais() != null ? clienteDTO.getPais().trim() : "España");
        cliente.setFechaRegistro(LocalDate.now());
        cliente.setVip(clienteDTO.getVip() != null ? clienteDTO.getVip() : false);

        // Guardar
        Cliente clienteGuardado = clienteRepository.save(cliente);
        return convertirADTO(clienteGuardado);
    }

    /**
     * Actualizar cliente
     */
    @Transactional
    public ClienteDTO actualizarCliente(Integer id, ClienteDTO clienteDTO) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Validar DNI (si cambió)
        if (!cliente.getDni().equals(clienteDTO.getDni())) {
            if (clienteRepository.existsByDni(clienteDTO.getDni())) {
                throw new RuntimeException("Ya existe un cliente con este DNI");
            }
        }

        // Validar email (si cambió)
        if (!cliente.getEmail().equals(clienteDTO.getEmail().toLowerCase())) {
            if (clienteRepository.existsByEmail(clienteDTO.getEmail())) {
                throw new RuntimeException("Ya existe un cliente con este email");
            }
        }

        // Actualizar campos
        cliente.setDni(clienteDTO.getDni().trim().toUpperCase());
        cliente.setNombre(clienteDTO.getNombre().trim());
        cliente.setApellidos(clienteDTO.getApellidos().trim());
        cliente.setEmail(clienteDTO.getEmail().trim().toLowerCase());
        cliente.setTelefono(clienteDTO.getTelefono().trim());
        cliente.setDireccion(clienteDTO.getDireccion() != null ? clienteDTO.getDireccion().trim() : null);
        cliente.setCiudad(clienteDTO.getCiudad() != null ? clienteDTO.getCiudad().trim() : null);
        cliente.setCodigoPostal(clienteDTO.getCodigoPostal() != null ? clienteDTO.getCodigoPostal().trim() : null);
        cliente.setPais(clienteDTO.getPais() != null ? clienteDTO.getPais().trim() : "España");
        cliente.setVip(clienteDTO.getVip());

        // Guardar
        Cliente clienteActualizado = clienteRepository.save(cliente);
        return convertirADTO(clienteActualizado);
    }

    /**
     * Eliminar cliente
     */
    @Transactional
    public void eliminarCliente(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado");
        }
        try {
            clienteRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("No se puede eliminar el cliente. Puede tener reservas asociadas.");
        }
    }

    /**
     * Alternar estado VIP
     */
    @Transactional
    public ClienteDTO alternarVIP(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        cliente.setVip(!cliente.getVip());
        Cliente clienteActualizado = clienteRepository.save(cliente);
        return convertirADTO(clienteActualizado);
    }

    /**
     * Convertir Cliente a ClienteDTO
     * Aquí se calcularía el número de reservas cuando esté implementado
     */
    private ClienteDTO convertirADTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO(cliente);
        
        // TODO: Cuando se implemente el módulo de reservas, calcular:
        // dto.setNumeroReservas(reservaRepository.countByCliente(cliente));
        
        // Por ahora, valor simulado para testing
        dto.setNumeroReservas(0);
        
        return dto;
    }
}
