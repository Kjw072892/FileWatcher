import com.tcss.filewatcher.Model.SceneHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for SceneHandler Class.
 * Tests the static methods and behavior of the SceneHandler abstract class.
 *
 * @author salimahafurova
 * @version August 6, 2025
 */
class SceneHandlerTest {

    /**
     * Test implementation of SceneHandler.
     */
    private TestSceneHandler mySceneHandler;

    /**
     * Test logger handler to capture log messages.
     */
    private TestLogHandler myTestLogHandler;

    /**
     * Logger instance for testing.
     */
    private Logger myLogger;

    @BeforeEach
    void setUp() {
        // Reset static state before each test
        SceneHandler.stopWatcher();

        // Create test instance
        mySceneHandler = new TestSceneHandler();

        // Set up logging
        myLogger = Logger.getLogger("Stage status");
        myTestLogHandler = new TestLogHandler();
        myLogger.addHandler(myTestLogHandler);
        myLogger.setLevel(Level.ALL);
    }

    @AfterEach
    void tearDown() {
        // Clean up static state after each test
        SceneHandler.stopWatcher();

        // Remove test log handler
        if (myLogger != null && myTestLogHandler != null) {
            myLogger.removeHandler(myTestLogHandler);
        }
    }

    // Watcher running property tests
    @Test
    void testWatcherRunningPropertyInitialState() {
        assertFalse(SceneHandler.watcherRunningProperty(),
                "Initial watcher running property should be false");
    }

    @Test
    void testWatcherRunningPropertyAfterStart() {
        SceneHandler.startWatcher();
        assertTrue(SceneHandler.watcherRunningProperty(),
                "Watcher running property should be true after start");
    }


    @Test
    void testWatcherRunningPropertyAfterStop() {
        SceneHandler.startWatcher();
        SceneHandler.stopWatcher();
        assertFalse(SceneHandler.watcherRunningProperty(),
                "Watcher running property should be false after stop");
    }

    // Start watcher tests
    @Test
    void testStartWatcher() {
        SceneHandler.startWatcher();

        assertAll("Start watcher test",
                () -> assertTrue(SceneHandler.watcherRunningProperty(), "Should be running after start"),
                () -> assertTrue(SceneHandler.fileWatcherStatus(), "Status should return true"));
    }

    @Test
    void testStartWatcherMultipleTimes() {
        SceneHandler.startWatcher();
        SceneHandler.startWatcher(); // Start again

        assertTrue(SceneHandler.watcherRunningProperty(),
                "Should still be running after multiple starts");
    }


    // Stop watcher tests
    @Test
    void testStopWatcher() {
        SceneHandler.startWatcher();
        SceneHandler.stopWatcher();

        assertAll("Stop watcher test",
                () -> assertFalse(SceneHandler.watcherRunningProperty(), "Should not be running after stop"),
                () -> assertFalse(SceneHandler.fileWatcherStatus(), "Status should return false"));
    }

    @Test
    void testStopWatcherWhenNotRunning() {
        SceneHandler.stopWatcher(); // Stop when not running

        assertFalse(SceneHandler.watcherRunningProperty(),
                "Should remain not running when stopping inactive watcher");
    }


    // Multiple cycles tests
    @Test
    void testMultipleStartStopCycles() {
        for (int i = 0; i < 5; i++) {
            SceneHandler.startWatcher();
            assertTrue(SceneHandler.watcherRunningProperty(), "Should be running in cycle " + i);

            SceneHandler.stopWatcher();
            assertFalse(SceneHandler.watcherRunningProperty(), "Should not be running in cycle " + i);
        }
    }

