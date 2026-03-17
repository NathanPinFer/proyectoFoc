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

    @Autowired
    public StageManager(Stage primaryStage, ApplicationContext applicationContext) {
        this.primaryStage = primaryStage;
        this.applicationContext = applicationContext;
    }

    public void switchScene(FxmlView view) {
        Parent root = loadView(view.getFxmlFile());
        Scene scene = new Scene(root);

        // Cargar CSS específico según la vista
        String cssFile = getCssFileForView(view);
        if (cssFile != null) {
            scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource(cssFile)).toExternalForm()
            );
        }

        primaryStage.setScene(scene);
        primaryStage.setTitle(view.getTitle());
        
        // Configurar tamaño de ventana según la vista
        if (view == FxmlView.LOGIN) {
            primaryStage.setWidth(1200);
            primaryStage.setHeight(750);
        } else {
            primaryStage.setWidth(1700);
            primaryStage.setHeight(1000);
            primaryStage.setMaximized(false); // Permitir maximizar
        }
        
        primaryStage.show();
    }

    private Parent loadView(String fxmlPath) {
        SpringFXMLLoader loader = applicationContext.getBean(SpringFXMLLoader.class);
        return loader.load(fxmlPath);
    }

    /**
     * Obtener el archivo CSS correspondiente a cada vista
     */
    private String getCssFileForView(FxmlView view) {
        return switch (view) {
            case LOGIN -> "/css/login.css";
            case DASHBOARD -> "/css/dashboard.css";
            case CLIENTES -> "/css/clientes.css";
            default -> null;
        };
    }
}
