package com.proyectoFoc.controller;

import com.proyectoFoc.FxmlView;
import com.proyectoFoc.StageManager;
import com.proyectoFoc.dto.ClienteDTO;
import com.proyectoFoc.dto.ReservaListadoDTO;
import com.proyectoFoc.dto.ReservaRequestDTO;
import com.proyectoFoc.entity.Habitacion;
import com.proyectoFoc.service.AuthService;
import com.proyectoFoc.service.ClienteService;
import com.proyectoFoc.service.HabitacionService;
import com.proyectoFoc.service.ReservaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Component
public class ReservaController {

    // label para las estadísticas
    @FXML private Label lblTotalActivas;
    @FXML private Label lblConfirmadas;
    @FXML private Label lblPendientes;
    @FXML private Label lblCheckinHoy;

    // Filtros
    @FXML private TextField      txtBuscarCliente;
    @FXML private DatePicker     dpFechaEntrada;
    @FXML private ComboBox<String> cmbEstado;

    // Listado
    @FXML private VBox  listaReservasContainer;
    @FXML private HBox  paginacionContainer;

    // Modal Formulario
    @FXML private StackPane       modalFormContainer;
    @FXML private Label           lblModalTitulo;
    @FXML private DatePicker      dpFormEntrada;
    @FXML private DatePicker      dpFormSalida;
    @FXML private ComboBox<String> cmbFormHabitacion;
    @FXML private TextField       txtFormBuscarCliente;
    @FXML private ListView<ClienteDTO> listFormClientes;
    @FXML private Label           lblFormClienteSeleccionado;
    @FXML private Spinner<Integer> spnAdultos;
    @FXML private Spinner<Integer> spnNinos;
    @FXML private Label           lblFormPrecioNoche;
    @FXML private Label           lblFormTotal;
    @FXML private TextArea        txtFormObservaciones;
    @FXML private Label           lblFormError;

    // Modal Detalles
    @FXML private StackPane modalDetallesContainer;
    @FXML private Label     lblDetId;
    @FXML private Label     lblDetEstado;
    @FXML private Label     lblDetCliente;
    @FXML private Label     lblDetVip;
    @FXML private Label     lblDetHabitacion;
    @FXML private Label     lblDetFechaEntrada;
    @FXML private Label     lblDetFechaSalida;
    @FXML private Label     lblDetHuespedes;
    @FXML private Label     lblDetImporte;

    // Services
    @Autowired private ReservaService   reservaService;
    @Autowired private ClienteService   clienteService;
    @Autowired private HabitacionService habitacionService;
    @Autowired private AuthService       authService;
    @Autowired private StageManager      stageManager;

    // Estado interno
    private int paginaActual       = 0;
    private int elementosPorPagina = 5;
    private int totalPaginas       = 0;

    private Integer      reservaEnEdicionId      = null;
    private ClienteDTO   clienteSeleccionadoForm = null;
    private List<Habitacion> habitacionesDisponibles;

    // INICIALIZACIÓN
    @FXML
    public void initialize() {
        configurarComboEstado();
        configurarFormulario();
        actualizarEstadisticas();
        cargarReservas();
    }

    private void configurarComboEstado() {
        if (cmbEstado != null) {
            cmbEstado.setItems(FXCollections.observableArrayList(
                    "Todos", "Confirmada", "Pendiente", "Completada", "Cancelada", "No_presentado"));
            cmbEstado.setValue("Todos");
        }
    }

