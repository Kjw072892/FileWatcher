package com.tcss.filewatcher.Model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SceneHandler Class.
 * Tests the static methods and behavior of the SceneHandler abstract class.
 *
 * @author salimahafurova
 * @version August 6, 2025
 */
class SceneHandlerTest {

    /** Test implementation of SceneHandler. */
    private TestSceneHandler mySceneHandler;

    /** Test logger handler to capture log messages. */
    private TestLogHandler myTestLogHandler;

    /** Logger instance for testing. */
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
    void testWatcherRunningPropertyAfterPause() {
        SceneHandler.pauseWatcher();
        assertTrue(SceneHandler.watcherRunningProperty(),
                "Watcher running property should be true when paused");
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

    @Test
    void testStartWatcherAfterPause() {
        SceneHandler.pauseWatcher();
        SceneHandler.startWatcher();

        assertTrue(SceneHandler.watcherRunningProperty(),
                "Should be running after start from paused state");
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

    @Test
    void testStopWatcherFromPausedState() {
        SceneHandler.pauseWatcher();
        SceneHandler.stopWatcher();

        assertFalse(SceneHandler.watcherRunningProperty(),
                "Should not be running after stopping from paused state");
    }

    // Pause watcher tests
    @Test
    void testPauseWatcher() {
        SceneHandler.pauseWatcher();

        assertTrue(SceneHandler.watcherRunningProperty(),
                "Should be considered running when paused");
    }

    @Test
    void testPauseWatcherAfterStart() {
        SceneHandler.startWatcher();
        SceneHandler.pauseWatcher();

        assertTrue(SceneHandler.watcherRunningProperty(),
                "Should be considered running when paused after start");
    }

    @Test
    void testPauseWatcherMultipleTimes() {
        SceneHandler.pauseWatcher();
        SceneHandler.pauseWatcher(); // Pause again

        assertTrue(SceneHandler.watcherRunningProperty(),
                "Should still be considered running after multiple pauses");
    }

    // File watcher status tests
    @Test
    void testFileWatcherStatusWhenNotRunning() {
        boolean status = SceneHandler.fileWatcherStatus();

        assertAll("Status when not running test",
                () -> assertFalse(status, "Status should return false when not running"),
                () -> assertTrue(myTestLogHandler.hasLoggedMessage("is not running"),
                        "Should log 'is not running' message"));
    }

    @Test
    void testFileWatcherStatusWhenRunning() {
        SceneHandler.startWatcher();
        boolean status = SceneHandler.fileWatcherStatus();

        assertAll("Status when running test",
                () -> assertTrue(status, "Status should return true when running"),
                () -> assertTrue(myTestLogHandler.hasLoggedMessage("is running"),
                        "Should log 'is running' message"));
    }

    @Test
    void testFileWatcherStatusWhenPaused() {
        SceneHandler.pauseWatcher();
        boolean status = SceneHandler.fileWatcherStatus();

        assertAll("Status when paused test",
                () -> assertTrue(status, "Status should return true when paused"),
                () -> assertTrue(myTestLogHandler.hasLoggedMessage("is running"),
                        "Should log 'is running' message for paused state"));
    }

    @Test
    void testFileWatcherStatusLogging() {
        // Test logging for not running state
        SceneHandler.fileWatcherStatus();
        assertTrue(myTestLogHandler.hasLoggedLevel(Level.INFO), "Should log at INFO level");

        myTestLogHandler.clear();

        // Test logging for running state
        SceneHandler.startWatcher();
        SceneHandler.fileWatcherStatus();
        assertTrue(myTestLogHandler.hasLoggedLevel(Level.INFO), "Should log at INFO level when running");
    }





    // State transition tests
    @Test
    void testWatcherStateTransitions() {
        // Initial state
        assertFalse(SceneHandler.watcherRunningProperty(), "Should start not running");

        // Start -> Stop
        SceneHandler.startWatcher();
        assertTrue(SceneHandler.watcherRunningProperty(), "Should be running after start");

        SceneHandler.stopWatcher();
        assertFalse(SceneHandler.watcherRunningProperty(), "Should not be running after stop");

        // Start -> Pause -> Stop
        SceneHandler.startWatcher();
        SceneHandler.pauseWatcher();
        assertTrue(SceneHandler.watcherRunningProperty(), "Should be running when paused");

        SceneHandler.stopWatcher();
        assertFalse(SceneHandler.watcherRunningProperty(), "Should not be running after stop from pause");

        // Pause -> Start
        SceneHandler.pauseWatcher();
        SceneHandler.startWatcher();
        assertTrue(SceneHandler.watcherRunningProperty(), "Should be running after start from pause");
    }

    // Property change event tests (testing the abstract implementation)
    @Test
    void testPropertyChangeEvent() {
        PropertyChangeEvent event = new PropertyChangeEvent(this, "testProperty", "oldValue", "newValue");

        // Should not throw exception when calling propertyChange
        assertDoesNotThrow(() -> mySceneHandler.propertyChange(event),
                "Property change should not throw exception");
    }

    @Test
    void testPropertyChangeEventWithNullValues() {
        PropertyChangeEvent event = new PropertyChangeEvent(this, null, null, null);

        assertDoesNotThrow(() -> mySceneHandler.propertyChange(event),
                "Property change with null values should not throw exception");
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
            SceneHandler.pauseWatcher();
            assertTrue(SceneHandler.watcherRunningProperty(), "Should be paused in cycle " + i);

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
                        SceneHandler.pauseWatcher();
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
    void testPauseWatcherInternalState() {
        SceneHandler.pauseWatcher();

        assertAll("Pause watcher internal state",
                () -> assertTrue(SceneHandler.watcherRunningProperty(), "Running property should be true when paused"),
                () -> assertTrue(SceneHandler.fileWatcherStatus(), "File watcher status should be true when paused"));
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
    void testComplexStateSequence() {
        // Test a complex sequence of state changes
        SceneHandler.startWatcher();
        assertTrue(SceneHandler.watcherRunningProperty(), "Step 1: Should be running");

        SceneHandler.pauseWatcher();
        assertTrue(SceneHandler.watcherRunningProperty(), "Step 2: Should be running when paused");

        SceneHandler.startWatcher();
        assertTrue(SceneHandler.watcherRunningProperty(), "Step 3: Should be running after restart from pause");

        SceneHandler.stopWatcher();
        assertFalse(SceneHandler.watcherRunningProperty(), "Step 4: Should not be running after stop");

        SceneHandler.pauseWatcher();
        assertTrue(SceneHandler.watcherRunningProperty(), "Step 5: Should be running when paused from stopped");

        SceneHandler.stopWatcher();
        assertFalse(SceneHandler.watcherRunningProperty(), "Step 6: Should not be running after final stop");
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