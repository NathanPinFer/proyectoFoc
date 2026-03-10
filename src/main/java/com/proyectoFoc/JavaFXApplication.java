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
        // NO inicializar Spring aquí todavía
    }

    @Override
    public void start(Stage primaryStage) {
        // registrar el Stage como bean
        applicationContext = new SpringApplicationBuilder(Main.class)
                .initializers(context -> {
                    // Registrar el Stage antes de que Spring inicialice los beans
                    context.getBeanFactory().registerSingleton("primaryStage", primaryStage);
                })
                .run();

        //  Despues obtener el StageManager y cargar la vista
        StageManager stageManager = applicationContext.getBean(StageManager.class);
        stageManager.switchScene(FxmlView.LOGIN);
    }

    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }
}