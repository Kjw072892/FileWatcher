package com.tcss.filewatcher.Viewer;

import com.tcss.filewatcher.Common.Properties;
import com.tcss.filewatcher.Controller.EmailFileController;
import com.tcss.filewatcher.Model.DataBaseManager;
import com.tcss.filewatcher.Model.DirectoryEntry;
import static com.tcss.filewatcher.Model.EmailClient.start;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * The controller for the query scene.
 * This class handles date filtering, modification type filtering,
 * database queries, and email export of file history records.
 *
 * @author Kassie Whitney
 * @version 8.6.25
 */
public class QuerySceneController implements PropertyChangeListener {

    /** The table displaying query results. */
    @FXML
    private TableView<DirectoryEntry> myQuerySceneTable;

    /** The column showing file modification dates. */
    @FXML
    private TableColumn<DirectoryEntry, String> myDateColumn;

    /** The column showing file modification times. */
    @FXML
    private TableColumn<DirectoryEntry, String> myTimeColumn;

    /** The column showing file names. */
    @FXML
    private TableColumn<DirectoryEntry, String> myFileNameColumn;

    /** The column showing file modification types. */
    @FXML
    private TableColumn<DirectoryEntry, String> myModificationType;

    /** The column showing file directories. */
    @FXML
    private TableColumn<DirectoryEntry, String> myDirectory;

    /** The start date picker for filtering queries. */
    @FXML
    private DatePicker myFromDatePicker;

    /** The end date picker for filtering queries. */
    @FXML
    private DatePicker myToDatePicker;

    /** The combo box for selecting modification types. */
    @FXML
    private ComboBox<String> myModificationComboBox;

    /** The observable list that backs the table view. */
    private final ObservableList<DirectoryEntry> myTableView  = FXCollections.observableArrayList();

    /** A master copy of all entries pulled from the database. */
    private final ObservableList<DirectoryEntry> myMasterCopy = FXCollections.observableArrayList();

    /** A sub copy of filtered entries. */
    private final ObservableList<DirectoryEntry> mySubCopy    = FXCollections.observableArrayList();

    /** Support for property change notifications. */
    private final PropertyChangeSupport myChanges = new PropertyChangeSupport(this);

    /** The database manager used to fetch and filter entries. */
    private final DataBaseManager myDataBaseManager = new DataBaseManager(false);

    /** The email address of the logged-in user. */
    private String myUserEmailAddress;

    /** The JavaFX stage for this scene. */
    private Stage myStage;

    /** Formatter used for dates in entries. */
    private static final DateTimeFormatter ENTRY_FMT =
            DateTimeFormatter.ofPattern("dd MMM, uuuu", java.util.Locale.ENGLISH);

    /**
     * Initializes the scene components, sets up bindings,
     * and loads the initial database entries into the table.
     */
    @FXML
    private void initialize() {
        addPropertyChangeListener(this);

        myDateColumn.setCellValueFactory(cd -> cd.getValue().dateProperty());
        myTimeColumn.setCellValueFactory(cd -> cd.getValue().timeProperty());
        myDirectory.setCellValueFactory(cd -> cd.getValue().directoryProperty());
        myModificationType.setCellValueFactory(cd -> cd.getValue().modificationTypeProperty());
        myFileNameColumn.setCellValueFactory(cd -> cd.getValue().fileNameProperty());
        myQuerySceneTable.setItems(myTableView);

        myFromDatePicker.setValue(LocalDate.now().minusYears(1));
        myToDatePicker.setValue(LocalDate.now());

        myTableView.clear();
        final List<DirectoryEntry> all = myDataBaseManager.getAllEntries();
        myMasterCopy.setAll(all);
        mySubCopy.setAll(all);

        final String from = myFromDatePicker.getValue().format(ENTRY_FMT);
        final String to   = myToDatePicker.getValue().format(ENTRY_FMT);
        addEntriesHelper(from, to);
    }

