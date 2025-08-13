package com.tcss.filewatcher.Viewer;

import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The main method that initializes the main scene
 * @author Kassie Whitney
 * @version 7.16.25
 */
public class Main extends Application {
    @Override
    public void start(final Stage theStage) throws IOException {
      FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/tcss/filewatcher/MainScene.fxml"));

        final Scene scene = new Scene(fxmlLoader.load());

        final MainSceneController mainSceneController = fxmlLoader.getController();
        mainSceneController.setStage(theStage);

        theStage.setTitle("File Watcher");
        theStage.setResizable(false);
        theStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                "/icons/FileWatcherIcons.png"))));
        theStage.setScene(scene);
        theStage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}