package com.proyectoFoc.controller;

import com.proyectoFoc.FxmlView;
import com.proyectoFoc.StageManager;
import com.proyectoFoc.entity.Habitacion;
import com.proyectoFoc.entity.TipoHabitacion;
import com.proyectoFoc.service.AuthService;
import com.proyectoFoc.service.HabitacionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class HabitacionController {

    @FXML private StackPane modalDetalleHabitacionContainer;
    @FXML private Label detalleNumeroHabitacionLabel;
    @FXML private Label detallePisoHabitacionLabel;
    @FXML private Label detalleTipoHabitacionLabel;
    @FXML private Label detalleCapacidadHabitacionLabel;
    @FXML private Label detalleCamasHabitacionLabel;
    @FXML private Label detallePrecioHabitacionLabel;

    @FXML private FlowPane habitacionesContainer;
    @FXML private Button btnFiltroTodas;
    @FXML private Button btnFiltroDisponibles;
    @FXML private Button btnFiltroOcupadas;
    @FXML private Button btnFiltroLimpieza;
    @FXML private Button btnFiltroMantenimiento;

    @FXML private StackPane modalHabitacionContainer;
    @FXML private Label modalHabitacionTitle;
    @FXML private TextField numeroHabitacionField;
    @FXML private TextField pisoField;
    @FXML private TextField precioField;
    @FXML private ComboBox<String> tipoHabitacionCombo;
    @FXML private ComboBox<String> estadoHabitacionCombo;
    @FXML private CheckBox accesibleCheckBox;
    @FXML private Label errorHabitacionLabel;

    @Autowired private HabitacionService habitacionService;
    @Autowired private StageManager stageManager;
    @Autowired private AuthService authService;

    private List<Habitacion> habitaciones = new ArrayList<>();
    private String filtroActivo = "TODAS";
    private Habitacion habitacionEnEdicion = null;

    @FXML
    public void initialize() {
        configurarCombosFormulario();
        cargarHabitaciones();
    }

    private void configurarCombosFormulario() {
        if (estadoHabitacionCombo != null) {
            estadoHabitacionCombo.setItems(FXCollections.observableArrayList(
                    "DISPONIBLE",
                    "OCUPADA",
                    "LIMPIEZA",
                    "MANTENIMIENTO"
            ));
            estadoHabitacionCombo.setValue("DISPONIBLE");
        }

        if (tipoHabitacionCombo != null) {
            tipoHabitacionCombo.setItems(FXCollections.observableArrayList(
                    "Individual",
                    "Doble Estandar",
                    "Doble superior",
                    "Suite",
                    "Suite Deluxe",
                    "Suite Familiar"
            ));
            tipoHabitacionCombo.setValue("Individual");
        }
    }

    @FXML
    private void abrirModalNuevaHabitacion() {
        habitacionEnEdicion = null;
        modalHabitacionTitle.setText("Nueva Habitación");
        limpiarFormularioHabitacion();
        ocultarErrorHabitacion();
        mostrarModalHabitacion();
    }

    @FXML
    private void cerrarModalHabitacion() {
        ocultarModalHabitacion();
        limpiarFormularioHabitacion();
    }

    @FXML
    private void cerrarModalDetalleHabitacion() {
        if (modalDetalleHabitacionContainer != null) {
            modalDetalleHabitacionContainer.setVisible(false);
            modalDetalleHabitacionContainer.setManaged(false);
        }
    }

    private void mostrarModalDetalleHabitacion() {
        if (modalDetalleHabitacionContainer != null) {
            modalDetalleHabitacionContainer.setVisible(true);
            modalDetalleHabitacionContainer.setManaged(true);
        }
    }

    private void mostrarModalHabitacion() {
        if (modalHabitacionContainer != null) {
            modalHabitacionContainer.setVisible(true);
            modalHabitacionContainer.setManaged(true);
        }
    }

    private void ocultarModalHabitacion() {
        if (modalHabitacionContainer != null) {
            modalHabitacionContainer.setVisible(false);
            modalHabitacionContainer.setManaged(false);
        }
    }

    private void limpiarFormularioHabitacion() {
        if (numeroHabitacionField != null) numeroHabitacionField.clear();
        if (pisoField != null) pisoField.clear();
        if (precioField != null) precioField.clear();
        if (tipoHabitacionCombo != null) tipoHabitacionCombo.setValue("Individual");
        if (estadoHabitacionCombo != null) estadoHabitacionCombo.setValue("DISPONIBLE");
        if (accesibleCheckBox != null) accesibleCheckBox.setSelected(false);
    }

    private void ocultarErrorHabitacion() {
        if (errorHabitacionLabel != null) {
            errorHabitacionLabel.setVisible(false);
            errorHabitacionLabel.setText("");
        }
    }

    private void mostrarErrorHabitacion(String mensaje) {
        if (errorHabitacionLabel != null) {
            errorHabitacionLabel.setText(mensaje);
            errorHabitacionLabel.setVisible(true);
        }
    }

    private boolean validarFormularioHabitacion() {
        ocultarErrorHabitacion();

        if (numeroHabitacionField == null || numeroHabitacionField.getText().trim().isEmpty()) {
            mostrarErrorHabitacion("Debes indicar el número de habitación.");
            return false;
        }

        if (pisoField == null || pisoField.getText().trim().isEmpty()) {
            mostrarErrorHabitacion("Debes indicar el piso.");
            return false;
        }

        if (!pisoField.getText().trim().matches("\\d+")) {
            mostrarErrorHabitacion("El piso debe ser numérico.");
            return false;
        }

        if (precioField == null || precioField.getText().trim().isEmpty()) {
            mostrarErrorHabitacion("Debes indicar el precio.");
            return false;
        }

        try {
            new BigDecimal(precioField.getText().trim());
        } catch (Exception e) {
            mostrarErrorHabitacion("El precio debe ser numérico.");
            return false;
        }

        if (tipoHabitacionCombo == null || tipoHabitacionCombo.getValue() == null) {
            mostrarErrorHabitacion("Debes seleccionar un tipo de habitación.");
            return false;
        }

        if (estadoHabitacionCombo == null || estadoHabitacionCombo.getValue() == null) {
            mostrarErrorHabitacion("Debes seleccionar un estado.");
            return false;
        }

        return true;
    }

    @FXML
    private void guardarHabitacion() {
        if (!validarFormularioHabitacion()) return;

        try {
            String numero = numeroHabitacionField.getText().trim();
            Integer piso = Integer.parseInt(pisoField.getText().trim());
            String tipoNombre = tipoHabitacionCombo.getValue();
            String estado = estadoHabitacionCombo.getValue();
            Boolean accesible = accesibleCheckBox.isSelected();
            BigDecimal precio = new BigDecimal(precioField.getText().trim());

            // BUSCAR el TipoHabitacion existente en la BD
            TipoHabitacion tipo = habitacionService.obtenerTipoPorNombre(tipoNombre)
                    .orElseThrow(() -> new RuntimeException("Tipo de habitación no encontrado: " + tipoNombre));

            // ACTUALIZAR EL PRECIO DEL TIPO
            tipo.setPrecioBase(precio);
            habitacionService.guardarTipoHabitacion(tipo);  // Guardar el tipo con el precio actualizado

            if (habitacionEnEdicion == null) {
                // CREAR NUEVA
                Habitacion nueva = new Habitacion();
                nueva.setNumeroHabitacion(numero);
                nueva.setPiso(piso);
                nueva.setTipoHabitacion(tipo);  // Tipo con precio actualizado
                nueva.setEstadoHabitacion(estado);
                nueva.setAccesible(accesible);

                habitacionService.guardar(nueva);
            } else {
                // EDITAR EXISTENTE
                habitacionEnEdicion.setNumeroHabitacion(numero);
                habitacionEnEdicion.setPiso(piso);
                habitacionEnEdicion.setTipoHabitacion(tipo);  // Tipo con precio actualizado
                habitacionEnEdicion.setEstadoHabitacion(estado);
                habitacionEnEdicion.setAccesible(accesible);

                habitacionService.guardar(habitacionEnEdicion);
            }

            cerrarModalHabitacion();
            limpiarFormularioHabitacion();
            cargarHabitaciones();

        } catch (Exception e) {
            System.err.println("Error al guardar habitación: " + e.getMessage());
            mostrarErrorHabitacion("Error al guardar la habitación: " + e.getMessage());
        }
    }

    @FXML
    private void volverDashboard() {
        stageManager.switchScene(FxmlView.DASHBOARD);
    }

    @FXML
    private void navegarClientes() {
        stageManager.switchScene(FxmlView.CLIENTES);
    }

    @FXML
    private void navegarReservas() {
        stageManager.switchScene(FxmlView.RESERVAS);
    }

    @FXML
    private void navegarEmpleados() {
        stageManager.switchScene(FxmlView.EMPLEADOS);
    }

    @FXML
    private void cerrarSesion() {
        authService.logout();
        stageManager.switchScene(FxmlView.LOGIN);
    }

    @FXML
    private void filtrarTodas() {
        filtroActivo = "TODAS";
        aplicarFiltro();
    }

    @FXML
    private void filtrarDisponibles() {
        filtroActivo = "DISPONIBLE";
        aplicarFiltro();
    }

    @FXML
    private void filtrarOcupadas() {
        filtroActivo = "OCUPADA";
        aplicarFiltro();
    }

    @FXML
    private void filtrarLimpieza() {
        filtroActivo = "LIMPIEZA";
        aplicarFiltro();
    }

    @FXML
    private void filtrarMantenimiento() {
        filtroActivo = "MANTENIMIENTO";
        aplicarFiltro();
    }

    private void cargarHabitaciones() {
        habitaciones = habitacionService.obtenerTodas();
        actualizarContadores();
        aplicarFiltro();
    }

    private void actualizarContadores() {
        Map<String, Long> conteo = habitaciones.stream()
                .collect(Collectors.groupingBy(
                        h -> normalizarEstado(h.getEstadoHabitacion()),
                        Collectors.counting()
                ));

        btnFiltroTodas.setText("Todas (" + habitaciones.size() + ")");
        btnFiltroDisponibles.setText("Disponibles (" + conteo.getOrDefault("DISPONIBLE", 0L) + ")");
        btnFiltroOcupadas.setText("Ocupadas (" + conteo.getOrDefault("OCUPADA", 0L) + ")");
        btnFiltroLimpieza.setText("Limpieza (" + conteo.getOrDefault("LIMPIEZA", 0L) + ")");
        btnFiltroMantenimiento.setText("Mantenimiento (" + conteo.getOrDefault("MANTENIMIENTO", 0L) + ")");
    }

    private void aplicarFiltro() {
        Predicate<Habitacion> filtro = h -> true;

        if (!"TODAS".equals(filtroActivo)) {
            filtro = h -> filtroActivo.equals(normalizarEstado(h.getEstadoHabitacion()));
        }

        List<Habitacion> habitacionesFiltradas = habitaciones.stream()
                .filter(filtro)
                .collect(Collectors.toList());

        renderizarTarjetas(habitacionesFiltradas);
        actualizarEstadoBotones();
    }

    private void editarHabitacion(Habitacion habitacion) {
        if (habitacion == null) return;

        habitacionEnEdicion = habitacion;
        modalHabitacionTitle.setText("Editar Habitación");

        numeroHabitacionField.setText(habitacion.getNumeroHabitacion());
        pisoField.setText(habitacion.getPiso() != null ? habitacion.getPiso().toString() : "");

        if (habitacion.getTipoHabitacion() != null) {
            tipoHabitacionCombo.setValue(habitacion.getTipoHabitacion().getNombre());

            if (habitacion.getTipoHabitacion().getPrecioBase() != null) {
                precioField.setText(habitacion.getTipoHabitacion().getPrecioBase().toString());
            }
        }

        estadoHabitacionCombo.setValue(normalizarEstado(habitacion.getEstadoHabitacion()));
        accesibleCheckBox.setSelected(Boolean.TRUE.equals(habitacion.getAccesible()));

        ocultarErrorHabitacion();
        mostrarModalHabitacion();
    }

    private void renderizarTarjetas(List<Habitacion> habitacionesFiltradas) {
        habitacionesContainer.getChildren().clear();

        if (habitacionesFiltradas.isEmpty()) {
            VBox emptyState = new VBox(10);
            emptyState.getStyleClass().add("empty-state");
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(40));

            Label icono = new Label("🛏️");
            icono.getStyleClass().add("empty-state-icon");

            Label mensaje = new Label("No hay habitaciones para el filtro seleccionado");
            mensaje.getStyleClass().add("empty-state-text");

            emptyState.getChildren().addAll(icono, mensaje);
            habitacionesContainer.getChildren().add(emptyState);
            return;
        }

        for (Habitacion habitacion : habitacionesFiltradas) {
            habitacionesContainer.getChildren().add(crearTarjetaHabitacion(habitacion));
        }
    }

    private VBox crearTarjetaHabitacion(Habitacion habitacion) {
        TipoHabitacion tipo = habitacion.getTipoHabitacion();
        String estado = normalizarEstado(habitacion.getEstadoHabitacion());

        VBox card = new VBox();
        card.getStyleClass().add("room-card");
        card.setPrefWidth(250);
        card.setMinWidth(250);
        card.setMaxWidth(250);

        VBox banner = new VBox(10);
        banner.getStyleClass().add("room-card-banner");
        banner.setAlignment(Pos.TOP_RIGHT);
        banner.setPadding(new Insets(12, 12, 16, 12));

        Label badge = new Label(formatearEstado(estado));
        badge.getStyleClass().addAll("status-badge", obtenerClaseEstado(estado));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label bedIcon = new Label("🛏️");
        bedIcon.getStyleClass().add("room-icon");
        bedIcon.setMaxWidth(Double.MAX_VALUE);
        bedIcon.setAlignment(Pos.CENTER);

        banner.getChildren().addAll(badge, spacer, bedIcon);

        VBox body = new VBox(8);
        body.getStyleClass().add("room-card-body");
        body.setPadding(new Insets(16));

        Label numero = new Label("Hab. " + valorSeguro(habitacion.getNumeroHabitacion()));
        numero.getStyleClass().add("room-number");

        Label piso = new Label("Planta " + (habitacion.getPiso() != null ? habitacion.getPiso() : "-"));
        piso.getStyleClass().add("room-floor");

        Label tipoLabel = new Label(tipo != null ? valorSeguro(tipo.getNombre()) : "Sin tipo");
        tipoLabel.getStyleClass().add("room-type");

        HBox chipsRow1 = new HBox(8,
                crearChip((tipo != null && tipo.getCapacidadPersonas() != null ? tipo.getCapacidadPersonas() : 0) + " personas"),
                crearChip((tipo != null && tipo.getNumCamas() != null ? tipo.getNumCamas() : 0) + " camas")
        );
        chipsRow1.setAlignment(Pos.CENTER_LEFT);

        HBox chipsRow2 = new HBox(8,
                crearChip(tipo != null ? valorSeguro(tipo.getTipoCamas()) : "Sin camas"),
                crearChip(describirExtras(tipo, habitacion))
        );
        chipsRow2.setAlignment(Pos.CENTER_LEFT);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox precioBox = new VBox(2);
        Label precio = new Label(formatearPrecio(tipo != null ? tipo.getPrecioBase() : null));
        precio.getStyleClass().add("room-price");

        Label noche = new Label("por noche");
        noche.getStyleClass().add("room-price-caption");
        precioBox.getChildren().addAll(precio, noche);

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        Button btnVer = new Button("Ver");
        btnVer.setOnAction(e -> mostrarDetalleHabitacion(habitacion));
        btnVer.getStyleClass().addAll("btn-card", "btn-card-info");

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().addAll("btn-card", "btn-card-warning");
        btnEditar.setOnAction(e -> editarHabitacion(habitacion));

        HBox actions = new HBox(8, btnVer, btnEditar);
        actions.setAlignment(Pos.CENTER_RIGHT);

        footer.getChildren().addAll(precioBox, footerSpacer, actions);

        body.getChildren().addAll(numero, piso, tipoLabel, chipsRow1, chipsRow2, footer);
        card.getChildren().addAll(banner, body);
        return card;
    }

    private void mostrarDetalleHabitacion(Habitacion habitacion) {
        if (habitacion == null) return;

        TipoHabitacion tipo = habitacion.getTipoHabitacion();

        detalleNumeroHabitacionLabel.setText(valorSeguro(habitacion.getNumeroHabitacion()));
        detallePisoHabitacionLabel.setText(
                habitacion.getPiso() != null ? habitacion.getPiso().toString() : "-"
        );
        detalleTipoHabitacionLabel.setText(
                tipo != null ? valorSeguro(tipo.getNombre()) : "-"
        );
        detalleCapacidadHabitacionLabel.setText(
                tipo != null && tipo.getCapacidadPersonas() != null
                        ? tipo.getCapacidadPersonas() + " personas"
                        : "-"
        );
        detalleCamasHabitacionLabel.setText(
                tipo != null && tipo.getNumCamas() != null
                        ? tipo.getNumCamas() + " camas"
                        : "-"
        );
        detallePrecioHabitacionLabel.setText(
                formatearPrecio(tipo != null ? tipo.getPrecioBase() : null)
        );

        mostrarModalDetalleHabitacion();
    }

    private Label crearChip(String texto) {
        Label chip = new Label(texto);
        chip.getStyleClass().add("info-chip");
        return chip;
    }

    private void actualizarEstadoBotones() {
        List<Button> botones = List.of(
                btnFiltroTodas,
                btnFiltroDisponibles,
                btnFiltroOcupadas,
                btnFiltroLimpieza,
                btnFiltroMantenimiento
        );

        botones.forEach(btn -> btn.getStyleClass().remove("filter-chip-active"));

        switch (filtroActivo) {
            case "DISPONIBLE" -> btnFiltroDisponibles.getStyleClass().add("filter-chip-active");
            case "OCUPADA" -> btnFiltroOcupadas.getStyleClass().add("filter-chip-active");
            case "LIMPIEZA" -> btnFiltroLimpieza.getStyleClass().add("filter-chip-active");
            case "MANTENIMIENTO" -> btnFiltroMantenimiento.getStyleClass().add("filter-chip-active");
            default -> btnFiltroTodas.getStyleClass().add("filter-chip-active");
        }
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return "DISPONIBLE";
        }

        String valor = estado.trim().toUpperCase(Locale.ROOT);

        if (valor.startsWith("OCUP")) return "OCUPADA";
        if (valor.startsWith("LIMP")) return "LIMPIEZA";
        if (valor.startsWith("MANT")) return "MANTENIMIENTO";
        if (valor.startsWith("FUERA")) return "MANTENIMIENTO";

        return "DISPONIBLE";
    }

    private String formatearEstado(String estado) {
        return switch (estado) {
            case "OCUPADA" -> "OCUPADA";
            case "LIMPIEZA" -> "LIMPIEZA";
            case "MANTENIMIENTO" -> "MANTENIMIENTO";
            default -> "DISPONIBLE";
        };
    }

    private String obtenerClaseEstado(String estado) {
        return switch (estado) {
            case "OCUPADA" -> "status-ocupada";
            case "LIMPIEZA" -> "status-limpieza";
            case "MANTENIMIENTO" -> "status-mantenimiento";
            default -> "status-disponible";
        };
    }

    private String describirExtras(TipoHabitacion tipo, Habitacion habitacion) {
        if (habitacion != null && Boolean.TRUE.equals(habitacion.getAccesible())) {
            return "Accesible";
        }
        if (tipo != null && Boolean.TRUE.equals(tipo.getTieneVistaMar())) {
            return "Vista mar";
        }
        if (tipo != null && Boolean.TRUE.equals(tipo.getTieneBalcon())) {
            return "Balcón";
        }
        return "Vista interior";
    }

    private String formatearPrecio(BigDecimal precio) {
        if (precio == null) {
            return "0€";
        }
        DecimalFormat df = new DecimalFormat("0.##");
        return df.format(precio) + "€";
    }

    private String valorSeguro(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }
}