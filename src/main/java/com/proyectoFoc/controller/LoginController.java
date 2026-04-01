package com.proyectoFoc.controller;

import com.proyectoFoc.FxmlView;
import com.proyectoFoc.StageManager;
import com.proyectoFoc.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @FXML
    private TextField usuarioField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @Autowired
    private StageManager stageManager;

    @Autowired
    private AuthService authService;
    
    @Autowired
    private ApplicationContext applicationContext;

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

    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
    }

    @FXML
    public void initialize() {
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
}