    private void configurarFormulario() {
        if (spnAdultos != null)
            spnAdultos.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        if (spnNinos != null)
            spnNinos.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));

        // Búsqueda de cliente en el formulario
        if (txtFormBuscarCliente != null) {
            txtFormBuscarCliente.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal.trim().isEmpty()) {
                    ocultarListaClientes();
                    return;
                }
                List<ClienteDTO> clientes = clienteService.buscarClientes(newVal.trim());
                if (listFormClientes != null) {
                    listFormClientes.setItems(FXCollections.observableArrayList(clientes));
                    listFormClientes.setVisible(!clientes.isEmpty());
                    listFormClientes.setManaged(!clientes.isEmpty());
                }
            });
        }

        // Selección en la lista de clientes
        if (listFormClientes != null) {
            listFormClientes.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(ClienteDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNombre() + " " + item.getApellidos()
                                + " · " + item.getDni()
                                + (Boolean.TRUE.equals(item.getVip()) ? " ⭐" : ""));
                    }
                }
            });
            listFormClientes.setOnMouseClicked(e -> {
                ClienteDTO sel = listFormClientes.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    clienteSeleccionadoForm = sel;
                    if (lblFormClienteSeleccionado != null) {
                        String texto = "✔ " + sel.getNombre() + " " + sel.getApellidos()
                                + " (" + sel.getDni() + ")"
                                + (Boolean.TRUE.equals(sel.getVip()) ? " ⭐" : "");
                        lblFormClienteSeleccionado.setText(texto);
                        lblFormClienteSeleccionado.setStyle(
                                "-fx-text-fill: #27AE60; -fx-font-weight: bold; -fx-font-size: 13px;");
                    }
                    ocultarListaClientes();
                    if (txtFormBuscarCliente != null) txtFormBuscarCliente.clear();
                }
            });
        }

        // Recalcular total al cambiar habitación o fechas
        if (cmbFormHabitacion != null) cmbFormHabitacion.setOnAction(e -> recalcularTotal());
        if (dpFormEntrada     != null) dpFormEntrada.setOnAction(e -> recalcularTotal());
        if (dpFormSalida      != null) dpFormSalida.setOnAction(e -> recalcularTotal());
    }

    private void ocultarListaClientes() {
        if (listFormClientes != null) {
            listFormClientes.setVisible(false);
            listFormClientes.setManaged(false);
        }
    }

    // ESTADÍSTICAS
    private void actualizarEstadisticas() {
        if (lblTotalActivas != null) lblTotalActivas.setText(String.valueOf(reservaService.contarReservasActivas()));
        if (lblConfirmadas  != null) lblConfirmadas.setText(String.valueOf(reservaService.contarConfirmadas()));
        if (lblPendientes   != null) lblPendientes.setText(String.valueOf(reservaService.contarPendientes()));
        if (lblCheckinHoy   != null) lblCheckinHoy.setText(String.valueOf(reservaService.contarCheckinHoy()));
    }

    // CARGA DEL LISTADO RESERVAS
    private void cargarReservas() {
        cargarReservasPaginadas(paginaActual);
    }

    private void cargarReservasPaginadas(int pagina) {
        String filtroCliente = txtBuscarCliente != null ? txtBuscarCliente.getText().trim() : "";
        LocalDate fechaEntrada = dpFechaEntrada != null ? dpFechaEntrada.getValue() : null;
        String estado = cmbEstado != null ? cmbEstado.getValue() : "Todos";

        Page<ReservaListadoDTO> resultado = reservaService.buscarReservasPaginadas(
                filtroCliente, fechaEntrada, estado, pagina, elementosPorPagina);

        paginaActual = pagina;
        totalPaginas = resultado.getTotalPages();

        renderizarListado(resultado.getContent());
        actualizarBotonesPaginacion();
    }

    private void renderizarListado(List<ReservaListadoDTO> reservas) {
        if (listaReservasContainer == null) return;
        listaReservasContainer.getChildren().clear();

        if (reservas.isEmpty()) {
            Label lbl = new Label("No se encontraron reservas con los filtros aplicados.");
            lbl.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 14px; -fx-padding: 30;");
            listaReservasContainer.getChildren().add(lbl);
            return;
        }

        for (ReservaListadoDTO r : reservas) {
            listaReservasContainer.getChildren().add(crearCardReserva(r));
        }
    }

    // Construcción del card

    private VBox crearCardReserva(ReservaListadoDTO r) {
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);" +
                        "-fx-border-radius: 12px;"
        );
        VBox.setMargin(card, new Insets(0, 0, 14, 0));

        // Cabecera
        HBox cabecera = new HBox();
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.setPadding(new Insets(14, 20, 12, 20));
        cabecera.setStyle("-fx-border-color: #ECF0F1; -fx-border-width: 0 0 1 0;");

        Label lblId = new Label("#RES-" + r.getIdReserva());
        lblId.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        HBox.setHgrow(lblId, Priority.ALWAYS);

        Label badge = crearBadge(r.getEstadoReserva());
        cabecera.getChildren().addAll(lblId, badge);

        // Fila 1: Cliente | Habitación | Fecha entrada | Fecha salida
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(5);
        grid.setPadding(new Insets(14, 20, 10, 20));
        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            grid.getColumnConstraints().add(cc);
        }

        // Cliente
        grid.add(etiquetaCampo("CLIENTE"), 0, 0);
        grid.add(valorConIcono("👤", r.getNombreClienteCompleto(),
                Boolean.TRUE.equals(r.getClienteVip()) ? " ⭐" : ""), 0, 1);

        // Habitación
        grid.add(etiquetaCampo("HABITACIÓN"), 1, 0);
        grid.add(valorConIcono("🛏", r.getHabitacionDescripcion(), ""), 1, 1);

        // Fecha entrada
        grid.add(etiquetaCampo("FECHA ENTRADA"), 2, 0);
        grid.add(valorConIcono("📅", r.getFechaEntradaFormateada(), ""), 2, 1);

        // Fecha salida
        grid.add(etiquetaCampo("FECHA SALIDA"), 3, 0);
        grid.add(valorConIcono("📅", r.getFechaSalidaFormateada(), ""), 3, 1);

        // Fila 2: Huéspedes | Importe
        GridPane grid2 = new GridPane();
        grid2.setHgap(20);
        grid2.setVgap(5);
        grid2.setPadding(new Insets(2, 20, 14, 20));
        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            grid2.getColumnConstraints().add(cc);
        }
        grid2.add(etiquetaCampo("HUÉSPEDES"), 0, 0);
        grid2.add(valorConIcono("👥", r.getHuespedesTexto(), ""), 0, 1);
        grid2.add(etiquetaCampo("IMPORTE TOTAL"), 1, 0);
        grid2.add(valorConIcono("💰", r.getImporteTotalFormateado(), ""), 1, 1);

        // Botones
        HBox botones = new HBox(10);
        botones.setPadding(new Insets(10, 20, 14, 20));
        botones.setAlignment(Pos.CENTER_LEFT);
        botones.setStyle("-fx-border-color: #ECF0F1; -fx-border-width: 1 0 0 0;");

        Button btnVer     = crearBoton("👁  Ver Detalles", "#3498DB");
        Button btnEditar  = crearBoton("✏  Editar", "#F39C12");
        Button btnCancelar = crearBoton("✖  Cancelar", "#E74C3C");

        btnVer.setOnAction(e -> abrirModalDetalles(r));
        btnEditar.setOnAction(e -> abrirModalEditar(r));
        btnCancelar.setOnAction(e -> cancelarReserva(r));
        btnCancelar.setDisable("Cancelada".equals(r.getEstadoReserva()));

        botones.getChildren().addAll(btnVer, btnEditar, btnCancelar);

        card.getChildren().addAll(cabecera, grid, grid2, botones);
        return card;
    }

    private Label etiquetaCampo(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #95A5A6; -fx-padding: 0 0 2 0;");
        return l;
    }

    private HBox valorConIcono(String icono, String valor, String extra) {
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icono);
        Label lbl = new Label(valor + extra);
        lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        box.getChildren().addAll(ico, lbl);
        return box;
    }

    private Label crearBadge(String estado) {
        String color = switch (estado != null ? estado : "") {
            case "Confirmada"    -> "#27AE60";
            case "Pendiente"     -> "#F39C12";
            case "Cancelada"     -> "#E74C3C";
            case "Completada"    -> "#3498DB";
            case "No_presentado" -> "#95A5A6";
            default              -> "#BDC3C7";
        };
        Label badge = new Label(estado != null ? estado.toUpperCase() : "");
        badge.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 5 14 5 14;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );
        return badge;
    }

    private Button crearBoton(String texto, String color) {
        Button btn = new Button(texto);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20 10 20;"
        );
        btn.setOnMouseEntered(e -> { btn.setScaleX(1.03); btn.setScaleY(1.03); });
        btn.setOnMouseExited(e  -> { btn.setScaleX(1.0);  btn.setScaleY(1.0);  });
        return btn;
    }

    // ACTUALIZACIÓN PAGINACIÓN
    private void actualizarBotonesPaginacion() {
        if (paginacionContainer == null) return;
        paginacionContainer.getChildren().clear();

        if (totalPaginas <= 1) {
            paginacionContainer.setVisible(false);
            paginacionContainer.setManaged(false);
            return;
        }
        paginacionContainer.setVisible(true);
        paginacionContainer.setManaged(true);

        Button btnAnt = new Button("◀");
        btnAnt.getStyleClass().add("page-btn");
        btnAnt.setDisable(paginaActual == 0);
        btnAnt.setOnAction(e -> cargarReservasPaginadas(paginaActual - 1));
        paginacionContainer.getChildren().add(btnAnt);

        int inicio = Math.max(0, paginaActual - 2);
        int fin    = Math.min(totalPaginas, paginaActual + 3);
        for (int i = inicio; i < fin; i++) {
            final int p = i;
            Button bp = new Button(String.valueOf(i + 1));
            bp.getStyleClass().add("page-btn");
            if (i == paginaActual) bp.getStyleClass().add("active");
            bp.setOnAction(e -> cargarReservasPaginadas(p));
            paginacionContainer.getChildren().add(bp);
        }

        Button btnSig = new Button("▶");
        btnSig.getStyleClass().add("page-btn");
        btnSig.setDisable(paginaActual >= totalPaginas - 1);
        btnSig.setOnAction(e -> cargarReservasPaginadas(paginaActual + 1));
        paginacionContainer.getChildren().add(btnSig);
    }

    // BUSCADOR (FILTROS)
    @FXML
    private void buscarReservas() {
        paginaActual = 0;
        cargarReservas();
    }

    // MODAL FORMULARIO
    @FXML
    private void abrirModalNueva() {
        reservaEnEdicionId      = null;
        clienteSeleccionadoForm = null;
        if (lblModalTitulo != null) lblModalTitulo.setText("Nueva Reserva");
        limpiarFormulario();
        cargarHabitaciones();
        mostrarModal(modalFormContainer);
    }

    private void abrirModalEditar(ReservaListadoDTO r) {
        reservaEnEdicionId      = r.getIdReserva();
        clienteSeleccionadoForm = null;
        if (lblModalTitulo != null) lblModalTitulo.setText("Editar Reserva #RES-" + r.getIdReserva());
        limpiarFormulario();
        cargarHabitaciones();

        if (dpFormEntrada != null) dpFormEntrada.setValue(r.getFechaEntrada());
        if (dpFormSalida  != null) dpFormSalida.setValue(r.getFechaSalida());
        if (spnAdultos    != null) spnAdultos.getValueFactory().setValue(r.getNumAdultos() != null ? r.getNumAdultos() : 1);
        if (spnNinos      != null) spnNinos.getValueFactory().setValue(r.getNumNinos()    != null ? r.getNumNinos()   : 0);

        // Seleccionar habitación
        if (cmbFormHabitacion != null && r.getNumeroHabitacion() != null) {
            cmbFormHabitacion.getItems().stream()
                    .filter(s -> s.startsWith(r.getNumeroHabitacion()))
                    .findFirst()
                    .ifPresent(cmbFormHabitacion::setValue);
        }

        // Marcar cliente seleccionado
        ClienteDTO c = new ClienteDTO();
        c.setIdCliente(r.getIdCliente());
        c.setNombre(r.getNombreCliente());
        c.setApellidos(r.getApellidosCliente());
        c.setVip(r.getClienteVip());
        clienteSeleccionadoForm = c;
        if (lblFormClienteSeleccionado != null) {
            lblFormClienteSeleccionado.setText("✔ " + r.getNombreClienteCompleto());
            lblFormClienteSeleccionado.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
        }

        recalcularTotal();
        mostrarModal(modalFormContainer);
    }

    private void cargarHabitaciones() {
        habitacionesDisponibles = habitacionService.obtenerTodas();
        if (cmbFormHabitacion == null) return;
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Habitacion h : habitacionesDisponibles) {
            String tipo = h.getTipoHabitacion() != null ? h.getTipoHabitacion().getNombre() : "";
            items.add(h.getNumeroHabitacion() + " - " + tipo);
        }
        cmbFormHabitacion.setItems(items);
    }

    private void recalcularTotal() {
        if (cmbFormHabitacion == null || dpFormEntrada == null || dpFormSalida == null) return;
        int idx = cmbFormHabitacion.getSelectionModel().getSelectedIndex();
        if (idx < 0 || habitacionesDisponibles == null || idx >= habitacionesDisponibles.size()) {
            if (lblFormPrecioNoche != null) lblFormPrecioNoche.setText("0.00 €/noche");
            if (lblFormTotal       != null) lblFormTotal.setText("Total: 0.00 €");
            return;
        }
        Habitacion hab = habitacionesDisponibles.get(idx);
        BigDecimal precio = (hab.getTipoHabitacion() != null && hab.getTipoHabitacion().getPrecioBase() != null)
                ? hab.getTipoHabitacion().getPrecioBase()
                : BigDecimal.ZERO;

        if (lblFormPrecioNoche != null)
            lblFormPrecioNoche.setText(String.format("%.2f €/noche", precio.doubleValue()));

        LocalDate entrada = dpFormEntrada.getValue();
        LocalDate salida  = dpFormSalida.getValue();
        if (entrada != null && salida != null && salida.isAfter(entrada)) {
            long noches = ChronoUnit.DAYS.between(entrada, salida);
            BigDecimal total = precio.multiply(BigDecimal.valueOf(noches));
            if (lblFormTotal != null)
                lblFormTotal.setText(String.format("Total: %.2f €  (%d noches)", total.doubleValue(), noches));
        } else {
            if (lblFormTotal != null) lblFormTotal.setText("Total: 0.00 €");
        }
    }

    @FXML
    private void guardarReserva() {
        if (lblFormError != null) lblFormError.setVisible(false);
        if (!validarFormulario()) return;

        try {
            int idx = cmbFormHabitacion.getSelectionModel().getSelectedIndex();
            Habitacion hab = habitacionesDisponibles.get(idx);
            BigDecimal precio = (hab.getTipoHabitacion() != null && hab.getTipoHabitacion().getPrecioBase() != null)
                    ? hab.getTipoHabitacion().getPrecioBase()
                    : BigDecimal.ZERO;

            ReservaRequestDTO dto = new ReservaRequestDTO();
            dto.setIdHabitacion(hab.getIdHabitacion());
            dto.setIdCliente(clienteSeleccionadoForm.getIdCliente());
            dto.setFechaEntrada(dpFormEntrada.getValue());
            dto.setFechaSalida(dpFormSalida.getValue());
            dto.setPrecioNoche(precio);
            dto.setNumAdultos(spnAdultos != null ? spnAdultos.getValue() : 1);
            dto.setNumNinos(spnNinos   != null ? spnNinos.getValue()   : 0);
            dto.setObservaciones(txtFormObservaciones != null ? txtFormObservaciones.getText().trim() : "");

            if (reservaEnEdicionId == null) {
                reservaService.crearReserva(dto);
            } else {
                reservaService.actualizarReserva(reservaEnEdicionId, dto);
            }

            cerrarModal(modalFormContainer);
            actualizarEstadisticas();
            cargarReservas();

        } catch (Exception ex) {
            mostrarErrorForm(ex.getMessage());
        }
    }

    private boolean validarFormulario() {
        if (clienteSeleccionadoForm == null) {
            mostrarErrorForm("Debes seleccionar un cliente.");
            return false;
        }
        if (cmbFormHabitacion == null || cmbFormHabitacion.getSelectionModel().getSelectedIndex() < 0) {
            mostrarErrorForm("Debes seleccionar una habitación.");
            return false;
        }
        if (dpFormEntrada == null || dpFormEntrada.getValue() == null) {
            mostrarErrorForm("Debes indicar la fecha de entrada.");
            return false;
        }
        if (dpFormSalida == null || dpFormSalida.getValue() == null) {
            mostrarErrorForm("Debes indicar la fecha de salida.");
            return false;
        }
        if (!dpFormSalida.getValue().isAfter(dpFormEntrada.getValue())) {
            mostrarErrorForm("La fecha de salida debe ser posterior a la de entrada.");
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        clienteSeleccionadoForm = null;
        if (dpFormEntrada != null) dpFormEntrada.setValue(null);
        if (dpFormSalida  != null) dpFormSalida.setValue(null);
        if (cmbFormHabitacion != null) cmbFormHabitacion.getSelectionModel().clearSelection();
        if (txtFormBuscarCliente != null) txtFormBuscarCliente.clear();
        ocultarListaClientes();
        if (lblFormClienteSeleccionado != null) {
            lblFormClienteSeleccionado.setText("(ningún cliente seleccionado)");
            lblFormClienteSeleccionado.setStyle("-fx-text-fill: #95A5A6; -fx-font-style: italic;");
        }
        if (spnAdultos != null) spnAdultos.getValueFactory().setValue(1);
        if (spnNinos   != null) spnNinos.getValueFactory().setValue(0);
        if (lblFormPrecioNoche != null) lblFormPrecioNoche.setText("0.00 €/noche");
        if (lblFormTotal       != null) lblFormTotal.setText("Total: 0.00 €");
        if (txtFormObservaciones != null) txtFormObservaciones.clear();
        if (lblFormError != null) lblFormError.setVisible(false);
    }

    // MODAL DETALLES
    private void abrirModalDetalles(ReservaListadoDTO r) {
        if (modalDetallesContainer == null) return;
        if (lblDetId          != null) lblDetId.setText("Reserva #RES-" + r.getIdReserva());
        if (lblDetEstado      != null) lblDetEstado.setText(r.getEstadoReserva() != null ? r.getEstadoReserva().toUpperCase() : "-");
        if (lblDetCliente     != null) lblDetCliente.setText(r.getNombreClienteCompleto());
        if (lblDetVip         != null) lblDetVip.setText(Boolean.TRUE.equals(r.getClienteVip()) ? "⭐ Cliente VIP" : "");
        if (lblDetHabitacion  != null) lblDetHabitacion.setText(r.getHabitacionDescripcion());
        if (lblDetFechaEntrada != null) lblDetFechaEntrada.setText(r.getFechaEntradaFormateada());
        if (lblDetFechaSalida  != null) lblDetFechaSalida.setText(r.getFechaSalidaFormateada());
        if (lblDetHuespedes   != null) lblDetHuespedes.setText(r.getHuespedesTexto());
        if (lblDetImporte     != null) lblDetImporte.setText(r.getImporteTotalFormateado());
        mostrarModal(modalDetallesContainer);
    }

    // CANCELAR RESERVA
    private void cancelarReserva(ReservaListadoDTO r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar reserva");
        confirm.setHeaderText("¿Cancelar la reserva #RES-" + r.getIdReserva() + "?");
        confirm.setContentText("Esta acción cambiará el estado a 'Cancelada' y no se puede deshacer.");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                reservaService.cancelarReserva(r.getIdReserva());
                actualizarEstadisticas();
                cargarReservas();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
            }
        }
    }

    // HELPERS MODALES
    private void mostrarModal(StackPane modal) {
        if (modal != null) { modal.setVisible(true); modal.setManaged(true); }
    }

    private void cerrarModal(StackPane modal) {
        if (modal != null) { modal.setVisible(false); modal.setManaged(false); }
    }

    @FXML private void cerrarModalForm()     { cerrarModal(modalFormContainer);     limpiarFormulario(); }
    @FXML private void cerrarModalDetalles() { cerrarModal(modalDetallesContainer); }

    private void mostrarErrorForm(String msg) {
        if (lblFormError != null) { lblFormError.setText(msg); lblFormError.setVisible(true); lblFormError.setManaged(true); }
    }

    // NAVEGACIÓN
    @FXML private void navegarDashboard() { stageManager.switchScene(FxmlView.DASHBOARD); }
    @FXML private void navegarClientes()  { stageManager.switchScene(FxmlView.CLIENTES);  }
    @FXML private void navegarEmpleados()  { stageManager.switchScene(FxmlView.EMPLEADOS);  }

    @FXML
    private void cerrarSesion() {
        authService.logout();
        stageManager.switchScene(FxmlView.LOGIN);
    }
}