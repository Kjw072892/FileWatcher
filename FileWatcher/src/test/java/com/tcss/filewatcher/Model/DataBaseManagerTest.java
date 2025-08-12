package com.tcss.filewatcher.Model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;


/**
 * Unit tests for DataBaseManager Class.
 *
 * @author salimahafurova
 * @version August 4, 2025
 */
class DataBaseManagerTest {
    /**
     * Test database manager instance.
     */
    private DataBaseManager myDBManager;

    /**
     * Test data constants.
     */
    private static final String TEST_DATE = "2025-01-01";
    private static final String TEST_TIME = "12:00:00";
    private static final String TEST_FILE_NAME = "test.txt";
    private static final String TEST_ABSOLUTE_PATH = "/home/user/test.txt";
    private static final String TEST_EVENT_TYPE = "CREATE";

    @BeforeEach
    void setUp() {
        // Create a fresh database manager for each test
        myDBManager = new DataBaseManager();
        // Clear any existing data
        myDBManager.clearDatabase();
    }

    @AfterEach
    void tearDown() {
        if (myDBManager != null) {
            myDBManager.clearDatabase();
        }
    }

    // Constructor test
    @Test
    void testConstructor() {
        assertNotNull(myDBManager, "DataBaseManager should be created successfully");
        assertEquals(0, myDBManager.getTableSize(), "Initial event count should be 0");
    }

    // Insert event tests
    @Test
    void testInsertFileEventWithAllParameters() {
       myDBManager.insertFileEvent(TEST_DATE, TEST_TIME, TEST_ABSOLUTE_PATH,
                TEST_FILE_NAME, TEST_EVENT_TYPE);
       assertEquals(1, myDBManager.getTableSize(), "Event count should be 1 after insert with all parameters");
    }

    @Test
    void testInsertMultipleEvents() {
        myDBManager.insertFileEvent("2025-01-01", "10:00:00", "/path/file1.txt", "file1.txt", "CREATE");
        myDBManager.insertFileEvent("2025-01-02", "11:00:00", "/path/file2.txt", "file2.txt", "MODIFY");
        myDBManager.insertFileEvent("2025-01-03", "12:00:00", "/path/file3.txt", "file3.txt", "DELETE");
        assertEquals(3, myDBManager.getTableSize(), "Should have 3 events in database after inserts with current time");
    }


    // Clear database test
    @Test
    void testClearDatabase() {
        // Insert some events first
        myDBManager.insertFileEvent("2025-01-01", "10:00:00", "/path/file1.txt", "file1.txt", "CREATE");
        myDBManager.insertFileEvent("2025-01-02", "11:00:00", "/path/file2.txt", "file2.txt", "MODIFY");
        assertEquals(2, myDBManager.getTableSize(), "Should have 2 events before clear");

        myDBManager.clearDatabase();

        assertEquals(0, myDBManager.getTableSize(), "Event count should be 0 after clear");
    }


    // Query by extension test
    @Test
    void testQueryByExtension() {
        // Insert test data with different extensions
        myDBManager.insertFileEvent("2025-01-01", "10:00:00", "/path/test1.txt", "test1.txt", "CREATE");
        myDBManager.insertFileEvent("2025-01-01", "10:00:00", "/path/test2.txt", "test2.txt", "MODIFY");
        myDBManager.insertFileEvent("2025-01-01", "10:00:00", "/path/test.java", "test.java", "CREATE");
        List<DirectoryEntry> results = myDBManager.queryByExtension(".txt");
        assertAll("Query by extension test",
                () -> assertEquals(2, results.size(), "Should find 2 .txt files"),
                () -> assertTrue(results.getFirst().getFileName().endsWith(".txt"), "First result should be .txt file"),
                () -> assertTrue(results.get(1).getFileName().endsWith(".txt"), "Second result should be .txt file")
        );

    }

    @Test
    void testQueryByExtensionWithoutDot() {
        myDBManager.insertFileEvent("2025-01-01", "12:00:00", "/path/test.java", "test.java", "CREATE");
        List<DirectoryEntry> results = myDBManager.queryByExtension("java");
        assertEquals(1, results.size(), "Should find 1 java file when searching without dot ");
    }

    @Test
    void testQueryByExtensionNoResults() {
        myDBManager.insertFileEvent("2025-01-01", "12:00:00", "/path/test.txt", "test.txt", "CREATE");
        List<DirectoryEntry> results = myDBManager.queryByExtension(".pdf");
        assertEquals(0, results.size(), "Should find no .pdf files");
    }

