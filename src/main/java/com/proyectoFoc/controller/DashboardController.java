package com.proyectoFoc.controller;

import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

@Component
public class DashboardController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Dashboard cargado correctamente.");
    }
}