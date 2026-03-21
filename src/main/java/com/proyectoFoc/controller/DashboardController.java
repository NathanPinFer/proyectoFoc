package com.proyectoFoc.controller;

import com.proyectoFoc.FxmlView;
import com.proyectoFoc.StageManager;
import com.proyectoFoc.dto.ClienteDTO;
import com.proyectoFoc.dto.ReservaCalendarioDTO;
import com.proyectoFoc.dto.ReservaRequestDTO;
import com.proyectoFoc.entity.Habitacion;
import com.proyectoFoc.service.AuthService;
import com.proyectoFoc.service.ClienteService;
import com.proyectoFoc.service.HabitacionService;
import com.proyectoFoc.service.ReservaService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Component
public class DashboardController {

    @FXML
    private Button btnEmpleados;
    @FXML
    private Label lblMesAnio;
    @FXML
    private VBox panelDetalle;
    @FXML
    private Label lblDetalleTitulo;
    @FXML
    private Label lblDetalleEstado;
    @FXML
    private Label lblDetalleHabitacion;
    @FXML
    private Label lblDetalleTipoHab;
    @FXML
    private Label lblDetalleCliente;
    @FXML
    private Label lblDetalleVip;
    @FXML
    private Label lblDetalleFechaEntrada;
    @FXML
    private Label lblDetalleFechaSalida;
    @FXML
    private Label lblDetalleNoches;
    @FXML
    private Label lblDetallePrecioNoche;
    @FXML
    private Label lblDetalleSubtotal;

    @FXML
    private GridPane gridDias;
    @FXML
    private GridPane gridHabitaciones;
    @FXML
    private GridPane gridCeldas;
    @FXML
    private ScrollPane scrollDias;
    @FXML
    private ScrollPane scrollHabs;
    @FXML
    private ScrollPane scrollCeldas;

    @Autowired
    private StageManager stageManager;

    @Autowired
    private AuthService authService;

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ClienteService clienteService;

    private YearMonth mesActual;

    private static final int COL_HABITACION = 130;
    private static final int COL_DIA = 38;
    private static final int ROW_HEADER = 36;
    private static final int ROW_HAB = 44;

    @FXML
    public void initialize() {
        mesActual = YearMonth.now();
        cargarCalendario();
        ocultarMenuSegunCargo();
        sincronizarScrolls();
    }

    private void sincronizarScrolls() {
        // Scroll horizontal: sincronizar scrollDias con scrollCeldas
        scrollCeldas.hvalueProperty().addListener((obs, oldVal, newVal) -> scrollDias.setHvalue(newVal.doubleValue()));
        scrollDias.hvalueProperty().addListener((obs, oldVal, newVal) -> scrollCeldas.setHvalue(newVal.doubleValue()));

        // Scroll vertical: sincronizar scrollHabs con scrollCeldas
        scrollCeldas.vvalueProperty().addListener((obs, oldVal, newVal) -> scrollHabs.setVvalue(newVal.doubleValue()));
        scrollHabs.vvalueProperty().addListener((obs, oldVal, newVal) -> scrollCeldas.setVvalue(newVal.doubleValue()));
    }

    @FXML
    private void navegarClientes() {
        stageManager.switchScene(FxmlView.CLIENTES);
    }

    @FXML
    private void cerrarSesion() {
        authService.logout();
        stageManager.switchScene(FxmlView.LOGIN);
    }

    @FXML
    private void mesAnterior() {
        mesActual = mesActual.minusMonths(1);
        cargarCalendario();
    }

    @FXML
    private void mesSiguiente() {
        mesActual = mesActual.plusMonths(1);
        cargarCalendario();
    }

    private void cargarCalendario() {
        int mes = mesActual.getMonthValue();
        int anio = mesActual.getYear();
        lblMesAnio.setText(mesActual.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("es", "ES"))
                + " " + anio);

        List<Habitacion> habitaciones = habitacionService.obtenerTodas();
        List<ReservaCalendarioDTO> reservas = reservaService.obtenerCalendario(mes, anio);

