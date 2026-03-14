package com.hotel.gestion_hotelera.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotel.gestion_hotelera.dto.ReservaCalendarioDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
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

@Controller // <- este es de Spring: org.springframework.stereotype.Controller
public class DashboardController {

    @FXML
    private GridPane gridCalendario;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label lblMesAnio;

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

                System.out.println("Reservas recibidas: " + reservas.size());
                for (ReservaCalendarioDTO r : reservas) {
                    System.out.println("  -> Hab " + r.getNumeroHabitacion()
                            + " | " + r.getFechaEntrada()
                            + " | idHab=" + r.getIdHabitacion());
                }

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

        return bloque;
    }
}