package com.proyectoFoc.controller;

import com.proyectoFoc.FxmlView;
import com.proyectoFoc.StageManager;
import com.proyectoFoc.dto.EmpleadoDTO;
import com.proyectoFoc.service.AuthService;
import com.proyectoFoc.service.EmpleadoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Component
public class EmpleadosController {

    // =============================================
    // COMPONENTES FXML
    // =============================================
    
    @FXML private TableView<EmpleadoDTO> empleadosTable;
    @FXML private TableColumn<EmpleadoDTO, String> nombreColumn;
    @FXML private TableColumn<EmpleadoDTO, String> dniColumn;
    @FXML private TableColumn<EmpleadoDTO, String> emailColumn;
    @FXML private TableColumn<EmpleadoDTO, String> usuarioColumn;
    @FXML private TableColumn<EmpleadoDTO, String> cargoColumn;
    @FXML private TableColumn<EmpleadoDTO, String> fechaColumn;
    @FXML private TableColumn<EmpleadoDTO, String> estadoColumn;
    @FXML private TableColumn<EmpleadoDTO, Void> accionesColumn;
    
    @FXML private TextField buscarField;
    @FXML private Label totalEmpleadosLabel;
    @FXML private Label gerentesLabel;
    @FXML private Label recepcionistasLabel;
    @FXML private Label mantenimientoLabel;
    @FXML private Label activosLabel;
    @FXML private HBox paginacionContainer;
    
    // Modal Formulario
    @FXML private StackPane modalFormContainer;
    @FXML private Label modalFormTitle;
    @FXML private TextField nombreField;
    @FXML private TextField apellidosField;
    @FXML private TextField dniField;
    @FXML private TextField telefonoField;
    @FXML private TextField emailField;
    @FXML private TextField usuarioField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> cargoCombo;
    @FXML private TextField salarioField;
    @FXML private VBox passwordContainer;
    @FXML private CheckBox activoCheckBox;  // Nuevo: gestionar activo/inactivo
    @FXML private Label errorFormLabel;
    
    // Modal Detalles
    @FXML private StackPane modalDetallesContainer;
    @FXML private Label detallesNombreLabel;
    @FXML private Label detallesDniLabel;
    @FXML private Label detallesEmailLabel;
    @FXML private Label detallesTelefonoLabel;
    @FXML private Label detallesUsuarioLabel;
    @FXML private Label detallesCargoLabel;
    @FXML private Label detallesSalarioLabel;
    @FXML private Label detallesFechaLabel;
    @FXML private Label detallesEstadoLabel;
    
    // Modal Mostrar Password
    @FXML private StackPane modalPasswordContainer;
    @FXML private Label passwordMostradaLabel;

    // SERVICIOS
    
    @Autowired
    private StageManager stageManager;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private EmpleadoService empleadoService;

    // VARIABLES
    
    private int paginaActual = 0;
    private final int ITEMS_POR_PAGINA = 10;
    private EmpleadoDTO empleadoEditando = null;
    private String passwordTemporalGenerada = null;

    // INICIALIZACIÓN
    
    @FXML
    public void initialize() {
        configurarTabla();
        configurarComboBoxes();
        cargarEmpleados(0);
        cargarEstadisticas();
    }

    // CONFIGURACIÓN DE TABLA
    
