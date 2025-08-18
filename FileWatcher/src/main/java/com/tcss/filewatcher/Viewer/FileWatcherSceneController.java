package com.tcss.filewatcher.Viewer;

import com.tcss.filewatcher.Common.Properties;
import com.tcss.filewatcher.Controller.EmailFileController;
import com.tcss.filewatcher.Model.DataBaseManager;
import com.tcss.filewatcher.Model.DirectoryEntry;
import static com.tcss.filewatcher.Model.EmailClient.start;
import com.tcss.filewatcher.Model.FileExtensionHandler;
import com.tcss.filewatcher.Model.SceneHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class FileWatcherSceneController extends SceneHandler implements PropertyChangeListener {

    /**
     * Controls the table.
     */
    @FXML
    private TableView<DirectoryEntry> myFileWatcherTable;

    /**
     * The date Column.
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myDateTableColumn;

    /**
     * The Time Column.
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myTimeTableColumn;

    /**
     * The directory Column
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myDirectoriesColumn;

    /**
     * The modification type
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myModificationType;

    /**
     * The name of the file
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myFileNameColumn;


    /**
     * Starts the filewatcher program.
     */
    @FXML
    private Button myStartButton;


    /**
     * Stops the filewatcher program (Stores the records and clears the table).
     */
    @FXML
    private Button myStopButton;


    /**
     * Gives the buttons the ability to fire changes.
     */
    private final PropertyChangeSupport myChanges =
            new PropertyChangeSupport(this);

    /**
     * Adds an observable list of type DirectoryEntry.
     */
    private final ObservableList<DirectoryEntry> myTableview =
            FXCollections.observableArrayList();

    /**
     * Instantiates database manager.
     */
    private final DataBaseManager dbManager = new DataBaseManager(false);

    /**
     * This stage object.
     */
    private Stage myStage;

    /**
     * The users Email address.
     */
    private String myUserEmailAddress;


    /**
     * Clears SQL database for debugging.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean clearDataBase = false;



    /**
     * Initializes the scene.
     */
    @FXML
    public void initialize() {

        addPropertyChangeListener(this);
        myDateTableColumn.setCellValueFactory(theEntry -> theEntry.getValue().dateProperty());

        myTimeTableColumn.setCellValueFactory(theEntry -> theEntry.getValue().timeProperty());

        myFileNameColumn.setCellValueFactory(theEntry -> theEntry.getValue().fileNameProperty());

        myDirectoriesColumn.setCellValueFactory(theEntry -> theEntry.getValue().directoryProperty());

        myModificationType.setCellValueFactory(theEntry -> theEntry.getValue().modificationTypeProperty());

        myFileWatcherTable.setItems(myTableview);


        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern(
                "dd MMM, yyyy"));

        if (dbManager.getTableSize() > 0) {
            final List<DirectoryEntry> recentList = dbManager.queryByDateRange(yesterday,
                    today);
            myTableview.addAll(recentList);
        }

        //Only used for debugging
        if (clearDataBase) {
            dbManager.clearDatabase();
        }

    }

    @FXML
    public void handleStartButton() {
        myStopButton.setDisable(false);
        myStopButton.setDisable(true);
        myChanges.firePropertyChange(Properties.START.toString(), false, true);
        startWatcher();
    }

    @FXML
    public void handleStopButton() {
        myChanges.firePropertyChange(Properties.STOPPING.toString(), null, true);
    }

    @FXML
    public void handleResetButton() {
        myTableview.clear();
    }

    @FXML
    public void handleTabViewer() {
        myStage.setIconified(true);
    }


    @FXML
    public void handleSendEmail() throws IOException {

        final Path tmp = EmailFileController.getTmpFilePath();

        EmailFileController.send(myTableview, tmp, "Entries within 24 hours");

        final String email = myUserEmailAddress;

        new Thread(() -> {
            if (start(email, tmp)) {
                Platform.runLater(() -> {

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Email sent Successfully");
                    alert.setResizable(false);
                    alert.show();
                    Logger.getAnonymousLogger().log(Level.INFO, "Email: " + email);

                });

            } else {
                Platform.runLater(() -> {

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Email had failed to send!");
                    alert.setResizable(false);
                    alert.show();
                });
            }
        }).start();
    }

    /**
     * Connects the main scene to the filewatcher scene.
     * <P>Ensures bidirectional communication.
     *
     * @param theScene the main scene object.
     */
    protected void setMyMainSceneController(final MainSceneController theScene) {
        theScene.addPropertyChangeListener(this);
        theScene.setFileWatcherScene(this);
    }


    /**
     * Sets the stage for the filewatcher (live) scene
     *
     * @param theStage the Filewatcher (live) stage
     */
    protected void setStage(final Stage theStage) {

        myStage = theStage;
    }

    /**
     * Notifies main when this stage closes.
     * @param theStage The fireWatcherSceneController stage.
     */
    public void watchStage(final Stage theStage) {
        theStage.setOnHidden(theEvent -> {
            myChanges.firePropertyChange(Properties.CLOSED.toString(), null, true);
        });
    }

    /**
     * Adds a listener to the property change support list.
     */
    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        myChanges.addPropertyChangeListener(theListener);
    }


    /**
     * Listener for the property changes
     *
     * @param theEvent A PropertyChangeEvent object describing the event source
     *                 and the property that has changed.
     */
    @Override
    public void propertyChange(final PropertyChangeEvent theEvent) {

        final Logger logger = Logger.getLogger("FileWatcher Scene Controller logger");

        if (theEvent.getPropertyName().equals(Properties.START.toString())) {
            myStartButton.setDisable(true);
            myStopButton.setDisable(false);
            startWatcher();
            handleExitOnActive(myStage);

        } else if (theEvent.getPropertyName().equals(Properties.STOP.toString())
                || theEvent.getPropertyName().equals(Properties.STOPPED.toString())) {

            myStartButton.setDisable(false);
            myStopButton.setDisable(true);
            stopWatcher();
            handleExitOnActive(myStage);

        } else if (theEvent.getPropertyName().equals(Properties.NEW_FILE_EVENT.toString())) {

            final DirectoryEntry entry = (DirectoryEntry) theEvent.getNewValue();

            Platform.runLater(() -> {
                final List<String> extensions = getExtensionsFromDir(entry.getDirectory());

                if (FileExtensionHandler.canAddExtension(extensions, entry)) {
                    myTableview.add(entry);
                    logger.log(Level.INFO, "Added file to table: " + entry.getFileName()+"\n");
                }
            });

        } else if (theEvent.getPropertyName().equals(Properties.USERS_EMAIL.toString())) {
            myUserEmailAddress = (String) theEvent.getNewValue();
        }
    }


}
