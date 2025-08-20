package com.tcss.filewatcher.Viewer;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * The main method that initializes the main scene
 *
 * @author Kassie Whitney
 * @version 7.16.25
 */
public class Main extends Application {
    @Override
    public void start(final Stage theStage) {

        try {
            final FXMLLoader loginScreen =
                    new FXMLLoader(MainSceneController
                            .class.getResource("/com/tcss/filewatcher/EmailAndPassCheck" +
                            ".fxml"));
            final Scene loginScene = new Scene(loginScreen.load());

            theStage.setScene(loginScene);
            theStage.setTitle("Admin Login");
            theStage.getIcons().add(new Image(Objects.requireNonNull(getClass()
                    .getResourceAsStream("/icons/record_icon.png"))));
            theStage.setResizable(false);
            theStage.show();


        } catch (final IOException theEvent) {
            final Logger logger = Logger.getLogger("Main class");
            logger.log(Level.SEVERE, "Unable to load program: "
                    + theEvent.getMessage() +"\n");
        }

    }

    public static void main(String[] args) {
        launch();
    }
}