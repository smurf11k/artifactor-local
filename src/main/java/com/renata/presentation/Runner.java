package com.renata.presentation;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.renata.infrastructure.InfrastructureConfig;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import com.renata.infrastructure.persistence.util.PersistenceInitializer;
import com.renata.presentation.util.SpringFXMLLoader;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/** Головний клас JavaFX додатку. */
@ComponentScan("com.renata")
public class Runner extends Application {

    public static AnnotationConfigApplicationContext springContext;
    private Stage splashStage;
    private ProgressBar progressBar;

    @Override
    public void start(Stage stage) throws Exception {
        showSplashScreen();

        // Initialize data in a background task
        Task<Void> initTask =
                new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        var connectionManager = springContext.getBean(ConnectionPool.class);
                        var databaseInitializer =
                                springContext.getBean(PersistenceInitializer.class);
                        for (int i = 0; i < 100; i++) {
                            Thread.sleep(30);
                            updateProgress(i + 1, 100);
                        }
                        databaseInitializer.init();
                        updateProgress(100, 100);
                        return null;
                    }
                };

        progressBar.progressProperty().bind(initTask.progressProperty());

        initTask.setOnSucceeded(
                event -> {
                    FadeTransition fadeOut =
                            new FadeTransition(
                                    Duration.millis(500), splashStage.getScene().getRoot());
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(
                            e -> {
                                splashStage.hide();
                                showMainApp(stage);
                            });
                    fadeOut.play();
                });

        initTask.setOnFailed(
                event -> {
                    System.err.println("Initialization failed: " + initTask.getException());
                    Platform.exit();
                });

        new Thread(initTask).start();
    }

    private void showSplashScreen() {
        splashStage = new Stage();
        splashStage.initStyle(StageStyle.TRANSPARENT);
        splashStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        ImageView imageView =
                new ImageView(new Image(getClass().getResourceAsStream("/images/splash.png")));
        imageView.setFitWidth(400);
        imageView.setFitHeight(300);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        progressBar = new ProgressBar();
        progressBar.setId("splash-progress-bar");
        progressBar.setPrefWidth(360);
        // progressBar.setStyle("-fx-padding: 0 20 20 20;");

        StackPane stackPane = new StackPane(imageView, progressBar);
        stackPane.setAlignment(Pos.BOTTOM_CENTER);

        Scene splashScene = new Scene(stackPane, 400, 300);
        splashScene.setFill(null);
        splashScene.getStylesheets().add(new PrimerLight().getUserAgentStylesheet());
        splashScene
                .getStylesheets()
                .add(getClass().getResource("/css/splash.css").toExternalForm());
        splashStage.setScene(splashScene);
        splashStage.setTitle("Artifactor - Loading");
        splashStage.show();
    }

    private void showMainApp(Stage stage) {
        try {
            var fxmlLoader = new SpringFXMLLoader(springContext);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            var mainFxmlResource = Runner.class.getResource("/com/renata/view/Main.fxml");
            Parent parent = (Parent) fxmlLoader.load(mainFxmlResource);
            Scene scene = new Scene(parent, 1120, 650);
            stage.setTitle("Artifactor");
            stage.setScene(scene);
            stage.setMinWidth(1120);
            stage.setMinHeight(650);
            stage.show();
        } catch (Exception e) {
            System.err.println("Failed to load main application: " + e.getMessage());
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        springContext = new AnnotationConfigApplicationContext(InfrastructureConfig.class);
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        launch(args);
        springContext.getBean(ConnectionPool.class).shutdown();
    }
}
