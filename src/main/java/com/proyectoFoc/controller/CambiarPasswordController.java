package com.proyectoFoc.controller;

import com.proyectoFoc.FxmlView;
import com.proyectoFoc.StageManager;
import com.proyectoFoc.entity.Empleado;
import com.proyectoFoc.service.EmpleadoService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CambiarPasswordController {

    @FXML
    private PasswordField passwordActualField;
    
    @FXML
    private PasswordField passwordNuevaField;
    
    @FXML
    private PasswordField passwordConfirmarField;
    
    @FXML
    private Label errorLabel;

    @Autowired
    private EmpleadoService empleadoService;
    
    @Autowired
    private StageManager stageManager;
    
    private Empleado empleado;
    private Stage modalStage;

    /**
     * Configurar el empleado que debe cambiar su contraseña
     */
    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }
    
    /**
     * Configurar el stage del modal (para cerrarlo después)
     */
    public void setModalStage(Stage stage) {
        this.modalStage = stage;
    }

    /**
     * Cambiar contraseña
     */
    @FXML
    private void cambiarPassword() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        
        String passwordActual = passwordActualField.getText();
        String passwordNueva = passwordNuevaField.getText();
        String passwordConfirmar = passwordConfirmarField.getText();
        
        // VALIDACIÓN 1: Campos vacíos
        if (passwordActual.isEmpty() || passwordNueva.isEmpty() || passwordConfirmar.isEmpty()) {
            mostrarError("Todos los campos son obligatorios");
            return;
        }
        
        // VALIDACIÓN 2: Contraseña actual correcta
        if (!passwordActual.equals(empleado.getPassword())) {
            mostrarError("La contraseña actual no es correcta");
            return;
        }
        
        // VALIDACIÓN 3: Nueva contraseña longitud mínima
        if (passwordNueva.length() < 6) {
            mostrarError("La nueva contraseña debe tener al menos 6 caracteres");
            return;
        }
        
        // VALIDACIÓN 4: Contraseñas coinciden
        if (!passwordNueva.equals(passwordConfirmar)) {
            mostrarError("Las contraseñas nuevas no coinciden");
            return;
        }
        
        // VALIDACIÓN 5: Nueva contraseña diferente a la actual
        if (passwordNueva.equals(passwordActual)) {
            mostrarError("La nueva contraseña debe ser diferente a la actual");
            return;
        }
        
        try {
            // Cambiar contraseña
            empleadoService.cambiarPassword(empleado.getIdEmpleado(), passwordNueva);
            
            // Cerrar modal y continuar al dashboard
            if (modalStage != null) {
                modalStage.close();
            }
            
            // Navegar al dashboard
            stageManager.switchScene(FxmlView.DASHBOARD);
            
        } catch (Exception e) {
            mostrarError("Error al cambiar contraseña: " + e.getMessage());
        }
    }
    
    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
