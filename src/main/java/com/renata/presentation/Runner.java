package com.renata.presentation;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.renata.infrastructure.InfrastructureConfig;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import com.renata.infrastructure.persistence.util.PersistenceInitializer;
import com.renata.presentation.util.SpringFXMLLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.renata")
public class Runner extends Application {

    public static AnnotationConfigApplicationContext springContext;

    @Override
    public void start(Stage stage) throws Exception {
        var fxmlLoader = new SpringFXMLLoader(springContext);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        // var mainFxmlResource = Runner.class.getResource(Path.of("view", "Main.fxml").toString());
        var mainFxmlResource = Runner.class.getResource("/com/renata/view/Main.fxml");
        Parent parent = (Parent) fxmlLoader.load(mainFxmlResource);
        Scene scene = new Scene(parent, 900, 600);
        stage.setTitle("Artifactor");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        springContext = new AnnotationConfigApplicationContext(InfrastructureConfig.class);
        var connectionManager = springContext.getBean(ConnectionPool.class);
        var databaseInitializer = springContext.getBean(PersistenceInitializer.class);

        // Підключення atlantaFX
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        try {
            databaseInitializer.init();
            launch(args);
        } finally {
            connectionManager.shutdown(); // close
        }
    }
}