    // Query by event type tests
    @Test
    void testQueryByEventType() {
        // Insert test data with different event types
        myDBManager.insertFileEvent("2025-01-01", "10:00:00", "/path/test1.txt", "test1.txt", "CREATE");
        myDBManager.insertFileEvent("2025-01-01", "11:00:00", "/path/test2.txt", "test2.txt", "CREATE");
        myDBManager.insertFileEvent("2025-01-01", "12:00:00", "/path/test3.txt", "test3.txt", "MODIFY");
        List<DirectoryEntry> results = myDBManager.queryByEventType("CREATE");
        assertAll("Query by event type test",
                () -> assertEquals(2, results.size(), "Should find 2 CREATE events"),
                () -> assertEquals("CREATE", results.getFirst().getModificationType(), "First result should be CREATE event"),
                () -> assertEquals("CREATE", results.get(1).getModificationType(), "Second result should be CREATE event")
        );

    }

    @Test
    void testQueryByEventTypeCaseInsensitive() {
        myDBManager.insertFileEvent("2025-01-01", "12:00:00", "/path/test.txt", "test.txt", "CREATE");
        List<DirectoryEntry> results = myDBManager.queryByEventType("create");
        assertEquals(1, results.size(), "Should find CREATE event with lowercase search");
    }

    @Test
    void testQueryByEventTypeNoResults() {
        myDBManager.insertFileEvent("2025-01-01", "12:00:00", "/path/test.txt", "test.txt", "CREATE");
        List<DirectoryEntry> results = myDBManager.queryByEventType("DELETE");
        assertEquals(0, results.size(), "Should find no DELETE events");
    }

    // Query by directory tests
    @Test
    void testQueryByDirectory() {
        // Insert test data in different directories - fix parameter order
        myDBManager.insertFileEvent("2025-01-01", "10:00:00", "test1.txt", "/home/user/documents/test1.txt", "CREATE");
        myDBManager.insertFileEvent("2025-01-01", "11:00:00", "test2.txt", "/home/user/documents/test2.txt", "MODIFY");
        myDBManager.insertFileEvent("2025-01-01", "12:00:00", "test3.txt", "/home/user/documents/test3.txt", "CREATE");

        List<DirectoryEntry> results = myDBManager.queryByDirectory("/home/user/documents");

        assertAll("Query by directory test",
                () -> assertEquals(3, results.size(), "Should find 3 files in documents directory"),
                () -> assertTrue(results.getFirst().getDirectory().contains("/home/user/documents"), "First result should be in documents directory"),
                () -> assertTrue(results.get(1).getDirectory().contains("/home/user/documents"), "Second result should be in documents directory"),
                () -> assertTrue(results.get(2).getDirectory().contains("/home/user/documents"), "Third result should be in documents directory"));
    }

    @Test
    void testQueryByDirectoryNoResults() {
        myDBManager.insertFileEvent("2025-01-01", "12:00:00", "/home/user/documents/test.txt", "test.txt", "CREATE");
        List<DirectoryEntry> results = myDBManager.queryByDirectory("/home/user/downloads");
        assertEquals(0, results.size(), "Should find no files in downloads directory");
    }

    // Query by date range tests
    @Test
    void testQueryByDateRange() {
        // Insert test data with different dates
        myDBManager.insertFileEvent("2025-01-01", "10:00:00", "/path/test1.txt", "test1.txt", "CREATE");
        myDBManager.insertFileEvent("2025-01-02", "11:00:00", "/path/test2.txt", "test2.txt", "MODIFY");
        myDBManager.insertFileEvent("2025-01-03", "12:00:00", "/path/test3.txt", "test3.txt", "DELETE");
        List<DirectoryEntry> results = myDBManager.queryByDateRange("2025-01-01", "2025-01-02");

        assertAll("Query by date range test",
                () -> assertEquals(2, results.size(), "Should find 2 events in data range"),
                () -> assertTrue(results.getFirst().getDate().startsWith("2025-01-0"), "Results should be in correct date range"),
                () -> assertTrue(results.get(1).getDate().startsWith("2025-01-0"), "Results should be in correct date range"));
    }

