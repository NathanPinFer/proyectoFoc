package com.proyectoFoc;

import lombok.Getter;

@Getter
public enum FxmlView {
    LOGIN("/fxml/login.fxml", "Login - Hotel Management"),
    DASHBOARD("/fxml/dashboard.fxml", "Dashboard - Hotel Management"),
    CLIENTES("/fxml/clientes.fxml", "Clientes - Hotel Management"),
    EMPLEADOS("/fxml/empleados.fxml", "Empleados - Hotel Management");

    private final String fxmlFile;
    private final String title;

    FxmlView(String fxmlFile, String title) {
        this.fxmlFile = fxmlFile;
        this.title = title;
    }
}
