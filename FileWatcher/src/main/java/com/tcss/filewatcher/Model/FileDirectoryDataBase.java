package com.tcss.filewatcher.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.sqlite.SQLiteDataSource;

/**
 * This class manages the SQL database for watchlist
 *
 * @author Kassie Whitney
 * @version 7.31.25
 */
public class FileDirectoryDataBase {

    /**
     * The data source.
     */
    private SQLiteDataSource myDataSource;

    /**
     * The url for the database.
     */
    private static final String DB_URL = "jdbc:sqlite:filewatcher.db";

    /**
     * Logger object.
     */
    private static final Logger MY_LOGGER = Logger.getLogger("File Directory Database");

    /**
     * Constructor that initializes the database and creates the table.
     */
    public FileDirectoryDataBase(final boolean theDebuggerStatus) {
        initialize();
        createTable();
        setDebugger(theDebuggerStatus);
    }

    /**
     * Sets the debugger on or off.
     */
    private void setDebugger(final boolean theDebuggerStatus) {
        if (!theDebuggerStatus) {
            MY_LOGGER.log(Level.OFF, "");
        }
    }

    /**
     * initializes the database connection.
     */
    private void initialize() {
        try {
            myDataSource = new SQLiteDataSource();
            myDataSource.setUrl(DB_URL);

            MY_LOGGER.log(Level.INFO, "Database connection established successfully\n");
        } catch (final Exception theEvent) {

            MY_LOGGER.log(Level.SEVERE, "Failed to initialize database: " + theEvent+"\n");
            throw new RuntimeException("Failed to initialize database: ", theEvent);
        }
    }

    /**
     * Creates a new table or connects the database to the table.
     */
    private void createTable() {
        String query = """
                CREATE TABLE IF NOT EXISTS watchList (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                time TEXT NOT NULL,
                file_extension TEXT NOT NULL,
                directory TEXT NOT NULL
                )
                """;

        try (Connection conn = myDataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(query);

            MY_LOGGER.log(Level.INFO, "Table 'filewatcher' created successfully or already " +
                    "exists.\n");

        } catch (final SQLException theEvent) {
            MY_LOGGER.log(Level.SEVERE, "Error creating table: " + theEvent.getMessage() +
                    "\n");
            throw new RuntimeException("Failed to create table: ", theEvent);
        }
    }

    /**
     * Validates a date string against the format "dd MMM, yyyy" (e.g., "01 Jan, 2025")
     */
    private boolean isValidDate(final String theDate) {
        try {

            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy");
            LocalDate.parse(theDate, formatter);
            return true;

        } catch (final DateTimeParseException theEvent) {

            MY_LOGGER.log(Level.SEVERE, "The date time is not formated correctly!\n");
            return false;
        }
    }

    /**
     * Validates a time string against the format "HH:mm:ss"
     */
    private boolean isValidTime(final String theTime) {
        try {
            LocalTime.parse(theTime);
            return true;

        } catch (final DateTimeParseException theEvent) {

            MY_LOGGER.log(Level.SEVERE, "The date time is not formated correctly!\n");
            return false;
        }
    }

    /**
     * Adds the object into the watchList database.
     *
     * @param theDate          the current date.
     * @param theTime          the current time.
     * @param theFileExtension the user's chosen file extension.
     * @param theDirectory     the user generated path of the file.
     */
    public void insertDirectory(final String theDate, final String theTime,
                                final String theFileExtension,
                                final String theDirectory) {
        if (!isValidDate(theDate) || !isValidTime(theTime)) {

            final String errorMessage = "Invalid date format. Expected format: dd " +
                    "MMM, yyyy" +
                    " or Invalid time format. Expected format: HH:mm:ss";

            MY_LOGGER.log(Level.SEVERE, errorMessage + "\n");

            throw new IllegalArgumentException(errorMessage);
        }

        final String insertSQL = "INSERT INTO watchList (date, time, file_extension, " +
                "directory) VALUES (?, ?, ?, ?)";

        try (final Connection conn = myDataSource.getConnection();
             final PreparedStatement prepStmnt = conn.prepareStatement(insertSQL)) {

            prepStmnt.setObject(1, theDate);
            prepStmnt.setObject(2, theTime);
            prepStmnt.setObject(3, theFileExtension);
            prepStmnt.setObject(4, theDirectory);

            prepStmnt.executeUpdate();

        } catch (final SQLException theEvent) {

            MY_LOGGER.log(Level.SEVERE,
                    "Error inserting file event: " + theEvent.getMessage() +"\n");

        }
    }

    /**
     * Removes a directory from the watchList database based on the directory
     *
     * @param theDirectory the directory to be removed
     * @param theExtension the file extension associated with the directory
     */
    public void removeDirectory(final String theDirectory, final String theExtension) {

        final String removeDir = "DELETE FROM watchList WHERE directory = ? AND " +
                "file_extension = ?";

        try (final Connection conn = myDataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(removeDir)) {

            pstmt.setString(1, theDirectory);
            pstmt.setString(2, theExtension);

            pstmt.executeUpdate();

        } catch (final SQLException theEvent) {

            MY_LOGGER.log(Level.SEVERE,
                    "Unable to remove the directory: " + theEvent.getMessage() + "\n");
        }
    }

    /**
     * Clears the watchList database.
     */
    public void clearDatabase() {
        final String deleteSQL = "DELETE FROM watchList";
        try (final Connection conn = myDataSource.getConnection();
             final Statement stmt = conn.createStatement()) {

            int rowsDeleted = stmt.executeUpdate(deleteSQL);

            MY_LOGGER.log(Level.INFO,"Deleted " + rowsDeleted + " rows from database\n");

        } catch (final SQLException theEvent) {

            MY_LOGGER.log(Level.SEVERE, "Error clearing database: " + theEvent.getMessage()+
                    "\n");
        }
    }

    /**
     * Gives the number of items in the database
     */
    public int getTableSize() {
        final String countItems = "SELECT COUNT(*) FROM watchList";
        try (final Connection conn = myDataSource.getConnection();
             final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(countItems)) {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (final SQLException theEvent) {

            MY_LOGGER.log(Level.SEVERE, "Error counting items: " + theEvent.getMessage()+"\n");
            return -1;
        }
    }

    /**
     * Gets all the entries from within the database
     */
    public List<DirectoryEntry> getAllEntries() {
        final List<DirectoryEntry> sqlEntries = new ArrayList<>();
        final String query = """
                SELECT date, time, file_extension, directory
                FROM watchList
                ORDER BY date DESC , time DESC
                """;

        try (final Connection conn = myDataSource.getConnection();
             final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {

                String date = rs.getString("date");
                String time = rs.getString("time");
                String extension = rs.getString("file_extension");
                String directory = rs.getString("directory");

                DirectoryEntry entry = new DirectoryEntry(date, time, extension, directory);

                sqlEntries.add(entry);
            }

        } catch (final SQLException theEvent) {
            MY_LOGGER.log(Level.SEVERE,
                    "Error retrieving entries: " + theEvent.getMessage()+"\n");
        }

        return sqlEntries;
    }
}
