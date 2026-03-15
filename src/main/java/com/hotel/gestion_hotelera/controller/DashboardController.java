package com.hotel.gestion_hotelera.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotel.gestion_hotelera.dto.ReservaCalendarioDTO;
import com.hotel.gestion_hotelera.model.Cliente;
import com.hotel.gestion_hotelera.model.Habitacion;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Controller;

import com.hotel.gestion_hotelera.model.Habitacion;
import javafx.scene.control.*;

@Controller // <- este es de Spring: org.springframework.stereotype.Controller
public class DashboardController {

    @FXML
    private GridPane gridCalendario;
    @FXML
    private ScrollPane scrollPane;
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

    private YearMonth mesActual;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final String BASE_URL = "http://localhost:8081/api/dashboard";
    private static final int COL_HABITACION = 130;
    private static final int COL_DIA = 38;
    private static final int ROW_HEADER = 36;
    private static final int ROW_HAB = 44;

    @FXML
    public void initialize() {
        mesActual = YearMonth.now();
        cargarCalendario();
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

    private void abrirDetalle(ReservaCalendarioDTO reserva) {
        // Rellenar datos
        lblDetalleTitulo.setText("Reserva #" + reserva.getIdReserva());

        // Estado con color
        String estadoTexto = switch (reserva.getEstadoBloque()) {
            case "CHECKIN_HOY" -> "CHECK-IN HOY";
            case "CHECKOUT_HOY" -> "CHECK-OUT HOY";
            case "EN_CURSO" -> "EN CURSO";
            default -> "RESERVA FUTURA";
        };
        String estadoColor = switch (reserva.getEstadoBloque()) {
            case "CHECKIN_HOY" -> "#27ae60";
            case "CHECKOUT_HOY" -> "#e67e22";
            case "EN_CURSO" -> "#2980b9";
            default -> "#8e44ad";
        };
        lblDetalleEstado.setText(estadoTexto);
        lblDetalleEstado.setStyle("-fx-background-color: " + estadoColor +
                "; -fx-background-radius: 4; -fx-padding: 4 10 4 10; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Habitación
        lblDetalleHabitacion.setText("Habitación " + reserva.getNumeroHabitacion());
        lblDetalleTipoHab.setText(reserva.getTipoHabitacion() +
                " · Piso " + reserva.getPiso());

        // Cliente
        lblDetalleCliente.setText(reserva.getNombreCliente() + " " +
                reserva.getApellidosCliente());
        lblDetalleVip.setText(Boolean.TRUE.equals(reserva.getClienteVip())
                ? "★ Cliente VIP"
                : "");

        // Fechas
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblDetalleFechaEntrada.setText("Entrada: " +
                reserva.getFechaEntrada().format(fmt));
        lblDetalleFechaSalida.setText("Salida: " +
                reserva.getFechaSalida().format(fmt));
        long noches = java.time.temporal.ChronoUnit.DAYS.between(
                reserva.getFechaEntrada(), reserva.getFechaSalida());
        lblDetalleNoches.setText(noches + " noche" + (noches != 1 ? "s" : ""));

        // Importe
        lblDetallePrecioNoche.setText(reserva.getPrecioNoche() + " €/noche");
        lblDetalleSubtotal.setText("Total: " + reserva.getSubtotal() + " €");

        // Mostrar panel
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
        dialog.getDialogPane().setMinWidth(480);

        VBox contenido = new VBox(12);
        contenido.setPadding(new Insets(20));

        // Info habitación (solo lectura)
        Label lblInfoHab = new Label("Habitación " + habitacion.getNumeroHabitacion()
                + " · " + habitacion.getTipoHabitacion().getNombre()
                + " · " + habitacion.getTipoHabitacion().getPrecioBase() + "€/noche");
        lblInfoHab.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        // Fechas
        HBox filaDates = new HBox(12);
        DatePicker dpEntrada = new DatePicker(fecha);
        DatePicker dpSalida = new DatePicker(fecha.plusDays(1));
        dpEntrada.setPromptText("Entrada");
        dpSalida.setPromptText("Salida");
        filaDates.getChildren().addAll(
                new VBox(4, new Label("Entrada:"), dpEntrada),
                new VBox(4, new Label("Salida:"), dpSalida));

        // Precio
        HBox filaPrecio = new HBox(12);
        TextField txtPrecio = new TextField(
                habitacion.getTipoHabitacion().getPrecioBase().toString());
        filaPrecio.getChildren().addAll(
                new VBox(4, new Label("Precio/noche (€):"), txtPrecio));

        // Separador cliente
        Label lblSecCliente = new Label("HUÉSPED");
        lblSecCliente.setStyle("-fx-font-size: 11px; -fx-text-fill: #5a7a9a; -fx-font-weight: bold;");

        // Búsqueda cliente existente
        TextField txtBuscarCliente = new TextField();
        txtBuscarCliente.setPromptText("Buscar cliente por nombre o DNI...");
        ListView<Cliente> listaClientes = new ListView<>();
        listaClientes.setPrefHeight(80);
        listaClientes.setVisible(false);
        listaClientes.setManaged(false);

        // Referencia al cliente seleccionado
        final Cliente[] clienteSeleccionado = { null };
        Label lblClienteSeleccionado = new Label("");
        lblClienteSeleccionado.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

        // Búsqueda en tiempo real
        txtBuscarCliente.textProperty().addListener((obs, oldVal, newVal) -> {
            clienteSeleccionado[0] = null;
            lblClienteSeleccionado.setText("");
            if (newVal.length() >= 2) {
                try {
                    String url = "http://localhost:8081/api/clientes/buscar?q="
                            + java.net.URLEncoder.encode(newVal, "UTF-8");
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(url)).GET().build();
                    HttpResponse<String> resp = httpClient.send(req,
                            HttpResponse.BodyHandlers.ofString());
                    List<Cliente> resultados = mapper.readValue(resp.body(),
                            new TypeReference<>() {
                            });
                    Platform.runLater(() -> {
                        listaClientes.getItems().setAll(resultados);
                        boolean hayResultados = !resultados.isEmpty();
                        listaClientes.setVisible(hayResultados);
                        listaClientes.setManaged(hayResultados);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                listaClientes.setVisible(false);
                listaClientes.setManaged(false);
            }
        });

        // Selección de cliente de la lista
        listaClientes.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Cliente c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null)
                    setText(null);
                else
                    setText(c.getNombre() + " " + c.getApellidos() + " · " + c.getDni());
            }
        });
        listaClientes.setOnMouseClicked(e -> {
            Cliente sel = listaClientes.getSelectionModel().getSelectedItem();
            if (sel != null) {
                clienteSeleccionado[0] = sel;
                txtBuscarCliente.setText(sel.getNombre() + " " + sel.getApellidos());
                lblClienteSeleccionado.setText("✓ Cliente existente seleccionado");
                listaClientes.setVisible(false);
                listaClientes.setManaged(false);
            }
        });

        // Campos nuevo cliente
        Label lblNuevoCliente = new Label("— O introduce datos del nuevo cliente —");
        lblNuevoCliente.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        HBox filaNombre = new HBox(12);
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre");
        TextField txtApellidos = new TextField();
        txtApellidos.setPromptText("Apellidos");
        filaNombre.getChildren().addAll(
                new VBox(4, new Label("Nombre:"), txtNombre),
                new VBox(4, new Label("Apellidos:"), txtApellidos));

        HBox filaDni = new HBox(12);
        TextField txtDni = new TextField();
        txtDni.setPromptText("12345678A");
        TextField txtTelefono = new TextField();
        txtTelefono.setPromptText("+34 600 000 000");
        filaDni.getChildren().addAll(
                new VBox(4, new Label("DNI:"), txtDni),
                new VBox(4, new Label("Teléfono:"), txtTelefono));

        CheckBox chkVip = new CheckBox("Cliente VIP");

        // Observaciones
        TextField txtObs = new TextField();
        txtObs.setPromptText("Observaciones (opcional)");

        contenido.getChildren().addAll(
                lblInfoHab, filaDates, filaPrecio,
                lblSecCliente,
                txtBuscarCliente, listaClientes, lblClienteSeleccionado,
                lblNuevoCliente, filaNombre, filaDni, chkVip,
                new VBox(4, new Label("Observaciones:"), txtObs));

        dialog.getDialogPane().setContent(contenido);

        // Acción guardar
        dialog.getDialogPane().lookupButton(btnGuardar).addEventFilter(
                javafx.event.ActionEvent.ACTION, event -> {

                    // Validación básica
                    if (dpEntrada.getValue() == null || dpSalida.getValue() == null) {
                        mostrarError("Introduce las fechas de entrada y salida.");
                        event.consume();
                        return;
                    }
                    if (dpSalida.getValue().isBefore(dpEntrada.getValue())
                            || dpSalida.getValue().isEqual(dpEntrada.getValue())) {
                        mostrarError("La fecha de salida debe ser posterior a la de entrada.");
                        event.consume();
                        return;
                    }
                    if (clienteSeleccionado[0] == null
                            && (txtNombre.getText().isBlank() || txtDni.getText().isBlank())) {
                        mostrarError("Introduce al menos nombre y DNI del huésped.");
                        event.consume();
                        return;
                    }

                    // Construir request
                    try {
                        java.util.Map<String, Object> body = new java.util.HashMap<>();

                        if (clienteSeleccionado[0] != null) {
                            body.put("idCliente", clienteSeleccionado[0].getIdCliente());
                        } else {
                            body.put("nombre", txtNombre.getText().trim());
                            body.put("apellidos", txtApellidos.getText().trim());
                            body.put("dni", txtDni.getText().trim());
                            body.put("telefono", txtTelefono.getText().trim());
                            body.put("vip", chkVip.isSelected());
                        }

                        body.put("idHabitacion", habitacion.getIdHabitacion());
                        body.put("fechaEntrada", dpEntrada.getValue().toString());
                        body.put("fechaSalida", dpSalida.getValue().toString());
                        body.put("precioNoche", new java.math.BigDecimal(txtPrecio.getText().trim()));
                        body.put("numAdultos", 1);
                        body.put("observaciones", txtObs.getText().trim());

                        String json = mapper.writeValueAsString(body);

                        HttpRequest req = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8081/api/dashboard/reservas"))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(json))
                                .build();

                        HttpResponse<String> resp = httpClient.send(req,
                                HttpResponse.BodyHandlers.ofString());

                        if (resp.statusCode() == 200) {
                            cargarCalendario(); // Refresca el calendario
                        } else {
                            mostrarError("Error al guardar: " + resp.body());
                            event.consume();
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        mostrarError("Error inesperado: " + ex.getMessage());
                        event.consume();
                    }
                });

        dialog.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cargarCalendario() {
        // Actualizar label del mes
        String nombreMes = mesActual.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);
        lblMesAnio.setText(nombreMes + " " + mesActual.getYear());

        int mes = mesActual.getMonthValue();
        int anio = mesActual.getYear();

        // Llamada HTTP en hilo separado
        Thread thread = new Thread(() -> {
            try {
                // Obtener habitaciones
                HttpRequest reqHabs = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/habitaciones"))
                        .GET().build();
                HttpResponse<String> respHabs = httpClient.send(reqHabs,
                        HttpResponse.BodyHandlers.ofString());

                List<com.hotel.gestion_hotelera.model.Habitacion> habitaciones = mapper.readValue(respHabs.body(),
                        new TypeReference<>() {
                        });

                // Obtener calendario
                HttpRequest reqCal = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/calendario?mes=" + mes + "&anio=" + anio))
                        .GET().build();
                HttpResponse<String> respCal = httpClient.send(reqCal,
                        HttpResponse.BodyHandlers.ofString());

                List<ReservaCalendarioDTO> reservas = mapper.readValue(respCal.body(),
                        new TypeReference<>() {
                        });

                Platform.runLater(() -> renderizarCalendario(habitaciones, reservas, mes, anio));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void renderizarCalendario(
            List<com.hotel.gestion_hotelera.model.Habitacion> habitaciones,
            List<ReservaCalendarioDTO> reservas,
            int mes, int anio) {

        gridCalendario.getChildren().clear();
        gridCalendario.getColumnConstraints().clear();
        gridCalendario.getRowConstraints().clear();

        YearMonth ym = YearMonth.of(anio, mes);
        int diasEnMes = ym.lengthOfMonth();
        LocalDate hoy = LocalDate.now();

        // ── Columna 0: habitaciones (fija) ──
        ColumnConstraints ccHab = new ColumnConstraints(COL_HABITACION);
        ccHab.setHgrow(Priority.NEVER);
        gridCalendario.getColumnConstraints().add(ccHab);

        // ── Columnas 1..N: días ──
        for (int d = 1; d <= diasEnMes; d++) {
            ColumnConstraints cc = new ColumnConstraints(COL_DIA);
            cc.setHgrow(Priority.NEVER);
            gridCalendario.getColumnConstraints().add(cc);
        }

        // ── Fila 0: cabecera (esquina + días) ──
        RowConstraints rcHeader = new RowConstraints(ROW_HEADER);
        gridCalendario.getRowConstraints().add(rcHeader);

        // Esquina superior izquierda
        Pane esquina = new Pane();
        esquina.getStyleClass().add("celda-habitacion");
        esquina.setMinSize(COL_HABITACION, ROW_HEADER);
        gridCalendario.add(esquina, 0, 0);

        // Cabecera de días
        for (int d = 1; d <= diasEnMes; d++) {
            LocalDate fecha = LocalDate.of(anio, mes, d);
            boolean esHoy = fecha.equals(hoy);
            boolean esFinSemana = fecha.getDayOfWeek() == DayOfWeek.SATURDAY
                    || fecha.getDayOfWeek() == DayOfWeek.SUNDAY;

            VBox celdaDia = new VBox();
            celdaDia.setAlignment(Pos.CENTER);
            celdaDia.getStyleClass().add("celda-dia");
            if (esHoy)
                celdaDia.getStyleClass().add("celda-dia-hoy");
            celdaDia.setMinSize(COL_DIA, ROW_HEADER);

            Label lblDia = new Label(String.valueOf(d));
            lblDia.getStyleClass().add("celda-dia-label");

            String diaSemana = fecha.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
            Label lblDiaSem = new Label(diaSemana.substring(0, 1).toUpperCase());
            lblDiaSem.getStyleClass().add("celda-dia-label");
            lblDiaSem.setStyle("-fx-font-weight: normal; -fx-font-size: 9px;");

            celdaDia.getChildren().addAll(lblDia, lblDiaSem);
            gridCalendario.add(celdaDia, d, 0);
        }

        // ── Filas 1..N: una por habitación ──
        for (int i = 0; i < habitaciones.size(); i++) {
            var hab = habitaciones.get(i);
            int fila = i + 1;

            RowConstraints rc = new RowConstraints(ROW_HAB);
            gridCalendario.getRowConstraints().add(rc);

            // Celda nombre habitación
            VBox celdaHab = new VBox(2);
            celdaHab.setAlignment(Pos.CENTER_LEFT);
            celdaHab.getStyleClass().add("celda-habitacion");
            celdaHab.setMinSize(COL_HABITACION, ROW_HAB);
            celdaHab.setPadding(new Insets(0, 8, 0, 10));

            Label lblNum = new Label("Hab. " + hab.getNumeroHabitacion());
            lblNum.getStyleClass().add("celda-habitacion-num");

            Label lblTipo = new Label(hab.getTipoHabitacion().getNombre());
            lblTipo.getStyleClass().add("celda-habitacion-tipo");

            celdaHab.getChildren().addAll(lblNum, lblTipo);
            gridCalendario.add(celdaHab, 0, fila);

            // Celdas vacías para cada día
            for (int d = 1; d <= diasEnMes; d++) {
                LocalDate fecha = LocalDate.of(anio, mes, d);
                boolean esFinSemana = fecha.getDayOfWeek() == DayOfWeek.SATURDAY
                        || fecha.getDayOfWeek() == DayOfWeek.SUNDAY;

                Pane celdaVacia = new Pane();
                celdaVacia.getStyleClass().add("celda-vacia");
                if (esFinSemana)
                    celdaVacia.getStyleClass().add("celda-vacia-fin-semana");
                celdaVacia.setMinSize(COL_DIA, ROW_HAB);
                final LocalDate fechaCelda = fecha;
                final Habitacion habCelda = hab;
                celdaVacia.setOnMouseClicked(e -> abrirFormularioReserva(habCelda, fechaCelda));
                celdaVacia.setStyle("-fx-cursor: hand;");
                gridCalendario.add(celdaVacia, d, fila);
            }
        }

        // ── Bloques de reserva ──
        for (ReservaCalendarioDTO reserva : reservas) {
            // Encontrar fila de la habitación
            int filaHab = -1;
            for (int i = 0; i < habitaciones.size(); i++) {
                if (habitaciones.get(i).getIdHabitacion()
                        .equals(reserva.getIdHabitacion())) {
                    filaHab = i + 1;
                    break;
                }
            }
            if (filaHab == -1)
                continue;

            // Calcular columna inicio y span dentro del mes visible
            LocalDate inicioMes = LocalDate.of(anio, mes, 1);
            LocalDate finMes = LocalDate.of(anio, mes, diasEnMes);

            LocalDate bloqueInicio = reserva.getFechaEntrada().isBefore(inicioMes)
                    ? inicioMes
                    : reserva.getFechaEntrada();
            LocalDate bloqueFin = reserva.getFechaSalida().isAfter(finMes)
                    ? finMes
                    : reserva.getFechaSalida();

            int colInicio = bloqueInicio.getDayOfMonth();
            int span = (int) (bloqueFin.toEpochDay() - bloqueInicio.toEpochDay());
            if (span < 1)
                span = 1;

            // Crear bloque visual
            HBox bloque = crearBloqueReserva(reserva, span);
            gridCalendario.add(bloque, colInicio, filaHab);
            GridPane.setColumnSpan(bloque, span);
        }
    }

    private HBox crearBloqueReserva(ReservaCalendarioDTO reserva, int span) {
        HBox bloque = new HBox(4);
        bloque.setAlignment(Pos.CENTER_LEFT);
        bloque.getStyleClass().add("bloque-reserva");
        bloque.setMaxHeight(ROW_HAB - 8);
        bloque.setMinHeight(ROW_HAB - 8);
        bloque.setPadding(new Insets(3, 6, 3, 6));

        // Color según estado
        String colorClass = switch (reserva.getEstadoBloque()) {
            case "CHECKIN_HOY" -> "bloque-checkin";
            case "EN_CURSO" -> "bloque-encurso";
            case "CHECKOUT_HOY" -> "bloque-checkout";
            default -> "bloque-futuro";
        };
        bloque.getStyleClass().add(colorClass);

        VBox textos = new VBox(1);
        textos.setAlignment(Pos.CENTER_LEFT);

        // Nombre + estrella VIP
        String nombreTexto = reserva.getNombreCliente() + " "
                + reserva.getApellidosCliente();
        if (Boolean.TRUE.equals(reserva.getClienteVip())) {
            nombreTexto = "★ " + nombreTexto;
        }
        Label lblNombre = new Label(nombreTexto);
        lblNombre.getStyleClass().add("bloque-nombre");
        lblNombre.setMaxWidth((span * COL_DIA) - 12.0);

        Label lblPrecio = new Label(reserva.getPrecioNoche() + "€/noche");
        lblPrecio.getStyleClass().add("bloque-precio");

        textos.getChildren().addAll(lblNombre, lblPrecio);
        bloque.getChildren().add(textos);

        // Tooltip con info completa
        String tooltipTexto = String.format(
                "%s %s%s\nHab. %s · %s\n%s → %s\n%.0f€/noche · Total: %.0f€",
                reserva.getNombreCliente(),
                reserva.getApellidosCliente(),
                Boolean.TRUE.equals(reserva.getClienteVip()) ? " ⭐ VIP" : "",
                reserva.getNumeroHabitacion(),
                reserva.getTipoHabitacion(),
                reserva.getFechaEntrada().format(DateTimeFormatter.ofPattern("dd/MM")),
                reserva.getFechaSalida().format(DateTimeFormatter.ofPattern("dd/MM")),
                reserva.getPrecioNoche().doubleValue(),
                reserva.getSubtotal().doubleValue());
        Tooltip tooltip = new Tooltip(tooltipTexto);
        tooltip.setShowDelay(Duration.millis(300));
        Tooltip.install(bloque, tooltip);

        bloque.setOnMouseClicked(e -> abrirDetalle(reserva));

        return bloque;
    }
}