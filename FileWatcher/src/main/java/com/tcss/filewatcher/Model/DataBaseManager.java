package com.tcss.filewatcher.Model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sqlite.SQLiteDataSource;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;

/**
 * This class manages the SQLite database for the File Watcher application.
 * Handles all SQLite database operations for storing and querying file events.
 *
 * @author Salima Hafurova
 * @version 7/23/25
 */
public class DataBaseManager {

    /**
     * The SQLite data source for database connections.
     */
    private SQLiteDataSource myDs;

    /**
     * The JDBC URL for the SQLite database file.
     */
    private static final String DB_URL = "jdbc:sqlite:filewatcher.db";

    /**
     * Constructs a new DataBaseManager, initializing the database and creating the table if needed.
     */
    public DataBaseManager() {
        initializeDatabase();
        createTable();
    }

    /**
     * Initializes the SQLite data source and sets the database URL.
     * Throws a RuntimeException if initialization fails.
     */
    private void initializeDatabase() {
        try {
            myDs = new SQLiteDataSource();
            myDs.setUrl(DB_URL);
            System.out.println("Database connection established successfully");
        } catch (Exception e) {
            System.out.println("invalid");
            throw new RuntimeException("Failed to initialize database: ", e);
        }
    }

    /**
     * Creates the 'filewatcher' table in the database if it does not already exist.
     * Throws a RuntimeException if table creation fails.
     */
    private void createTable() {
        String query = """
                CREATE TABLE IF NOT EXISTS filewatcher (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_date TEXT NOT NULL,
                event_time TEXT NOT NULL,
                file_name TEXT NOT NULL,
                absolute_path TEXT NOT NULL,
                event_type TEXT NOT NULL
                )""";


        try (Connection conn = myDs.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            System.out.println("Table 'filewatcher' created successfully or already exists.");
        } catch (SQLException theE) {
            System.out.println("Error creating table: " + theE.getMessage());
            throw new RuntimeException("Failed to create table: ", theE);
        }
    }