    @Test
    void testQueryByDateRangeNoResults() {
        myDBManager.insertFileEvent("2025-01-01", "12:00:00", "/path/test.txt", "test.txt", "CREATE");
        List<DirectoryEntry> results = myDBManager.queryByDateRange("2025-02-01", "2025-02-28");
        assertEquals(0, results.size(), "Should find no events in different date range");

    }

    // Get all events tests
    @Test
    void testGetAllEvents() {
        // Insert test data
        myDBManager.insertFileEvent("2025-01-01", "10:00:00", "/path/test1.txt", "test1.txt", "CREATE");
        myDBManager.insertFileEvent("2025-01-01", "11:00:00", "/path/test2.txt", "test2.txt", "MODIFY");
        myDBManager.insertFileEvent("2025-01-01", "12:00:00", "/path/test3.txt", "test3.txt", "DELETE");

        List<DirectoryEntry> results = myDBManager.getAllEntries();
        assertAll("Get all events test",
                () -> assertEquals(3, results.size(), "Should return all 3 events"),
                () -> assertNotNull(results.getFirst().getFileName(), "File name should not be null"),
                () -> assertNotNull(results.getFirst().getDirectory(), "Absolute path should not be null"),
                () -> assertNotNull(results.getFirst().getModificationType(), "Event type should not be null"),
                () -> assertNotNull(results.getFirst().getTime(), "Event time should not be null")
        );

    }




    // Event count tests
    @Test
    void testGetTableSize() {
        assertEquals(0, myDBManager.getTableSize(), "Initial count should be 0");
        myDBManager.insertFileEvent("2025-01-01", "10:00:00", "/path/test1.txt", "test1.txt", "CREATE");
        assertEquals(1, myDBManager.getTableSize(), "Count should be 1 after one insert");
        myDBManager.insertFileEvent("2025-01-01", "11:00:00", "/path/test2.txt", "test2.txt", "MODIFY");
        assertEquals(2, myDBManager.getTableSize(), "Count should be 2 after two inserts");
    }

    // Edge cases and error handling tests
    @Test
    void testInsertEmptyStrings() {
        myDBManager.insertFileEvent("", "", "", "", "");
        // Depending on implementation this might succeed or fail
        // The test verifies the method doesn't crash
        assertDoesNotThrow(() -> myDBManager.getTableSize(), "Should not crash after inserting empty strings");
    }

    @Test
    void testQueryResultsOrder() {
        // Insert events with specific times to test ordering
        myDBManager.insertFileEvent("2025-01-01", "10:00:00", "/path/test1.txt", "test1.txt", "CREATE");
        myDBManager.insertFileEvent("2025-01-01", "12:00:00", "/path/test2.txt", "test2.txt", "MODIFY");
        myDBManager.insertFileEvent("2025-01-01", "11:00:00", "/path/test3.txt", "test3.txt", "DELETE");

        List<DirectoryEntry> results = myDBManager.getAllEntries();

        // Results should be ordered by date, time
        assertTrue(results.get(0).getTime().compareTo(results.get(1).getTime()) <= 0,
                "Results should be ordered by time (ASC)");
        assertTrue(results.get(1).getTime().compareTo(results.get(2).getTime()) <= 0,
                "Results should be ordered by time (ASC)");
    }
    @Test
    void testInsertNullValues() {
        assertDoesNotThrow(() -> {
            myDBManager.insertFileEvent(null, null, null, null, null);
        }, "Should handle null values gracefully");
    }

    @Test
    void testQueryByDirectoryWithSubdirectories() {
        // Parameters in correct order: date, time, fileName, absolutePath, eventType
        myDBManager.insertFileEvent(TEST_DATE, TEST_TIME,
                "file.txt", "/root/parent/child/file.txt",
                TEST_EVENT_TYPE);
        myDBManager.insertFileEvent(TEST_DATE, TEST_TIME,
                "file.txt", "/root/parent/file.txt",
                TEST_EVENT_TYPE);

        List<DirectoryEntry> results = myDBManager.queryByDirectory("/root/parent");
        assertEquals(2, results.size(), "Should find files in directory and subdirectories");
    }

    @Test
    void testDatabasePersistence() {
        myDBManager.insertFileEvent(TEST_DATE, TEST_TIME, TEST_ABSOLUTE_PATH, TEST_FILE_NAME, TEST_EVENT_TYPE);

        // Create new instance
        DataBaseManager newManager = new DataBaseManager();
        List<DirectoryEntry> results = newManager.getAllEntries();
        assertEquals(1, results.size(), "Data should persist across instances");
    }
}