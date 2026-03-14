package com.hotel.gestion_hotelera.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class MainController {

    @FXML private StackPane contenidoCentral;

    @FXML private Label btnDashboard;
    @FXML private Label btnReservas;
    @FXML private Label btnClientes;
    @FXML private Label btnHabitaciones;
    @FXML private Label btnFacturacion;
    @FXML private Label btnEmpleados;

    private final ApplicationContext springContext;
    private Label labelActivo;

    // Estilos sidebar
    private static final String ESTILO_ACTIVO =
        "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
        "-fx-padding: 12 0 12 5; -fx-border-color: #3498DB; " +
        "-fx-border-width: 0 0 0 3; -fx-background-color: rgba(52,152,219,0.2); -fx-cursor: hand;";
    private static final String ESTILO_INACTIVO =
        "-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 14px; " +
        "-fx-padding: 12 0 12 0; -fx-cursor: hand;";

    public MainController(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    @FXML
    public void initialize() {
        // Cargar Dashboard por defecto al arrancar
        navegarDashboard();
    }

    @FXML private void navegarDashboard()    { cargarVista("/fxml/dashboard.fxml", btnDashboard); }
    @FXML private void navegarClientes()     { cargarVista("/fxml/clientes.fxml",  btnClientes); }
    @FXML private void navegarReservas()     { mostrarProximamente("Reservas",     btnReservas); }
    @FXML private void navegarHabitaciones() { mostrarProximamente("Habitaciones", btnHabitaciones); }
    @FXML private void navegarFacturacion()  { mostrarProximamente("Facturación",  btnFacturacion); }
    @FXML private void navegarEmpleados()    { mostrarProximamente("Empleados",    btnEmpleados); }

    private void cargarVista(String fxmlPath, Label boton) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(fxmlPath));

            // Usar Spring para inyectar dependencias en los controllers
            loader.setControllerFactory(springContext::getBean);

            Node vista = loader.load();
            contenidoCentral.getChildren().setAll(vista);
            activarBoton(boton);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarProximamente(String seccion, Label boton) {
        Label msg = new Label("🚧  " + seccion + " — próximamente");
        msg.setStyle("-fx-font-size: 20px; -fx-text-fill: #7f8c8d;");
        contenidoCentral.getChildren().setAll(msg);
        activarBoton(boton);
    }

    private void activarBoton(Label boton) {
        if (labelActivo != null) {
            labelActivo.setStyle(ESTILO_INACTIVO);
        }
        boton.setStyle(ESTILO_ACTIVO);
        labelActivo = boton;
    }
}