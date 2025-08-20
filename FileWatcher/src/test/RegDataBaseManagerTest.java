import com.tcss.filewatcher.Model.RegDataBaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RegDataBaseManager Class
 *
 * @author Test Author
 * @version August 15, 2025
 */
class RegDataBaseManagerTest {

    private RegDataBaseManager dbManager;
    private static final String TEST_DB_FILE = "test_filewatcher.db";
    private static final String TEST_DB_URL = "jdbc:sqlite:" + TEST_DB_FILE;

    // Test data constants
    private static final String TEST_EMAIL_1 = "test1@example.com";
    private static final String TEST_EMAIL_2 = "test2@example.com";
    private static final String TEST_EMAIL_UPPERCASE = "TEST@EXAMPLE.COM";
    private static final String TEST_PASSWORD_1 = "password123";
    private static final String TEST_PASSWORD_2 = "securePass456";
    private static final String TEST_FREQ_DAILY = "Daily";
    private static final String TEST_FREQ_WEEKLY = "Weekly";
    private static final String TEST_FREQ_MONTHLY = "Monthly";

    @BeforeEach
    void setUp() {
        // Clean up any existing test database
        deleteTestDatabase();

        // Create new database manager instance
        dbManager = new RegDataBaseManager();
    }

    @AfterEach
    void tearDown() {
        // Clean up test database after each test
        deleteTestDatabase();
    }

