package com.proyectoFoc;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFXApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(Main.class).run();
    }

    @Override
    public void start(Stage primaryStage) {
        // Registrar el Stage en Spring
        applicationContext.getBeanFactory().registerSingleton("primaryStage", primaryStage);

        // Obtener StageManager y mostrar Login
        StageManager stageManager = applicationContext.getBean(StageManager.class);
        stageManager.switchScene(FxmlView.LOGIN);
    }

    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }
}