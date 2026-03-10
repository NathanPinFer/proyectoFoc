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
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/login.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle(view.getTitle());
        primaryStage.show();
    }

    private Parent loadView(String fxmlPath) {
        SpringFXMLLoader loader = applicationContext.getBean(SpringFXMLLoader.class);
        return loader.load(fxmlPath);
    }
}

