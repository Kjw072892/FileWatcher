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
    private SQLiteDataSource ds;
    private static final String DB_URL = "jdbc:sqlite:filewatcher.db";

    public DataBaseManager() {
        initializeDatabase();
        createTable();
    }

    private void initializeDatabase() {
        try {
            ds = new SQLiteDataSource();
            ds.setUrl(DB_URL);
            System.out.println("Database connection established successfully");
        } catch (Exception e) {
            System.out.println("invalid");
            throw new RuntimeException("Failed to initialize database: ", e);
        }
    }

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


        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            System.out.println("Table 'filewatcher' created successfully or already exists.");
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
            throw new RuntimeException("Failed to create table: ", e);
        }
    }

    public final void insertFileEvent(final String currentDate, final String currentTime,
                                      final String absolutePath, final String fileName,
                                      final String eventType) {

        String insertSQL = "INSERT INTO filewatcher (event_date, event_time, file_name, absolute_path, event_type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setObject(1, currentDate);
            pstmt.setObject(2, currentTime);
            pstmt.setObject(3, absolutePath);
            pstmt.setObject(4, fileName);
            pstmt.setObject(5, eventType);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error inserting file event: " + e.getMessage());
        }
    }


    public final void clearDatabase() {
        String deleteSQL = "DELETE FROM filewatcher";
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            int rowsDeleted = stmt.executeUpdate(deleteSQL);
            System.out.println("Deleted" + rowsDeleted + " rows from database");
        } catch (SQLException e) {
            System.out.println("Error clearing database: " + e.getMessage());
        }
    }

    public final int getTableSize() {
        String countItems = "SELECT COUNT(*) FROM filewatcher";
        try (Connection conn = ds.getConnection();
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

    // query by file name returns the list of string arrays where each array contain [event_date,
    // event_time, file_name, absolute_path, event_type]
    public final List<DirectoryEntry> queryByExtension(final String extension) {
        List<DirectoryEntry> results = new ArrayList<>();
        String query = "SELECT event_date, event_time, file_name, absolute_path, event_type FROM filewatcher WHERE file_name LIKE ? "
                + "ORDER by event_date, event_time";
        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            // Make sure file name start with dot for searching
            String searchPattern;
            searchPattern = "%" + extension;
            pstmt.setString(1, searchPattern);
            final ResultSet resultSet = pstmt.executeQuery();
            addToResultList(results, resultSet);
        } catch (SQLException e) {
            System.out.println("Error querying by extension: " + e.getMessage());
        }
        return results;
    }

    private void addToResultList(List<DirectoryEntry> results, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String date = resultSet.getString("event_date");
            String time = resultSet.getString("event_time");
            String fileName = resultSet.getString("file_name");
            String directory = resultSet.getString("absolute_path");
            String eventType = resultSet.getString("event_type");

            DirectoryEntry entry = new DirectoryEntry(date, time, fileName, directory, eventType);

            results.add(entry);
        }
    }

    // query events by event type
    public final List<DirectoryEntry> queryByEventType(final String theEventType) {
        List<DirectoryEntry> results = new ArrayList<>();
        String query = "SELECT event_date, event_time, file_name, absolute_path, event_type FROM filewatcher WHERE event_type = ? "
                + "ORDER BY event_date, event_time";
        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, theEventType.toUpperCase());
            ResultSet rs = pstmt.executeQuery();
            addToResultList(results, rs);
        } catch (SQLException e) {
            System.out.println("Error querying by event type: " + e.getMessage());
        }
        return results;
    }

    // queries by directory path
    public final List<DirectoryEntry> queryByDirectory(final String theDirectoryPath) {
        List<DirectoryEntry> results = new ArrayList<>();
        String query = "SELECT event_date, event_time, file_name, absolute_path, event_type FROM filewatcher WHERE absolute_path "
                + "LIKE ? ORDER BY event_date, event_time";
        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, theDirectoryPath + "%");
            ResultSet rs = pstmt.executeQuery();
            addToResultList(results, rs);

        } catch (SQLException e) {
            System.out.println("Error querying by directory: " + e.getMessage());
        }
        return results;
    }

    // Query events within a date range.
    public final List<DirectoryEntry> queryByDateRange(final String theStartDate,
                                                       final String theEndDate) {
        List<DirectoryEntry> results = new ArrayList<>();
        String query = "SELECT event_date, event_time, file_name, absolute_path, event_type " +
                "FROM filewatcher WHERE event_date BETWEEN ? AND ? ORDER BY event_date, event_time";
        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, theStartDate);
            pstmt.setString(2, theEndDate);
            ResultSet rs = pstmt.executeQuery();
            addToResultList(results, rs);

        } catch (SQLException e) {
            System.out.println("Error querying by date range: " + e.getMessage());
        }
        return results;
    }

    // Retrieve all file events from database
    public final List<DirectoryEntry> getAllEntries() {
        final List<DirectoryEntry> sqlEntries = new ArrayList<>();
        final String query = """
                SELECT event_date, event_time, file_name, absolute_path, event_type
                FROM filewatcher
                ORDER BY event_date, event_time
                """;

        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            addToResultList(sqlEntries, rs);

        } catch (final SQLException theSQLexception) {
            System.out.println("Error retrieving entries: " + theSQLexception.getMessage());
        }
        return sqlEntries;
    }


    // close database cnxn, don't need for SQLite, but I think its good practice
    public void close() {
        System.out.println("Database manager closed");
    }

}



