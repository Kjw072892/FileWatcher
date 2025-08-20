package com.tcss.filewatcher.Viewer;

import com.tcss.filewatcher.Common.Properties;
import com.tcss.filewatcher.Model.FileEventWatcher;
import com.tcss.filewatcher.Model.RegDataBaseManager;
import com.tcss.filewatcher.Model.SceneHandler;
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

public class LoginSceneController {

    @FXML
    private Button myLoginButton;

    @FXML
    private TextField myEmailTextBox;


    @FXML
    private PasswordField myPasswordTextBox;

    @FXML
    private void handleDefaultButton() {
        myLoginButton.setDefaultButton(!myPasswordTextBox.getText().isEmpty());
    }

    private final Logger myLogger = Logger.getLogger("Login Scene Controller");

    private final PropertyChangeSupport MY_CHANGES = new PropertyChangeSupport(this);


    @FXML
    public void handleLogin() {
        final String userEmail = myEmailTextBox.getText();
        final String userPassword = myPasswordTextBox.getText();
        final RegDataBaseManager regUser = new RegDataBaseManager();

        if (!regUser.checkPassword(userEmail, userPassword)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setContentText("Invalid login.\nYour username and/or password is incorrect" +
                    ".");
            alert.setTitle("Invalid login");
            alert.setResizable(false);
            alert.showAndWait();

            myPasswordTextBox.selectAll();
        } else {

            if (!SceneHandler.fileWatcherStatus()) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(EmailAndPassCheckController.class.getResource("/com/tcss" +
                            "/filewatcher/MainScene.fxml"));
                    final Stage mainStage = new Stage();
                    final Scene scene = new Scene(fxmlLoader.load());


                    final MainSceneController mainSceneController = fxmlLoader.getController();
                    mainSceneController.setStage(mainStage);

                    mainSceneController.setUserEmailAddress(userEmail);


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
     * Adds the controller as a listener
     */
    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        MY_CHANGES.addPropertyChangeListener(theListener);
    }
}
