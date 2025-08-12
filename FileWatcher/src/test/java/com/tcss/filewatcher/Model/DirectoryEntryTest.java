//package com.tcss.filewatcher.Model;
//
//import javafx.beans.property.SimpleStringProperty;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertAll;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertSame;
//import static org.junit.jupiter.api.Assertions.assertNotSame;
//
///**
// * Unit tests for DirectoryEntry Class
// *
// * @author salimahafurova
// * @version August 04, 2025
// */
//class DirectoryEntryTest {
//
//    /**
//     * Test data constants.
//     */
//    private static final String TEST_DATE = "2025-01-01";
//    private static final String TEST_TIME = "12:30:45";
//    private static final String TEST_FILE_EXTENSION = ".txt";
//    private static final String TEST_DIRECTORY = "/home/user/documents";
//    private static final String TEST_MODIFICATION_TYPE = "CREATE";
//
//    /**
//     * Test DirectoryEntry instance.
//     */
//    private DirectoryEntry myDirectoryEntry;
//
//    @BeforeEach
//    void setUp() {
//        myDirectoryEntry = new DirectoryEntry(TEST_DATE, TEST_TIME, TEST_FILE_EXTENSION, TEST_DIRECTORY);
//    }
//
//    // Constructor tests
//    @Test
//    void testConstructorWithModificationType() {
//        DirectoryEntry entry = new DirectoryEntry(TEST_MODIFICATION_TYPE);
//        assertAll("Constructor with modification type test",
//                () -> assertNotNull(entry, "DirectoryEntry should be created"),
//                () -> assertEquals(TEST_MODIFICATION_TYPE, entry.getModificationType(),
//                        "Modification type should match"),
//                () -> assertNotNull(entry.modificationType(),
//                        "Modification type property should not be null")
//        );
//    }
//
//    @Test
//    void testConstructorWithAllParameters() {
//        assertAll("Constructor with all parameters test",
//                () -> assertNotNull(myDirectoryEntry, "DirectoryEntry should be created"),
//                () -> assertEquals(TEST_DATE, myDirectoryEntry.getDate(), "Date should match"),
//                () -> assertEquals(TEST_TIME, myDirectoryEntry.getTime(), "Time should match"),
//                () -> assertEquals(TEST_FILE_EXTENSION, myDirectoryEntry.getFileExtension(),
//                        "File extension should match"),
//                () -> assertEquals(TEST_DIRECTORY, myDirectoryEntry.getDirectory(),
//                        "Directory should match"),
//                () -> assertNull(myDirectoryEntry.getModificationType(),
//                        "Modification type should be null when not provided")
//        );
//    }
//
//    @Test
//    void testConstructorWithNullValues() {
//        DirectoryEntry entry = new DirectoryEntry(null, null, null, null);
//        assertAll("Constructor with null values test",
//                () -> assertNotNull(entry, "DirectoryEntry should be created with null values"),
//                () -> assertNull(entry.getDate(), "Date should be null"),
//                () -> assertNull(entry.getTime(), "Time should be null"),
//                () -> assertNull(entry.getFileExtension(), "File extension should be null"),
//                () -> assertNull(entry.getDirectory(), "Directory should be null")
//        );
//    }
//
//    // Getter tests
//    @Test
//    void testGetDate() {
//        assertEquals(TEST_DATE, myDirectoryEntry.getDate(),
//                "getDate should return the correct date");
//    }
//
//    @Test
//    void testGetTime() {
//        assertEquals(TEST_TIME, myDirectoryEntry.getTime(),
//                "getTime should return the correct time");
//    }
//
//    @Test
//    void testGetDirectory() {
//        assertEquals(TEST_DIRECTORY, myDirectoryEntry.getDirectory(),
//                "getDirectory should return the correct directory");
//    }
//
//    @Test
//    void testGetFileExtension() {
//        assertEquals(TEST_FILE_EXTENSION, myDirectoryEntry.getFileExtension(),
//                "getFileExtension should return the correct file extension");
//    }
//
//    @Test
//    void testGetModificationType() {
//        DirectoryEntry entryWithModType = new DirectoryEntry(TEST_MODIFICATION_TYPE);
//        assertEquals(TEST_MODIFICATION_TYPE, entryWithModType.getModificationType(),
//                "getModificationType should return the correct modification type");
//    }
//
//    @Test
//    void testGetModificationTypeNull() {
//        assertNull(myDirectoryEntry.getModificationType(),
//                "getModificationType should return null when not set");
//    }
//
//    // Property tests
//    @Test
//    void testDateProperty() {
//        SimpleStringProperty dateProperty = myDirectoryEntry.dateProperty();
//        assertAll("Date property test",
//                () -> assertNotNull(dateProperty, "Date property should not be null"),
//                () -> assertEquals(TEST_DATE, dateProperty.get(), "Date property value should match"),
//                () -> assertSame(dateProperty, myDirectoryEntry.dateProperty(),
//                        "Should return same property instance"));
//    }
//
//    @Test
//    void testTimeProperty() {
//        SimpleStringProperty timeProperty = myDirectoryEntry.timeProperty();
//        assertAll("Time property test",
//                () -> assertNotNull(timeProperty, "Time property should not be null"),
//                () -> assertEquals(TEST_TIME, timeProperty.get(), "Time property value should match"),
//                () -> assertSame(timeProperty, myDirectoryEntry.timeProperty(),
//                        "Should return same property instance"));
//    }
//
//    @Test
//    void testDirectoryProperty() {
//        SimpleStringProperty directoryProperty = myDirectoryEntry.directoryProperty();
//        assertAll("Directory property test",
//                () -> assertNotNull(directoryProperty, "Directory property should not be null"),
//                () -> assertEquals(TEST_DIRECTORY, directoryProperty.get(),
//                        "Directory property value should match"),
//                () -> assertSame(directoryProperty, myDirectoryEntry.directoryProperty(),
//                        "Should return same property instance"));
//    }
//
//    @Test
//    void setTestModificationTypeProperty() {
//        DirectoryEntry entryWithModType = new DirectoryEntry(TEST_MODIFICATION_TYPE);
//        SimpleStringProperty modificationProperty = entryWithModType.modificationType();
//        assertAll("Modification type property test",
//                () -> assertNotNull(modificationProperty, "Modification type property should not be null"),
//                () -> assertEquals(TEST_MODIFICATION_TYPE, modificationProperty.get(),
//                        "Modification type property value should match"),
//                () -> assertSame(modificationProperty, entryWithModType.modificationType(),
//                        "Should return same property instance"));
//    }
//
//    // Property binding and updates tests
//    @Test
//    void testPropertyUpdates() {
//        // Test tht property changes are reflected in getter
//        SimpleStringProperty dateProperty = myDirectoryEntry.dateProperty();
//        String newDate = "2025-02-01";
//        dateProperty.set(newDate);
//        assertEquals(newDate, myDirectoryEntry.getDate(),
//                "Getter should reflect property changes");
//    }
//
//    @Test
//    void testMultiplePropertiesIndependence() {
//        // Test that different properties are independent
//        SimpleStringProperty dateProperty = myDirectoryEntry.dateProperty();
//        SimpleStringProperty timeProperty = myDirectoryEntry.timeProperty();
//        assertNotSame(dateProperty, timeProperty,
//                "Different properties should be different instances");
//
//    }
//
//    // Edge cases
//    @Test
//    void testEmptyStringValues() {
//        DirectoryEntry entry = new DirectoryEntry("", "", "", "");
//        assertAll("Empty string values test",
//                () -> assertEquals("", entry.getDate(), "Date should be empty string"),
//                () -> assertEquals("", entry.getTime(), "Time should be empty string"),
//                () -> assertEquals("", entry.getFileExtension(), "File extension should be empty string"),
//                () -> assertEquals("", entry.getDirectory(), "Directory should be empty string"));
//    }
//
//    @Test
//    void testWhiteSpaceValues() {
//        String whitespace = "   ";
//        DirectoryEntry entry = new DirectoryEntry(whitespace, whitespace, whitespace, whitespace);
//        assertAll("Whitespace values test",
//                () -> assertEquals(whitespace, entry.getDate(), "Date should preserve whitespace"),
//                () -> assertEquals(whitespace, entry.getTime(), "Time should preserve whitespace"),
//                () -> assertEquals(whitespace, entry.getFileExtension(),
//                        "File extension should preserve whitespace"),
//                () -> assertEquals(whitespace, entry.getDirectory(),
//                        "Directory should preserve whitespace")
//        );
//    }
//
//    @Test
//    void testSpecialCharacters() {
//        String specialChars = "test~!@#$%^&*()";
//        DirectoryEntry entry = new DirectoryEntry(specialChars, specialChars, specialChars, specialChars);
//
//        assertAll("Special characters test",
//                () -> assertEquals(specialChars, entry.getDate(), "Date should handle special characters"),
//                () -> assertEquals(specialChars, entry.getTime(), "Time should handle special characters"),
//                () -> assertEquals(specialChars, entry.getFileExtension(),
//                        "File extension should handle special characters"),
//                () -> assertEquals(specialChars, entry.getDirectory(),
//                        "Directory should handle special characters"));
//
//    }
//
//    @Test
//    void testLongString() {
//        String longString = "a".repeat(1000);
//        DirectoryEntry entry = new DirectoryEntry(longString, longString, longString, longString);
//        assertAll("Long strings test",
//                () -> assertEquals(longString, entry.getDate(), "Date should handle long strings"),
//                () -> assertEquals(longString, entry.getTime(), "Time should handle long strings"),
//                () -> assertEquals(longString, entry.getFileExtension(),
//                        "File extension should handle long strings"),
//                () -> assertEquals(longString, entry.getDirectory(),
//                        "Directory should handle long strings"));
//    }
//
//    // JavaFX Property behavior test
//    @Test
//    void testPropertyNotNull() {
//        // Ensure all properties are initialized and not null
//        assertAll("Properties not null test",
//                () -> assertNotNull(myDirectoryEntry.dateProperty(), "Date property should not be null"),
//                () -> assertNotNull(myDirectoryEntry.timeProperty(), "Time property should not be null"),
//                () -> assertNotNull(myDirectoryEntry.fileExtensionProperty(),
//                        "File extension property should not be null"),
//                () -> assertNotNull(myDirectoryEntry.directoryProperty(),
//                        "Directory property should not be null"));
//    }
//
//    @Test
//    void testPropertyConsistency() {
//        // Test the properties remain consistent across multiple calls
//        SimpleStringProperty dateProperty1 = myDirectoryEntry.dateProperty();
//        SimpleStringProperty dateProperty2 = myDirectoryEntry.dateProperty();
//        assertSame(dateProperty1, dateProperty2,
//                "Multiple calls to dateProperty should return same instance");
//    }
//}