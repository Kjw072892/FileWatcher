package com.tcss.filewatcher.Viewer;

import com.tcss.filewatcher.Common.Properties;
import com.tcss.filewatcher.Model.RegDataBaseManager;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Objects;
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
     * Fires a property change object.
     */
    private final PropertyChangeSupport myChanges = new PropertyChangeSupport(this);



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

            System.err.println("Unable to load program: " + theEvent.getMessage());
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
                mainStage.setResizable(false);
                mainStage.show();
                Stage myStage = (Stage) myEmailTextBox.getScene().getWindow();
                myStage.close();

            } catch (final IOException theEvent) {
                System.err.println("Something bad happened.");
            }
        }
    }


    /**
     * Adds a controller as a listener.
     *
     * @param theListener the controller that is listening for the email data.
     */
    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        myChanges.addPropertyChangeListener(theListener);
    }


}
