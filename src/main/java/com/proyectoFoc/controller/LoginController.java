package com.proyectoFoc.controller;

import com.proyectoFoc.FxmlView;
import com.proyectoFoc.StageManager;
import com.proyectoFoc.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.prefs.Preferences;

@Component
public class LoginController {

    @FXML
    private TextField usuarioField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;
    
    @FXML
    private CheckBox recordarmeCheckBox;

    @Autowired
    private StageManager stageManager;

    @Autowired
    private AuthService authService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    // Preferences para guardar credenciales
    private static final String PREFS_NODE = "HotelManagement";
    private static final String PREF_USUARIO = "usuario";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_RECORDAR = "recordar";
    
    private Preferences prefs = Preferences.userRoot().node(PREFS_NODE);

    @FXML
    public void initialize() {
        // Cargar credenciales guardadas si existen
        cargarCredencialesGuardadas();
        
        // Permitir login con tecla Enter en cualquier campo
        usuarioField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                handleLogin();
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                handleLogin();
            }
        });
    }

    @FXML
    private void handleLogin() {
        // Limpiar error previo
        errorLabel.setVisible(false);

        // Obtener valores
        String usuario = usuarioField.getText().trim();
        String password = passwordField.getText();

        // Validar campos vacíos
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor, completa todos los campos");
            return;
        }

        // Intentar login (ahora devuelve boolean)
        boolean loginExitoso = authService.login(usuario, password);

        if (loginExitoso) {
            // Login exitoso
            System.out.println("Login exitoso: " + authService.getEmpleadoActual().getNombre() + 
                             " - " + authService.getEmpleadoActual().getCargo());

            // Guardar credenciales si "Recordarme" está marcado
            if (recordarmeCheckBox.isSelected()) {
                guardarCredenciales(usuario, password);
            } else {
                limpiarCredencialesGuardadas();
            }

            // VERIFICAR SI DEBE CAMBIAR CONTRASEÑA
            if (authService.debeCambiarPassword()) {
                // Mostrar modal de cambio de contraseña obligatorio
                mostrarModalCambioPassword();
            } else {
                // Ir directamente al dashboard
                stageManager.switchScene(FxmlView.DASHBOARD);
            }

        } else {
            // Login fallido
            mostrarError("Usuario o contraseña incorrectos");
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    /**
     * Mostrar modal de cambio de contraseña obligatorio
     */
    private void mostrarModalCambioPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cambiar_password.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            CambiarPasswordController controller = loader.getController();
            controller.setEmpleado(authService.getEmpleadoActual());

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Cambio de Contraseña Obligatorio");
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);

            // NO PERMITIR CERRAR CON X (obligatorio cambiar contraseña)
            modalStage.setOnCloseRequest(event -> {
                event.consume(); // Bloquear cierre
            });

            controller.setModalStage(modalStage);
            
            // Mostrar modal y esperar
            modalStage.showAndWait();

            // Después de cambiar contraseña, recargar empleado y ir al dashboard
            authService.recargarEmpleadoActual();
            
            // Solo ir al dashboard si ya no debe cambiar password
            if (!authService.debeCambiarPassword()) {
                stageManager.switchScene(FxmlView.DASHBOARD);
            }

        } catch (Exception e) {
            System.err.println("Error al abrir modal de cambio de contraseña: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al cargar el formulario de cambio de contraseña");
        }
    }
    
    /**
     * Guardar credenciales en Preferences
     */
    private void guardarCredenciales(String usuario, String password) {
        prefs.put(PREF_USUARIO, usuario);
        prefs.put(PREF_PASSWORD, password);
        prefs.putBoolean(PREF_RECORDAR, true);
    }
    
    /**
     * Cargar credenciales guardadas si existen
     */
    private void cargarCredencialesGuardadas() {
        boolean recordar = prefs.getBoolean(PREF_RECORDAR, false);
        
        if (recordar) {
            String usuario = prefs.get(PREF_USUARIO, "");
            String password = prefs.get(PREF_PASSWORD, "");
            
            if (!usuario.isEmpty() && !password.isEmpty()) {
                usuarioField.setText(usuario);
                passwordField.setText(password);
                recordarmeCheckBox.setSelected(true);
            }
        }
    }
    
    /**
     * Limpiar credenciales guardadas
     */
    private void limpiarCredencialesGuardadas() {
        prefs.remove(PREF_USUARIO);
        prefs.remove(PREF_PASSWORD);
        prefs.putBoolean(PREF_RECORDAR, false);
    }

    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
    }
}
