package com.tcss.filewatcher.Viewer;

import com.tcss.filewatcher.Model.EmailClient;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class EmailClientScene{

    /**
     * The textbox where the user inputs a password
     */
    @FXML
    public PasswordField myPasswordTextBox;

    /**
     * The register button
     */
    @FXML
    private Button myRegisterButton;

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
     * The Name text box. (Client's name).
     */
    @FXML
    private TextField myNameTextBox;

    /**
     * The email text box. (Clients email).
     */
    @FXML
    private TextField myEmailTextBox;


    /**
     * Initializes the scene on startup.
     */
    @FXML
    public void initialize() {

    }

    /**
     * Handles the registration button.
     */
    @FXML
    private void handleMyRegisterButton() {
        final String email = myEmailTextBox.getText();
        final String password = myPasswordTextBox.getText();
        final boolean isSend5pmSelected = mySend5pmCheckBox.isSelected();
        final boolean isSendOnNewEventsSelected = mySendEveryNewEventCheckBox.isSelected();

        final String selection = isSend5pmSelected ? mySend5pmCheckBox.getText() :
                mySendEveryNewEventCheckBox.getText();


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
     * Handles the checkbox for sending all events occuring today at 5pm every day.
     */
    @FXML
    private void handleMySend5pmCheckBox() {
        if (mySend5pmCheckBox.isSelected()) {
            mySendEveryNewEventCheckBox.setSelected(false);
        }
    }

    /**
     * Handles the name textBox.
     */
    @FXML
    private void handleMyNameTextBox() {

    }

    /**
     * Handles the Email textBox.
     */
    @FXML
    private void handleMyEmailTextBox() {

    }

}