    @Test
    void testMultiplePauseStartCycles() {
        for (int i = 0; i < 5; i++) {

            SceneHandler.startWatcher();
            assertTrue(SceneHandler.watcherRunningProperty(), "Should be running in cycle " + i);

            SceneHandler.stopWatcher();
            assertFalse(SceneHandler.watcherRunningProperty(), "Should be stopped in cycle " + i);
        }
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        final int numThreads = 10;
        final CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    if (threadId % 2 == 0) {
                        SceneHandler.startWatcher();
                    } else {
                        SceneHandler.stopWatcher();
                    }

                    // Call status method
                    SceneHandler.fileWatcherStatus();

                    SceneHandler.stopWatcher();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "All threads should complete");
        assertFalse(SceneHandler.watcherRunningProperty(), "Should not be running after concurrent access");
    }

    // Comprehensive state validation tests
    @Test
    void testStartWatcherInternalState() {
        SceneHandler.startWatcher();

        assertAll("Start watcher internal state",
                () -> assertTrue(SceneHandler.watcherRunningProperty(), "Running property should be true"),
                () -> assertTrue(SceneHandler.fileWatcherStatus(), "File watcher status should be true"));
    }


    @Test
    void testStopWatcherInternalState() {
        SceneHandler.startWatcher();
        SceneHandler.stopWatcher();

        assertAll("Stop watcher internal state",
                () -> assertFalse(SceneHandler.watcherRunningProperty(), "Running property should be false"),
                () -> assertFalse(SceneHandler.fileWatcherStatus(), "File watcher status should be false"));
    }

    @Test
    void testAddMonitoredDirectory() {
        String path = "/test/path";
        String extension = ".txt";

        SceneHandler.addMonitoredDirectory(path, extension);

        List<String> extensions = SceneHandler.getExtensionsFromDir(path);
        assertAll("Add monitored directory",
                () -> assertTrue(extensions.contains(extension), "Extension should be added"),
                () -> assertEquals(1, extensions.size(), "Should have one extension")
        );
    }

    @Test
    void testAddMonitoredDirectoryNormalizesPath() {
        String path1 = "/test/./path";
        String path2 = "/test/path";
        String extension = ".txt";

        SceneHandler.addMonitoredDirectory(path1, extension);

        List<String> extensions = SceneHandler.getExtensionsFromDir(path2);
        assertNotNull(extensions, "Should find extensions with normalized path");
        assertTrue(extensions.contains(extension), "Should find extension with normalized path");
    }

    @Test
    void testRemoveMonitoredExtension() {
        String path = "/test/path";
        String extension = ".txt";

        SceneHandler.addMonitoredDirectory(path, extension);
        SceneHandler.removeMonitoredExtension(path, extension);

        List<String> extensions = SceneHandler.getExtensionsFromDir(path);
        assertNull(extensions, "Directory should be removed when last extension is removed");
    }

    @Test
    void testRemoveMonitoredDirectory() {
        String path = "/test/path";
        String extension = ".txt";

        SceneHandler.addMonitoredDirectory(path, extension);
        SceneHandler.removeMonitoredDirectory(path);

        List<String> extensions = SceneHandler.getExtensionsFromDir(path);
        assertNull(extensions, "Extensions should be null after directory removal");
    }

    @Test
    void testMultipleExtensionsForDirectory() {
        String path = "/test/path";
        String extension1 = ".txt";
        String extension2 = ".pdf";

        SceneHandler.addMonitoredDirectory(path, extension1);
        SceneHandler.addMonitoredDirectory(path, extension2);

        List<String> extensions = SceneHandler.getExtensionsFromDir(path);
        assertAll("Multiple extensions",
                () -> assertEquals(2, extensions.size(), "Should have two extensions"),
                () -> assertTrue(extensions.contains(extension1), "Should contain first extension"),
                () -> assertTrue(extensions.contains(extension2), "Should contain second extension")
        );
    }

    /**
     * Test implementation of SceneHandler for testing purposes.
     */
    private static class TestSceneHandler extends SceneHandler {
        // No additional implementation needed for testing
    }

    /**
     * Test log handler to capture log messages for verification.
     */
    private static class TestLogHandler extends Handler {
        private final List<LogRecord> myLogRecords = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            myLogRecords.add(record);
        }

        @Override
        public void flush() {
            // No-op
        }

        @Override
        public void close() throws SecurityException {
            myLogRecords.clear();
        }

        public boolean hasLoggedMessage(String message) {
            return myLogRecords.stream()
                    .anyMatch(record -> record.getMessage().contains(message));
        }

        public boolean hasLoggedLevel(Level level) {
            return myLogRecords.stream()
                    .anyMatch(record -> record.getLevel().equals(level));
        }

        public void clear() {
            myLogRecords.clear();
        }

    }
}