package com.tcss.filewatcher.Viewer;

import com.tcss.filewatcher.Model.RegDataBaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Handles the registration scene.
 *
 * @author Kassie Whitney
 * @version 08.15.25
 */
public class UserRegSceneController {

    /**
     * The textbox where the user inputs a password.
     */
    @FXML
    private PasswordField myPasswordTextBox;

    /**
     * The textbox where the user verify their password.
     */
    @FXML
    private PasswordField myVerifyPassword;

    /**
     * The checkbox for emailing everything at 5pm.
     */
    @FXML
    private CheckBox mySend5pmCheckBox;

    /**
     * The email text box. (Clients email).
     */
    @FXML
    private TextField myEmailTextBox;


    /**
     * Adds new administrator as a registered admin.
     */
    @FXML
    private void handleMyRegisterButton() {

        final String email = myEmailTextBox.getText();
        final String password = myPasswordTextBox.getText();
        final String verifyPass = myVerifyPassword.getText();
        final boolean isSend5pmSelected = mySend5pmCheckBox.isSelected();

        final String selection = isSend5pmSelected ? mySend5pmCheckBox.getText() :
                "None";

        if (password != null && !password.isEmpty()) {

            if (password.length() < 8) {

                final Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Invalid Password");
                alert.setContentText("Password must be at least 8 characters long.");
                alert.setResizable(false);
                alert.showAndWait();

            } else if (!password.equals(verifyPass)) {

                final Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Passwords don't match!");
                alert.setContentText("The password's didn't match!\nTry again!");
                alert.setResizable(false);
                alert.showAndWait();

            } else {
                final RegDataBaseManager newUser = new RegDataBaseManager();
                if (newUser.hasAUserAlreadyRegistered(email)) {

                    final Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("This email has already been registered");
                    alert.setResizable(false);
                    alert.showAndWait();

                } else {

                    newUser.insertNewUserData(email, verifyPass, selection);
                    final Stage myStage = (Stage) myEmailTextBox.getScene().getWindow();
                    myStage.close();
                }

            }
        }

    }
}
