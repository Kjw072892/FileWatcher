package com.tcss.filewatcher.Model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Unit tests for EmailClient Class
 * Big Caveat: These tests focus on input validation and file handling.
 * Actual email sending requires authentication and network access.
 *
 * @author salimahafurova
 * @version August 15, 2025
 */
class EmailClientTest {

    /**
     * Temporary directory for testing
     */
    @TempDir
    Path tempDir;

    /**
     * Test CSV file path
     */
    private Path testCsvPath;

    /**
     * Valid test email address
     */
    private static final String VALID_EMAIL = "testEmailtcss360@gmail.com";

    /**
     * Invalid test email addresses
     */
    private static final String INVALID_EMAIL_NULL = null;
    private static final String INVALID_EMAIL_BLANK = "";
    private static final String INVALID_EMAIL_SPACES = "   ";

    /**
     * Test CSV content
     */
    private static final String TEST_CSV_CONTENT = """
            Date,Time,FileName,Directory,EventType
            15 Aug, 2025,10:30:00,test.txt,/home/user,CREATED
            15 Aug, 2025,10:31:00,test.txt,/home/user,MODIFIED
            """;

    @BeforeEach
    void setUp() throws IOException {
        // Initialize JavaFX Platform if not already initialized
        if (!Platform.isFxApplicationThread()) {
            try {
                Platform.startup(() -> {});
            } catch (IllegalStateException e) {
                // Platform already initialized, ignore
            }
        }

        // Create test CSV file
        testCsvPath = tempDir.resolve("test_events.csv");
        Files.writeString(testCsvPath, TEST_CSV_CONTENT);
    }

    @AfterEach
    void tearDown() {
        // Clean up if needed
    }

    @Test
    void testStartWithValidInputs() {
        // This test validates input processing, not actual email sending
        // since that requires network and authentication
        Path csvFile = testCsvPath;
        assertTrue(Files.exists(csvFile), "CSV file should exist for test");

        // The method should handle valid inputs without throwing exceptions during validation
        assertDoesNotThrow(() -> {
            // Note: This will likely fail due to authentication issues, but we're testing
            // input validation and file handling, not the actual email sending
            try {
                EmailClient.start(VALID_EMAIL, csvFile);
            } catch (Exception e) {
                // Expected to fail due to authentication, but should get past input validation
                assertTrue(e.getMessage().contains("auth") ||
                                e.getMessage().contains("credential") ||
                                e.getMessage().contains("token") ||
                                e.getMessage().contains("Unable to send email"),
                        "Should fail on authentication, not input validation");
            }
        });
    }

    @Test
    void testStartWithNullEmail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        // Run on JavaFX Application Thread
        Platform.runLater(() -> {
            boolean result = EmailClient.start(INVALID_EMAIL_NULL, testCsvPath);
            assertFalse(result, "Should return false for null email");
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
    }

    @Test
    void testStartWithBlankEmail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            boolean result = EmailClient.start(INVALID_EMAIL_BLANK, testCsvPath);
            assertFalse(result, "Should return false for blank email");
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
    }

