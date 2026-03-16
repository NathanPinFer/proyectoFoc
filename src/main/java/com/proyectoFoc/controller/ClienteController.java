package com.proyectoFoc.controller;

import com.proyectoFoc.FxmlView;
import com.proyectoFoc.StageManager;
import com.proyectoFoc.dto.ClienteDTO;
import com.proyectoFoc.service.AuthService;
import com.proyectoFoc.service.ClienteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class ClienteController {

    // Estadísticas
    @FXML private Label totalClientesLabel;
    @FXML private Label clientesVIPLabel;
    @FXML private Label clientesRegularesLabel;
    @FXML private Label clientesNuevosHoyLabel;

    // Búsqueda
    @FXML private TextField buscarField;

    // Tabla
    @FXML private TableView<ClienteDTO> clientesTable;
    @FXML private TableColumn<ClienteDTO, String> nombreColumn;
    @FXML private TableColumn<ClienteDTO, String> dniColumn;
    @FXML private TableColumn<ClienteDTO, String> telefonoColumn;
    @FXML private TableColumn<ClienteDTO, String> ciudadColumn;
    @FXML private TableColumn<ClienteDTO, Integer> reservasColumn;
    @FXML private TableColumn<ClienteDTO, String> estadoColumn;
    @FXML private TableColumn<ClienteDTO, Void> accionesColumn;

    // Paginación
    @FXML private HBox paginacionContainer;
    @FXML private VBox tableContainer;
    private int paginaActual = 0;
    private int elementosPorPagina = 10;
    private int totalPaginas = 0;

    // Modal crear/editar
    @FXML private StackPane modalFormContainer;
    @FXML private Label modalFormTitle;
    @FXML private TextField dniField;
    @FXML private TextField nombreField;
    @FXML private TextField apellidosField;
    @FXML private TextField emailField;
    @FXML private TextField telefonoField;
    @FXML private TextField direccionField;
    @FXML private TextField ciudadField;
    @FXML private TextField codigoPostalField;
    @FXML private ComboBox<String> paisCombo;
    @FXML private CheckBox vipCheckBox;
    @FXML private Label errorFormLabel;

    // Modal ver detalles
    @FXML private StackPane modalDetallesContainer;
    @FXML private Label detallesNombreLabel;
    @FXML private Label detallesDniLabel;
    @FXML private Label detallesEmailLabel;
    @FXML private Label detallesTelefonoLabel;
    @FXML private Label detallesDireccionLabel;
    @FXML private Label detallesCiudadLabel;
    @FXML private Label detallesCodigoPostalLabel;
    @FXML private Label detallesPaisLabel;
    @FXML private Label detallesFechaRegistroLabel;
    @FXML private Label detallesEstadoLabel;
    @FXML private Label detallesReservasLabel;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AuthService authService;

    @Autowired
    private StageManager stageManager;

    private ObservableList<ClienteDTO> clientesData = FXCollections.observableArrayList();
    private ClienteDTO clienteEnEdicion = null;

    @FXML
    public void initialize() {
        configurarComboBox();
        configurarTabla();
        cargarClientes();
        actualizarEstadisticas();
    }

    /**
     * Configurar ComboBox de países
     */
    private void configurarComboBox() {
        if (paisCombo != null) {
            paisCombo.getItems().addAll("España", "Francia", "Italia", "Portugal", "Reino Unido", "Alemania", "Países Bajos");
            paisCombo.setValue("España");
        }
    }

    /**
     * Configurar las columnas de la tabla
     */
    private void configurarTabla() {
        // Columna Nombre con avatar e iniciales
        nombreColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombreCompleto())
        );
        nombreColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    ClienteDTO cliente = getTableView().getItems().get(getIndex());

                    // Avatar con iniciales
                    Label avatar = new Label(cliente.getIniciales());
                    avatar.setStyle(
                            "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-pref-width: 40; -fx-pref-height: 40;" +
                                    "-fx-min-width: 40; -fx-min-height: 40;" +
                                    "-fx-max-width: 40; -fx-max-height: 40;" +
                                    "-fx-background-radius: 50%;" +
                                    "-fx-alignment: center;"
                    );

                    // Info del cliente
                    VBox infoBox = new VBox(2);
                    Label nombreLabel = new Label(item);
                    nombreLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                    Label emailLabel = new Label(cliente.getEmail());
                    emailLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");
                    infoBox.getChildren().addAll(nombreLabel, emailLabel);

                    HBox hbox = new HBox(10, avatar, infoBox);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(hbox);
                }
            }
        });

        // Columna DNI
        dniColumn.setCellValueFactory(new PropertyValueFactory<>("dni"));

        // Columna Teléfono
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        // Columna Ciudad
        ciudadColumn.setCellValueFactory(new PropertyValueFactory<>("ciudad"));

        // Columna Reservas
        reservasColumn.setCellValueFactory(new PropertyValueFactory<>("numeroReservas"));

        // Columna Estado (VIP / Regular) - SIN ESTRELLA
        estadoColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getVip() ? "VIP" : "Regular")
        );
        estadoColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if (item.equals("VIP")) {
                        badge.setStyle(
                                "-fx-background-color: linear-gradient(to bottom right, #F39C12, #E67E22);" +
                                        "-fx-text-fill: white;" +
                                        "-fx-padding: 5 12 5 12;" +
                                        "-fx-background-radius: 20;" +
                                        "-fx-font-size: 11px;" +
                                        "-fx-font-weight: bold;"
                        );
                    } else {
                        badge.setStyle(
                                "-fx-background-color: #ECF0F1;" +
                                        "-fx-text-fill: #7F8C8D;" +
                                        "-fx-padding: 5 12 5 12;" +
                                        "-fx-background-radius: 20;" +
                                        "-fx-font-size: 11px;" +
                                        "-fx-font-weight: bold;"
                        );
                    }
                    setGraphic(badge);
                }
            }
        });

        // Columna Acciones (botones Ver, Editar, Eliminar) - CON ICONOS
        accionesColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = crearBotonAccion("ℹ️", "#3498DB", "Ver detalles");
            private final Button btnEditar = crearBotonAccion("✏️", "#F39C12", "Editar");
            private final Button btnEliminar = crearBotonAccion("🗑️", "#E74C3C", "Eliminar");

            {
                btnVer.setOnAction(event -> {
                    ClienteDTO cliente = getTableView().getItems().get(getIndex());
                    abrirModalDetalles(cliente);
                });

                btnEditar.setOnAction(event -> {
                    ClienteDTO cliente = getTableView().getItems().get(getIndex());
                    abrirModalEditar(cliente);
                });

                btnEliminar.setOnAction(event -> {
                    ClienteDTO cliente = getTableView().getItems().get(getIndex());
                    eliminarCliente(cliente);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(8);
                    buttons.setAlignment(Pos.CENTER);
                    buttons.getChildren().addAll(btnVer, btnEditar, btnEliminar);
                    setGraphic(buttons);
                }
            }
        });

        clientesTable.setItems(clientesData);

        // Hacer que la tabla ajuste su altura según el contenido
        clientesTable.setFixedCellSize(60); // Altura de cada fila
    }

    /**
     * Crear botón de acción con estilo e icono
     */
    private Button crearBotonAccion(String icono, String color, String tooltip) {
        Button btn = new Button(icono);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-pref-width: 35; -fx-pref-height: 35;" +
                        "-fx-font-size: 16px;"
        );

        // Tooltip
        Tooltip tip = new Tooltip(tooltip);
        btn.setTooltip(tip);

        // Efecto hover
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-scale-x: 1.1; -fx-scale-y: 1.1;"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle() + "-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        return btn;
    }

    /**
     * Cargar todos los clientes
     */
    private void cargarClientes() {
        cargarClientesPaginados(paginaActual);
    }

    /**
     * Cargar clientes con paginación
     */
    private void cargarClientesPaginados(int pagina) {
        String filtro = buscarField != null ? buscarField.getText().trim() : "";
        Page<ClienteDTO> paginaClientes = clienteService.buscarClientesPaginados(filtro, pagina, elementosPorPagina);

        clientesData.clear();
        clientesData.addAll(paginaClientes.getContent());

        totalPaginas = paginaClientes.getTotalPages();
        paginaActual = pagina;

        // Ajustar altura de la tabla dinámicamente
        ajustarAlturaTabla();

        actualizarBotonesPaginacion();
    }

    /**
     * Ajustar altura de la tabla según el número de elementos
     */
    private void ajustarAlturaTabla() {
        int numFilas = clientesData.size();
        if (numFilas == 0) numFilas = 1; // Mínimo 1 fila para mostrar "Sin datos"

        // Calcular altura: cabecera (40px) + filas (60px cada una) + padding
        double alturaCalculada = 40 + (numFilas * 60) + 10;

        // Altura máxima para no ocupar toda la pantalla
        double alturaMaxima = 700; // Ajusta según tu diseño

        double alturaFinal = Math.min(alturaCalculada, alturaMaxima);

        clientesTable.setPrefHeight(alturaFinal);
        clientesTable.setMaxHeight(alturaFinal);
    }

    /**
     * Actualizar botones de paginación
     */
    private void actualizarBotonesPaginacion() {
        if (paginacionContainer == null) return;

        paginacionContainer.getChildren().clear();

        // Si solo hay una página o ningún resultado, ocultar paginación
        if (totalPaginas <= 1) {
            paginacionContainer.setVisible(false);
            paginacionContainer.setManaged(false);
            return;
        }

        paginacionContainer.setVisible(true);
        paginacionContainer.setManaged(true);

        // Botón anterior
        Button btnAnterior = new Button("◀");
        btnAnterior.setDisable(paginaActual == 0);
        btnAnterior.setOnAction(e -> cargarClientesPaginados(paginaActual - 1));
        btnAnterior.getStyleClass().add("page-btn");
        paginacionContainer.getChildren().add(btnAnterior);

        // Botones de páginas
        int inicio = Math.max(0, paginaActual - 2);
        int fin = Math.min(totalPaginas, paginaActual + 3);

        for (int i = inicio; i < fin; i++) {
            final int pagina = i;
            Button btnPagina = new Button(String.valueOf(i + 1));
            btnPagina.getStyleClass().add("page-btn");
            if (i == paginaActual) {
                btnPagina.getStyleClass().add("active");
            }
            btnPagina.setOnAction(e -> cargarClientesPaginados(pagina));
            paginacionContainer.getChildren().add(btnPagina);
        }

        // Botón siguiente
        Button btnSiguiente = new Button("▶");
        btnSiguiente.setDisable(paginaActual >= totalPaginas - 1);
        btnSiguiente.setOnAction(e -> cargarClientesPaginados(paginaActual + 1));
        btnSiguiente.getStyleClass().add("page-btn");
        paginacionContainer.getChildren().add(btnSiguiente);
    }

    /**
     * Actualizar estadísticas
     */
    private void actualizarEstadisticas() {
        long total = clienteService.contarClientes();
        long vips = clienteService.contarClientesVIP();
        long regulares = clienteService.contarClientesRegulares();
        long nuevosHoy = clienteService.contarClientesNuevosHoy();

        if (totalClientesLabel != null) totalClientesLabel.setText(String.valueOf(total));
        if (clientesVIPLabel != null) clientesVIPLabel.setText(String.valueOf(vips));
        if (clientesRegularesLabel != null) clientesRegularesLabel.setText(String.valueOf(regulares));
        if (clientesNuevosHoyLabel != null) clientesNuevosHoyLabel.setText(String.valueOf(nuevosHoy));
    }

    /**
     * Buscar clientes
     */
    @FXML
    private void buscarClientes() {
        paginaActual = 0;
        cargarClientes();
    }

    /**
     * Abrir modal para crear nuevo cliente
     */
    @FXML
    private void abrirModalNuevo() {
        clienteEnEdicion = null;
        if (modalFormTitle != null) modalFormTitle.setText("Nuevo Cliente");
        limpiarFormulario();
        if (errorFormLabel != null) errorFormLabel.setVisible(false);
        mostrarModal(modalFormContainer);
    }

    /**
     * Abrir modal para editar cliente
     */
    private void abrirModalEditar(ClienteDTO cliente) {
        clienteEnEdicion = cliente;
        if (modalFormTitle != null) modalFormTitle.setText("Editar Cliente");
        cargarDatosEnFormulario(cliente);
        if (errorFormLabel != null) errorFormLabel.setVisible(false);
        mostrarModal(modalFormContainer);
    }

    /**
     * Abrir modal de detalles del cliente
     */
    private void abrirModalDetalles(ClienteDTO cliente) {
        if (modalDetallesContainer == null) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (detallesNombreLabel != null) detallesNombreLabel.setText(cliente.getNombreCompleto());
        if (detallesDniLabel != null) detallesDniLabel.setText(cliente.getDni());
        if (detallesEmailLabel != null) detallesEmailLabel.setText(cliente.getEmail());
        if (detallesTelefonoLabel != null) detallesTelefonoLabel.setText(cliente.getTelefono());
        if (detallesDireccionLabel != null) detallesDireccionLabel.setText(cliente.getDireccion() != null ? cliente.getDireccion() : "No especificada");
        if (detallesCiudadLabel != null) detallesCiudadLabel.setText(cliente.getCiudad() != null ? cliente.getCiudad() : "-");
        if (detallesCodigoPostalLabel != null) detallesCodigoPostalLabel.setText(cliente.getCodigoPostal() != null ? cliente.getCodigoPostal() : "-");
        if (detallesPaisLabel != null) detallesPaisLabel.setText(cliente.getPais() != null ? cliente.getPais() : "-");
        if (detallesFechaRegistroLabel != null) detallesFechaRegistroLabel.setText(cliente.getFechaRegistro() != null ? cliente.getFechaRegistro().format(formatter) : "-");
        if (detallesEstadoLabel != null) detallesEstadoLabel.setText(cliente.getVip() ? "VIP" : "Regular");
        if (detallesReservasLabel != null) detallesReservasLabel.setText(String.valueOf(cliente.getNumeroReservas()));

        mostrarModal(modalDetallesContainer);
    }

    /**
     * Guardar cliente (crear o actualizar) - SIN ALERTA DE ÉXITO
     */
    @FXML
    private void guardarCliente() {
        if (errorFormLabel != null) errorFormLabel.setVisible(false);

        // Validar formulario
        if (!validarFormulario()) {
            return;
        }

        try {
            // Crear DTO con los datos del formulario
            ClienteDTO clienteDTO = new ClienteDTO();
            clienteDTO.setDni(dniField.getText().trim());
            clienteDTO.setNombre(nombreField.getText().trim());
            clienteDTO.setApellidos(apellidosField.getText().trim());
            clienteDTO.setEmail(emailField.getText().trim());
            clienteDTO.setTelefono(telefonoField.getText().trim());
            clienteDTO.setDireccion(direccionField.getText().trim());
            clienteDTO.setCiudad(ciudadField.getText().trim());
            clienteDTO.setCodigoPostal(codigoPostalField.getText().trim());
            clienteDTO.setPais(paisCombo.getValue());
            clienteDTO.setVip(vipCheckBox.isSelected());

            if (clienteEnEdicion == null) {
                // Crear nuevo
                clienteService.crearCliente(clienteDTO);
            } else {
                // Actualizar existente
                clienteService.actualizarCliente(clienteEnEdicion.getIdCliente(), clienteDTO);
            }

            // Cerrar modal y recargar sin mostrar alerta
            cerrarModal(modalFormContainer);
            cargarClientes();
            actualizarEstadisticas();

        } catch (Exception e) {
            mostrarErrorModal(e.getMessage());
        }
    }

    /**
     * Eliminar cliente
     */
    private void eliminarCliente(ClienteDTO cliente) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar cliente?");
        confirmacion.setContentText("¿Está seguro de eliminar a " + cliente.getNombreCompleto() + "?\nEsta acción no se puede deshacer.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                clienteService.eliminarCliente(cliente.getIdCliente());
                // Recargar sin mostrar alerta de éxito
                cargarClientes();
                actualizarEstadisticas();
            } catch (Exception e) {
                mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Validar formulario
     */
    private boolean validarFormulario() {
        if (dniField.getText().trim().isEmpty() ||
                nombreField.getText().trim().isEmpty() ||
                apellidosField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                telefonoField.getText().trim().isEmpty()) {

            mostrarErrorModal("Por favor, completa todos los campos obligatorios (*)");
            return false;
        }

        // Validar formato de email
        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            mostrarErrorModal("El formato del email no es válido");
            return false;
        }

        // Validar DNI
        String dni = dniField.getText().trim();
        if (!dni.matches("^\\d{8}[a-zA-Z]$")) {
            mostrarErrorModal("El DNI debe contener exactamente 8 números y 1 letra.");
            return false;
        }

        // Validar Teléfono
        String telefono = telefonoField.getText().trim();
        if (!telefono.matches("^\\d{9}$")) {
            mostrarErrorModal("El teléfono debe contener exactamente 9 dígitos.");
            return false;
        }

        return true;
    }

    /**
     * Cargar datos en el formulario
     */
    private void cargarDatosEnFormulario(ClienteDTO cliente) {
        dniField.setText(cliente.getDni());
        nombreField.setText(cliente.getNombre());
        apellidosField.setText(cliente.getApellidos());
        emailField.setText(cliente.getEmail());
        telefonoField.setText(cliente.getTelefono());
        direccionField.setText(cliente.getDireccion() != null ? cliente.getDireccion() : "");
        ciudadField.setText(cliente.getCiudad() != null ? cliente.getCiudad() : "");
        codigoPostalField.setText(cliente.getCodigoPostal() != null ? cliente.getCodigoPostal() : "");
        paisCombo.setValue(cliente.getPais() != null ? cliente.getPais() : "España");
        vipCheckBox.setSelected(cliente.getVip());
    }

    /**
     * Limpiar formulario
     */
    private void limpiarFormulario() {
        dniField.clear();
        nombreField.clear();
        apellidosField.clear();
        emailField.clear();
        telefonoField.clear();
        direccionField.clear();
        ciudadField.clear();
        codigoPostalField.clear();
        paisCombo.setValue("España");
        vipCheckBox.setSelected(false);
    }

    /**
     * Mostrar modal
     */
    private void mostrarModal(StackPane modal) {
        if (modal != null) {
            modal.setVisible(true);
            modal.setManaged(true);
        }
    }

    /**
     * Cerrar modal
     */
    @FXML
    private void cerrarModalForm() {
        cerrarModal(modalFormContainer);
        limpiarFormulario();
    }

    @FXML
    private void cerrarModalDetalles() {
        cerrarModal(modalDetallesContainer);
    }

    private void cerrarModal(StackPane modal) {
        if (modal != null) {
            modal.setVisible(false);
            modal.setManaged(false);
        }
    }

    /**
     * Mostrar error en el modal
     */
    private void mostrarErrorModal(String mensaje) {
        if (errorFormLabel != null) {
            errorFormLabel.setText(mensaje);
            errorFormLabel.setVisible(true);
        }
    }

    /**
     * Mostrar alerta (solo para errores)
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Volver al dashboard
     */
    @FXML
    private void volverDashboard() {
        stageManager.switchScene(FxmlView.DASHBOARD);
    }

    /**
     * Cerrar sesión
     */
    @FXML
    private void cerrarSesion() {
        authService.logout();
        stageManager.switchScene(FxmlView.LOGIN);
    }
}