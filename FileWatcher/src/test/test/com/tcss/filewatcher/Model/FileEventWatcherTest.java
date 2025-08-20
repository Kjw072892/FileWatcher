
package com.tcss.filewatcher.Model;

import com.tcss.filewatcher.Common.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for FileEventWatcher Class
 *
 * @author salimahafurova
 * @version August 6, 2025
 */

class FileEventWatcherTest {

    /**
     * Test watcher instance.
     */

    private FileEventWatcher myWatcher;


    /**
     * Temporary directory for testing
     */

    @TempDir
    Path myTempDir;


    /**
     * Test property change listener.
     */

    private TestPropertyChangeListener myListener;


    /**
     * Test constants.
     */

    private static final String TEST_EXTENSION_TXT = ".txt";
    private static final String TEST_EXTENSION_JAVA = ".java";
    private static final String TEST_EXTENSION_PDF = ".pdf";


    @BeforeEach
    void setUp() {
        myWatcher = new FileEventWatcher(false);
        myListener = new TestPropertyChangeListener();
        myWatcher.addPropertyChangeListener(myListener);
    }

    @AfterEach
    void tearDown() {
        if (myWatcher != null && myWatcher.isWatching()) {
            myWatcher.stopWatching();
        }
        if (myWatcher != null) {
            myWatcher.removePropertyChangeListener(myListener);
        }
    }

    @Test
    void testDefaultConstructor() {
        FileEventWatcher watcher = new FileEventWatcher(false);
        assertAll("Default constructor test",
                () -> assertNotNull(watcher, "Watcher should be created"),
                () -> assertNull(watcher.getAbsolutePath(), "Initial path should be null"),
                () -> assertTrue(watcher.getWatchedExtensions().isEmpty(), "Initial extensions should be empty"),
                () -> assertFalse(watcher.isWatching(), "Should not be watching initially"),
                () -> assertTrue(watcher.getCurrentEvents().isEmpty(), "Should have no events initially"),
                () -> assertFalse(watcher.hasUnsavedEvents(), "Should have no unsaved events initially")
        );
    }

    @Test
    void testConstructorWithPath() {
        String testPath = myTempDir.toString();
        FileEventWatcher watcher = new FileEventWatcher(testPath);
        assertAll("Constructor with path test",
                () -> assertNotNull(watcher, "Watcher should be created"),
                () -> assertEquals(testPath, watcher.getAbsolutePath(), "Path should be set correctly"));
    }

    @Test
    void testConstructorWithNullPath() {
        assertThrows(IllegalArgumentException.class,
                () -> new FileEventWatcher(null),
                "Should throw exception for null path");
    }

    @Test
    void testConstructorWithEmptyPath() {
        assertThrows(IllegalArgumentException.class,
                () -> new FileEventWatcher(""),
                "Should throw exception for empty path");
    }

    @Test
    void testConstructorWithWhitespacePath() {
        assertThrows(IllegalArgumentException.class,
                () -> new FileEventWatcher("     "),
                "Should throw exception for whitespace path");
    }

    // Watch path tests
    @Test
    void testSetWatchPath() {
        String testPath = myTempDir.toString();
        myWatcher.setWatchPath(testPath);
        assertAll("Set watch path test",
                () -> assertEquals(testPath, myWatcher.getAbsolutePath(), "Path should be set"),
                () -> assertEquals(1, myListener.getEventCount(), "Should fire property change event"));
    }

    @Test
    void testSetWatchPathNull() {
        assertThrows(IllegalArgumentException.class,
                () -> myWatcher.setWatchPath(null),
                "Should throw exception for null path");
    }

