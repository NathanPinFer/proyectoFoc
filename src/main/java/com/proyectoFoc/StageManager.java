package com.proyectoFoc;

import com.proyectoFoc.config.SpringFXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StageManager {

    private final Stage primaryStage;
    private final ApplicationContext applicationContext;
    private boolean inicializado = false;

    @Autowired
    public StageManager(Stage primaryStage, ApplicationContext applicationContext) {
        this.primaryStage = primaryStage;
        this.applicationContext = applicationContext;
    }

    public void switchScene(FxmlView view) {
        Parent root = loadView(view.getFxmlFile());
        Scene scene = new Scene(root);

        String cssFile = getCssFileForView(view);
        if (cssFile != null) {
            scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource(cssFile)).toExternalForm()
            );
        }

        primaryStage.setScene(scene);
        primaryStage.setTitle(view.getTitle());

        // Solo configurar tamaño la primera vez
        if (!inicializado) {
            if (view == FxmlView.LOGIN) {
                primaryStage.setWidth(1200);
                primaryStage.setHeight(750);
            } else {
                primaryStage.setMaximized(true);
            }
            inicializado = true;
        } else if (view == FxmlView.LOGIN) {
            // Al volver al login (cerrar sesión), resetear tamaño
            primaryStage.setMaximized(false);
            primaryStage.setWidth(1200);
            primaryStage.setHeight(750);
        }

        primaryStage.show();
    }

    private Parent loadView(String fxmlPath) {
        SpringFXMLLoader loader = applicationContext.getBean(SpringFXMLLoader.class);
        return loader.load(fxmlPath);
    }

    private String getCssFileForView(FxmlView view) {
        return switch (view) {
            case LOGIN -> "/css/login.css";
            case DASHBOARD -> "/css/dashboard.css";
            case CLIENTES -> "/css/clientes.css";
            default -> null;
        };
    }
}