    /**
     * Inserts a file event into the database.
     *
     * @param theCurrentDate  the current date of the event in "yyyy-MM-dd" format
     * @param theCurrentTime  the current time of the event in "HH:mm:ss" format
     * @param theAbsolutePath the absolute path of the file
     * @param theFileName     the name of the file
     * @param theEventType    the type of event (e.g., CREATED, MODIFIED, DELETED)
     * @throws IllegalArgumentException if any parameter is null
     */
    public final void insertFileEvent(final String theCurrentDate, final String theCurrentTime,
                                      final String theAbsolutePath, final String theFileName,
                                      final String theEventType) {

        String insertSQL = "INSERT INTO filewatcher (event_date, event_time, file_name, absolute_path, event_type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = myDs.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setObject(1, theCurrentDate);
            pstmt.setObject(2, theCurrentTime);
            pstmt.setObject(3, theAbsolutePath);
            pstmt.setObject(4, theFileName);
            pstmt.setObject(5, theEventType);
            pstmt.executeUpdate();

        } catch (SQLException theE) {
            System.out.println("Error inserting file event: " + theE.getMessage());
        }
    }

    /**
     * Deletes all records from the 'filewatcher' table.
     */
    public final void clearDatabase() {
        String deleteSQL = "DELETE FROM filewatcher";
        try (Connection conn = myDs.getConnection();
             Statement stmt = conn.createStatement()) {
            int rowsDeleted = stmt.executeUpdate(deleteSQL);
            System.out.println("Deleted" + rowsDeleted + " rows from database");
        } catch (SQLException e) {
            System.out.println("Error clearing database: " + e.getMessage());
        }
    }

    /**
     * Returns the number of records in the 'filewatcher' table.
     *
     * @return the number of records, or -1 if an error occurs
     */
    public final int getTableSize() {
        String countItems = "SELECT COUNT(*) FROM filewatcher";
        try (Connection conn = myDs.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countItems)) {

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
     * Queries file events by file extension.
     *
     * @param theExtension the file extension to search for (e.g., ".txt")
     * @return a list of string arrays, each containing [fileName, absolutePath, eventType, eventTime]
     */
    public final List<DirectoryEntry> queryByExtension(final String theExtension) {
        List<DirectoryEntry> results = new ArrayList<>();
        String query = "SELECT event_date, event_time, file_name, absolute_path, event_type FROM filewatcher WHERE file_name LIKE ? "
                + "ORDER by event_date, event_time";
        try (Connection conn = myDs.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            // Make sure file name start with dot for searching
            String searchPattern;
            searchPattern = "%" + theExtension;
            pstmt.setString(1, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String date = rs.getString("event_date");
                String time = rs.getString("event_time");
                String fileName = rs.getString("file_name");
                String directory = rs.getString("absolute_path");
                String eventType = rs.getString("event_type");

                DirectoryEntry entry = new DirectoryEntry(date, time, fileName, directory, eventType);

                results.add(entry);
            }
        } catch (SQLException theE) {
            System.out.println("Error querying by extension: " + theE.getMessage());
        }
        return results;
    }

    /**
     * Queries file events by event type.
     *
     * @param theEventType the event type to search for (e.g., "CREATED")
     * @return a list of string arrays, each containing [fileName, absolutePath, eventType, eventTime]
     * @throws IllegalArgumentException if event type is null
     */
    public final List<DirectoryEntry> queryByEventType(final String theEventType) {
        List<DirectoryEntry> results = new ArrayList<>();
        String query = "SELECT event_date, event_time, file_name, absolute_path, event_type FROM filewatcher WHERE event_type = ? "
                + "ORDER BY event_date, event_time";
        try (Connection conn = myDs.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, theEventType.toUpperCase());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String date = rs.getString("event_date");
                String time = rs.getString("event_time");
                String fileName = rs.getString("file_name");
                String directory = rs.getString("absolute_path");
                String eventType = rs.getString("event_type");

                DirectoryEntry entry = new DirectoryEntry(date, time, fileName, directory, eventType);

                results.add(entry);
            }
        } catch (SQLException theE) {
            System.out.println("Error querying by event type: " + theE.getMessage());
        }
        return results;
    }

    /**
     * Queries file events by directory path.
     *
     * @param theDirectoryPath the directory path to search for
     * @return a list of string arrays, each containing [fileName, absolutePath, eventType, eventTime]
     * @throws IllegalArgumentException if directory path is null
     */
    public final List<DirectoryEntry> queryByDirectory(final String theDirectoryPath) {
        List<DirectoryEntry> results = new ArrayList<>();
        String query = "SELECT event_date, event_time, file_name, absolute_path, event_type FROM filewatcher WHERE absolute_path "
                + "LIKE ? ORDER BY event_date, event_time";
        try (Connection conn = myDs.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, theDirectoryPath + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String date = rs.getString("event_date");
                String time = rs.getString("event_time");
                String fileName = rs.getString("file_name");
                String directory = rs.getString("absolute_path");
                String eventType = rs.getString("event_type");

                DirectoryEntry entry = new DirectoryEntry(date, time, fileName, directory, eventType);

                results.add(entry);
            }

        } catch (SQLException theE) {
            System.out.println("Error querying by directory: " + theE.getMessage());
        }
        return results;
    }

    /**
     * Queries file events within a date range.
     *
     * @param theStartDate the start date (inclusive) in "yyyy-MM-dd HH:mm:ss" format
     * @param theEndDate   the end date (inclusive) in "yyyy-MM-dd HH:mm:ss" format
     * @return a list of string arrays, each containing [fileName, absolutePath, eventType, eventTime]
     * @throws IllegalArgumentException if either date is null
     */
    public final List<DirectoryEntry> queryByDateRange(final String theStartDate,
                                                       final String theEndDate) {
        List<DirectoryEntry> results = new ArrayList<>();
        String query = "SELECT event_date, event_time, file_name, absolute_path, event_type " +
                "FROM filewatcher WHERE event_date BETWEEN ? AND ? ORDER BY event_date, event_time";
        try (Connection conn = myDs.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, theStartDate);
            pstmt.setString(2, theEndDate);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {

                String date = rs.getString("event_date");
                String time = rs.getString("event_time");
                String fileName = rs.getString("file_name");
                String directory = rs.getString("absolute_path");
                String eventType = rs.getString("event_type");

                DirectoryEntry entry = new DirectoryEntry(date, time, fileName, directory, eventType);

                results.add(entry);

            }

        } catch (SQLException theE) {
            System.out.println("Error querying by date range: " + theE.getMessage());
        }
        return results;
    }

    /**
     * Retrieves all file events from the database.
     *
     * @return a list of string arrays, each containing [fileName, absolutePath, eventType, eventTime]
     */
    public final List<DirectoryEntry> getAllEntries() {
        final List<DirectoryEntry> sqlEntries = new ArrayList<>();
        final String query = """
                SELECT event_date, event_time, file_name, absolute_path, event_type
                FROM filewatcher
                ORDER BY event_date, event_time
                """;

        try (Connection conn = myDs.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {

                String date = rs.getString("event_date");
                String time = rs.getString("event_time");
                String fileName = rs.getString("file_name");
                String directory = rs.getString("absolute_path");
                String eventType = rs.getString("event_type");

                DirectoryEntry entry = new DirectoryEntry(date, time, fileName, directory, eventType);

                sqlEntries.add(entry);
            }

        } catch (final SQLException theSQLexception) {
            System.out.println("Error retrieving entries: " + theSQLexception.getMessage());
        }
        return sqlEntries;
    }

}



