package com.tcss.filewatcher.Viewer;

import com.tcss.filewatcher.Common.Properties;
import com.tcss.filewatcher.Model.FileEventWatcher;
import com.tcss.filewatcher.Model.RegDataBaseManager;
import com.tcss.filewatcher.Model.SceneHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class EmailAndPassCheckController {

    /**
     * Used to set the login button as a default button.
     */
    @FXML
    public Button myLoginButton;
    /**
     * The password text box.
     */
    @FXML
    private PasswordField myPasswordTextBox;


    /**
     * The email text box.
     */
    @FXML
    private TextField myEmailTextBox;

    /**
     * Logger for debugging
     */
    private final Logger myLogger = Logger.getLogger("Email and Pass Check Logger");

    /**
     * Property Change support object
     */
    private final PropertyChangeSupport MY_CHANGES = new PropertyChangeSupport(this);

    /**
     * When the textbox has a character in it for the password field, the login default
     * button is set to true.
     */
    @FXML
    private void handleDefaultButton() {
        myLoginButton.setDefaultButton(!myPasswordTextBox.getText().isEmpty());
    }

    /**
     * Opens the registration scene.
     */
    @FXML
    private void handleRegisterButton() {
        try {
            final FXMLLoader registrationFXML =
                    new FXMLLoader(MainSceneController
                            .class.getResource("/com/tcss/filewatcher/UserRegistrationScene.fxml"));
            final Scene registrationScene = new Scene(registrationFXML.load());
            final Stage registrationStage = new Stage();

            registrationStage.setScene(registrationScene);
            registrationStage.setTitle("Register A New User");
            registrationStage.getIcons().add(new Image(Objects.requireNonNull(getClass()
                    .getResourceAsStream("/icons/registration_icon.png"))));

            registrationStage.setResizable(false);
            registrationStage.show();

        } catch (final IOException theEvent) {

            myLogger.log(Level.SEVERE, "Unable to load program: " + theEvent.getMessage() +
                    "\n");
        }
    }

    /**
     * Logs the user in and saves the logged-in users email as a state in main.
     */
    @FXML
    private void handleLogin() {
        final String email = myEmailTextBox.getText();
        final String password = myPasswordTextBox.getText();
        final RegDataBaseManager regUser = new RegDataBaseManager();

        if (!regUser.checkPassword(email, password)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid login");
            alert.setContentText("Invalid login.\nYour username and/or password is incorrect" +
                    ".");
            alert.setResizable(false);
            alert.showAndWait();
        } else {

            System.out.println(FileEventWatcher.fileWatcherStatus());

            if (!FileEventWatcher.fileWatcherStatus()) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(EmailAndPassCheckController.class.getResource("/com/tcss" +
                            "/filewatcher/MainScene.fxml"));

                    final Scene scene = new Scene(fxmlLoader.load());
                    final Stage mainStage = new Stage();

                    final MainSceneController mainSceneController = fxmlLoader.getController();

                    mainSceneController.setUserEmailAddress(email);

                    mainSceneController.setStage(mainStage);

                    mainStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                            "/icons/FileWatcherIcons.png"))));
                    mainStage.setScene(scene);
                    mainStage.setTitle("File Watcher");
                    mainStage.setResizable(false);
                    mainStage.show();

                } catch (final IOException theEvent) {
                    myLogger.log(Level.SEVERE, theEvent.getMessage() + "\n");
                }

            } else {
                MY_CHANGES.firePropertyChange(Properties.LOGGED_IN.toString(),
                        null, true);
            }

            final Stage myStage = (Stage) myEmailTextBox.getScene().getWindow();
            myStage.close();
        }
    }

    /**
     * Adds the the controller as a listener
     */
    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        MY_CHANGES.addPropertyChangeListener(theListener);
    }

}