    /**
     * Handles modification type selection from the combo box.
     * Updates the table view with entries matching the selection.
     */
    @FXML
    private void handleModifyComboBox() {
        final String selected = myModificationComboBox.getSelectionModel().getSelectedItem();

        if (selected == null || "ALL_TYPES".equals(selected)) {
            myTableView.setAll(mySubCopy);
            myQuerySceneTable.setItems(myTableView);
            return;
        }

        final List<DirectoryEntry> filtered = new ArrayList<>();
        for (final DirectoryEntry e : mySubCopy) {
            if (selected.equals(e.getModificationType())) {
                filtered.add(e);
            }
        }
        myTableView.setAll(filtered);
        myQuerySceneTable.setItems(myTableView);
    }

    /**
     * Handles the from-date picker.
     * Ensures valid ranges and applies filtering.
     */
    @FXML
    private void handleFromDatePicker() {
        normalizePickers();
        myModificationComboBox.setValue(myModificationComboBox.getPromptText());

        final String from = myFromDatePicker.getValue().format(ENTRY_FMT);
        final String to   = myToDatePicker.getValue().format(ENTRY_FMT);
        addEntriesHelper(from, to);
    }

    /**
     * Handles the to-date picker.
     * Ensures valid ranges and applies filtering.
     */
    @FXML
    private void handleToDatePicker() {
        normalizePickers();
        myModificationComboBox.setValue(myModificationComboBox.getPromptText());

        final String from = myFromDatePicker.getValue().format(ENTRY_FMT);
        final String to   = myToDatePicker.getValue().format(ENTRY_FMT);
        addEntriesHelper(from, to);
    }

    /**
     * Normalizes the values of the date pickers.
     * Ensures no null values and that the from-date is before the to-date.
     */
    private void normalizePickers() {
        LocalDate today = LocalDate.now();

        if (myFromDatePicker.getValue() == null) myFromDatePicker.setValue(today.minusYears(1));
        if (myToDatePicker.getValue()   == null) myToDatePicker.setValue(today);

        if (myToDatePicker.getValue().isAfter(today)) {
            myToDatePicker.setValue(today);
        }
        if (myFromDatePicker.getValue().isAfter(myToDatePicker.getValue())) {
            myFromDatePicker.setValue(myToDatePicker.getValue());
        }
    }

    /**
     * Helper method for adding entries between two dates.
     *
     * @param theFromDate the lower bound date string
     * @param theToDate   the upper bound date string
     */
    private void addEntriesHelper(final String theFromDate, final String theToDate) {
        final List<DirectoryEntry> entryList = new ArrayList<>();
        addEntriesHelper(theFromDate, theToDate, entryList);
        mySubCopy.setAll(myTableView);
        myQuerySceneTable.setItems(myTableView);
    }

    /**
     * Helper method for adding entries between two dates into a list.
     *
     * @param theFromDate  the lower bound date string
     * @param theToDate    the upper bound date string
     * @param theEntryList the list to populate
     */
    private void addEntriesHelper(final String theFromDate,
                                  final String theToDate,
                                  final List<DirectoryEntry> theEntryList) {
        final LocalDate from = LocalDate.parse(theFromDate, ENTRY_FMT);
        final LocalDate to   = LocalDate.parse(theToDate,   ENTRY_FMT);

        myTableView.setAll(myMasterCopy);
        theEntryList.clear();

        for (final DirectoryEntry entry : myMasterCopy) {
            try {
                final LocalDate date = LocalDate.parse(entry.getDate(), ENTRY_FMT);
                if (!date.isBefore(from) && !date.isAfter(to)) {
                    theEntryList.add(entry);
                }
            } catch (DateTimeException ex) {
                Logger.getAnonymousLogger().log(Level.FINE,
                        "Skipping entry with unparsable date: " + entry.getDate());
            }
        }
        myTableView.setAll(theEntryList);
    }

    /**
     * Resets all filters to default values and restores all entries.
     */
    @FXML
    private void handleResetButton() {
        myModificationComboBox.setValue(myModificationComboBox.getPromptText());
        myFromDatePicker.setValue(LocalDate.now().minusYears(1));
        myToDatePicker.setValue(LocalDate.now());
        myTableView.setAll(myMasterCopy);
        mySubCopy.setAll(myMasterCopy);
        myQuerySceneTable.setItems(myTableView);
    }

