package com.tcss.filewatcher.Viewer;

import com.tcss.filewatcher.Common.Properties;
import com.tcss.filewatcher.Model.DataBaseManager;
import com.tcss.filewatcher.Model.DirectoryEntry;
import com.tcss.filewatcher.Model.FileEventWatcher;
import com.tcss.filewatcher.Model.SceneHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class FileWatcherSceneController extends SceneHandler implements PropertyChangeListener {

    /**
     * Controls the table.
     */
    @FXML
    public TableView<DirectoryEntry> myFileWatcherTable;

    /**
     * The date Column.
     */
    @FXML
    public TableColumn<DirectoryEntry, String> myDateTableColumn;

    /**
     * The Time Column.
     */
    @FXML
    public TableColumn<DirectoryEntry, String> myTimeTableColumn;

    /**
     * The directory Column
     */
    @FXML
    public TableColumn<DirectoryEntry, String> myDirectoriesColumn;

    /**
     * The modification type
     */
    @FXML
    public TableColumn<DirectoryEntry, String> myModificationType;

    /**
     * The name of the file
     */
    @FXML
    public TableColumn<DirectoryEntry, String> myFileNameColumn;


    /**
     * Starts the filewatcher program.
     */
    @FXML
    public Button myStartButton;


    /**
     * Stops the filewatcher program (Stores the records and clears the table).
     */
    @FXML
    public Button myStopButton;

    /**
     * Clears the table without saving while the filewatcher is still active.
     */
    @FXML
    public Button myResetButton;

    /**
     * Hides the table.
     */
    @FXML
    public Button myHideButton;

    /**
     * Saves the report to mySQL.
     */
    @FXML
    public Button mySaveReportButton;

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
    private final DataBaseManager dbManager = new DataBaseManager();

    /**
     * This stage object.
     */
    private Stage myStage;

    /**
     * Clears SQL database for debugging.
     */
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
            List<DirectoryEntry> recentList = dbManager.queryByDateRange(yesterday, today);
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
    }

    @FXML
    public void handleStopButton() {
        myStopButton.setDisable(true);
        myStartButton.setDisable(false);
        myChanges.firePropertyChange(Properties.STOP.toString(), false, true);
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
    public void handleEmailEventSetup() {

    }


    /**
     * Connects the main scene to the filewatcher scene.
     * <P>Ensures bidirectional communication.
     *
     * @param theScene the main scene object.
     */
    protected void setMyMainSceneController(final MainSceneController theScene) {
        theScene.addPropertyChangeListener(this);
        theScene.setFileWatcherSceneController(this);
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

        if (theEvent.getPropertyName().equals(Properties.START.toString())) {
            myStartButton.setDisable(true);
            myStopButton.setDisable(false);
            startWatcher();
            handleExitOnActive(myStage);

        } else if (theEvent.getPropertyName().equals(Properties.STOP.toString())) {

            myStartButton.setDisable(false);
            myStopButton.setDisable(true);
            stopWatcher();
            handleExitOnActive(myStage);

        } else if (theEvent.getPropertyName().equals(Properties.NEW_FILE_EVENT.toString())) {

            final DirectoryEntry entry = (DirectoryEntry) theEvent.getNewValue();

            Platform.runLater(() -> {
                final List<String> extensions = getExtensionsFromDir(entry.getDirectory());

                // Defensive null check
                if (extensions != null && extensions.contains(entry.getFileExtension()) || Objects.requireNonNull(extensions).contains("All Extensions")) {
                    dbManager.insertFileEvent(entry.getDate(), entry.getTime(), entry.getFileName(),
                            entry.getDirectory(), entry.getModificationType());

                    myTableview.add(entry);
                    System.out.println("Added file to table: " + entry.getFileName());
                }

            });

        }
    }


}
