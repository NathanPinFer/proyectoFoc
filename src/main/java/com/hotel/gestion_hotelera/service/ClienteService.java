package com.hotel.gestion_hotelera.service;

import com.hotel.gestion_hotelera.model.Cliente;
import com.hotel.gestion_hotelera.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    public List<Cliente> buscarPorNombre(String texto) {
        return clienteRepository.findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase(texto, texto);
    }

    public List<Cliente> obtenerVip() {
        return clienteRepository.findByVip(true);
    }

    public Cliente guardar(Cliente cliente) {
        if (cliente.getFechaRegistro() == null) {
            cliente.setFechaRegistro(LocalDate.now());
        }
        if (cliente.getPais() == null || cliente.getPais().isEmpty()) {
            cliente.setPais("España");
        }
        return clienteRepository.save(cliente);
    }

    public void eliminar(Integer id) {
        clienteRepository.deleteById(id);
    }

    public Cliente obtenerPorId(Integer id) {
        return clienteRepository.findById(id).orElse(null);
    }
}