package com.proyectoFoc.controller;

import com.proyectoFoc.FxmlView;
import com.proyectoFoc.StageManager;
import com.proyectoFoc.dto.ClienteDTO;
import com.proyectoFoc.entity.Empleado;
import com.proyectoFoc.service.AuthService;
import com.proyectoFoc.service.ClienteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
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

    @FXML
    private Label totalClientesLabel;
    @FXML
    private Label clientesVIPLabel;
    @FXML
    private Label clientesRegularesLabel;
    @FXML
    private Label clientesNuevosHoyLabel;
    @FXML
    private TextField buscarField;
    @FXML
    private TableView<ClienteDTO> clientesTable;
    @FXML
    private TableColumn<ClienteDTO, String> nombreColumn;
    @FXML
    private TableColumn<ClienteDTO, String> dniColumn;
    @FXML
    private TableColumn<ClienteDTO, String> telefonoColumn;
    @FXML
    private TableColumn<ClienteDTO, String> ciudadColumn;
    @FXML
    private TableColumn<ClienteDTO, Integer> reservasColumn;
    @FXML
    private TableColumn<ClienteDTO, String> estadoColumn;
    @FXML
    private TableColumn<ClienteDTO, Void> accionesColumn;
    @FXML
    private HBox paginacionContainer;
    @FXML
    private VBox tableContainer;
    private int paginaActual = 0;
    private int elementosPorPagina = 10;
    private int totalPaginas = 0;
    @FXML
    private StackPane modalFormContainer;
    @FXML
    private Label modalFormTitle;
    @FXML
    private TextField dniField;
    @FXML
    private TextField nombreField;
    @FXML
    private TextField apellidosField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telefonoField;
    @FXML
    private TextField direccionField;
    @FXML
    private TextField ciudadField;
    @FXML
    private TextField codigoPostalField;
    @FXML
    private ComboBox<String> paisCombo;
    @FXML
    private CheckBox vipCheckBox;
    @FXML
    private Label errorFormLabel;
    @FXML
    private StackPane modalDetallesContainer;
    @FXML
    private Label detallesNombreLabel;
    @FXML
    private Label detallesDniLabel;
    @FXML
    private Label detallesEmailLabel;
    @FXML
    private Label detallesTelefonoLabel;
    @FXML
    private Label detallesDireccionLabel;
    @FXML
    private Label detallesCiudadLabel;
    @FXML
    private Label detallesCodigoPostalLabel;
    @FXML
    private Label detallesPaisLabel;
    @FXML
    private Label detallesFechaRegistroLabel;
    @FXML
    private Label detallesEstadoLabel;
    @FXML
    private Label detallesReservasLabel;

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
        configurarDobleClick();
        cargarClientes();
        actualizarEstadisticas();
    }

    private void configurarComboBox() {
        if (paisCombo != null) {
            paisCombo.getItems().addAll("España", "Francia", "Italia", "Portugal", "Reino Unido", "Alemania", "Países Bajos");
            paisCombo.setValue("España");
        }
    }

    private void configurarDobleClick() {
        clientesTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                ClienteDTO clienteSeleccionado = clientesTable.getSelectionModel().getSelectedItem();
                if (clienteSeleccionado != null) {
                    abrirModalDetalles(clienteSeleccionado);
                }
            }
        });
    }

    private void configurarTabla() {
        clientesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Columna Nombre
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

                    VBox infoBox = new VBox(2);
                    Label nombreLabel = new Label(item);
                    nombreLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                    Label emailLabel = new Label(cliente.getEmail());
                    emailLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");
                    infoBox.getChildren().addAll(nombreLabel, emailLabel);

                    HBox hbox = new HBox(10, avatar, infoBox);
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    HBox container = new HBox(hbox);
                    container.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(container);
                }
            }
        });

        // Columna DNI
        dniColumn.setCellValueFactory(new PropertyValueFactory<>("dni"));
        dniColumn.setCellFactory(column -> new TableCell<ClienteDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    label.setStyle("-fx-font-size: 14px;");

                    HBox container = new HBox(label);
                    container.setAlignment(Pos.CENTER);  // CENTRADO
                    setGraphic(container);
                }
            }
        });

        // Columna Teléfono
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        telefonoColumn.setCellFactory(column -> new TableCell<ClienteDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    label.setStyle("-fx-font-size: 14px;");

                    HBox container = new HBox(label);
                    container.setAlignment(Pos.CENTER);  // CENTRADO
                    setGraphic(container);
                }
            }
        });

        // Columna Ciudad
        ciudadColumn.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        ciudadColumn.setCellFactory(column -> new TableCell<ClienteDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    label.setStyle("-fx-font-size: 14px;");

                    HBox container = new HBox(label);
                    container.setAlignment(Pos.CENTER);  // CENTRADO
                    setGraphic(container);
                }
            }
        });

        // Columna Reservas
        reservasColumn.setCellValueFactory(new PropertyValueFactory<>("numeroReservas"));
        reservasColumn.setCellFactory(column -> new TableCell<ClienteDTO, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(String.valueOf(item));
                    label.setStyle("-fx-font-size: 14px;");

                    HBox container = new HBox(label);
                    container.setAlignment(Pos.CENTER);  // CENTRADO
                    setGraphic(container);
                }
            }
        });

        // Columna Estado
        estadoColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEstadoTexto())
        );
        estadoColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if (item.contains("VIP")) {
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
                    HBox container = new HBox(badge);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });

        // Columna Acciones
        accionesColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = crearBotonAccion("VER", "#3498DB");
            private final Button btnEditar = crearBotonAccion("EDIT", "#F39C12");
            private final Button btnEliminar = crearBotonAccion("DEL", "#E74C3C");

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
        clientesTable.setFixedCellSize(60);
    }

    private Button crearBotonAccion(String texto, String color) {
        Button btn = new Button(texto);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-pref-width: 45; -fx-pref-height: 35;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;"
        );
        btn.setOnMouseEntered(e -> {
            btn.setScaleX(1.1);
            btn.setScaleY(1.1);
        });
        btn.setOnMouseExited(e -> {
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
        });
        return btn;
    }

    private void cargarClientes() {
        cargarClientesPaginados(paginaActual);
    }

    private void cargarClientesPaginados(int pagina) {
        String filtro = buscarField != null ? buscarField.getText().trim() : "";
        Page<ClienteDTO> paginaClientes = clienteService.buscarClientesPaginados(filtro, pagina, elementosPorPagina);

        clientesData.clear();
        clientesData.addAll(paginaClientes.getContent());

        totalPaginas = paginaClientes.getTotalPages();
        paginaActual = pagina;

        ajustarAlturaTabla();
        actualizarBotonesPaginacion();
    }

    private void ajustarAlturaTabla() {
        int numFilas = clientesData.size();
        if (numFilas == 0) numFilas = 1;

        double alturaCalculada = 40 + (numFilas * 60) + 10;
        double alturaMaxima = 700;
        double alturaFinal = Math.min(alturaCalculada, alturaMaxima);

        clientesTable.setPrefHeight(alturaFinal);
        clientesTable.setMaxHeight(alturaFinal);
    }

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

        Button btnAnterior = new Button("◀");
        btnAnterior.setDisable(paginaActual == 0);
        btnAnterior.setOnAction(e -> cargarClientesPaginados(paginaActual - 1));
        btnAnterior.getStyleClass().add("page-btn");
        paginacionContainer.getChildren().add(btnAnterior);

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

        Button btnSiguiente = new Button("▶");
        btnSiguiente.setDisable(paginaActual >= totalPaginas - 1);
        btnSiguiente.setOnAction(e -> cargarClientesPaginados(paginaActual + 1));
        btnSiguiente.getStyleClass().add("page-btn");
        paginacionContainer.getChildren().add(btnSiguiente);
    }

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

    @FXML
    private void buscarClientes() {
        paginaActual = 0;
        cargarClientes();
    }

    @FXML
    private void abrirModalNuevo() {
        clienteEnEdicion = null;
        if (modalFormTitle != null) modalFormTitle.setText("Nuevo Cliente");
        limpiarFormulario();
        if (errorFormLabel != null) errorFormLabel.setVisible(false);
        mostrarModal(modalFormContainer);
    }

    private void abrirModalEditar(ClienteDTO cliente) {
        clienteEnEdicion = cliente;
        if (modalFormTitle != null) modalFormTitle.setText("Editar Cliente");
        cargarDatosEnFormulario(cliente);
        if (errorFormLabel != null) errorFormLabel.setVisible(false);
        mostrarModal(modalFormContainer);
    }

    private void abrirModalDetalles(ClienteDTO cliente) {
        if (modalDetallesContainer == null) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (detallesNombreLabel != null) detallesNombreLabel.setText(cliente.getNombreCompleto());
        if (detallesDniLabel != null) detallesDniLabel.setText(cliente.getDni());
        if (detallesEmailLabel != null) detallesEmailLabel.setText(cliente.getEmail());
        if (detallesTelefonoLabel != null) detallesTelefonoLabel.setText(cliente.getTelefono());
        if (detallesDireccionLabel != null)
            detallesDireccionLabel.setText(cliente.getDireccion() != null ? cliente.getDireccion() : "No especificada");
        if (detallesCiudadLabel != null)
            detallesCiudadLabel.setText(cliente.getCiudad() != null ? cliente.getCiudad() : "-");
        if (detallesCodigoPostalLabel != null)
            detallesCodigoPostalLabel.setText(cliente.getCodigoPostal() != null ? cliente.getCodigoPostal() : "-");
        if (detallesPaisLabel != null) detallesPaisLabel.setText(cliente.getPais() != null ? cliente.getPais() : "-");
        if (detallesFechaRegistroLabel != null)
            detallesFechaRegistroLabel.setText(cliente.getFechaRegistro() != null ? cliente.getFechaRegistro().format(formatter) : "-");
        if (detallesEstadoLabel != null) detallesEstadoLabel.setText(cliente.getVip() ? "VIP" : "Regular");
        if (detallesReservasLabel != null) detallesReservasLabel.setText(String.valueOf(cliente.getNumeroReservas()));

        mostrarModal(modalDetallesContainer);
    }

    @FXML
    private void guardarCliente() {
        if (errorFormLabel != null) errorFormLabel.setVisible(false);

        if (!validarFormulario()) {
            return;
        }

        try {
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
                clienteService.crearCliente(clienteDTO);
            } else {
                clienteService.actualizarCliente(clienteEnEdicion.getIdCliente(), clienteDTO);
            }

            cerrarModal(modalFormContainer);
            cargarClientes();
            actualizarEstadisticas();

        } catch (Exception e) {
            mostrarErrorModal(e.getMessage());
        }
    }

    private void eliminarCliente(ClienteDTO cliente) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar cliente?");
        confirmacion.setContentText("¿Está seguro de eliminar a " + cliente.getNombreCompleto() + "?\nEsta acción no se puede deshacer.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                clienteService.eliminarCliente(cliente.getIdCliente());
                cargarClientes();
                actualizarEstadisticas();
            } catch (Exception e) {
                mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validarFormulario() {
        if (dniField.getText().trim().isEmpty() ||
                nombreField.getText().trim().isEmpty() ||
                apellidosField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                telefonoField.getText().trim().isEmpty()) {

            mostrarErrorModal("Por favor, completa todos los campos obligatorios (*)");
            return false;
        }

        //Validar gmail
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

    private void mostrarModal(StackPane modal) {
        if (modal != null) {
            modal.setVisible(true);
            modal.setManaged(true);
        }
    }

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

    private void mostrarErrorModal(String mensaje) {
        if (errorFormLabel != null) {
            errorFormLabel.setText(mensaje);
            errorFormLabel.setVisible(true);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void volverDashboard() {
        stageManager.switchScene(FxmlView.DASHBOARD);
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
}
