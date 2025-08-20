package com.tcss.filewatcher.Model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for FileDirectoryDataBase Class.
 *
 * @author salimahafurova
 * @version August 5, 2025
 */
class FileDirectoryDataBaseTest {

    /**
     * Test database instance.
     */
    private FileDirectoryDataBase myDataBase;

    /**
     * Test data constants.
     */
    private static final String TEST_DATE = "01 Jan, 2025";
    private static final String TEST_TIME = "12:30:45";
    private static final String TEST_FILE_EXTENSION = ".txt";
    private static final String TEST_DIRECTORY = "/home/user/documents";

    @BeforeEach
    void setUp() {
        myDataBase = new FileDirectoryDataBase(false);
        // Clear any existing data
        myDataBase.clearDatabase();
    }

    @AfterEach
    void tearDown() {
        if (myDataBase != null) {
            myDataBase.clearDatabase();
        }
    }

    // Constructor tests
    @Test
    void testConstructor() {
        assertNotNull(myDataBase, "FileDirectoryDataBase should be created successfully");
        assertEquals(0, myDataBase.getTableSize(), "Initial table size should be 0");
    }

    /// Insert directory tests
    @Test
    void testInsertDirectory() {
        myDataBase.insertDirectory(TEST_DATE, TEST_TIME, TEST_FILE_EXTENSION, TEST_DIRECTORY);
        assertEquals(1, myDataBase.getTableSize(), "Table size should be 1 after insert");
    }

    @Test
    void testInsertMultipleDirectories() {
        myDataBase.insertDirectory("01 Jan, 2020", "10:00:00", ".txt", "/path1");
        myDataBase.insertDirectory("02 Jan, 2020", "11:00:00", ".java", "/path2");
        myDataBase.insertDirectory("03 Jan, 2020", "12:00:00", ".pdf", "/path3");
        assertEquals(3, myDataBase.getTableSize(), "Should have 3 entries after multiple inserts");
    }


    @Test
    void testInsertDirectoryWithEmptyStrings() {
        assertThrows(IllegalArgumentException.class, () ->
                myDataBase.insertDirectory("", "", "", ""),
                "Empty strings should throw exception");
    }

    // Remove directory tests
    @Test
    void testRemoveDirectory() {
        // Insert a directory first
        myDataBase.insertDirectory(TEST_DATE, TEST_TIME, TEST_FILE_EXTENSION, TEST_DIRECTORY);
        assertEquals(1, myDataBase.getTableSize(), "Should have 1 entry after insert");

        // Remove it
        myDataBase.removeDirectory(TEST_DIRECTORY, TEST_FILE_EXTENSION);
        assertEquals(0, myDataBase.getTableSize(), "Should have 0 entries after removal");
    }

    @Test
    void testRemoveNonExistentDirectory() {
        // Try to remove a directory that doesn't exist
        myDataBase.removeDirectory("/nonexistent", ".txt");
        assertEquals(0, myDataBase.getTableSize(), "Should still have 0 entries");
    }

    // Clear database tests
    @Test
    void testClearDatabase() {
        // Insert some data first
        myDataBase.insertDirectory(TEST_DATE, TEST_TIME, TEST_FILE_EXTENSION, TEST_DIRECTORY);
        myDataBase.insertDirectory("02 Jan, 2025", "13:30:45", ".java", "/home/user/project");
        assertEquals(2, myDataBase.getTableSize(), "Should have 2 entries before clear");

        myDataBase.clearDatabase();
        assertEquals(0, myDataBase.getTableSize(), "Table size should be 0 after clear");
    }

    @Test
    void testClearEmptyDatabase() {
        myDataBase.clearDatabase();
        assertEquals(0, myDataBase.getTableSize(), "Table size should remain 0");
    }

    // Get table size tests
    @Test
    void testGetTableSize() {
        assertEquals(0, myDataBase.getTableSize(), "Empty table size should be 0");
    }

    @Test
    void testGetTableSizeAfterInserts() {
        assertEquals(0, myDataBase.getTableSize(), "Initial size should be 0");
        myDataBase.insertDirectory(TEST_DATE, TEST_TIME, TEST_FILE_EXTENSION, TEST_DIRECTORY);
        assertEquals(1, myDataBase.getTableSize(), "Size should be 1 after one insert");
        myDataBase.insertDirectory("02 Jan, 2025", "14:30:45", ".java", "/home/user/src");
        assertEquals(2, myDataBase.getTableSize(), "Size should be 2 after two inserts");
    }

    @Test
    void testInsertDirectoryWithInvalidTimeFormat() {
        assertThrows(IllegalArgumentException.class, () -> myDataBase.insertDirectory(TEST_DATE,
                "invalid-time", TEST_FILE_EXTENSION, TEST_DIRECTORY),
                "Invalid time format should throw exception");
    }

    @Test
    void testInsertDirectoryWithInvalidDateFormat() {
        assertThrows(IllegalArgumentException.class, () -> myDataBase.insertDirectory("invalid-date",
                TEST_TIME, TEST_FILE_EXTENSION, TEST_DIRECTORY),
                "Invalid date format should throw exception");
    }

    // Get all entries tests
    @Test
    void testGetAllEntriesEmpty() {
        List<DirectoryEntry> entries = myDataBase.getAllEntries();
        assertNotNull(entries, "getAllEntries should not return null");
        assertTrue(entries.isEmpty(), "getAllEntries should return empty list for empty database");

    }

