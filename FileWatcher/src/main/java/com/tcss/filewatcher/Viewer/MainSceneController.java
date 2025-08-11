package com.tcss.filewatcher.Viewer;

import com.tcss.filewatcher.Common.Properties;
import com.tcss.filewatcher.Model.DirectoryEntry;
import com.tcss.filewatcher.Model.FileDirectoryDataBase;
import com.tcss.filewatcher.Model.FileEventWatcher;
import com.tcss.filewatcher.Model.SceneHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
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
    public TableView<DirectoryEntry> myDirectoriesToMonitorTable;

    /**
     * Stores the date inside the date column.
     */
    @FXML
    public TableColumn<DirectoryEntry, String> myDate;

    /**
     * Stores the current time in the time column.
     */
    @FXML
    public TableColumn<DirectoryEntry, String> myTime;

    /**
     * Stores the directory in the directory column.
     */
    @FXML
    public TableColumn<DirectoryEntry, String> myDirectory;

    /**
     * Stores the extensions in the file extension column.
     */
    @FXML
    public TableColumn<DirectoryEntry, String> myFileExtension;

    /**
     * The textbox where the user inputs the directory they want to monitor.
     */
    @FXML
    public TextField myDirectoryToMonitorTB;

    /**
     * The query menu item button.
     */
    @FXML
    public MenuItem myQueryMenuItem;


    /**
     * The query button.
     */
    @FXML
    public Button myQueryButton;

    /**
     * Clears the textboxes and sets the combobox to default.
     */
    public Button myClearButton;

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
    private final static Logger MY_LOGGER = Logger.getLogger("Main Scene Controller");

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
     * Only used for debugging.
     */
    boolean clearSQLData = false;

    /**
     * Initializes the scene before showing.
     */
    @FXML
    private void initialize() {

        addPropertyChangeListener(this);

        myDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        myTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        myDirectory.setCellValueFactory(cellData -> cellData.getValue().directoryProperty());
        myFileExtension.setCellValueFactory(cellData -> cellData.getValue().fileExtensionProperty());

        myDirectoriesToMonitorTable.setItems(myTableView);

        if (myFileDirectoryDatabase.getTableSize() > 0) {
            myTableView.addAll(myFileDirectoryDatabase.getAllEntries());

            myQueryButton.setDisable(false);
            myClearButton.setDisable(false);

            for (DirectoryEntry entry : myFileDirectoryDatabase.getAllEntries()) {
                String dir = entry.getDirectory();
                String ext = entry.getFileExtension();
                addMonitoredDirectory(dir, ext);
            }
        }

        myFileWatcher = new FileEventWatcher();

        //Only used for debugging.
        if (clearSQLData) {
            myFileDirectoryDatabase.clearDatabase();
        }

        myDeleteDirectoryBTN.setDisable(myTableView.isEmpty());

    }

    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        myChanges.addPropertyChangeListener(theListener);
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

        if (myFileWatcher == null) {
            myFileWatcher = new FileEventWatcher();
        }

        try {

            final FXMLLoader fxmlLoader = new FXMLLoader(MainSceneController.class.getResource(
                    "/com/tcss/filewatcher/FileWatcherScene.fxml"));

            final Scene fileWatcherScene = new Scene(fxmlLoader.load());
            FileWatcherSceneController fileWatcherSceneController = fxmlLoader.getController();
            fileWatcherSceneController.setMyMainSceneController(this);

            //Connects the two controllers to the fileWatcher class.
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


        } catch (final IOException ioe) {

            MY_LOGGER.log(Level.SEVERE, "The scene was unable to load!");
        }
    }

    /**
     * Opens the Query Scene
     */
    @FXML
    private void handleQueryScene() {
        try {
            final FXMLLoader queryFxmlLoader =
                    new FXMLLoader(MainSceneController.class.getResource("/com/tcss/filewatcher/QueryScene.fxml"));
            final Scene queryScene = new Scene(queryFxmlLoader.load());
            final QuerySceneController querySceneController = queryFxmlLoader.getController();
            final Stage queryStage = new Stage();

            querySceneController.setMyMainSceneController(this);
            queryStage.setScene(queryScene);
            queryStage.setTitle("Historical Records");
            queryStage.getIcons().add(new Image(Objects.requireNonNull(getClass()
                    .getResourceAsStream("/icons/record_icon.png"))));

            queryStage.show();
            queryStage.setResizable(false);


        } catch (final IOException theIOE) {
            MY_LOGGER.log(Level.SEVERE, "The scene was unable to load!");
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


        final String directory = myDirectoryToMonitorTB.getText();

        if (directory == null || directory.isBlank() || !Files.isDirectory(Path.of(directory))) {
            MY_LOGGER.warning("Invalid or nonexistent directory: '" + directory + "'");
            return;
        }

        final String date = java.time.LocalDate.now().format(myFormatedDate);
        final String time = java.time.LocalTime.now().withNano(0).format(myFormatedTime);

        final boolean dirCheck =
                myTableView.stream().anyMatch(theEvent -> theEvent.getDirectory().equals(directory));
        final boolean extCheck =
                myTableView.stream().anyMatch(theEvent -> theEvent.getFileExtension().equals(extension));

        if (dirCheck && extCheck) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Duplicate directories");
            alert.setHeaderText("Unable to add directory");
            alert.setContentText("You are trying to add a directory that has already been " +
                    "added!");
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/FileWatcherIcons.png"))));
            alert.showAndWait();
            return;
        }

        DirectoryEntry entry = new DirectoryEntry(date, time, extension, directory);

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
        myChanges.firePropertyChange(Properties.NEW_ENTRY.toString(), null, entry);

    }

    /**
     * Handles the elements within the watchlist table.
     */
    @FXML
    private void handleTableView() {

        myDirectoriesToMonitorTable.setOnMouseClicked(theEvent -> {
                    DirectoryEntry selectedItem =
                            myDirectoriesToMonitorTable.getSelectionModel().getSelectedItem();

                    if (selectedItem != null) {
                        String selectedDirectory = selectedItem.getDirectory();
                        String selectedExtension = selectedItem.getFileExtension();

                        myDirectoryToMonitorTB.setText(selectedDirectory);
                        myMonitorByExtensionComBox.setPromptText(selectedExtension);
                    }
                }
        );
    }

    /**
     * Handles the delete button.
     */
    @FXML
    private void handleDeleteButton() {
        DirectoryEntry entry =
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
            myDeleteDirectoryBTN.setDisable(true);
            myQueryButton.setDisable(true);
        }

    }

    /**
     * Adds the filewatcher live scene to the mains property change listener.
     *
     * @param theScene the filewatcher live scene.
     */
    protected void setFileWatcherSceneController(final FileWatcherSceneController theScene) {
        theScene.addPropertyChangeListener(this);
    }

    /**
     * Adds the queryScene to the mains property change listener.
     *
     * @param theScene the query scene.
     */
    protected void setAboutSceneController(final QuerySceneController theScene) {
        theScene.addPropertyChangeListener(this);
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

        } else if (theEvent.getPropertyName().equals(Properties.STOP.toString())) {
            myStartIconBtn.setDisable(false);
            myStopIconBtn.setDisable(true);

        } else if (theEvent.getPropertyName().equals(Properties.NEW_ENTRY.toString())) {
            myDeleteDirectoryBTN.setDisable(false);

        }
    }
}