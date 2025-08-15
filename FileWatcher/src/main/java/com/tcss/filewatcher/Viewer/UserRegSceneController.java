package com.tcss.filewatcher.Viewer;

import com.tcss.filewatcher.Model.RegDataBaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
     * The checkbox for emailing every new event.
     */
    @FXML
    private CheckBox mySendEveryNewEventCheckBox;

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
     * The stage of this scene.
     */
    private Stage myStage;

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
                mySendEveryNewEventCheckBox.getText();

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

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("This email has already been registered");
                    alert.setResizable(false);
                    alert.showAndWait();

                } else {
                    newUser.insertNewUserData(email, verifyPass, selection);
                    Stage myStage = (Stage) myEmailTextBox.getScene().getWindow();
                    myStage.close();
                }

            }
        }

    }

    /**
     * Handles the checkbox for send an email for every new event.
     */
    @FXML
    private void handleMySendEveryNewEventCheckBox() {
        if (mySendEveryNewEventCheckBox.isSelected()) {
            mySend5pmCheckBox.setSelected(false);
        }
    }

    /**
     * Handles the checkbox for sending all events occurring today at 5pm every day.
     */
    @FXML
    private void handleMySend5pmCheckBox() {
        if (mySend5pmCheckBox.isSelected()) {
            mySendEveryNewEventCheckBox.setSelected(false);
        }
    }

    /**
     * Sets the stage for stage manipulation.
     *
     * @param theStage the userRegSceneController stage.
     */
    public void setStage(final Stage theStage) {
        myStage = theStage;
    }

}