    @Test
    void testGetAllEntriesSingle() {
        myDataBase.insertDirectory(TEST_DATE, TEST_TIME, TEST_FILE_EXTENSION, TEST_DIRECTORY);
        List<DirectoryEntry> entries = myDataBase.getAllEntries();

        assertAll("Single entry test",
                () -> assertNotNull(entries, "Entries list should not be null"),
                () -> assertEquals(1, entries.size(), "Should have one entry"),
                () -> assertEquals(TEST_DATE, entries.getFirst().getDate(), "Date should match"),
                () -> assertEquals(TEST_TIME, entries.getFirst().getTime(), "Time should match"),
                () -> assertEquals(TEST_FILE_EXTENSION, entries.getFirst().getFileExtension(), "Extension should match"),
                () -> assertEquals(TEST_DIRECTORY, entries.getFirst().getDirectory(), "Directory should match"));
    }

    @Test
    void testGetAllEntriesMultiple() {
        // Insert entries in non-chronological order to test ordering
        myDataBase.insertDirectory("01 Jan, 2025", "10:00:00", ".txt", "/path1");
        myDataBase.insertDirectory("03 Jan, 2025", "10:00:00", ".java", "/path3");
        myDataBase.insertDirectory("02 Jan, 2025", "10:00:00", ".pdf", "/path2");
        myDataBase.insertDirectory("03 Jan, 2025", "09:00:00", ".xml", "/path4");
        List<DirectoryEntry> entries = myDataBase.getAllEntries();
        assertAll("Multiple entries test",
                () -> assertEquals(4, entries.size(), "Should have 4 entries"),
                // Test ordering should be date DESC, time DESC
                () -> assertEquals("03 Jan, 2025", entries.getFirst().getDate(), "First entry should be latest date"),
                () -> assertEquals("10:00:00", entries.getFirst().getTime(), "First entry should be later time"),
                () -> assertEquals("03 Jan, 2025", entries.get(1).getDate(), "Second entry should be same latest date"),
                () -> assertEquals("09:00:00", entries.get(1).getTime(), "Second entry should be earlier time"),
                () -> assertEquals("02 Jan, 2025", entries.get(2).getDate(), "Third entry should be middle date"),
                () -> assertEquals("01 Jan, 2025", entries.get(3).getDate(), "Fourth entry should be earliest date"));
    }

    @Test
    void testGetAllEntriesAfterClear() {
        // Insert data
        myDataBase.insertDirectory(TEST_DATE, TEST_TIME, TEST_FILE_EXTENSION, TEST_DIRECTORY);
        myDataBase.insertDirectory("02 Jan, 2025", "13:30:45", ".java", "/home/user/project");
        assertEquals(2, myDataBase.getAllEntries().size(), "Should have 2 entries before clear");

        // Clear database
        myDataBase.clearDatabase();
        List<DirectoryEntry> entries = myDataBase.getAllEntries();
        assertAll("Entries after clear test",
                () -> assertNotNull(entries, "Entries should not be null after clear"),
                () -> assertTrue(entries.isEmpty(), "Entries should be empty after clear"));
    }

    // Edge test cases
    @Test
    void testInsertDirectoryWithSpecialCharacters() {
        String specialDate = "31 Dec, 2025";
        String specialTime = "23:59:59";
        String specialExtension = ".special-file_ext";
        String specialDirectory = "/path/with spaces/ and-special_chars/СЛАВА УКРАИНА";
        myDataBase.insertDirectory(specialDate, specialTime, specialExtension, specialDirectory);
        assertEquals(1, myDataBase.getTableSize(), "Should have 1 entry with special characters");
        List<DirectoryEntry> entries = myDataBase.getAllEntries();
        DirectoryEntry entry = entries.getFirst();
        assertAll("Special characters test",
                () -> assertEquals(specialDate, entry.getDate()),
                () -> assertEquals(specialTime, entry.getTime()),
                () -> assertEquals(specialExtension, entry.getFileExtension()),
                () -> assertEquals(specialDirectory, entry.getDirectory()));
    }

    @Test
    void testInsertDirectoryWithLongStrings() {
        String longDirectory = "/very/long/path/that/goes/on/and/on/with/many/subdirectories/to/test/string/length/handling";
        myDataBase.insertDirectory(TEST_DATE, TEST_TIME, TEST_FILE_EXTENSION, longDirectory);
        List<DirectoryEntry> entries = myDataBase.getAllEntries();
        assertEquals(longDirectory, entries.getFirst().getDirectory(), "Long directory path should be preserved");
    }

    @Test
    void testDatabasePersistence() {
        // Insert data
        myDataBase.insertDirectory(TEST_DATE, TEST_TIME, TEST_FILE_EXTENSION, TEST_DIRECTORY);
        assertEquals(1, myDataBase.getTableSize(), "Should have 1 entry after insert");

        // Create new database instance (should connect to same database file)
        FileDirectoryDataBase newDatabase = new FileDirectoryDataBase(false);
        assertEquals(1, newDatabase.getTableSize(), "New database instance should see existing data");

        List<DirectoryEntry> entries = newDatabase.getAllEntries();
        assertEquals(TEST_DIRECTORY, entries.getFirst().getDirectory(), "Data should persist across instances");
    }
}