        renderizarCalendario(habitaciones, reservas, mes, anio);
    }

    private void renderizarCalendario(
            List<Habitacion> habitaciones,
            List<ReservaCalendarioDTO> reservas,
            int mes, int anio) {

        gridDias.getChildren().clear();
        gridDias.getColumnConstraints().clear();
        gridDias.getRowConstraints().clear();

        gridHabitaciones.getChildren().clear();
        gridHabitaciones.getColumnConstraints().clear();
        gridHabitaciones.getRowConstraints().clear();

        gridCeldas.getChildren().clear();
        gridCeldas.getColumnConstraints().clear();
        gridCeldas.getRowConstraints().clear();

        YearMonth ym = YearMonth.of(anio, mes);
        int diasEnMes = ym.lengthOfMonth();
        LocalDate hoy = LocalDate.now();

        // --- GRID DÍAS (cabecera horizontal) ---
        for (int d = 1; d <= diasEnMes; d++) {
            ColumnConstraints cc = new ColumnConstraints(COL_DIA);
            cc.setHgrow(Priority.NEVER);
            gridDias.getColumnConstraints().add(cc);
        }
        RowConstraints rcDias = new RowConstraints(ROW_HEADER);
        gridDias.getRowConstraints().add(rcDias);

        for (int d = 1; d <= diasEnMes; d++) {
            LocalDate fecha = LocalDate.of(anio, mes, d);
            boolean esHoy = fecha.equals(hoy);

            VBox celdaDia = new VBox();
            celdaDia.setAlignment(Pos.CENTER);
            celdaDia.setStyle("-fx-background-color: " + (esHoy ? "#2980b9" : "#F8F9FA") +
                    "; -fx-border-color: #ECF0F1; -fx-border-width: 0 1 1 0;");
            celdaDia.setMinSize(COL_DIA, ROW_HEADER);

            Label lblDia = new Label(String.valueOf(d));
            lblDia.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: "
                    + (esHoy ? "white" : "#2C3E50") + ";");

            String diaSemana = fecha.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
            Label lblDiaSem = new Label(diaSemana.substring(0, 1).toUpperCase());
            lblDiaSem.setStyle("-fx-font-weight: normal; -fx-font-size: 9px; -fx-text-fill: "
                    + (esHoy ? "white" : "#2C3E50") + ";");

            celdaDia.getChildren().addAll(lblDia, lblDiaSem);
            gridDias.add(celdaDia, d - 1, 0);
        }

        // --- GRID HABITACIONES (columna izquierda fija) ---
        ColumnConstraints ccHab = new ColumnConstraints(COL_HABITACION);
        ccHab.setHgrow(Priority.NEVER);
        gridHabitaciones.getColumnConstraints().add(ccHab);

        for (int i = 0; i < habitaciones.size(); i++) {
            Habitacion hab = habitaciones.get(i);

            RowConstraints rc = new RowConstraints(ROW_HAB);
            gridHabitaciones.getRowConstraints().add(rc);

            VBox celdaHab = new VBox(2);
            celdaHab.setAlignment(Pos.CENTER_LEFT);
            celdaHab.setStyle("-fx-background-color: #34495E; -fx-border-color: #2C3E50; -fx-border-width: 0 1 1 0;");
            celdaHab.setMinSize(COL_HABITACION, ROW_HAB);
            celdaHab.setPadding(new Insets(0, 8, 0, 10));

            Label lblNum = new Label("Hab. " + hab.getNumeroHabitacion());
            lblNum.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

            Label lblTipo = new Label(hab.getTipoHabitacion().getNombre());
            lblTipo.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 10px;");

            celdaHab.getChildren().addAll(lblNum, lblTipo);
            gridHabitaciones.add(celdaHab, 0, i);
        }

        // --- GRID CELDAS (área central con reservas) ---
        for (int d = 1; d <= diasEnMes; d++) {
            ColumnConstraints cc = new ColumnConstraints(COL_DIA);
            cc.setHgrow(Priority.NEVER);
            gridCeldas.getColumnConstraints().add(cc);
        }

        for (int i = 0; i < habitaciones.size(); i++) {
            Habitacion hab = habitaciones.get(i);

            RowConstraints rc = new RowConstraints(ROW_HAB);
            gridCeldas.getRowConstraints().add(rc);

            for (int d = 1; d <= diasEnMes; d++) {
                LocalDate fecha = LocalDate.of(anio, mes, d);
                boolean esFinSemana = fecha.getDayOfWeek() == DayOfWeek.SATURDAY
                        || fecha.getDayOfWeek() == DayOfWeek.SUNDAY;

                Pane celdaVacia = new Pane();
                celdaVacia.setStyle("-fx-background-color: " + (esFinSemana ? "#FAFAFA" : "white") +
                        "; -fx-border-color: #ECF0F1; -fx-border-width: 0 1 1 0;");
                celdaVacia.setMinSize(COL_DIA, ROW_HAB);

                final LocalDate fechaCelda = fecha;
                final Habitacion habCelda = hab;
                celdaVacia.setOnMouseClicked(e -> abrirFormularioReserva(habCelda, fechaCelda));
                celdaVacia.setStyle(celdaVacia.getStyle() + " -fx-cursor: hand;");

                gridCeldas.add(celdaVacia, d - 1, i);
            }
        }

        // --- BLOQUES DE RESERVAS ---
        LocalDate inicioMes = LocalDate.of(anio, mes, 1);
        LocalDate finMes = LocalDate.of(anio, mes, diasEnMes);

        for (ReservaCalendarioDTO reserva : reservas) {
            int filaHab = -1;
            for (int i = 0; i < habitaciones.size(); i++) {
                if (habitaciones.get(i).getIdHabitacion().equals(reserva.getIdHabitacion())) {
                    filaHab = i;
                    break;
                }
            }
            if (filaHab == -1)
                continue;

            LocalDate bloqueInicio = reserva.getFechaEntrada().isBefore(inicioMes)
                    ? inicioMes
                    : reserva.getFechaEntrada();
            LocalDate bloqueFin = reserva.getFechaSalida().isAfter(finMes)
                    ? finMes
                    : reserva.getFechaSalida();

            int colInicio = bloqueInicio.getDayOfMonth() - 1;
            int span = (int) (bloqueFin.toEpochDay() - bloqueInicio.toEpochDay());
            if (span < 1)
                span = 1;

            HBox bloque = crearBloqueReserva(reserva, span);
            gridCeldas.add(bloque, colInicio, filaHab);
            GridPane.setColumnSpan(bloque, span);
        }
    }

    private HBox crearBloqueReserva(ReservaCalendarioDTO reserva, int span) {
        HBox bloque = new HBox(4);
        bloque.setAlignment(Pos.CENTER_LEFT);
        bloque.setMaxHeight(ROW_HAB - 8);
        bloque.setMinHeight(ROW_HAB - 8);
        bloque.setPadding(new Insets(3, 6, 3, 6));

        String color = switch (reserva.getEstadoBloque()) {
            case "CHECKIN_HOY" -> "#27ae60";
            case "EN_CURSO" -> "#2980b9";
            case "CHECKOUT_HOY" -> "#e67e22";
            case "FINALIZADA" -> "#95a5a6";
            default -> "#8e44ad";
        };
        bloque.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4; -fx-cursor: hand;");

        VBox textos = new VBox(1);
        textos.setAlignment(Pos.CENTER_LEFT);

        String nombreTexto = reserva.getNombreCliente() + " " + reserva.getApellidosCliente();
        if (Boolean.TRUE.equals(reserva.getClienteVip())) {
            nombreTexto = "★ " + nombreTexto;
        }
        Label lblNombre = new Label(nombreTexto);
        lblNombre.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        lblNombre.setMaxWidth((span * COL_DIA) - 12.0);

        Label lblPrecio = new Label(reserva.getPrecioNoche() + "€/noche");
        lblPrecio.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 9px;");

        textos.getChildren().addAll(lblNombre, lblPrecio);
        bloque.getChildren().add(textos);

        bloque.setOnMouseClicked(e -> abrirDetalle(reserva));

        return bloque;
    }

    private void abrirDetalle(ReservaCalendarioDTO reserva) {
        lblDetalleTitulo.setText("Reserva #" + reserva.getIdReserva());

        String estadoTexto = switch (reserva.getEstadoBloque()) {
            case "CHECKIN_HOY" -> "CHECK-IN HOY";
            case "CHECKOUT_HOY" -> "CHECK-OUT HOY";
            case "EN_CURSO" -> "EN CURSO";
            case "FINALIZADA" -> "FINALIZADA";
            default -> "RESERVA FUTURA";
        };
        String estadoColor = switch (reserva.getEstadoBloque()) {
            case "CHECKIN_HOY" -> "#27ae60";
            case "CHECKOUT_HOY" -> "#e67e22";
            case "EN_CURSO" -> "#2980b9";
            case "FINALIZADA" -> "#95a5a6";
            default -> "#8e44ad";
        };
        lblDetalleEstado.setText(estadoTexto);
        lblDetalleEstado.setStyle("-fx-background-color: " + estadoColor +
                "; -fx-background-radius: 4; -fx-padding: 4 10 4 10; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;");

        lblDetalleHabitacion.setText("Habitación " + reserva.getNumeroHabitacion());
        lblDetalleTipoHab.setText(reserva.getTipoHabitacion() + " · Piso " + reserva.getPiso());

        lblDetalleCliente.setText(reserva.getNombreCliente() + " " + reserva.getApellidosCliente());
        lblDetalleVip.setText(Boolean.TRUE.equals(reserva.getClienteVip()) ? "★ Cliente VIP" : "");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblDetalleFechaEntrada.setText("Entrada: " + reserva.getFechaEntrada().format(fmt));
        lblDetalleFechaSalida.setText("Salida: " + reserva.getFechaSalida().format(fmt));
        long noches = java.time.temporal.ChronoUnit.DAYS.between(
                reserva.getFechaEntrada(), reserva.getFechaSalida());
        lblDetalleNoches.setText(noches + " noche" + (noches != 1 ? "s" : ""));

        lblDetallePrecioNoche.setText(reserva.getPrecioNoche() + " €/noche");
        lblDetalleSubtotal.setText("Total: " + reserva.getSubtotal() + " €");

        panelDetalle.setVisible(true);
        panelDetalle.setManaged(true);
    }

    @FXML
    private void cerrarDetalle() {
        panelDetalle.setVisible(false);
        panelDetalle.setManaged(false);
    }

    private void abrirFormularioReserva(Habitacion habitacion, LocalDate fecha) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nueva Reserva — Hab. " + habitacion.getNumeroHabitacion());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);
        dialog.getDialogPane().setMinWidth(500);

        VBox contenido = new VBox(15);
        contenido.setPadding(new Insets(20));
        contenido.setStyle("-fx-background-color: white;");

        Label lblInfoHab = new Label("Habitación " + habitacion.getNumeroHabitacion()
                + " · " + habitacion.getTipoHabitacion().getNombre()
                + " · " + habitacion.getTipoHabitacion().getPrecioBase() + "€/noche");
        lblInfoHab.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2C3E50;");

        GridPane gridFechas = new GridPane();
        gridFechas.setHgap(15);
        gridFechas.setVgap(10);

        Label lblEntrada = new Label("Fecha Entrada:");
        lblEntrada.setStyle("-fx-font-size: 12px; -fx-text-fill: #2C3E50;");
        DatePicker dpEntrada = new DatePicker(fecha);
        dpEntrada.setStyle("-fx-pref-width: 200px;");

        Label lblSalida = new Label("Fecha Salida:");
        lblSalida.setStyle("-fx-font-size: 12px; -fx-text-fill: #2C3E50;");
        DatePicker dpSalida = new DatePicker(fecha.plusDays(1));
        dpSalida.setStyle("-fx-pref-width: 200px;");

        gridFechas.add(lblEntrada, 0, 0);
        gridFechas.add(dpEntrada, 1, 0);
        gridFechas.add(lblSalida, 0, 1);
        gridFechas.add(dpSalida, 1, 1);

        Label lblPrecio = new Label("Precio/noche (€):");
        lblPrecio.setStyle("-fx-font-size: 12px; -fx-text-fill: #2C3E50;");
        TextField txtPrecio = new TextField(habitacion.getTipoHabitacion().getPrecioBase().toString());
        txtPrecio.setStyle("-fx-pref-width: 200px;");

        HBox boxPrecio = new HBox(15);
        boxPrecio.getChildren().addAll(lblPrecio, txtPrecio);

        Label lblSecCliente = new Label("HUÉSPED");
        lblSecCliente.setStyle("-fx-font-size: 12px; -fx-text-fill: #5a7a9a; -fx-font-weight: bold;");

        TextField txtBuscarCliente = new TextField();
        txtBuscarCliente.setPromptText("Buscar cliente por nombre o DNI...");
        txtBuscarCliente.setStyle("-fx-pref-width: 100%;");

        ListView<ClienteDTO> listaClientes = new ListView<>();
        listaClientes.setPrefHeight(100);
        listaClientes.setVisible(false);

        final ClienteDTO[] clienteSeleccionado = { null };
        Label lblClienteSeleccionado = new Label("(ningún cliente seleccionado)");
        lblClienteSeleccionado.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");

        txtBuscarCliente.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                listaClientes.setVisible(false);
                return;
            }

            List<ClienteDTO> clientes = clienteService.buscarClientes(newVal);
            listaClientes.getItems().setAll(clientes);
            listaClientes.setVisible(!clientes.isEmpty());
        });

        listaClientes.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ClienteDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombre() + " " + item.getApellidos() +
                            " — " + item.getDni() +
                            (Boolean.TRUE.equals(item.getVip()) ? " ⭐" : ""));
                }
            }
        });

        listaClientes.setOnMouseClicked(e -> {
            ClienteDTO selected = listaClientes.getSelectionModel().getSelectedItem();
            if (selected != null) {
                clienteSeleccionado[0] = selected;
                lblClienteSeleccionado.setText("✓ " + selected.getNombre() + " " +
                        selected.getApellidos() + " (" + selected.getDni() + ")");
                lblClienteSeleccionado.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                listaClientes.setVisible(false);
                txtBuscarCliente.clear();
            }
        });

        contenido.getChildren().addAll(
                lblInfoHab,
                new Separator(),
                gridFechas,
                boxPrecio,
                new Separator(),
                lblSecCliente,
                txtBuscarCliente,
                listaClientes,
                lblClienteSeleccionado);

        dialog.getDialogPane().setContent(contenido);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                if (clienteSeleccionado[0] == null) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Cliente requerido");
                    alert.setHeaderText("Debes seleccionar un cliente");
                    alert.showAndWait();
                    return null;
                }

                try {
                    ReservaRequestDTO reservaDTO = new ReservaRequestDTO();
                    reservaDTO.setIdHabitacion(habitacion.getIdHabitacion());
                    reservaDTO.setIdCliente(clienteSeleccionado[0].getIdCliente());
                    reservaDTO.setFechaEntrada(dpEntrada.getValue());
                    reservaDTO.setFechaSalida(dpSalida.getValue());
                    reservaDTO.setPrecioNoche(new BigDecimal(txtPrecio.getText()));

                    reservaService.crearReserva(reservaDTO);

                    // CORREGIDO: Solo recargar calendario, SIN mensaje de confirmación
                    cargarCalendario();

                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error al crear reserva");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
            return btn;
        });

        dialog.show();
    }

    private void ocultarMenuSegunCargo() {
        // Si no es Gerente, ocultar botón
        if (!authService.isGerente()) {
            btnEmpleados.setVisible(false);
            btnEmpleados.setManaged(false); // No ocupa espacio
        }
    }
}