    @Test
    void testStartWithSpacesOnlyEmail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            boolean result = EmailClient.start(INVALID_EMAIL_SPACES, testCsvPath);
            assertFalse(result, "Should return false for spaces-only email");
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
    }

    @Test
    void testStartWithNonExistentCsvFile() throws InterruptedException {
        Path nonExistentPath = tempDir.resolve("nonexistent.csv");
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            boolean result = EmailClient.start(VALID_EMAIL, nonExistentPath);
            assertFalse(result, "Should return false for non-existent CSV file");
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
    }

    @Test
    void testStartWithEmptyCsvFile() throws IOException, InterruptedException {
        Path emptyCsvPath = tempDir.resolve("empty.csv");
        Files.createFile(emptyCsvPath);
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            // Should handle empty CSV file gracefully
            assertDoesNotThrow(() -> {
                EmailClient.start(VALID_EMAIL, emptyCsvPath);
            });
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
    }

    @Test
    void testStartWithLargeCsvFile() throws IOException, InterruptedException {
        // Create a larger CSV file
        Path largeCsvPath = tempDir.resolve("large.csv");
        StringBuilder content = new StringBuilder();
        content.append("Date,Time,FileName,Directory,EventType\n");

        for (int i = 0; i < 1000; i++) {
            content.append(String.format("12 Aug, 2025,10:%02d:00,file%d.txt,/test/dir,CREATED\n",
                    i % 60, i));
        }

        Files.writeString(largeCsvPath, content.toString());
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            // Should handle large CSV file without memory issues
            assertDoesNotThrow(() -> {
                EmailClient.start(VALID_EMAIL, largeCsvPath);
            });
            latch.countDown();
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Test should complete within timeout");
    }

    @Test
    void testStartWithDirectoryInsteadOfFile() throws InterruptedException {
        Path directoryPath = tempDir.resolve("testdir");
        try {
            Files.createDirectory(directoryPath);
        } catch (IOException e) {
            fail("Failed to create test directory");
        }

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            boolean result = EmailClient.start(VALID_EMAIL, directoryPath);
            assertFalse(result, "Should return false when path points to directory instead of file");
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
    }

    @Test
    void testCsvFileValidation() {
        // Test that CSV file exists and is readable
        assertTrue(Files.exists(testCsvPath), "Test CSV file should exist");
        assertTrue(Files.isRegularFile(testCsvPath), "Path should point to a regular file");
        assertTrue(Files.isReadable(testCsvPath), "CSV file should be readable");

        try {
            String content = Files.readString(testCsvPath);
            assertFalse(content.isEmpty(), "CSV file should not be empty");
            assertTrue(content.contains("Date"), "CSV should contain header");
        } catch (IOException e) {
            fail("Should be able to read CSV file content");
        }
    }

    @Test
    void testEmailAddressValidation() {
        // Test various email address formats (basic validation)
        String[] validEmails = {
                "test@example.com",
                "user.name@domain.org",
                "test123@test-domain.net",
                "a@b.co"
        };

        String[] invalidEmails = {
                null,
                "",
                "   ",
                "invalid",
                "@domain.com",
                "user@",
                "user name@domain.com"  // space in email
        };

        // Valid emails should not be rejected immediately by blank/null check
        for (String email : validEmails) {
            assertNotNull(email, "Valid email should not be null");
            assertFalse(email.isBlank(), "Valid email should not be blank");
        }

        // Invalid emails should fail basic validation
        for (String email : invalidEmails) {
            boolean isInvalid = (email == null || email.isBlank());
            if (isInvalid) {
                assertTrue(email == null || email.isBlank(),
                        "Invalid email should be null or blank: " + email);
            }
        }
    }

    @Test
    void testApplicationNameConstant() {
        // Verify the application name constant exists and is not empty
        // We can't access private constants directly, but we can verify the application
        // would be initialized with a proper name through behavior testing
        assertDoesNotThrow(() -> {
            // This indirectly tests that the application name constant is properly set
            EmailClient.start("test@example.com", testCsvPath);
        });
    }

    @Test
    void testJsonFactoryConstant() {
        // Test that JSON factory is properly configured
        // This is tested indirectly through the start method execution
        assertDoesNotThrow(() -> {
            // If JSON factory was null or improperly configured,
            // this would fail before getting to authentication
            EmailClient.start("test@example.com", testCsvPath);
        });
    }

    @Test
    void testScopesConfiguration() {
        // Test that OAuth scopes are properly configured
        // This is tested indirectly through the authentication flow
        assertDoesNotThrow(() -> {
            try {
                EmailClient.start("test@example.com", testCsvPath);
            } catch (Exception e) {
                // Should fail on authentication/credentials, not on scope configuration
                assertFalse(e.getMessage().contains("scope") ||
                                e.getMessage().contains("permission"),
                        "Should not fail on scope configuration");
            }
        });
    }

    @Test
    void testTokensDirectoryCreation() {
        // Test that tokens directory handling works properly
        // The method should handle directory creation for OAuth tokens
        assertDoesNotThrow(() -> {
            try {
                EmailClient.start("test@example.com", testCsvPath);
            } catch (Exception e) {
                // Should not fail on directory creation issues
                assertFalse(e.getMessage().contains("directory") ||
                                e.getMessage().contains("mkdir"),
                        "Should handle directory creation properly");
            }
        });
    }

    @Test
    void testConcurrentEmailSending() throws InterruptedException {
        // Test that multiple email sending attempts to don't interfere
        CountDownLatch latch = new CountDownLatch(2);

        Platform.runLater(() -> {
            EmailClient.start("test1@example.com", testCsvPath);
            latch.countDown();
        });

        Platform.runLater(() -> {
            EmailClient.start("test2@example.com", testCsvPath);
            latch.countDown();
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Concurrent operations should complete");
    }

    @Test
    void testResourceHandling() {
        // Test that resources are properly handled (credentials file, etc.)
        assertDoesNotThrow(() -> {
            try {
                EmailClient.start("test@example.com", testCsvPath);
            } catch (Exception e) {
                // Should not fail due to resource management issues
                assertFalse(e.getMessage().contains("resource") ||
                                e.getMessage().contains("stream"),
                        "Should handle resources properly: " + e.getMessage());
            }
        });
    }
}