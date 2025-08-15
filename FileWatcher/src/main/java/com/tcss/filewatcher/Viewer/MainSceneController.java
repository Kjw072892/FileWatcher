package com.tcss.filewatcher.Viewer;

import com.tcss.filewatcher.Common.Properties;
import com.tcss.filewatcher.Controller.EmailFileController;
import com.tcss.filewatcher.Model.DirectoryEntry;
import com.tcss.filewatcher.Model.EmailClient;
import com.tcss.filewatcher.Model.FileDirectoryDataBase;
import com.tcss.filewatcher.Model.FileEventWatcher;
import com.tcss.filewatcher.Model.RegDataBaseManager;
import com.tcss.filewatcher.Model.SceneHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainSceneController extends SceneHandler implements PropertyChangeListener {
    /**
     * The table that showcases all the directories being currently monitored.
     */
    @FXML
    private TableView<DirectoryEntry> myDirectoriesToMonitorTable;

    /**
     * Stores the date inside the date column.
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myDate;

    /**
     * Stores the current time in the time column.
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myTime;

    /**
     * Stores the directory in the directory column.
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myDirectory;

    /**
     * Stores the extensions in the file extension column.
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myFileExtension;

    /**
     * The textbox where the user inputs the directory they want to monitor.
     */
    @FXML
    private TextField myDirectoryToMonitorTB;

    /**
     * The query menu item button.
     */
    @FXML
    private MenuItem myQueryMenuItem;


    /**
     * The query button.
     */
    @FXML
    private Button myQueryButton;

    /**
     * Clears the textboxes and sets the combobox to default.
     */
    @FXML
    private Button myClearButton;

    /**
     * Opens the filewatcher viewer (live) if started.
     */
    @FXML
    private MenuItem myFileWatcherViewerMenuItem;

    /**
     * Opens the email registration
     */
    @FXML
    private Button myEmailIconButton;

    /**
     * The combobox that gives the user a number of extensions to choose from.
     */
    @FXML
    private ComboBox<String> myMonitorByExtensionComBox;

    /**
     * The combobox that gives the user a number of extensions to choose from.
     */
    @FXML
    private ComboBox<String> myQueryByExtensionComBox;

    /**
     * Deletes the chosen directory from the list of added directories that's being monitored
     */
    @FXML
    private Button myDeleteDirectoryBTN;

    /**
     * The text box that the user fills in the directory that they want to query.
     */
    @FXML
    private TextField myDirectoryToQueryTB;


    /**
     * Starts the filewatcher program.
     */
    @FXML
    private Button myStartIconBtn;

    /**
     * Stops the filewatcher program.
     * Saves the report to the database, removes all the
     * added directory, removes all the items in the current report.
     */
    @FXML
    private Button myStopIconBtn;

    /**
     * Opens the filewatcher scene if active.
     */
    @FXML
    private Button myFileWatcherViewerIconBtn;


    /**
     * The current stage.
     */
    private Stage myStage;

    /**
     * Logger object to log errors and potential actions for debugging purposes.
     */
    private final Logger MY_LOGGER = Logger.getLogger("Main Scene Controller");

    /**
     * The stage for the child scene fireWatcher (live).
     */
    private final Stage fileWatcherStage = new Stage();

    /**
     * Sets the format of the current time.
     */
    private final DateTimeFormatter myFormatedTime = DateTimeFormatter.ofPattern("hh:mm:ss");

    /**
     * Sets the format of the current date.
     */
    private final DateTimeFormatter myFormatedDate = DateTimeFormatter.
            ofPattern("dd MMM, yyyy");

    /**
     * The property change support object.
     * Used to fire property changes.
     */
    private final PropertyChangeSupport myChanges = new PropertyChangeSupport(this);

    /**
     * The observable list object. Used to store objects that will go into the table.
     */
    private final ObservableList<DirectoryEntry> myTableView =
            FXCollections.observableArrayList();

    /**
     * The file directory database sql class.
     */
    private final FileDirectoryDataBase myFileDirectoryDatabase = new FileDirectoryDataBase();

    /**
     * Checks if the watcher service is stopped.
     */
    private boolean isWatchServiceOn = true;

    /**
     * The FileEventWatcher object.
     */
    private FileEventWatcher myFileWatcher;

    /**
     * The users email address.
     */
    private String myUsersEmailAddress;

    /**
     * Only used for debugging.
     */
    boolean clearSQLData = false;

    /**
     * Initializes the scene before showing.
     */
    @FXML
    private void initialize() {

        addPropertyChangeListener(this);
        myFileWatcher = new FileEventWatcher();

        myFileWatcher.connectToControllers(this, null);

        myDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        myTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        myDirectory.setCellValueFactory(cellData -> cellData.getValue().directoryProperty());
        myFileExtension.setCellValueFactory(cellData -> cellData.getValue().fileExtensionProperty());

        myDirectoriesToMonitorTable.setItems(myTableView);

        if (myFileDirectoryDatabase.getTableSize() > 0) {
            myTableView.addAll(myFileDirectoryDatabase.getAllEntries());
            myQueryMenuItem.setDisable(false);

            for (DirectoryEntry entry : myFileDirectoryDatabase.getAllEntries()) {
                String dir = entry.getDirectory();
                String ext = entry.getFileExtension();
                addMonitoredDirectory(dir, ext);
            }
        }


        //Only used for debugging.
        if (clearSQLData) {
            myFileDirectoryDatabase.clearDatabase();
        }
    }


    /**
     * Sets the current stage to this scene.
     *
     * @param theStage The reference to this scene.
     */
    public void setStage(final Stage theStage) {
        myStage = theStage;
    }


    /**
     * Starts the filewatcher sequences.
     */
    @FXML
    private void handleStartFileWatcher() {

        myStopIconBtn.setDisable(false);
        myStartIconBtn.setDisable(true);
        myFileWatcherViewerIconBtn.setDisable(false);

        EmailFileController.start(EmailFileController.getTmpFilePath());

        if (myFileWatcher == null) {
            myFileWatcher = new FileEventWatcher();
        }

        for (final DirectoryEntry theEntry : myTableView) {
            String dir = theEntry.getDirectory();
            String ext = theEntry.getFileExtension();

            myFileWatcher.addWatchPath(dir);

            if (ext != null && !"All Extensions".equals(ext)) {
                String normalized = ext.startsWith(".") ? ext : "." + ext;
                myFileWatcher.addWatchedExtension(normalized);
            }
        }

        try {

            final FXMLLoader fxmlLoader = new FXMLLoader(MainSceneController.class.getResource(
                    "/com/tcss/filewatcher/FileWatcherScene.fxml"));

            final Scene fileWatcherScene = new Scene(fxmlLoader.load());
            final FileWatcherSceneController fileWatcherSceneController =
                    fxmlLoader.getController();

            fileWatcherSceneController.setMyMainSceneController(this);

            myFileWatcher.connectToControllers(this, fileWatcherSceneController);

            startWatcher();
            handleExitOnActive(myStage);

            fileWatcherSceneController.setStage(fileWatcherStage);
            fileWatcherStage.setScene(fileWatcherScene);
            fileWatcherStage.setTitle("File Watcher (live)");

            fileWatcherStage.getIcons().add(new Image(Objects.requireNonNull(getClass()
                    .getResourceAsStream("/icons/watcher.png"))));
            fileWatcherStage.setX(myStage.getX() + myStage.getWidth());
            fileWatcherStage.setY(myStage.getY());

            fileWatcherStage.show();
            fileWatcherStage.setResizable(false);

            myChanges.firePropertyChange(Properties.START.toString(), null, Properties.START);
            myChanges.firePropertyChange(Properties.USERS_EMAIL.toString(), null, myUsersEmailAddress);


        } catch (final IOException ioe) {

            MY_LOGGER.log(Level.SEVERE, "The scene was unable to load!");
        }
    }

    @FXML
    private void openQueryScene() {
        try {
            final FXMLLoader querySceneFxmlLoader =
                    new FXMLLoader(MainSceneController
                            .class.getResource("/com/tcss/filewatcher/QueryScene.fxml"));
            final Scene queryScene = new Scene(querySceneFxmlLoader.load());
            final QuerySceneController querySceneController =
                    querySceneFxmlLoader.getController();
            final Stage queryStage = new Stage();

            querySceneController.setMyMainSceneController(this);
            queryStage.setScene(queryScene);
            queryStage.setTitle("Historical Records");
            queryStage.getIcons().add(new Image(Objects.requireNonNull(getClass()
                    .getResourceAsStream("/icons/record_icon.png"))));

            querySceneController.setStage(queryStage);

            queryStage.show();
            queryStage.setResizable(false);


        } catch (final IOException theIOE) {
            MY_LOGGER.log(Level.SEVERE, "The scene was unable to load!");
        }
    }

    /**
     * Opens the historical Query scene.
     */
    @FXML
    private void handleQueryIconButton() {
        openQueryScene();
        myChanges.firePropertyChange(Properties.QUERY_ALL.toString(), null, null);
    }

    /**
     * Opens the Query Scene
     */
    @FXML
    private void handleQueryButton() {

        if (myDirectoryToQueryTB.getText().isEmpty() && myQueryByExtensionComBox.getValue() == null) {
            final Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty Query");
            alert.setHeaderText("Unable To Query!");
            alert.setContentText("""
                    There is nothing to query against!
                    Please try again!
                    """);
            final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass()
                    .getResourceAsStream("/icons/FileWatcherIcons.png"))));
            alert.setResizable(false);
            alert.showAndWait();
            return;
        }


        if (!myDirectoryToQueryTB.getText().isEmpty()
                && (myQueryByExtensionComBox.getValue() == null)) {
            openQueryScene();
            myChanges.firePropertyChange(Properties.QUERY_DIRECTORY.toString(),
                    myDirectoryToQueryTB.getText(), null);

        } else if (myDirectoryToQueryTB.getText().isEmpty()
                && myQueryByExtensionComBox.getValue() != null
                && !myQueryByExtensionComBox.getValue().equals("All Extensions")) {

            openQueryScene();
            myChanges.firePropertyChange(Properties.QUERY_EXTENSION.toString(), null,
                    myQueryByExtensionComBox.getValue());

        } else if (myDirectoryToQueryTB.getText().isEmpty()
                && myQueryByExtensionComBox.getValue().equals("All Extensions")) {

            openQueryScene();
            myChanges.firePropertyChange(Properties.QUERY_ALL.toString(), null, "All " +
                    "Extensions");
        } else {
            System.out.println("test");
            openQueryScene();
            myChanges.firePropertyChange(Properties.QUERY_DIRECTORY_EXTENSION.toString(),
                    myDirectoryToQueryTB.getText(), myQueryByExtensionComBox.getValue());
        }

    }


    /**
     * Opens the about File Watcher scene.
     */
    @FXML
    private void handleAboutScene() {
        try {
            final FXMLLoader aboutFxmlLoader =
                    new FXMLLoader(MainSceneController.class.getResource(
                            "/com/tcss/filewatcher/AboutScene.fxml"));

            final Scene aboutScene = new Scene(aboutFxmlLoader.load());
            AboutSceneController aboutSceneController = aboutFxmlLoader.getController();

            Stage aboutStage = new Stage();

            aboutSceneController.setStage(aboutStage);

            aboutStage.setScene(aboutScene);

            aboutStage.setTitle("About File Watcher");

            aboutStage.getIcons().add(new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/icons/about_Icon.png"))));

            aboutStage.show();
            aboutStage.setResizable(false);

        } catch (final IOException theIoe) {
            MY_LOGGER.log(Level.SEVERE, "The scene was unable to load!");
        }
    }


    /**
     * Stops the filewatcher sequences.
     */
    @FXML
    private void handleStopFileWatcher() {

        stopWatcher();
        myFileWatcher.stopWatching();
        handleExitOnActive(myStage);
        myStopIconBtn.setDisable(true);
        myStartIconBtn.setDisable(false);
        myChanges.firePropertyChange(Properties.STOP.toString(), null, Properties.STOP);

    }

    /**
     * Brings the firewatcher (live) scene into view.
     */
    @FXML
    private void handleFileWatcherButton() {
        fileWatcherStage.setIconified(false);
        fileWatcherStage.toFront();
    }

    /**
     * Handles the closing sequence.
     */
    @FXML
    private void handleOnClose() {

        handleExitOnActive(myStage);
        Platform.exit();
    }

    /**
     * Adds the directory generated by the user into the monitored list.
     */
    @FXML
    private void handleAddDirectoryButton() {

        final String extension;

        if (myMonitorByExtensionComBox.getValue() != null) {

            extension = myMonitorByExtensionComBox.getValue();

        } else {
            extension = myMonitorByExtensionComBox.getPromptText();
        }

        myDirectoryToQueryTB.clear();
        myQueryByExtensionComBox.setPromptText("");

        String directory = myDirectoryToMonitorTB.getText();
        directory = directory.replace("\"", "");


        if (directory.isBlank() || !Files.isDirectory(Path.of(directory))) {
            MY_LOGGER.warning("Invalid or nonexistent directory: '" + directory + "'");
            return;
        }

        final String finalDirectory = directory;

        final String date = java.time.LocalDate.now().format(myFormatedDate);
        final String time = java.time.LocalTime.now().withNano(0).format(myFormatedTime);

        List<String> listOfUsedExten = new ArrayList<>();

        final boolean dirCheck =
                myTableView.stream().anyMatch(theEvent -> theEvent.getDirectory().equals(finalDirectory));

        final boolean extCheck =
                myTableView.stream().anyMatch(theEvent -> theEvent.getFileExtension().equals(extension));

        final boolean isAllExtOn =
                myTableView.stream().anyMatch(theEvent -> theEvent.getFileExtension().equals("All Extensions"));

        if (dirCheck) {
            listOfUsedExten = getExtensionsFromDir(finalDirectory);
        }

        if (listOfUsedExten.contains(extension) || (dirCheck && extCheck)) {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Duplicate directories");
            alert.setHeaderText("Unable to add directory");
            alert.setContentText("You are trying to add a directory that has already been " +
                    "added!");
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass()
                    .getResourceAsStream("/icons/FileWatcherIcons.png"))));
            alert.setResizable(false);
            alert.showAndWait();
            return;

        } else if (dirCheck && isAllExtOn) {
            final Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Action");
            alert.setHeaderText("Unable to add directory");
            alert.setContentText(""" 
                    You are trying to watch a specific file extension
                    under a directory that is already watching all file extensions!
                    """);
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass()
                    .getResourceAsStream("/icons/FileWatcherIcons.png"))));
            alert.setResizable(false);
            alert.showAndWait();
            return;
        }

        final DirectoryEntry entry = new DirectoryEntry(date, time, extension, directory);

        myTableView.add(entry);

        addMonitoredDirectory(entry.getDirectory(), entry.getFileExtension());

        myFileDirectoryDatabase.insertDirectory(entry.getDate(), entry.getTime(),
                entry.getFileExtension(), entry.getDirectory());

        if (isWatchServiceOn) {

            if (extension.equals("All Extensions")) {

                myFileWatcher.addWatchPath(directory);

            } else {

                myFileWatcher.addWatchPath(directory);

                myFileWatcher.addWatchedExtension(extension);
            }

        }

        myClearButton.setDisable(false);
        myQueryButton.setDisable(false);
        myQueryMenuItem.setDisable(false);
        myChanges.firePropertyChange(Properties.NEW_ENTRY.toString(), null, entry);

    }

    @FXML
    private void handleQueryComboBox() {
        if (myQueryByExtensionComBox.getPromptText() == null) {
            myQueryButton.setDisable(false);
            myClearButton.setDisable(false);
        }
    }

    /**
     * Handles the elements within the watchlist table.
     */
    @FXML
    private void handleTableView() {

        myDirectoriesToMonitorTable.setOnMouseClicked(theEvent -> {
                    final DirectoryEntry selectedItem =
                            myDirectoriesToMonitorTable.getSelectionModel().getSelectedItem();

                    if (selectedItem != null) {
                        final String selectedDirectory = selectedItem.getDirectory();
                        final String selectedExtension = selectedItem.getFileExtension();

                        myDeleteDirectoryBTN.setDisable(false);
                        myQueryButton.setDisable(false);
                        myClearButton.setDisable(false);

                        myDirectoryToQueryTB.setText(selectedDirectory);
                        myQueryByExtensionComBox.setValue(selectedExtension);
                        myDirectoryToMonitorTB.setText(selectedDirectory);
                    }
                }
        );
    }


    /**
     * Disables and enables the clear button, query button and combo-boxes
     */
    @FXML
    private void handleClearButton() {
        myDirectoryToQueryTB.clear();
        myQueryByExtensionComBox.setPromptText(null);
        myQueryByExtensionComBox.setValue(null);
        myClearButton.setDisable(true);
        myQueryByExtensionComBox.setDisable(false);
    }

    /**
     * Handles the delete button.
     * Removes the entry from the local List and from the watch list on the database.
     */
    @FXML
    private void handleDeleteButton() {
        final DirectoryEntry entry =
                myDirectoriesToMonitorTable.getSelectionModel().getSelectedItem();

        if (entry == null) {
            return;
        }

        myTableView.remove(entry);
        removeMonitoredExtension(entry.getDirectory(), entry.getFileExtension());
        myFileDirectoryDatabase.removeDirectory(entry.getDirectory(), entry.getFileExtension());

        //checks if the extension still exists
        final boolean doesExtExist =
                myTableView.stream().anyMatch(theEntry -> theEntry.getFileExtension().equals(entry.getFileExtension()));

        //Checks if the directory is still being used with other extensions.
        final boolean doesDirExist =
                myTableView.stream().anyMatch(theEvent -> theEvent.getDirectory().equals(entry.getDirectory()));

        //If the extension and the directory no longer exists, remove both.
        if (!doesExtExist && !doesDirExist) {

            myChanges.firePropertyChange(Properties.REMOVED_EXTENSION.toString(), entry.getDirectory(),
                    entry.getFileExtension());
            myChanges.firePropertyChange(Properties.REMOVED_DIRECTORY.toString(), null,
                    entry.getDirectory());

            // Remove the extension from the directory if directory no longer has the extension
        } else if (doesDirExist && !doesExtExist) {
            myChanges.firePropertyChange(Properties.REMOVED_EXTENSION.toString(), entry.getDirectory(),
                    entry.getFileExtension());
            removeMonitoredExtension(entry.getDirectory(), entry.getFileExtension());
        }
        if (myTableView.isEmpty()) {
            myQueryMenuItem.setDisable(true);
            myQueryButton.setDisable(true);
            myDeleteDirectoryBTN.setDisable(true);
        }
    }


    /**
     * Adds the queryScene to the mains property change listener.
     *
     * @param theScene the query scene.
     */
    protected void setQuerySceneController(final QuerySceneController theScene) {
        theScene.addPropertyChangeListener(this);
    }

    /**
     * Adds the email client as a listener.
     *
     * @param theEmailClient the emailClient object.
     */
    public void setEmailClientListener(final EmailClient theEmailClient) {
        theEmailClient.addPropertyChangeListener(this);
    }

    /**
     * Adds a listener to the listener list.
     *
     * @param theListener the scene that's listening for changes.
     */
    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        myChanges.addPropertyChangeListener(theListener);
    }

    /**
     * Sets the users email from the login screen.
     *
     * @param theEmail the users email.
     */
    public void setUserEmailAddress(final String theEmail) {
        myUsersEmailAddress = theEmail;
    }

    /**
     * Handles the property change events.
     *
     * @param theEvent A PropertyChangeEvent object describing the event source
     *                 and the property that has changed.
     */
    @Override
    public void propertyChange(final PropertyChangeEvent theEvent) {
        if (theEvent.getPropertyName().equals(Properties.STOPPED_WATCHING.toString())) {
            isWatchServiceOn = (boolean) theEvent.getNewValue();
        } else if (theEvent.getPropertyName().equals(Properties.START.toString())) {
            myStartIconBtn.setDisable(true);
            myStopIconBtn.setDisable(false);
            myFileWatcherViewerMenuItem.setDisable(false);

        } else if (theEvent.getPropertyName().equals(Properties.STOP.toString())) {
            myStartIconBtn.setDisable(false);
            myStopIconBtn.setDisable(true);
            myFileWatcherViewerMenuItem.setDisable(true);

        } else if (theEvent.getPropertyName().equals(Properties.NEW_ENTRY.toString())) {
            myDeleteDirectoryBTN.setDisable(false);
        }
    }
}