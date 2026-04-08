package com.proyectoFoc.service;

import com.proyectoFoc.dto.ReservaCalendarioDTO;
import com.proyectoFoc.dto.ReservaListadoDTO;
import com.proyectoFoc.dto.ReservaRequestDTO;
import com.proyectoFoc.entity.*;
import com.proyectoFoc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Reserva de habitaciones
 */
@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private DetalleReservaRepository detalleReservaRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // CALENDARIO
    
    /**
     * Obtener reservas del calendario para un mes específico
     */
    public List<ReservaCalendarioDTO> obtenerCalendario(int mes, int anio) {
        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDate inicioMes = yearMonth.atDay(1);
        LocalDate finMes = yearMonth.atEndOfMonth();
        
        return detalleReservaRepository.obtenerCalendario(inicioMes, finMes);
    }

    // LISTADO CON FILTROS Y PAGINACIÓN
    
    /**
     * Buscar reservas con filtros y paginación
     * @param filtroCliente Nombre del cliente (parcial)
     * @param fechaEntrada Fecha de entrada específica
     * @param estado Estado de la reserva (Confirmada, Pendiente, etc.)
     * @param pagina Número de página (0-indexed)
     * @param elementosPorPagina Cantidad de elementos por página
     * @return Página de reservas con DTOs
     */
    public Page<ReservaListadoDTO> buscarReservasPaginadas(
            String filtroCliente,
            LocalDate fechaEntrada,
            String estado,
            int pagina,
            int elementosPorPagina) {

        Pageable pageable = PageRequest.of(pagina, elementosPorPagina);

        // Convertir "Todos" o vacío a null para la query
        String estadoParam = (estado == null || estado.isBlank() || "Todos".equals(estado)) ? null : estado;
        String clienteParam = (filtroCliente == null || filtroCliente.isBlank()) ? null : filtroCliente;

        Page<Reserva> paginaReservas = reservaRepository.buscarConFiltros(
                clienteParam, fechaEntrada, estadoParam, pageable);

        return paginaReservas.map(this::mapToListadoDTO);
    }

    /**
     * Convertir Reserva a DTO de listado
     */
    private ReservaListadoDTO mapToListadoDTO(Reserva r) {
        ReservaListadoDTO dto = new ReservaListadoDTO();
        dto.setIdReserva(r.getIdReserva());

        // Cliente
        if (r.getCliente() != null) {
            dto.setIdCliente(r.getCliente().getIdCliente());
            dto.setNombreCliente(r.getCliente().getNombre());
            dto.setApellidosCliente(r.getCliente().getApellidos());
            dto.setClienteVip(r.getCliente().getVip());
        }

        // Habitación
        detalleReservaRepository.findFirstByReservaIdReserva(r.getIdReserva())
                .ifPresent(det -> {
                    if (det.getHabitacion() != null) {
                        dto.setIdHabitacion(det.getHabitacion().getIdHabitacion());
                        dto.setNumeroHabitacion(det.getHabitacion().getNumeroHabitacion());
                        if (det.getHabitacion().getTipoHabitacion() != null) {
                            dto.setTipoHabitacion(det.getHabitacion().getTipoHabitacion().getNombre());
                        }
                    }
                });

        // Fechas, huéspedes, importe, estado
        dto.setFechaEntrada(r.getFechaEntrada());
        dto.setFechaSalida(r.getFechaSalida());
        dto.setNumAdultos(r.getNumAdultos() != null ? r.getNumAdultos() : 1);
        dto.setNumNinos(r.getNumNinos() != null ? r.getNumNinos() : 0);
        dto.setImporteTotal(r.getImporteTotal());
        dto.setEstadoReserva(r.getEstadoReserva());

        return dto;
    }

    // CREAR RESERVA
    
    /**
     * Crear nueva reserva con validaciones completas
     */
    @Transactional
    public Reserva crearReserva(ReservaRequestDTO dto) {
        // Validar que la habitación existe
        Habitacion habitacion = habitacionRepository.findById(dto.getIdHabitacion())
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

        // Validar que el cliente existe
        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // VALIDACIÓN Fechas coherentes
        if (dto.getFechaEntrada().isAfter(dto.getFechaSalida())) {
            throw new RuntimeException("La fecha de entrada no puede ser posterior a la de salida");
        }

        // VALIDACIÓN Fecha entrada no anterior a hoy
        if (dto.getFechaEntrada().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de entrada no puede ser anterior a hoy");
        }

        // VALIDACIÓN Verificar disponibilidad
        Long reservasExistentes = detalleReservaRepository.verificarDisponibilidad(
                dto.getIdHabitacion(),
                dto.getFechaEntrada(),
                dto.getFechaSalida());

        if (reservasExistentes > 0) {
            throw new RuntimeException("La habitación no está disponible en esas fechas");
        }

        // Calcular subtotal
        long noches = ChronoUnit.DAYS.between(dto.getFechaEntrada(), dto.getFechaSalida());
        BigDecimal subtotal = dto.getPrecioNoche().multiply(BigDecimal.valueOf(noches));
        
        // Crear la reserva
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setEstadoReserva("Confirmada");
        reserva.setFechaReserva(LocalDate.now());
        reserva.setFechaEntrada(dto.getFechaEntrada());
        reserva.setFechaSalida(dto.getFechaSalida());
        reserva.setNumAdultos(dto.getNumAdultos() != null ? dto.getNumAdultos() : 1);
        reserva.setNumNinos(dto.getNumNinos() != null ? dto.getNumNinos() : 0);
        reserva.setImporteTotal(subtotal);
        reserva.setObservaciones(dto.getObservaciones());
        
        Reserva reservaGuardada = reservaRepository.save(reserva);
        
        // Crear el detalle de reserva
        DetalleReserva detalle = new DetalleReserva();
        detalle.setReserva(reservaGuardada);
        detalle.setHabitacion(habitacion);
        detalle.setFechaEntrada(dto.getFechaEntrada());
        detalle.setFechaSalida(dto.getFechaSalida());
        detalle.setPrecioNoche(dto.getPrecioNoche());
        detalle.setSubtotal(subtotal);
        
        detalleReservaRepository.save(detalle);

        return reservaGuardada;
    }

    // ACTUALIZAR RESERVA
    
    /**
     * Actualizar una reserva existente
     */
    @Transactional
    public void actualizarReserva(Integer id, ReservaRequestDTO dto) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Validar fechas
        if (dto.getFechaEntrada().isAfter(dto.getFechaSalida())) {
            throw new RuntimeException("La fecha de entrada no puede ser posterior a la de salida");
        }

        // Calcular subtotal
        long noches = ChronoUnit.DAYS.between(dto.getFechaEntrada(), dto.getFechaSalida());
        BigDecimal subtotal = dto.getPrecioNoche().multiply(BigDecimal.valueOf(noches));

        // Actualizar reserva
        reserva.setCliente(cliente);
        reserva.setFechaEntrada(dto.getFechaEntrada());
        reserva.setFechaSalida(dto.getFechaSalida());
        reserva.setNumAdultos(dto.getNumAdultos() != null ? dto.getNumAdultos() : 1);
        reserva.setNumNinos(dto.getNumNinos() != null ? dto.getNumNinos() : 0);
        reserva.setImporteTotal(subtotal);
        reserva.setObservaciones(dto.getObservaciones());
        reservaRepository.save(reserva);

        // Actualizar detalle de reserva
        detalleReservaRepository.findFirstByReservaIdReserva(id).ifPresent(det -> {
            Habitacion hab = habitacionRepository.findById(dto.getIdHabitacion())
                    .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));
            det.setHabitacion(hab);
            det.setFechaEntrada(dto.getFechaEntrada());
            det.setFechaSalida(dto.getFechaSalida());
            det.setPrecioNoche(dto.getPrecioNoche());
            det.setSubtotal(subtotal);
            detalleReservaRepository.save(det);
        });
    }

    // OBTENER RESERVA POR ID
    
    /**
     * Obtener reserva por ID
     */
    public Optional<Reserva> obtenerPorId(Integer id) {
        return reservaRepository.findById(id);
    }

    // CANCELAR RESERVA
    
    /**
     * Cancelar reserva
     */
    @Transactional
    public void cancelarReserva(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        
        reserva.setEstadoReserva("Cancelada");
        reservaRepository.save(reserva);
    }

    // ACTUALIZAR ESTADO
    
    /**
     * Actualizar estado de reserva
     * (Método original - se mantiene para compatibilidad con código existente)
     */
    @Transactional
    public void actualizarEstado(Integer idReserva, String estado) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        reserva.setEstadoReserva(estado);
        reservaRepository.save(reserva);
    }

    // ESTADÍSTICAS
    
    /**
     * Contar reservas activas (no canceladas ni completadas)
     */
    public Long contarReservasActivas() {
        return reservaRepository.countReservasActivas();
    }

    /**
     * Contar reservas confirmadas
     */
    public Long contarConfirmadas() {
        return reservaRepository.countByEstadoReserva("Confirmada");
    }

    /**
     * Contar reservas pendientes
     */
    public Long contarPendientes() {
        return reservaRepository.countByEstadoReserva("Pendiente");
    }

    /**
     * Contar check-ins de hoy
     */
    public Long contarCheckinHoy() {
        return reservaRepository.countCheckinHoy(LocalDate.now());
    }

    /**
     * Calcular ingresos del mes actual
     */
    public BigDecimal ingresosDelMes() {
        LocalDate hoy = LocalDate.now();
        return reservaRepository.ingresosDelMes(hoy.getMonthValue(), hoy.getYear());
    }

    /**
     * Contar número de reservas de un cliente
     * (Funcionalidad original - mantener)
     */
    public Long contarReservasCliente(Integer idCliente) {
        return reservaRepository.countByClienteIdCliente(idCliente);
    }
}