    private void configurarTabla() {
        // Columna EMPLEADO con avatar circular y nombre (NO centrada)
        nombreColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNombreCompleto())
        );
        nombreColumn.setCellFactory(column -> new TableCell<EmpleadoDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    EmpleadoDTO empleado = getTableView().getItems().get(getIndex());
                    
                    // Crear avatar circular con iniciales
                    String iniciales = obtenerIniciales(empleado.getNombre(), empleado.getApellidos());
                    String color = generarColorPorNombre(empleado.getNombre());
                    
                    Label avatar = new Label(iniciales);
                    avatar.setStyle(
                        "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-min-width: 40px;" +
                        "-fx-min-height: 40px;" +
                        "-fx-max-width: 40px;" +
                        "-fx-max-height: 40px;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-alignment: center;"
                    );
                    
                    // VBox con nombre y email
                    Label lblNombre = new Label(item);
                    lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    
                    Label lblEmail = new Label(empleado.getEmail());
                    lblEmail.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");
                    
                    VBox info = new VBox(2, lblNombre, lblEmail);
                    
                    HBox cell = new HBox(12, avatar, info);
                    cell.setAlignment(Pos.CENTER_LEFT);
                    
                    setGraphic(cell);
                    setText(null);
                }
            }
        });
        
        // DNI
        dniColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDni())
        );
        dniColumn.setStyle("-fx-alignment: CENTER;");
        
        // EMAIL
        emailColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEmail())
        );
        emailColumn.setStyle("-fx-alignment: CENTER;");
        
        // USUARIO
        usuarioColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsuario())
        );
        usuarioColumn.setStyle("-fx-alignment: CENTER;");
        
        // CARGO
        cargoColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCargo())
        );
        cargoColumn.setStyle("-fx-alignment: CENTER;");
        
        // FECHA CONTRATACIÓN
        fechaColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaContratacion() != null) {
                return new SimpleStringProperty(
                    cellData.getValue().getFechaContratacion()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new SimpleStringProperty("-");
        });
        fechaColumn.setStyle("-fx-alignment: CENTER;");
        
        // ESTADO
        estadoColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEstadoTexto())
        );
        estadoColumn.setCellFactory(column -> new TableCell<EmpleadoDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle(
                        "-fx-background-color: " + (item.equals("Activo") ? "#27AE60" : "#95A5A6") + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 5 12 5 12;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
                    );
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        
        // ACCIONES
        accionesColumn.setCellFactory(column -> new TableCell<EmpleadoDTO, Void>() {
            private final Button btnVer = crearBotonAccionTexto("VER", "#3498DB");
            private final Button btnEditar = crearBotonAccionTexto("EDIT", "#F39C12");
            private final Button btnEliminar = crearBotonAccionTexto("DEL", "#E74C3C");
            
            {
                btnVer.setOnAction(e -> {
                    EmpleadoDTO empleado = getTableView().getItems().get(getIndex());
                    verDetalleEmpleado(empleado);
                });
                
                btnEditar.setOnAction(e -> {
                    EmpleadoDTO empleado = getTableView().getItems().get(getIndex());
                    abrirModalEditar(empleado);
                });
                
                btnEliminar.setOnAction(e -> {
                    EmpleadoDTO empleado = getTableView().getItems().get(getIndex());
                    eliminarEmpleado(empleado);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox botones = new HBox(8, btnVer, btnEditar, btnEliminar);
                    botones.setAlignment(Pos.CENTER);
                    setGraphic(botones);
                }
            }
        });
    }
    
    /**
     * Crear botón de acción con TEXTO (como en Clientes)
     */
    private Button crearBotonAccionTexto(String texto, String color) {
        Button btn = new Button(texto);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-background-radius: 8px;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );
        btn.setOnMouseEntered(e -> {
            btn.setStyle(btn.getStyle() + "-fx-translate-y: -2px;");
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-padding: 8 16 8 16;" +
                "-fx-background-radius: 8px;" +
                "-fx-cursor: hand;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;"
            );
        });
        return btn;
    }
    
    /**
     * Obtener iniciales del nombre
     */
    private String obtenerIniciales(String nombre, String apellidos) {
        String inicialNombre = nombre != null && !nombre.isEmpty() ? nombre.substring(0, 1).toUpperCase() : "";
        String inicialApellido = apellidos != null && !apellidos.isEmpty() ? apellidos.substring(0, 1).toUpperCase() : "";
        return inicialNombre + inicialApellido;
    }
    
    /**
     * Generar color consistente basado en el nombre
     */
    private String generarColorPorNombre(String nombre) {
        String[] colores = {
            "#9B59B6", // Púrpura
            "#3498DB", // Azul
            "#1ABC9C", // Turquesa
            "#F39C12", // Naranja
            "#E74C3C", // Rojo
            "#2ECC71", // Verde
            "#34495E", // Gris oscuro
            "#16A085", // Verde azulado
            "#8E44AD", // Púrpura oscuro
            "#2980B9"  // Azul oscuro
        };
        
        int hash = nombre.hashCode();
        int index = Math.abs(hash) % colores.length;
        return colores[index];
    }

    private void configurarComboBoxes() {
        cargoCombo.getItems().addAll("Gerente", "Recepcionista", "Mantenimiento");
    }

    // CARGAR DATOS
    
    private void cargarEmpleados(int pagina) {
        try {
            Page<EmpleadoDTO> page = empleadoService.buscarEmpleados(
                buscarField.getText(),
                PageRequest.of(pagina, ITEMS_POR_PAGINA)
            );
            
            empleadosTable.getItems().setAll(page.getContent());
            paginaActual = pagina;
            actualizarPaginacion(page);
            
        } catch (Exception e) {
            mostrarError("Error al cargar empleados", e.getMessage());
        }
    }
    
    private void cargarEstadisticas() {
        try {
            totalEmpleadosLabel.setText(String.valueOf(empleadoService.contarTotal()));
            gerentesLabel.setText(String.valueOf(empleadoService.contarActivosPorCargo("Gerente")));
            recepcionistasLabel.setText(String.valueOf(empleadoService.contarActivosPorCargo("Recepcionista")));
            mantenimientoLabel.setText(String.valueOf(empleadoService.contarActivosPorCargo("Mantenimiento")));
            activosLabel.setText(String.valueOf(empleadoService.contarActivos()));
        } catch (Exception e) {
            System.err.println("Error al cargar estadísticas: " + e.getMessage());
        }
    }

    // PAGINACIÓN
    
    private void actualizarPaginacion(Page<EmpleadoDTO> page) {
        paginacionContainer.getChildren().clear();
        
        if (page.getTotalPages() <= 1) return;
        
        // Botón anterior
        Button btnAnterior = new Button("◀");
        btnAnterior.getStyleClass().add("page-btn");
        btnAnterior.setDisable(page.getNumber() == 0);
        btnAnterior.setOnAction(e -> cargarEmpleados(page.getNumber() - 1));
        paginacionContainer.getChildren().add(btnAnterior);
        
        // Números de página
        int inicio = Math.max(0, page.getNumber() - 2);
        int fin = Math.min(page.getTotalPages() - 1, page.getNumber() + 2);
        
        for (int i = inicio; i <= fin; i++) {
            final int pageNum = i;
            Button btnPagina = new Button(String.valueOf(i + 1));
            btnPagina.getStyleClass().add("page-btn");
            if (i == page.getNumber()) {
                btnPagina.getStyleClass().add("active");
            }
            btnPagina.setOnAction(e -> cargarEmpleados(pageNum));
            paginacionContainer.getChildren().add(btnPagina);
        }
        
        // Botón siguiente
        Button btnSiguiente = new Button("▶");
        btnSiguiente.getStyleClass().add("page-btn");
        btnSiguiente.setDisable(page.getNumber() == page.getTotalPages() - 1);
        btnSiguiente.setOnAction(e -> cargarEmpleados(page.getNumber() + 1));
        paginacionContainer.getChildren().add(btnSiguiente);
    }

    // BÚSQUEDA
    
    @FXML
    private void buscarEmpleados() {
        cargarEmpleados(0);
    }

    // MODAL CREAR/EDITAR

    @FXML
    private void abrirModalNuevo() {
        empleadoEditando = null;
        limpiarFormulario();
        modalFormTitle.setText("Nuevo Empleado");
        passwordContainer.setVisible(true);
        passwordContainer.setManaged(true);
        
        // Ocultar checkbox activo al crear (siempre será TRUE por defecto)
        activoCheckBox.setVisible(false);
        activoCheckBox.setManaged(false);
        
        modalFormContainer.setVisible(true);
        modalFormContainer.setManaged(true);
    }
    
    private void abrirModalEditar(EmpleadoDTO empleado) {
        empleadoEditando = empleado;
        cargarDatosFormulario(empleado);
        modalFormTitle.setText("Editar Empleado");
        
        // Ocultar campo password al editar
        passwordContainer.setVisible(false);
        passwordContainer.setManaged(false);
        
        // Mostrar checkbox activo al editar
        activoCheckBox.setVisible(true);
        activoCheckBox.setManaged(true);
        
        modalFormContainer.setVisible(true);
        modalFormContainer.setManaged(true);
    }
    
    @FXML
    private void cerrarModalForm() {
        modalFormContainer.setVisible(false);
        modalFormContainer.setManaged(false);
        limpiarFormulario();
    }
    
    private void limpiarFormulario() {
        nombreField.clear();
        apellidosField.clear();
        dniField.clear();
        telefonoField.clear();
        emailField.clear();
        usuarioField.clear();
        passwordField.clear();
        cargoCombo.setValue(null);
        salarioField.clear();
        activoCheckBox.setSelected(true);
        activoCheckBox.setVisible(false);
        activoCheckBox.setManaged(false);
        errorFormLabel.setVisible(false);
        errorFormLabel.setManaged(false);
    }
    
    private void cargarDatosFormulario(EmpleadoDTO empleado) {
        nombreField.setText(empleado.getNombre());
        apellidosField.setText(empleado.getApellidos());
        dniField.setText(empleado.getDni());
        telefonoField.setText(empleado.getTelefono());
        emailField.setText(empleado.getEmail());
        usuarioField.setText(empleado.getUsuario());
        cargoCombo.setValue(empleado.getCargo());
        if (empleado.getSalario() != null) {
            salarioField.setText(empleado.getSalario().toString());
        }

        activoCheckBox.setSelected(Boolean.TRUE.equals(empleado.getActivo()));
        activoCheckBox.setVisible(true);
        activoCheckBox.setManaged(true);
        
        errorFormLabel.setVisible(false);
        errorFormLabel.setManaged(false);
    }

    // GUARDAR EMPLEADO (CREAR/EDITAR)
    
    @FXML
    private void guardarEmpleado() {
        errorFormLabel.setVisible(false);
        errorFormLabel.setManaged(false);
        
        // VALIDACIÓN Campos obligatorios
        if (!validarCamposObligatorios()) {
            return;
        }
        
        String dni = dniField.getText().trim().toUpperCase();
        String email = emailField.getText().trim();
        String telefono = telefonoField.getText().trim();
        String usuario = usuarioField.getText().trim();
        
        // VALIDACIÓN Formato DNI
        if (!dni.matches("^[0-9]{8}[A-Z]$")) {
            mostrarErrorForm("DNI inválido. Formato correcto: 12345678A");
            return;
        }
        
        // VALIDACIÓN Formato Email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            mostrarErrorForm("Email inválido. Ejemplo: usuario@hotel.com");
            return;
        }
        
        // VALIDACIÓN Formato Teléfono (6 dígitos)
        if (!telefono.matches("^[0-9]{9}$")) {
            mostrarErrorForm("Teléfono inválido. Debe tener exactamente 9 dígitos");
            return;
        }
        
        // VALIDACIÓN Usuario sin espacios
        if (usuario.contains(" ")) {
            mostrarErrorForm("El usuario no puede contener espacios");
            return;
        }
        
        // VALIDACIÓN Salario numérico
        BigDecimal salario = null;
        if (!salarioField.getText().trim().isEmpty()) {
            try {
                salario = new BigDecimal(salarioField.getText().trim());
                if (salario.compareTo(BigDecimal.ZERO) < 0) {
                    mostrarErrorForm("El salario no puede ser negativo");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarErrorForm("Salario inválido. Use formato: 1500.00");
                return;
            }
        }
        
        try {
            EmpleadoDTO dto = new EmpleadoDTO();
            dto.setNombre(nombreField.getText().trim());
            dto.setApellidos(apellidosField.getText().trim());
            dto.setDni(dni);
            dto.setTelefono(telefono);
            dto.setEmail(email);
            dto.setUsuario(usuario);
            dto.setCargo(cargoCombo.getValue());
            dto.setSalario(salario);
            
            if (empleadoEditando == null) {
                // CREAR NUEVO
                String passwordTemporal = passwordField.getText().trim();
                
                if (passwordTemporal.isEmpty()) {
                    mostrarErrorForm("Debe establecer una contraseña temporal");
                    return;
                }
                
                if (passwordTemporal.length() < 6) {
                    mostrarErrorForm("La contraseña debe tener al menos 6 caracteres");
                    return;
                }
                
                empleadoService.crearEmpleado(dto, passwordTemporal);
                passwordTemporalGenerada = passwordTemporal;
                
                cerrarModalForm();
                mostrarPasswordModal();
                
            } else {
                Boolean activo = activoCheckBox.isSelected();
                dto.setActivo(activo);
                
                empleadoService.actualizarEmpleado(empleadoEditando.getIdEmpleado(), dto);
                cerrarModalForm();
            }
            
            cargarEmpleados(paginaActual);
            cargarEstadisticas();
            
        } catch (Exception e) {
            mostrarErrorForm(e.getMessage());
        }
    }
    
    private boolean validarCamposObligatorios() {
        if (nombreField.getText().trim().isEmpty() ||
            apellidosField.getText().trim().isEmpty() ||
            dniField.getText().trim().isEmpty() ||
            telefonoField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() ||
            usuarioField.getText().trim().isEmpty() ||
            cargoCombo.getValue() == null) {
            
            mostrarErrorForm("Todos los campos marcados con * son obligatorios");
            return false;
        }
        return true;
    }
    
    private void mostrarErrorForm(String mensaje) {
        errorFormLabel.setText(mensaje);
        errorFormLabel.setVisible(true);
        errorFormLabel.setManaged(true);
    }

    // MODAL MOSTRAR PASSWORD
    
    private void mostrarPasswordModal() {
        passwordMostradaLabel.setText(passwordTemporalGenerada);
        modalPasswordContainer.setVisible(true);
        modalPasswordContainer.setManaged(true);
    }
    
    @FXML
    private void cerrarModalPassword() {
        modalPasswordContainer.setVisible(false);
        modalPasswordContainer.setManaged(false);
        passwordTemporalGenerada = null;
    }

    // VER DETALLES
    
    private void verDetalleEmpleado(EmpleadoDTO empleado) {
        detallesNombreLabel.setText(empleado.getNombreCompleto());
        detallesDniLabel.setText(empleado.getDni());
        detallesEmailLabel.setText(empleado.getEmail());
        detallesTelefonoLabel.setText(empleado.getTelefono());
        detallesUsuarioLabel.setText(empleado.getUsuario());
        detallesCargoLabel.setText(empleado.getCargo());
        detallesSalarioLabel.setText(empleado.getSalario() != null ? empleado.getSalario() + " €" : "-");
        detallesFechaLabel.setText(empleado.getFechaContratacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        detallesEstadoLabel.setText(empleado.getEstadoTexto());
        
        modalDetallesContainer.setVisible(true);
        modalDetallesContainer.setManaged(true);
    }
    
    @FXML
    private void cerrarModalDetalles() {
        modalDetallesContainer.setVisible(false);
        modalDetallesContainer.setManaged(false);
    }

    // ELIMINAR EMPLEADO
    
    private void eliminarEmpleado(EmpleadoDTO empleado) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar empleado " + empleado.getNombreCompleto() + "?");
        confirmacion.setContentText("Esta acción eliminará permanentemente el empleado de la base de datos");
        
        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            try {
                empleadoService.eliminarEmpleado(empleado.getIdEmpleado());
                cargarEmpleados(paginaActual);
                cargarEstadisticas();
            } catch (Exception e) {
                mostrarError("Error al eliminar empleado", e.getMessage());
            }
        }
    }

    // NAVEGACIÓN
    
    @FXML
    private void navegarDashboard() {
        stageManager.switchScene(FxmlView.DASHBOARD);
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

    // UTILIDADES
    
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