    /**
     * Handles sending the currently displayed results by email.
     *
     * @throws IOException if the temporary file cannot be written
     */
    @FXML
    private void handleSendEmail() throws IOException {
        final Path tempFile = EmailFileController.getTmpFilePath();

        final String filterParam =
                "Date From: " + myFromDatePicker.getValue().format(ENTRY_FMT) + "\n" +
                "Date To: "   + myToDatePicker.getValue().format(ENTRY_FMT)   + "\n" +
                "Modification type: " +
                (myModificationComboBox.getValue() == null
                        ? myModificationComboBox.getPromptText()
                        : myModificationComboBox.getValue());

        EmailFileController.send(myTableView, tempFile, filterParam);

        final String email = myUserEmailAddress;

        new Thread(() -> {
            if (start(email, tempFile)) {
                Platform.runLater(() -> {
                    final Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setResizable(false);
                    alert.setContentText("Email sent Successfully");
                    alert.show();
                    Logger.getAnonymousLogger().log(Level.INFO, "Email: " + email);
                });
            } else {
                Platform.runLater(() -> {
                    final Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Email had failed to send!");
                    alert.setResizable(false);
                    alert.show();
                });
            }
        }).start();
    }

    /**
     * Connects this query scene controller to the main scene controller.
     *
     * @param theScene the main scene controller
     */
    protected void setMyMainSceneController(final MainSceneController theScene) {
        theScene.addPropertyChangeListener(this);
        theScene.setQuerySceneController(this);
    }

    /**
     * Adds a property change listener.
     *
     * @param theListener the listener to add
     */
    protected void addPropertyChangeListener(final PropertyChangeListener theListener) {
        myChanges.addPropertyChangeListener(theListener);
    }

    /**
     * Sets the stage for this scene.
     *
     * @param theStage the stage
     */
    protected void setStage(final Stage theStage) {
        myStage = theStage;
    }

    /**
     * Handles property changes signaled from other controllers.
     *
     * @param theEvent the property change event
     */
    @Override
    public void propertyChange(final PropertyChangeEvent theEvent) {
        List<DirectoryEntry> entryList = new ArrayList<>();
        final Properties prop = Properties.valueOf(String.valueOf(theEvent.getPropertyName()));
        switch (prop) {
            case Properties.QUERY_ALL: {
                myStage.setTitle("Historical Events");
                entryList = myDataBaseManager.getAllEntries();
                myTableView.setAll(entryList);
                myMasterCopy.setAll(myTableView);
                mySubCopy.setAll(myMasterCopy);
                myQuerySceneTable.setItems(myTableView);
                break;
            }
            case Properties.QUERY_EXTENSION: {
                final String extension = (String) theEvent.getNewValue();
                myStage.setTitle("Events Filtered by Extensions");
                entryList = myDataBaseManager.queryByExtension(extension);
                myTableView.setAll(entryList);
                myMasterCopy.setAll(myTableView);
                mySubCopy.setAll(myMasterCopy);
                myQuerySceneTable.setItems(myTableView);
                break;
            }
            case Properties.QUERY_DIRECTORY: {
                final String directory = (String) theEvent.getOldValue();
                myStage.setTitle("Events Filtered by Directories");
                entryList = myDataBaseManager.queryByDirectory(directory);
                myTableView.setAll(entryList);
                myMasterCopy.setAll(myTableView);
                mySubCopy.setAll(myMasterCopy);
                myQuerySceneTable.setItems(myTableView);
                break;
            }
            case Properties.QUERY_DIRECTORY_EXTENSION: {
                final String directory = (String) theEvent.getOldValue();
                final String extension = (String) theEvent.getNewValue();
                myStage.setTitle("Events Filtered by Directories and Extensions");
                List<DirectoryEntry> tempList = myDataBaseManager.queryByDirectory(directory);

                for (final DirectoryEntry entry : tempList) {
                    if ("All Extensions".equals(extension) || extension.equals(entry.getFileExtension())) {
                        entryList.add(entry);
                    }
                }

                myTableView.setAll(entryList);
                myMasterCopy.setAll(myTableView);
                mySubCopy.setAll(myMasterCopy);
                myQuerySceneTable.setItems(myTableView);
                break;
            }
            case Properties.USERS_EMAIL: {
                myUserEmailAddress = (String) theEvent.getNewValue();
                break;
            }
            default: { }
        }
    }
}