    @Test
    void testSetWatchPathEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> myWatcher.setWatchPath(""),
                "Should throw exception for empty path");
    }

    @Test
    void testAddWatchPath() throws IOException {
        Path subDir = Files.createDirectories(myTempDir.resolve("subdirectory"));
        myWatcher.addWatchPath(subDir.toString());
        assertEquals(1, myListener.getEventCount(), "Should fire property change event");
    }


    @Test
    void testAddWatchPathNonExistent() {
        String nonExistentPath = myTempDir.resolve("nonexistent").toString();
        // The current implementation doesn't validate path existence in addWatchPath
        // It only validates when starting to watch. So this test should not expect an exception.
        assertDoesNotThrow(() -> myWatcher.addWatchPath(nonExistentPath),
                "addWatchPath should not throw exception for non-existent path");
        assertEquals(1, myListener.getEventCount(), "Should fire property change event");
    }
    @Test
    void testAddWatchPathFile() throws IOException {
        Path file = Files.createFile(myTempDir.resolve("testing.txt"));
        // The current implementation doesn't validate that path is a directory in addWatchPath
        // It only validates when starting to watch. So this test should not expect an exception.
        assertDoesNotThrow(() -> myWatcher.addWatchPath(file.toString()),
                "addWatchPath should not throw exception for file path");
        assertEquals(1, myListener.getEventCount(), "Should fire property change event");
    }

    // Extension management tests
    @Test
    void testAddWatchedExtension() {
        myWatcher.addWatchedExtension(TEST_EXTENSION_TXT);
        assertAll("Add watched extension test",
                () -> assertTrue(myWatcher.getWatchedExtensions().contains(TEST_EXTENSION_TXT), "Extension should be added"),
                () -> assertEquals(1, myWatcher.getWatchedExtensions().size(), "Should have 1 extension"),
                () -> assertEquals(1, myListener.getEventCount(), "Should fire property change event"));
    }

    @Test
    void testAddMultipleWatchedExtensions() {
        myWatcher.addWatchedExtension(TEST_EXTENSION_TXT);
        myWatcher.addWatchedExtension(TEST_EXTENSION_JAVA);
        myWatcher.addWatchedExtension(TEST_EXTENSION_PDF);

        Set<String> extensions = myWatcher.getWatchedExtensions();
        assertAll("Multiple extension test",
                () -> assertEquals(3, extensions.size(), "Should have 3 extensions"),
                () -> assertTrue(extensions.contains(TEST_EXTENSION_TXT), "Should contain .txt"),
                () -> assertTrue(extensions.contains(TEST_EXTENSION_JAVA), "Should contain .java"),
                () -> assertTrue(extensions.contains(TEST_EXTENSION_PDF), "Should contain .pdf"));
    }

    @Test
    void testAddDuplicateWatchedExtension() {
        myWatcher.addWatchedExtension(TEST_EXTENSION_TXT);
        int initialEventCount = myListener.getEventCount();
        myWatcher.addWatchedExtension(TEST_EXTENSION_TXT); // Add same extension again
        assertAll("Duplicate extension test",
                () -> assertEquals(1, myWatcher.getWatchedExtensions().size(), "Should still have 1 extension"),
                () -> assertEquals(initialEventCount, myListener.getEventCount(), "Should not fire additional event"));
    }

    @Test
    void testAddWatchedExtensionNull() {
        // The method returns early for null, should not throw
        assertDoesNotThrow(() -> myWatcher.addWatchedExtension(null),
                "Should handle null extension gracefully");
    }

    @Test
    void testAddWatchedExtensionEmpty() {
        // The method returns early for empty string, should not throw
        assertDoesNotThrow(() -> myWatcher.addWatchedExtension(""),
                "Should handle empty extension gracefully");
    }

    @Test
    void testRemoveWatchedExtension() {
        myWatcher.addWatchedExtension(TEST_EXTENSION_TXT);
        myWatcher.addWatchedExtension(TEST_EXTENSION_JAVA);
        myWatcher.removeWatchedExtension(TEST_EXTENSION_TXT);
        assertAll("Remove extension test",
                () -> assertFalse(myWatcher.getWatchedExtensions().contains(TEST_EXTENSION_TXT), "Should not contain removed extension"),
                () -> assertTrue(myWatcher.getWatchedExtensions().contains(TEST_EXTENSION_JAVA), "Should still contain other extension"),
                () -> assertEquals(1, myWatcher.getWatchedExtensions().size(), "Should have 1 extension remaining"));
    }

    @Test
    void testRemoveNonExistentExtension() {
        myWatcher.addWatchedExtension(TEST_EXTENSION_TXT);
        int initialEventCount = myListener.getEventCount();
        myWatcher.removeWatchedExtension(TEST_EXTENSION_JAVA); // Remove non-existent extension
        assertAll("Remove non-existent extension test",
                () -> assertEquals(1, myWatcher.getWatchedExtensions().size(), "Should still have 1 extension"),
                () -> assertEquals(initialEventCount, myListener.getEventCount(), "Should not fire additional event"));
    }

    @Test
    void testRemoveWatchedExtensionNull() {
        // The method returns early for null, should not throw
        assertDoesNotThrow(() -> myWatcher.removeWatchedExtension(null),
                "Should handle null extension gracefully");
    }

    // Watching state tests
    @Test
    void testStartWatching() {
        myWatcher.setWatchPath(myTempDir.toString());
        assertDoesNotThrow(() -> myWatcher.startWatching(), "Should not throw when starting watching");
        assertTrue(myWatcher.isWatching(), "Should be watching after start");
    }


    @Test
    void testStartWatchingAlreadyWatching() throws IOException {
        myWatcher.setWatchPath(myTempDir.toString());
        myWatcher.startWatching();

        // Should not throw when starting again
        assertDoesNotThrow(() -> myWatcher.startWatching(), "Should handle duplicate start gracefully");
        assertTrue(myWatcher.isWatching(), "Should still be watching");
    }

    @Test
    void testStopWatching() throws IOException {
        myWatcher.setWatchPath(myTempDir.toString());
        myWatcher.startWatching();
        myWatcher.stopWatching();
        assertFalse(myWatcher.isWatching(), "Should not be watching after stop");
    }

    // Event management tests
    @Test
    void testClearCurrentEvents() {
        myWatcher.setWatchPath(myTempDir.toString()); // This triggers a property change event

        myWatcher.clearCurrentEvents();

        assertAll("Clear events test",
                () -> assertTrue(myWatcher.getCurrentEvents().isEmpty(), "Events list should be empty"),
                () -> assertFalse(myWatcher.hasUnsavedEvents(), "Should have no unsaved events"),
                () -> assertEquals(1, myListener.getEventCount(), "Should fire property change event")
        );
    }

    // Property change tests
    @Test
    void testPropertyChangeListener() {
        myWatcher.setWatchPath(myTempDir.toString());
        assertTrue(myListener.getEventCount() > 0, "Should have received property change events");
    }

    @Test
    void testAddRemovePropertyChangeListener() {
        TestPropertyChangeListener newListener = new TestPropertyChangeListener();
        myWatcher.addPropertyChangeListener(newListener);
        myWatcher.setWatchPath(myTempDir.toString());
        assertTrue(newListener.getEventCount() > 0, "New listener should receive events");
        myWatcher.removePropertyChangeListener(newListener);
        int eventCount = newListener.getEventCount();
        myWatcher.addWatchedExtension(TEST_EXTENSION_TXT);
        assertEquals(eventCount, newListener.getEventCount(), "Removed listener should not receive new events");
    }

    @Test
    void testPropertyChangeForSpecificProperty() {
        TestPropertyChangeListener specificListener = new TestPropertyChangeListener();
        myWatcher.addPropertyChangeListener(Properties.ADDED_EXTENSION.toString(), specificListener);
        myWatcher.setWatchPath(myTempDir.toString()); // Should not trigger specific listener
        assertEquals(0, specificListener.getEventCount(), "Specific listener should not receive unrelated events");
        myWatcher.addWatchedExtension(TEST_EXTENSION_TXT); // Should trigger specific listener
        assertEquals(1, specificListener.getEventCount(), "Specific listener should receive matching events");
    }

    // ToString test
    @Test
    void testToString() {
        myWatcher.setWatchPath(myTempDir.toString());
        myWatcher.addWatchedExtension(TEST_EXTENSION_TXT);
        String toString = myWatcher.toString();
        assertAll("ToString test",
                () -> assertNotNull(toString, "ToString should not be null"),
                () -> assertTrue(toString.contains(myTempDir.toString()), "Should contain watch path"),
                () -> assertTrue(toString.contains(TEST_EXTENSION_TXT), "Should contain extension"),
                () -> assertTrue(toString.contains(String.valueOf(myWatcher.isWatching())), "Should contain watching state"),
                () -> assertTrue(toString.contains(String.valueOf(myWatcher.getCurrentEvents().size())), "Should contain events count"),
                () -> assertTrue(toString.contains(String.valueOf(myWatcher.hasUnsavedEvents())), "Should contain unsaved events state")
        );
    }

    // Testing the FileEvent inner class
    @Test
    void testFileEventCreation() {
        FileEventWatcher.FileEvent event = new FileEventWatcher.FileEvent(
                "test.txt", "/path/to/test.txt", "CREATE", "12:00:00"
        );
        assertAll("FileEvent creation test",
                () -> assertNotNull(event, "Event should be created"),
                () -> assertNotNull(event.toString(), "Event toString should not be null"),
                () -> assertTrue(event.toString().contains("test.txt"), "Should contain filename"),
                () -> assertTrue(event.toString().contains("/path/to/test.txt"), "Should contain path"),
                () -> assertTrue(event.toString().contains("CREATE"), "Should contain event type"),
                () -> assertTrue(event.toString().contains("12:00:00"), "Should contain time")
        );
    }

    @Test
    void testFileEventEquals() {
        FileEventWatcher.FileEvent event1 = new FileEventWatcher.FileEvent(
                "test.txt", "/path/to/test.txt", "CREATE", "12:00:00"
        );
        FileEventWatcher.FileEvent event2 = new FileEventWatcher.FileEvent(
                "test.txt", "/path/to/test.txt", "CREATE", "12:00:00"
        );
        FileEventWatcher.FileEvent event3 = new FileEventWatcher.FileEvent(
                "test2.txt", "/path/to/test2.txt", "DELETE", "12:01:00"
        );

        assertAll("FileEvent equals test",
                () -> assertEquals(event1, event2, "Identical events should be equal"),
                () -> assertNotEquals(event1, event3, "Different events should not be equal"),
                () -> assertNotEquals(null, event1, "Event should not equal null"),
                () -> assertNotEquals("not an event", event1, "Event should not equal other types"),
                () -> assertEquals(event1.hashCode(), event2.hashCode(), "Equal events should have same hash")
        );
    }

    @Test
    void testFileEventToString() {
        FileEventWatcher.FileEvent event = new FileEventWatcher.FileEvent(
                "test.txt", "/path/to/test.txt", "CREATE", "2025-08-06 12:00:00");
        String toString = event.toString();
        assertAll("FileEvent toString test",
                () -> assertNotNull(toString, "ToString should not be null"),
                () -> assertTrue(toString.contains("test.txt"), "Should contain filename"),
                () -> assertTrue(toString.contains("/path/to/test.txt"), "Should contain path"),
                () -> assertTrue(toString.contains("CREATE"), "Should contain event type"),
                () -> assertTrue(toString.contains("2025-08-06 12:00:00"), "Should contain timestamp"));
    }


    private static class TestPropertyChangeListener implements PropertyChangeListener {
        private final List<PropertyChangeEvent> myEvents = new ArrayList<>();
        private final CountDownLatch myLatch = new CountDownLatch(1);

        @Override
        public void propertyChange(PropertyChangeEvent theEvt) {
            myEvents.add(theEvt);
            myLatch.countDown();
        }

        public int getEventCount() {
            return myEvents.size();
        }
    }
}
