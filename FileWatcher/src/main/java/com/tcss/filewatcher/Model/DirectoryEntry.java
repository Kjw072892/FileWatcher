package com.tcss.filewatcher.Model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Handles the data placed in the filewatcher table.
 *
 * @author Kassie Whitney
 * @version 7.31.25
 */
public class DirectoryEntry {

    /**
     * The date property.
     */
    private SimpleStringProperty myDate;

    /**
     * The time property.
     */
    private SimpleStringProperty myTime;

    /**
     * The directory property.
     */
    private SimpleStringProperty myDirectory;

    /**
     * The file extension property object.
     */
    private SimpleStringProperty myFileExtension;

    /**
     * The modification type
     */
    private final SimpleStringProperty myModificationType;

    /**
     * The name of the file that was modified
     */
    private SimpleStringProperty myFileName;


    /**
     * Constructor for the directory entry class.
     *
     * @param theModificationType how the file was modified.
     */
    public DirectoryEntry(final String theModificationType) {
        myModificationType = new SimpleStringProperty(theModificationType);
    }

    /**
     * Constructor the directory entry class.
     *
     * @param theDate      the current date.
     * @param theTime      the current system time.
     * @param theDirectory (user entry) The path to the file being watched.
     */
    public DirectoryEntry(final String theDate, final String theTime,
                          final String theFileExtension, final String theDirectory) {

        this(null);
        myDate = new SimpleStringProperty(theDate);
        myTime = new SimpleStringProperty(theTime);
        myDirectory = new SimpleStringProperty(theDirectory);
        myFileExtension = new SimpleStringProperty(theFileExtension);


    }

    public DirectoryEntry(final String theDate, final String theTime,
                          final String theFileName, final String theDirectory,
                          final String theEvent) {
        this(theEvent);
        myDate = new SimpleStringProperty(theDate);
        myTime = new SimpleStringProperty(theTime);
        myFileName = new SimpleStringProperty(theFileName);
        myDirectory = new SimpleStringProperty(theDirectory);
        myFileExtension = new SimpleStringProperty(extractExtension(theFileName));
    }

    private String extractExtension(final String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index != -1) ? fileName.substring(index) : "";
    }


    /**
     * Gets the files modification type.
     *
     * @return the string format of the modification type.
     */
    public String getModificationType() {
        return myModificationType.get();
    }

    /**
     * Gets the current date in a string format.
     *
     * @return the string format of the date.
     */
    public String getDate() {
        return myDate.get();
    }

    /**
     * Gets the current time in a string format.
     *
     * @return the string format of the time.
     */
    public String getTime() {
        return myTime.get();
    }

    /**
     * Gets the directory path.
     *
     * @return the directory path.
     */
    public String getDirectory() {
        return myDirectory.get();
    }

    /**
     * Gets the file extension that was passed by the user.
     *
     * @return the string representation of the file extension.
     */
    public String getFileExtension() {
        return (myFileExtension != null) ? myFileExtension.get() : "";
    }

    /**
     * Gets the file name.
     *
     * @return the string representation of the file name.
     */
    public String getFileName() {
        return myFileName.get();
    }


    /**
     * Gets the modification type property.
     *
     * @return the modification type property object
     */
    public SimpleStringProperty modificationTypeProperty() {
        return myModificationType;
    }

    /**
     * Gets the date property.
     *
     * @return the date as a simple string property object.
     */
    public SimpleStringProperty dateProperty() {
        return myDate;
    }

    /**
     * Gets the time property.
     *
     * @return the time as a simple string property object.
     */
    public SimpleStringProperty timeProperty() {
        return myTime;
    }

    /**
     * Gets the directory property.
     *
     * @return the directory as a simple string property object.
     */
    public SimpleStringProperty directoryProperty() {
        return myDirectory;
    }

    /**
     * Gets the file extension property object.
     *
     * @return the file extension simple string property object.
     */
    public SimpleStringProperty fileExtensionProperty() {
        return myFileExtension;
    }

    /**
     * Gets the file name property object.
     *
     * @return the file name simple string property object.
     */
    public SimpleStringProperty fileNameProperty() {
        return myFileName;
    }


}
