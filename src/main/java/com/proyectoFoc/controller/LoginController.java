package com.proyectoFoc.controller;

import com.proyectoFoc.FxmlView;
import com.proyectoFoc.StageManager;
import com.proyectoFoc.dto.EmpleadoDTO;
import com.proyectoFoc.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AuthService authService;

    @Autowired
    private StageManager stageManager;

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

        // Intentar login
        EmpleadoDTO empleado = authService.login(usuario, password);

        if (empleado != null) {
            // Login exitoso
            System.out.println("Login con exito: " + empleado.getNombre() + " - " + empleado.getCargo());

            // Cambiar a vista Dashboard
            stageManager.switchScene(FxmlView.DASHBOARD);

        } else {
            // Login fallido
            mostrarError("Usuario o contraseña incorrectos");
            passwordField.clear();
            passwordField.requestFocus();
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