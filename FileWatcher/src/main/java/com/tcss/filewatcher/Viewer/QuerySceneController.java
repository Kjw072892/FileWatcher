package com.tcss.filewatcher.Viewer;

import com.tcss.filewatcher.Common.Properties;
import com.tcss.filewatcher.Model.DataBaseManager;
import com.tcss.filewatcher.Model.DirectoryEntry;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
     * Sets this scenes stage for window manipulation.
     */
    private Stage myStage;


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

        String fromDate;
        String defaultToDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, " +
                "yyyy"));
        try {
            fromDate =
                    myFromDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd MMM, " +
                            "yyyy"));
        } catch (final RuntimeException theRunTimeException) {
            fromDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));
        }

        addEntriesHelper(fromDate, defaultToDate);
    }

    /**
     * Filters the results using From a date To a Date
     */
    @FXML
    private void handleToDatePicker() {

        String toDate;
        String fromDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM," +
                " " +
                "yyyy"));
        try {
            toDate =
                    myToDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd MMM, " +
                            "yyyy"));

            if (myFromDatePicker.getValue() != null) {
                fromDate = myFromDatePicker.getValue().format(DateTimeFormatter.ofPattern(
                        "dd MMM, yyyy"));
            }
        } catch (final RuntimeException theRunTimeException) {
            toDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));
        }

        addEntriesHelper(fromDate, toDate);

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
        final String[] splitFrom = theFromDate.split(" ");
        final String[] splitTo = theToDate.split(" ");
        final int fromDay = Integer.parseInt(splitFrom[0]);
        final int toDay = Integer.parseInt(splitTo[0]);
        myTableView.clear();
        myTableView.setAll(myMasterCopy);
        if (!myTableView.isEmpty()) {
            for (final DirectoryEntry theEntry : myTableView) {

                final String[] splitDate = theEntry.getDate().split(" ");
                final int day = Integer.parseInt(splitDate[0]);

                if (day >= fromDay && day <= toDay) {
                    theEntryList.add(theEntry);
                }
            }
        }

        myTableView.clear();
        myTableView.addAll(theEntryList);

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
            default: {
            }
        }


    }
}
