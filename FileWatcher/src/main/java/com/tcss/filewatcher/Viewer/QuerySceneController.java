package com.tcss.filewatcher.Viewer;

import com.google.api.client.util.DateTime;
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
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Date;
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
 * The query scene controller.
 *
 * @author Kassie Whitney
 * @version 8.6.25
 */
public class QuerySceneController implements PropertyChangeListener {

    /**
     * The table of which houses the historical records.
     */
    @FXML
    private TableView<DirectoryEntry> myQuerySceneTable;

    /**
     * The date column.
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myDateColumn;

    /**
     * The Time column.
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myTimeColumn;

    /**
     * Stores the name of the files.
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myFileNameColumn;

    /**
     * The modification type column.
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myModificationType;

    /**
     * The directory column.
     */
    @FXML
    private TableColumn<DirectoryEntry, String> myDirectory;

    /**
     * Date picker to query from x-date
     */
    @FXML
    private DatePicker myFromDatePicker;

    /**
     * Date picker to query to x-date
     * default: today's date
     */
    @FXML
    private DatePicker myToDatePicker;

    /**
     * The comboBox that holds modification values.
     */
    @FXML
    private ComboBox<String> myModificationComboBox;

    /**
     * An array observable list of type DirectoryEntry
     */
    private final ObservableList<DirectoryEntry> myTableView =
            FXCollections.observableArrayList();

    /**
     * A copy of the myTableView.
     */
    private final ObservableList<DirectoryEntry> myMasterCopy =
            FXCollections.observableArrayList();

    /**
     * A tertiary copy of the table
     */
    private final ObservableList<DirectoryEntry> mySubCopy =
            FXCollections.observableArrayList();

    /**
     * This scenes property change support object. Allows for firing property changes.
     */
    private final PropertyChangeSupport myChanges = new PropertyChangeSupport(this);

    /**
     * The database object reference.
     */
    private final DataBaseManager myDataBaseManager = new DataBaseManager(false);

    /**
     * The users email address
     */
    private String myUserEmailAddress;


    /**
     * Sets this scenes stage for window manipulation.
     */
    private Stage myStage;

    /**
     * Formatter used for dates in entries.
     */
    private static final DateTimeFormatter ENTRY_FMT =
            DateTimeFormatter.ofPattern("dd MMM, uuuu", java.util.Locale.ENGLISH);


    /**
     * Initializes the query scene.
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
        final String to = myToDatePicker.getValue().format(ENTRY_FMT);
        addEntriesHelper(from, to);


    }

    /**
     * Filters the results by modification types.
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
     * Filters the results using From a date To a Date default date
     */
    @FXML
    private void handleFromDatePicker() {

        normalizePickers();
        myModificationComboBox.setValue(myModificationComboBox.getPromptText());

        final String from = myFromDatePicker.getValue().format(ENTRY_FMT);
        final String to = myToDatePicker.getValue().format(ENTRY_FMT);
        addEntriesHelper(from, to);
    }

    /**
     * Filters the results using From a date To a Date
     */
    @FXML
    private void handleToDatePicker() {

        normalizePickers();
        myModificationComboBox.setValue(myModificationComboBox.getPromptText());

        final String from = myFromDatePicker.getValue().format(ENTRY_FMT);
        final String to = myToDatePicker.getValue().format(ENTRY_FMT);
        addEntriesHelper(from, to);

    }


    /**
     * Normalizes the values of the date pickers.
     * Ensures no null values and that the from-date is before the to-date.
     */
    private void normalizePickers() {
        LocalDate today = LocalDate.now();

        if (myFromDatePicker.getValue() == null)
            myFromDatePicker.setValue(today.minusYears(1));
        if (myToDatePicker.getValue() == null) myToDatePicker.setValue(today);

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
     * Resets the table to its original state.
     */
    @FXML
    private void handleResetButton() {
        myModificationComboBox.setValue(null);
        mySubCopy.setAll(myMasterCopy);
        myFromDatePicker.setValue(null);
        myToDatePicker.setValue(null);
        myTableView.setAll(myMasterCopy);
        myQuerySceneTable.setItems(myTableView);

    }

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
        final LocalDate to = LocalDate.parse(theToDate, ENTRY_FMT);

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
     * Connects the main scene to the filewatcher scene.
     * <P>Ensures bidirectional communication.
     *
     * @param theScene the main scene object.
     */
    protected void setMyMainSceneController(final MainSceneController theScene) {
        theScene.addPropertyChangeListener(this);
        theScene.setQuerySceneController(this);
    }

    /**
     * Adds the mainScene as a listener for this Scene.
     *
     * @param theListener the Main scene listener object.
     */
    protected void addPropertyChangeListener(final PropertyChangeListener theListener) {
        myChanges.addPropertyChangeListener(theListener);
    }

    protected void setStage(final Stage theStage) {
        myStage = theStage;
    }


    /**
     * Houses actionable events based on what is heard via the listener.
     *
     * @param theEvent A PropertyChangeEvent object describing the event source
     *                 and the property that has changed.
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
