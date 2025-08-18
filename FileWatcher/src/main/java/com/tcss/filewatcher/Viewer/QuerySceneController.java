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
     * The formatter for comparing the dates.
     */
    private static final DateTimeFormatter ENTRY_FMT =
            DateTimeFormatter.ofPattern("dd MMM, uuuu", java.util.Locale.ENGLISH);


    /**
     * Initializes the query scene.
     */
    @FXML
    private void initialize() {
        addPropertyChangeListener(this);
        myDateColumn.setCellValueFactory(theCellData -> theCellData.getValue().dateProperty());
        myTimeColumn.setCellValueFactory(theCellData -> theCellData.getValue().timeProperty());
        myDirectory.setCellValueFactory(theCellData -> theCellData.getValue().directoryProperty());
        myModificationType.setCellValueFactory(theCellData -> theCellData.getValue().modificationTypeProperty());
        myFileNameColumn.setCellValueFactory(theCellData -> theCellData.getValue().fileNameProperty());
        myQuerySceneTable.setItems(myTableView);

        myFromDatePicker.setValue(LocalDate.now().minusYears(1));
        myToDatePicker.setValue(LocalDate.now());


    }

    /**
     * Filters the results by modification types.
     */
    @FXML
    private void handleModifyComboBox() {
        final String selectedItem = myModificationComboBox.getSelectionModel().getSelectedItem();
        List<DirectoryEntry> entryList = new ArrayList<>();

        myTableView.setAll(mySubCopy);


        for (final DirectoryEntry entries : myTableView) {

            if (entries.getModificationType().equals(selectedItem)) {
                entryList.add(entries);

            } else if ("ALL_TYPES".equals(selectedItem)) {
                entryList.addAll(myTableView);
                break;
            }
        }

        myTableView.clear();
        myTableView.setAll(entryList);
        myQuerySceneTable.setItems(myTableView);
    }

    /**
     * Filters the results using From a date To a Date default date
     */
    @FXML
    private void handleFromDatePicker() {
        myModificationComboBox.setValue(myModificationComboBox.getPromptText());
        String fromDate;
        String toDate;

        final boolean isDayGreater =
                myFromDatePicker.getValue().getDayOfMonth() >= myToDatePicker.getValue().getDayOfMonth();

        final boolean isMonthGreater =
                myFromDatePicker.getValue().getMonthValue() >= myToDatePicker.getValue().getMonthValue();
        final boolean isYearGreater =
                myFromDatePicker.getValue().getYear() >= myToDatePicker.getValue().getYear();

        try {
            toDate = myToDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd MMM, " +
                    "uuuu"));

        } catch (final DateTimeException theEvent) {
            toDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd, MMM, uuuu"));
            myToDatePicker.setValue(LocalDate.now());
        }

        try {
            if (isDayGreater && isMonthGreater && isYearGreater) {
                fromDate = toDate;
                myFromDatePicker.setValue(myToDatePicker.getValue());
            } else {
                fromDate = myFromDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd MMM, " +
                        "uuuu"));
            }

        } catch (final RuntimeException theRunTimeException) {
            fromDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, uuuu"));
            myFromDatePicker.setValue(LocalDate.now());

        }

        addEntriesHelper(fromDate, toDate);

    }

    /**
     * Filters the results using From a date To a Date
     */
    @FXML
    private void handleToDatePicker() {


        myModificationComboBox.setValue(myModificationComboBox.getPromptText());
        String toDate;
        String fromDate;

        final boolean isToMonthLess = myToDatePicker.getValue().getMonthValue()
                <= myFromDatePicker.getValue().getMonthValue();

        final boolean isToDayLess = myToDatePicker.getValue().getDayOfMonth()
                <= myFromDatePicker.getValue().getDayOfMonth();

        final boolean isToYearLess = myToDatePicker.getValue().getYear()
                <= myFromDatePicker.getValue().getYear();

        final boolean isToMonthGreaterNow =
                myToDatePicker.getValue().getMonthValue() >= LocalDate.now().getMonthValue();

        final boolean isToDayGreaterNow =
                myToDatePicker.getValue().getDayOfMonth() >= LocalDate.now().getDayOfMonth();

        final boolean isToYearGreaterNow =
                myToDatePicker.getValue().getYear() >= LocalDate.now().getYear();


        // Checking if the From is Null
        if (myFromDatePicker.getValue() != null) {

            fromDate = myFromDatePicker.getValue().format(DateTimeFormatter.ofPattern(
                    "dd MMM, uuuu"));
        } else {

            myFromDatePicker.setValue(LocalDate.now());
            fromDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, uuuu"));
        }


        try {

            if (isToMonthLess && isToDayLess && isToYearLess) {
                myToDatePicker.setValue(myFromDatePicker.getValue());
                toDate = myFromDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd " +
                        "MMM, uuuu"));

            } else if (isToMonthGreaterNow && isToDayGreaterNow && isToYearGreaterNow) {
                myToDatePicker.setValue(LocalDate.now());
                toDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, uuuu"));
            } else {

                toDate = myToDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd " +
                        "MMM, uuuu"));
            }

        } catch (final DateTimeException theEvent) {

            toDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, uuuu"));
            myToDatePicker.setValue(LocalDate.now());
        }

        addEntriesHelper(fromDate, toDate);

    }

    /**
     * Helper method to add filter entries to table view.
     *
     * @param theFromDate the user Specified from date.
     * @param theToDate   the user specified to date.
     */
    private void addEntriesHelper(final String theFromDate, final String theToDate) {

        final List<DirectoryEntry> entryList = new ArrayList<>();
        addEntriesHelper(theFromDate, theToDate, entryList);

        mySubCopy.setAll(myTableView);
        myQuerySceneTable.setItems(myTableView);
    }

    /**
     * Overridden helper method.
     *
     * @param theFromDate  the date from the "from" date picker.
     * @param theToDate    the date from the "to" date picker.
     * @param theEntryList the entry list of which gets modified.
     */

    private void addEntriesHelper(final String theFromDate, final String theToDate,
                                  final List<DirectoryEntry> theEntryList) {
        final LocalDate from = LocalDate.parse(theFromDate, ENTRY_FMT);
        final LocalDate to = LocalDate.parse(theToDate, ENTRY_FMT);

        myTableView.setAll(myMasterCopy);
        theEntryList.clear();

        for (final DirectoryEntry entry : myMasterCopy) {
            final LocalDate date = LocalDate.parse(entry.getDate(), ENTRY_FMT);

            if (!date.isBefore(from) && !date.isAfter(to)) {
                theEntryList.add(entry);
            }
        }

        myTableView.setAll(theEntryList);
    }

    /**
     * Resets the table to its original state.
     */
    @FXML
    private void handleResetButton() {
        myModificationComboBox.setValue(myModificationComboBox.getPromptText());
        mySubCopy.setAll(myMasterCopy);
        myFromDatePicker.setValue(LocalDate.now().minusYears(1));
        myToDatePicker.setValue(LocalDate.now());
        myTableView.setAll(myMasterCopy);
        myQuerySceneTable.setItems(myTableView);

    }

    @FXML
    private void handleSendEmail() throws IOException {
        final Path tempFile = EmailFileController.getTmpFilePath();

        final String filterParam =
                //Date from:
                "Date From: " + (myFromDatePicker != null ?
                        myFromDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd MMM, " +
                                "uuuu")) : LocalDate.now().format(DateTimeFormatter.ofPattern(
                        "dd MMM, uuuu"))) + "\n" +

                        //Date to
                        "Date To: " + (myToDatePicker != null ?
                        myToDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd MMM, " +
                                "uuuu")) : LocalDate.now().format(DateTimeFormatter.ofPattern(
                        "dd MMM, uuuu"))) + "\n" +

                        //Modification typ
                        "Modification type: " + (myModificationComboBox.getValue() == null ?
                        myModificationComboBox.getPromptText() :
                        myModificationComboBox.getValue());

        EmailFileController.send(myTableView, tempFile, filterParam);

        final String email = myUserEmailAddress;

        new Thread(() -> {
            if (start(email, tempFile)) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setResizable(false);
                    alert.setContentText("Email sent Successfully");
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
        Properties prop = Properties.valueOf(String.valueOf(theEvent.getPropertyName()));
        switch (prop) {
            case Properties.QUERY_ALL: {
                myStage.setTitle("Historical Query");
                entryList = myDataBaseManager.getAllEntries();
                myTableView.addAll(entryList);
                myQuerySceneTable.setItems(myTableView);
                myMasterCopy.setAll(myTableView);
                mySubCopy.setAll(myMasterCopy);
                break;
            }
            case Properties.QUERY_EXTENSION: {
                final String extension = (String) theEvent.getNewValue();
                myStage.setTitle("Query by Extensions");
                entryList = myDataBaseManager.queryByExtension(extension);
                myTableView.addAll(entryList);
                myQuerySceneTable.setItems(myTableView);
                myMasterCopy.setAll(myTableView);
                mySubCopy.setAll(myMasterCopy);
                break;
            }
            case Properties.QUERY_DIRECTORY: {
                final String directory = (String) theEvent.getOldValue();
                myStage.setTitle("Query by Directories");
                entryList = myDataBaseManager.queryByDirectory(directory);
                myTableView.addAll(entryList);
                myQuerySceneTable.setItems(myTableView);
                myMasterCopy.setAll(myTableView);
                mySubCopy.setAll(myMasterCopy);
                break;
            }
            case Properties.QUERY_DIRECTORY_EXTENSION: {
                final String directory = (String) theEvent.getOldValue();
                final String extension = (String) theEvent.getNewValue();
                myStage.setTitle("Query by Directories and Extensions");
                List<DirectoryEntry> tempList = myDataBaseManager.queryByDirectory(directory);


                for (final DirectoryEntry entries : tempList) {
                    if (entries.getFileExtension().equals(extension)) {
                        entryList.add(entries);
                    } else if (extension.equals("All Extensions")) {
                        tempList = myDataBaseManager.queryByDirectory(directory);
                        entryList.addAll(tempList);
                        break;
                    }
                }

                myTableView.addAll(entryList);
                myQuerySceneTable.setItems(myTableView);
                myMasterCopy.setAll(myTableView);
                mySubCopy.setAll(myMasterCopy);
                break;
            }

            case Properties.USERS_EMAIL: {
                myUserEmailAddress = (String) theEvent.getNewValue();

            }

            default: {
            }
        }


    }
}
