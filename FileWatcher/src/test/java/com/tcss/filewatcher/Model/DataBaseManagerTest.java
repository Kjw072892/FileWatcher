//package com.tcss.filewatcher.Model;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertAll;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//import java.util.List;
//
//
///**
// * Unit tests for DataBaseManager Class.
// *
// * @author salimahafurova
// * @version August 4, 2025
// */
//class DataBaseManagerTest {
//    /**
//     * Test database manager instance.
//     */
//    private DataBaseManager myDBManager;
//
//    /**
//     * Test data constants.
//     */
//    private static final String TEST_FILE_NAME = "test.txt";
//    private static final String TEST_ABSOLUTE_PATH = "/home/user/test.txt";
//    private static final String TEST_EVENT_TYPE = "CREATE";
//    private static final String TEST_EVENT_TIME = "2025-01-01 12:00:00";
//
//    @BeforeEach
//    void setUp() {
//        // Create a fresh database manager for each test
//        myDBManager = new DataBaseManager();
//        // Clear any existing data
//        myDBManager.clearDatabase();
//    }
//
//    @AfterEach
//    void tearDown() {
//        if (myDBManager != null) {
//            myDBManager.clearDatabase();
//            myDBManager.close();
//        }
//    }
//
//    // Constructor test
//    @Test
//    void testConstructor() {
//        assertNotNull(myDBManager, "DataBaseManager should be created successfully");
//        assertEquals(0, myDBManager.getEventCount(), "Initial event count should be 0");
//    }
//
//    // Insert event tests
//    @Test
//    void testInsertFileEventWithAllParameters() {
//        boolean result = myDBManager.insertFileEvent(TEST_FILE_NAME, TEST_ABSOLUTE_PATH,
//                TEST_EVENT_TYPE, TEST_EVENT_TIME);
//
//        assertAll("Insert file event test",
//                () -> assertTrue(result, "Insert should return true"),
//                () -> assertEquals(1, myDBManager.getEventCount(), "Event count should be 1")
//        );
//    }
//
//    @Test
//    void testInsertFileEventWithCurrentTime() {
//        boolean result = myDBManager.insertFileEvent(TEST_FILE_NAME, TEST_ABSOLUTE_PATH, TEST_EVENT_TYPE);
//        assertAll("Insert file event with current time",
//                () -> assertTrue(result, "Insert should return true"),
//                () -> assertEquals(1, myDBManager.getEventCount(), "Event count should be 1")
//        );
//    }
//
//    @Test
//    void testInsertMultipleEvents() {
//        myDBManager.insertFileEvent("file1.txt", "/path/file1.txt", "CREATE");
//        myDBManager.insertFileEvent("file2.txt", "/path/file2.txt", "MODIFY");
//        myDBManager.insertFileEvent("file3.txt", "/path/file3.txt", "DELETE");
//        assertEquals(3, myDBManager.getEventCount(), "Should have 3 events in database");
//
//    }
//
//    // Clear database test
//    @Test
//    void testClearDatabase() {
//        // Insert some events first
//        myDBManager.insertFileEvent("file1.txt", "/path/file1.txt", "CREATE");
//        myDBManager.insertFileEvent("file2.txt", "/path/file2.txt", "MODIFY");
//        assertEquals(2, myDBManager.getEventCount(), "Should have 2 events before clear");
//
//        boolean result = myDBManager.clearDatabase();
//
//        assertAll("Clear database test",
//                () -> assertTrue(result, "Clear should return true"),
//                () -> assertEquals(0, myDBManager.getEventCount(), "Event count should be 0 after clear")
//        );
//    }
//
//    @Test
//    void testClearEmptyDatabase() {
//        boolean result = myDBManager.clearDatabase();
//        assertTrue(result, "Clear empty database should return true");
//        assertEquals(0, myDBManager.getEventCount(), "Event count should remain 0");
//    }
//
//    // Query by extension test
//    @Test
//    void testQueryByExtension() {
//        // Insert test data with different extensions
//        myDBManager.insertFileEvent("test1.txt", "/path/test1.txt", "CREATE", "2025-01-01 10:00:00");
//        myDBManager.insertFileEvent("test2.txt", "/path/test2.txt", "MODIFY", "2025-01-01 11:00:00");
//        myDBManager.insertFileEvent("test.java", "/path/test.java", "CREATE", "2025-01-01 12:00:00");
//
//        List<String[]> results = myDBManager.queryByExtension(".txt");
//        assertAll("Query by extension test",
//                () -> assertEquals(2, results.size(), "Should find 2 .txt files"),
//                () -> assertTrue(results.getFirst()[0].endsWith(".txt"), "First result should be .txt file"),
//                () -> assertTrue(results.get(1)[0].endsWith(".txt"), "Second result should be .txt file")
//        );
//
//    }
//
//    @Test
//    void testQueryByExtensionWithoutDot() {
//        myDBManager.insertFileEvent("test.java", "/path/test.java", "CREATE", "2025-01-01 12:00:00");
//        List<String[]> results = myDBManager.queryByExtension("java");
//        assertEquals(1, results.size(), "Should fine 1 java file when searching without dot ");
//    }
//
//    @Test
//    void testQueryByExtensionNoResults() {
//        myDBManager.insertFileEvent("test.txt", "/path/test.txt", "CREATE", "2025-01-01 12:00:00");
//        List<String[]> results = myDBManager.queryByExtension(".pdf");
//        assertEquals(0, results.size(), "Should find no .pdf files");
//    }
//
//    // Query by event type tests
//    @Test
//    void testQueryByEventType() {
//        // Insert test data with different event types
//        myDBManager.insertFileEvent("test1.txt", "/path/test1.txt", "CREATE", "2025-01-01 10:00:00");
//        myDBManager.insertFileEvent("test2.txt", "/path/test2.txt", "CREATE", "2025-01-01 11:00:00");
//        myDBManager.insertFileEvent("test3.txt", "/path/test3.txt", "MODIFY", "2025-01-01 12:00:00");
//
//        List<String[]> results = myDBManager.queryByEventType("CREATE");
//        assertAll("Query by event type test",
//                () -> assertEquals(2, results.size(), "Should find 2 CREATE events"),
//                () -> assertEquals("CREATE", results.getFirst()[2], "First result should be CREATE event"),
//                () -> assertEquals("CREATE", results.get(1)[2], "Second result should be CREATE event")
//        );
//
//    }
//
//    @Test
//    void testQueryByEventTypeCaseInsensitive() {
//        myDBManager.insertFileEvent("test.txt", "/path/test.txt", "CREATE", "2025-01-01 12:00:00");
//        List<String[]> results = myDBManager.queryByEventType("create");
//        assertEquals(1, results.size(), "Should find CREATE event with lowercase search");
//    }
//
//    @Test
//    void testQueryByEventTypeNoResults() {
//        myDBManager.insertFileEvent("test.txt", "/path/test.txt", "CREATE", "2025-01-01 12:00:00");
//        List<String[]> results = myDBManager.queryByEventType("DELETE");
//        assertEquals(0, results.size(), "Should find no DELETE events");
//    }
//
//    // Query by directory tests
//    @Test
//    void testQueryByDirectory() {
//        // Insert test data in different directories
//        myDBManager.insertFileEvent("test1.txt", "/home/user/documents/test1.txt", "CREATE", "2025-01-01 10:00:00");
//        myDBManager.insertFileEvent("test2.txt", "/home/user/documents/test2.txt", "MODIFY", "2025-01-01 11:00:00");
//        myDBManager.insertFileEvent("test3.txt", "/home/user/downloads/test3.txt", "CREATE", "2025-01-01 12:00:00");
//
//        List<String[]> results = myDBManager.queryByDirectory("/home/user/documents");
//
//        assertAll("Query by directory test",
//                () -> assertEquals(2, results.size(), "Should find 2 files in documents directory"),
//                () -> assertTrue(results.getFirst()[1].startsWith("/home/user/documents"), "First result should be in documents directory"),
//                () -> assertTrue(results.get(1)[1].startsWith("/home/user/documents"), "Second result should be in documents directory"));
//
//    }
//
//    @Test
//    void testQueryByDirectoryNoResults() {
//        myDBManager.insertFileEvent("test.txt", "/home/user/documents/test.txt", "CREATE", "2025-01-01 12:00:00");
//        List<String[]> results = myDBManager.queryByDirectory("/home/user/downloads");
//        assertEquals(0, results.size(), "Should find no files in downloads directory");
//    }
//
//    // Query by date range tests
//    @Test
//    void testQueryByDateRange() {
//        // Insert test data with different dates
//        myDBManager.insertFileEvent("test1.txt", "/path/test1.txt", "CREATE", "2025-01-01 10:00:00");
//        myDBManager.insertFileEvent("test2.txt", "/path/test2.txt", "MODIFY", "2025-01-02 11:00:00");
//        myDBManager.insertFileEvent("test3.txt", "/path/test3.txt", "DELETE", "2025-01-03 12:00:00");
//
//        List<String[]> results = myDBManager.queryByDateRange("2025-01-01 00:00:00", "2025-01-02 23:59:59");
//
//        assertAll("Query by date range test",
//                () -> assertEquals(2, results.size(), "Should find 2 events in data range"),
//                () -> assertTrue(results.getFirst()[3].startsWith("2025-01-0"), "Results should be in correct date range"),
//                () -> assertTrue(results.get(1)[3].startsWith("2025-01-0"), "Results should be in correct date range"));
//    }
//
//    @Test
//    void testQueryByDateRangeNoResults() {
//        myDBManager.insertFileEvent("test.txt", "/path/test.txt", "CREATE", "2025-01-01 12:00:00");
//        List<String[]> results = myDBManager.queryByDateRange("2025-02-01 00:00:00", "2025-02-28 23:59:59");
//        assertEquals(0, results.size(), "Should find no events in different date range");
//
//    }
//
//    // Get all events tests
//    @Test
//    void testGetAllEvents() {
//        // Insert test data
//        myDBManager.insertFileEvent("test1.txt", "/path/test1.txt", "CREATE", "2025-01-01 10:00:00");
//        myDBManager.insertFileEvent("test2.txt", "/path/test2.txt", "MODIFY", "2025-01-01 11:00:00");
//        myDBManager.insertFileEvent("test3.txt", "/path/test3.txt", "DELETE", "2025-01-01 12:00:00");
//
//        List<String[]> results = myDBManager.getAllEvents();
//        assertAll("Get all events test",
//                () -> assertEquals(3, results.size(), "Should return all 3 events"),
//                () -> assertNotNull(results.getFirst()[0], "File name should not be null"),
//                () -> assertNotNull(results.getFirst()[1], "Absolute path should not be null"),
//                () -> assertNotNull(results.getFirst()[2], "Event type should not be null"),
//                () -> assertNotNull(results.getFirst()[3], "Event time should not be null")
//        );
//
//    }
//
//    @Test
//    void testGetAllEventsEmpty() {
//        List<String[]> results = myDBManager.getAllEvents();
//        assertEquals(0, results.size(), "Should return empty list when no events");
//    }
//
//    // Event count tests
//    @Test
//    void testGetEventCount() {
//        assertEquals(0, myDBManager.getEventCount(), "Initial count should be 0");
//        myDBManager.insertFileEvent("test1.txt", "/path/test1.txt", "CREATE");
//        assertEquals(1, myDBManager.getEventCount(), "Count should be 1 after one insert");
//        myDBManager.insertFileEvent("test2.txt", "/path/test2.txt", "MODIFY");
//        assertEquals(2, myDBManager.getEventCount(), "Count should be 2 after two inserts");
//
//    }
//
//    // Edge cases and error handling tests
//    @Test
//    void testInsertNullValues() {
//        // Should throw IllegalArgumentException for null values
//        assertThrows(IllegalArgumentException.class, () -> myDBManager.insertFileEvent(null, null, null, null), "Should throw IllegalArgumentException with null values");
//    }
//
//    @Test
//    void testInsertEmptyStrings() {
//        myDBManager.insertFileEvent("", "", "", "");
//        // Depending on implementation this might succeed or fail
//        // The test verifies the method doesn't crash
//        assertDoesNotThrow(() -> myDBManager.getEventCount(), "Should not crash after inserting empty strings");
//    }
//
//    @Test
//    void testQueryResultsOrder() {
//        // Insert events with specific times to test ordering
//        myDBManager.insertFileEvent("test1.txt", "/path/test1.txt", "CREATE", "2025-01-01 10:00:00");
//        myDBManager.insertFileEvent("test2.txt", "/path/test2.txt", "MODIFY", "2025-01-01 12:00:00");
//        myDBManager.insertFileEvent("test3.txt", "/path/test3.txt", "DELETE", "2025-01-01 11:00:00");
//
//        List<String[]> results = myDBManager.getAllEvents();
//
//        // Results should be ordered by event_time DESC
//        assertTrue(results.get(0)[3].compareTo(results.get(1)[3]) >= 0,
//                "Results should be ordered by time (DESC)");
//        assertTrue(results.get(1)[3].compareTo(results.get(2)[3]) >= 0,
//                "Results should be ordered by time (DESC)");
//
//
//    }
//}