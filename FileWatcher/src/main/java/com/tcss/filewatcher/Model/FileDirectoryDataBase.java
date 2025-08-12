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
     * Constructor that initializes the database and creates the table.
     */
    public FileDirectoryDataBase() {

        initialize();
        createTable();

    }

    /**
     * initializes the database connection.
     */
    private void initialize() {
        try {
            myDataSource = new SQLiteDataSource();
            myDataSource.setUrl(DB_URL);
            System.out.println("Database connection established successfully");
        } catch (final Exception theException) {

            System.out.println("invalid");
            throw new RuntimeException("Failed to initialize database: ", theException);

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
            System.out.println("Table 'filewatcher' created successfully or already exists.");

        } catch (final SQLException theSQLException) {
            System.out.println("Error creating table: " + theSQLException.getMessage());
            throw new RuntimeException("Failed to create table: ", theSQLException);
        }
    }

//    /**
//     * Validates a date string against the format "yyyy-MM-dd"
//     */
//    private boolean isValidDate(String date) {
//        try {
//            LocalDate.parse(date);
//            return true;
//        } catch (DateTimeParseException e) {
//            return false;
//        }
//    }

    /**
     * Validates a time string against the format "HH:mm:ss"
     */
    private boolean isValidTime(String time) {
        try {
            LocalTime.parse(time);
            return true;
        } catch (DateTimeParseException e) {
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
        if (//!isValidDate(theDate)||
                !isValidTime(theTime)) {
            throw new IllegalArgumentException("Invalid date format. Expected format: dd " +
                    "MMM, yyyy" +
                    " or Invalid time format. Expected format: HH:mm:ss");
        }
        final String insertSQL = "INSERT INTO watchList (date, time, file_extension, " +
                "directory) " +
                "VALUES (?, ?, ?, ?)";
        try (final Connection conn = myDataSource.getConnection();
             final PreparedStatement prepStmnt = conn.prepareStatement(insertSQL)) {

            prepStmnt.setObject(1, theDate);
            prepStmnt.setObject(2, theTime);
            prepStmnt.setObject(3, theFileExtension);
            prepStmnt.setObject(4, theDirectory);

            prepStmnt.executeUpdate();

        } catch (final SQLException theSQLexception) {
            System.out.println("Error inserting file event: " + theSQLexception.getMessage());

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

        } catch (final SQLException theSQLException) {
            System.out.println("Unable to remove the directory");
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
            System.out.println("Deleted " + rowsDeleted + " rows from database");

        } catch (final SQLException theSQLexception) {

            System.out.println("Error clearing database: " + theSQLexception.getMessage());
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
        } catch (final SQLException theSQLException) {
            System.out.println("Error counting items: " + theSQLException.getMessage());
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

        } catch (final SQLException theSQLexception) {
            System.out.println("Error retrieving entries: " + theSQLexception.getMessage());
        }

        return sqlEntries;
    }
}