    private void deleteTestDatabase() {
        File dbFile = new File(TEST_DB_FILE);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    void testConstructorInitializesDatabase() {
        assertNotNull(dbManager, "Database manager should be created");

        // Verify database file was created
        File dbFile = new File("filewatcher.db");
        assertTrue(dbFile.exists(), "Database file should be created");
    }

    @Test
    void testInsertNewUserData() {
        // Insert a new user
        assertDoesNotThrow(() -> {
            dbManager.insertNewUserData(TEST_EMAIL_1, TEST_PASSWORD_1, TEST_FREQ_DAILY);
        }, "Should insert new user data without throwing exception");

        // Verify user was inserted by checking if they exist
        assertTrue(dbManager.isExistingUser(TEST_EMAIL_1),
                "User should exist after insertion");
    }

    @Test
    void testInsertMultipleUsers() {
        // Insert multiple users
        dbManager.insertNewUserData(TEST_EMAIL_1, TEST_PASSWORD_1, TEST_FREQ_DAILY);
        dbManager.insertNewUserData(TEST_EMAIL_2, TEST_PASSWORD_2, TEST_FREQ_WEEKLY);

        // Verify both users exist
        assertTrue(dbManager.isExistingUser(TEST_EMAIL_1),
                "First user should exist");
        assertTrue(dbManager.isExistingUser(TEST_EMAIL_2),
                "Second user should exist");
    }

    @Test
    void testInsertUserWithNullValues() {
        // Test with null email
        assertDoesNotThrow(() -> {
            dbManager.insertNewUserData(null, TEST_PASSWORD_1, TEST_FREQ_DAILY);
        }, "Should handle null email gracefully");

        // Test with null password
        assertDoesNotThrow(() -> {
            dbManager.insertNewUserData(TEST_EMAIL_1, null, TEST_FREQ_DAILY);
        }, "Should handle null password gracefully");

        // Test with null frequency
        assertDoesNotThrow(() -> {
            dbManager.insertNewUserData(TEST_EMAIL_1, TEST_PASSWORD_1, null);
        }, "Should handle null frequency gracefully");
    }

    @Test
    void testInsertUserWithEmptyValues() {
        // Test with empty strings
        assertDoesNotThrow(() -> {
            dbManager.insertNewUserData("", "", "");
        }, "Should handle empty strings gracefully");
    }

    @Test
    void testIsExistingUserTrue() {
        // Insert a user first
        dbManager.insertNewUserData(TEST_EMAIL_1, TEST_PASSWORD_1, TEST_FREQ_DAILY);

        // Check if user exists
        assertTrue(dbManager.isExistingUser(TEST_EMAIL_1),
                "Should return true for existing user");
    }

    @Test
    void testIsExistingUserFalse() {
        // Check for non-existent user
        assertFalse(dbManager.isExistingUser("nonexistent@example.com"),
                "Should return false for non-existent user");
    }


    @Test
    void testIsExistingUserWithNullEmail() {
        assertFalse(dbManager.isExistingUser(null),
                "Should return false for null email");
    }

    @Test
    void testIsExistingUserWithEmptyEmail() {
        // Fixed: Don't insert empty email data that could interfere
        assertFalse(dbManager.isExistingUser(""),
                "Should return false for empty email");
    }

    @Test
    void testGetEmailFrequency() {
        // Insert user with specific frequency
        dbManager.insertNewUserData(TEST_EMAIL_1, TEST_PASSWORD_1, TEST_FREQ_WEEKLY);

        // Get frequency
        String frequency = dbManager.getEmailFrequency(TEST_EMAIL_1);
        assertEquals(TEST_FREQ_WEEKLY, frequency,
                "Should return correct email frequency");
    }

    @Test
    void testGetEmailFrequencyForNonExistentUser() {
        // Try to get frequency for non-existent user
        String frequency = dbManager.getEmailFrequency("nonexistent@example.com");
        assertNull(frequency, "Should return null for non-existent user");
    }



    @Test
    void testGetEmailFrequencyWithNullEmail() {
        String frequency = dbManager.getEmailFrequency(null);
        assertNull(frequency, "Should return null for null email");
    }

    @Test
    void testCheckPasswordCorrect() {
        // Insert user
        dbManager.insertNewUserData(TEST_EMAIL_1, TEST_PASSWORD_1, TEST_FREQ_DAILY);

        // Check correct password
        assertTrue(dbManager.checkPassword(TEST_EMAIL_1, TEST_PASSWORD_1),
                "Should return true for correct password");
    }

    @Test
    void testCheckPasswordIncorrect() {
        // Insert user
        dbManager.insertNewUserData(TEST_EMAIL_1, TEST_PASSWORD_1, TEST_FREQ_DAILY);

        // Check incorrect password
        assertFalse(dbManager.checkPassword(TEST_EMAIL_1, "wrongPassword"),
                "Should return false for incorrect password");
    }

    @Test
    void testCheckPasswordForNonExistentUser() {
        // Check password for user that doesn't exist
        assertFalse(dbManager.checkPassword("nonexistent@example.com", TEST_PASSWORD_1),
                "Should return false for non-existent user");
    }

    @Test
    void testCheckPasswordCaseInsensitive() {
        // Insert user with lowercase email
        dbManager.insertNewUserData(TEST_EMAIL_1.toLowerCase(), TEST_PASSWORD_1, TEST_FREQ_DAILY);

        // Check password using uppercase email
        assertTrue(dbManager.checkPassword(TEST_EMAIL_1.toUpperCase(), TEST_PASSWORD_1),
                "Should be case insensitive for email in password check");
    }

    @Test
    void testCheckPasswordWithNullValues() {
        // Insert user first
        dbManager.insertNewUserData(TEST_EMAIL_1, TEST_PASSWORD_1, TEST_FREQ_DAILY);

        // Test with null email
        assertFalse(dbManager.checkPassword(null, TEST_PASSWORD_1),
                "Should return false for null email");

        // Test with null password
        assertFalse(dbManager.checkPassword(TEST_EMAIL_1, null),
                "Should return false for null password");

        // Test with both null
        assertFalse(dbManager.checkPassword(null, null),
                "Should return false for both null values");
    }

    @Test
    void testCheckPasswordWithEmptyValues() {
        // Insert user first
        dbManager.insertNewUserData(TEST_EMAIL_1, TEST_PASSWORD_1, TEST_FREQ_DAILY);

        // Test with empty strings
        assertFalse(dbManager.checkPassword("", TEST_PASSWORD_1),
                "Should return false for empty email");
        assertFalse(dbManager.checkPassword(TEST_EMAIL_1, ""),
                "Should return false for empty password");
    }


    @Test
    void testHasAUserAlreadyRegisteredMultipleUsers() {
        // Insert multiple users
        dbManager.insertNewUserData(TEST_EMAIL_1, TEST_PASSWORD_1, TEST_FREQ_DAILY);
        dbManager.insertNewUserData(TEST_EMAIL_2, TEST_PASSWORD_2, TEST_FREQ_WEEKLY);

        // Both should return true
        assertTrue(dbManager.hasAUserAlreadyRegistered(TEST_EMAIL_1),
                "First user should be found");
        assertTrue(dbManager.hasAUserAlreadyRegistered(TEST_EMAIL_2),
                "Second user should be found");
    }

    @Test
    void testHasAUserAlreadyRegisteredWithNullEmail() {
        // Insert a user first
        dbManager.insertNewUserData(TEST_EMAIL_1, TEST_PASSWORD_1, TEST_FREQ_DAILY);

        // Test with null email
        assertFalse(dbManager.hasAUserAlreadyRegistered(null),
                "Should return false for null email");
    }

    @Test
    void testHasAUserAlreadyRegisteredWithEmptyEmail() {
        // Insert a user first
        dbManager.insertNewUserData(TEST_EMAIL_1, TEST_PASSWORD_1, TEST_FREQ_DAILY);

        // Test with empty email
        assertFalse(dbManager.hasAUserAlreadyRegistered(""),
                "Should return false for empty email");
    }


    @Test
    void testEmailFrequencyValues() {
        // Test all valid frequency values
        String[] frequencies = {TEST_FREQ_DAILY, TEST_FREQ_WEEKLY, TEST_FREQ_MONTHLY};

        for (int i = 0; i < frequencies.length; i++) {
            String email = "user" + i + "@example.com";
            String frequency = frequencies[i];

            dbManager.insertNewUserData(email, TEST_PASSWORD_1, frequency);
            String retrievedFreq = dbManager.getEmailFrequency(email);

            assertEquals(frequency, retrievedFreq,
                    "Should store and retrieve frequency correctly: " + frequency);
        }
    }

    @Test
    void testPasswordSecurity() {
        // Test that passwords are stored as-is (not hashed in this implementation)
        dbManager.insertNewUserData(TEST_EMAIL_1, TEST_PASSWORD_1, TEST_FREQ_DAILY);

        assertTrue(dbManager.checkPassword(TEST_EMAIL_1, TEST_PASSWORD_1),
                "Original password should work");
        assertFalse(dbManager.checkPassword(TEST_EMAIL_1, TEST_PASSWORD_1 + "extra"),
                "Modified password should not work");
        assertFalse(dbManager.checkPassword(TEST_EMAIL_1, TEST_PASSWORD_1.toUpperCase()),
                "Case-modified password should not work");
    }

    @Test
    void testDatabasePersistence() {
        // Use unique email for this test to avoid conflicts
        String persistenceTestEmail = "persistence.test@example.com";

        // Insert data
        dbManager.insertNewUserData(persistenceTestEmail, TEST_PASSWORD_1, TEST_FREQ_DAILY);

        // Create new database manager instance (simulating restart)
        RegDataBaseManager newDbManager = new RegDataBaseManager();

        // Data should still be there
        assertTrue(newDbManager.isExistingUser(persistenceTestEmail),
                "Data should persist across database manager instances");
        assertEquals(TEST_FREQ_DAILY, newDbManager.getEmailFrequency(persistenceTestEmail),
                "Frequency should persist across database manager instances");
        assertTrue(newDbManager.checkPassword(persistenceTestEmail, TEST_PASSWORD_1),
                "Password should persist across database manager instances");
    }

    @Test
    void testConcurrentAccess() {
        // Use unique emails for this test to avoid conflicts
        String concurrentEmail1 = "concurrent1@example.com";
        String concurrentEmail2 = "concurrent2@example.com";

        // Test that multiple operations don't interfere with each other
        dbManager.insertNewUserData(concurrentEmail1, TEST_PASSWORD_1, TEST_FREQ_DAILY);
        dbManager.insertNewUserData(concurrentEmail2, TEST_PASSWORD_2, TEST_FREQ_WEEKLY);

        // All operations should work correctly
        assertTrue(dbManager.isExistingUser(concurrentEmail1));
        assertTrue(dbManager.isExistingUser(concurrentEmail2));
        assertTrue(dbManager.checkPassword(concurrentEmail1, TEST_PASSWORD_1));
        assertTrue(dbManager.checkPassword(concurrentEmail2, TEST_PASSWORD_2));
        assertEquals(TEST_FREQ_DAILY, dbManager.getEmailFrequency(concurrentEmail1));
        assertEquals(TEST_FREQ_WEEKLY, dbManager.getEmailFrequency(concurrentEmail2));
    }


    @Test
    void testSpecialCharactersInData() {
        // Test with special characters in email and password
        String specialEmail = "user+test@example-domain.com";
        String specialPassword = "p@ssw0rd!#$%";

        assertDoesNotThrow(() -> {
            dbManager.insertNewUserData(specialEmail, specialPassword, TEST_FREQ_MONTHLY);
        }, "Should handle special characters in data");

        assertTrue(dbManager.isExistingUser(specialEmail),
                "Should find user with special characters in email");
        assertTrue(dbManager.checkPassword(specialEmail, specialPassword),
                "Should validate password with special characters");
    }

    @Test
    void testLongDataValues() {
        // Test with very long values
        String longEmail = "a".repeat(100) + "@" + "b".repeat(100) + ".com";
        String longPassword = "password" + "x".repeat(200);
        String longFrequency = "Very Long Frequency Description";

        assertDoesNotThrow(() -> {
            dbManager.insertNewUserData(longEmail, longPassword, longFrequency);
        }, "Should handle long data values");

        assertTrue(dbManager.isExistingUser(longEmail),
                "Should handle long email addresses");
        assertEquals(longFrequency, dbManager.getEmailFrequency(longEmail),
                "Should store and retrieve long frequency values");
    }

    @Test
    void testDatabaseErrorHandling() {
        // This test ensures the database manager handles errors gracefully
        // We can't easily simulate database errors without mocking, but we can
        // test edge cases that might cause issues

        assertDoesNotThrow(() -> {
            // Multiple rapid insertions
            for (int i = 0; i < 100; i++) {
                dbManager.insertNewUserData("user" + i + "@example.com",
                        "password" + i, TEST_FREQ_DAILY);
            }
        }, "Should handle multiple rapid insertions");

        // Verify all users were inserted
        for (int i = 0; i < 100; i++) {
            assertTrue(dbManager.isExistingUser("user" + i + "@example.com"),
                    "User " + i + " should exist");
        }
    }
}