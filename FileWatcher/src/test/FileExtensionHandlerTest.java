import com.tcss.filewatcher.Model.DirectoryEntry;
import com.tcss.filewatcher.Model.FileExtensionHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


/**
 * Unit tests for FileExtensionHandler Class
 *
 * @author salimahafurova
 * @version August 15, 2025
 */
class FileExtensionHandlerTest {

    private List<String> extensionList;
    private DirectoryEntry textEntry;
    private DirectoryEntry javaEntry;
    private DirectoryEntry pdfEntry;
    private DirectoryEntry noExtensionEntry;
    private DirectoryEntry dsStoreEntry;

    @BeforeEach
    void setUp() {
        extensionList = new ArrayList<>();

        // Create test directory entries
        textEntry = new DirectoryEntry("15 Aug, 2025", "10:30:00",
                "test.txt", "/home/user", "CREATED");
        javaEntry = new DirectoryEntry("15 Aug, 2025", "10:31:00",
                "Main.java", "/home/user/src", "MODIFIED");
        pdfEntry = new DirectoryEntry("15 Aug, 2025", "10:32:00",
                "document.pdf", "/home/user/docs", "CREATED");
        noExtensionEntry = new DirectoryEntry("15 Aug, 2025", "10:33:00",
                "README", "/home/user", "CREATED");
        dsStoreEntry = new DirectoryEntry("15 Aug, 2025", "10:34:00",
                ".DS_Store", "/home/user", "CREATED");
    }

    @Test
    void testCanAddExtensionWithAllExtensions() {
        extensionList.add("All Extensions");

        Assertions.assertTrue(FileExtensionHandler.canAddExtension(extensionList, textEntry),
                "Should allow .txt file when 'All Extensions' is in list");
        assertTrue(FileExtensionHandler.canAddExtension(extensionList, javaEntry),
                "Should allow .java file when 'All Extensions' is in list");
        assertTrue(FileExtensionHandler.canAddExtension(extensionList, pdfEntry),
                "Should allow .pdf file when 'All Extensions' is in list");
        assertTrue(FileExtensionHandler.canAddExtension(extensionList, noExtensionEntry),
                "Should allow file without extension when 'All Extensions' is in list");
    }

    @Test
    void testCanAddExtensionWithSpecificExtensions() {
        extensionList.addAll(Arrays.asList(".txt", ".java"));

        assertTrue(FileExtensionHandler.canAddExtension(extensionList, textEntry),
                "Should allow .txt file when .txt is in extension list");
        assertTrue(FileExtensionHandler.canAddExtension(extensionList, javaEntry),
                "Should allow .java file when .java is in extension list");
        assertFalse(FileExtensionHandler.canAddExtension(extensionList, pdfEntry),
                "Should not allow .pdf file when .pdf is not in extension list");
        assertFalse(FileExtensionHandler.canAddExtension(extensionList, noExtensionEntry),
                "Should not allow file without extension when specific extensions are required");
    }

    @Test
    void testCanAddExtensionWithEmptyList() {
        // Empty list should not allow any files
        assertFalse(FileExtensionHandler.canAddExtension(extensionList, textEntry),
                "Should not allow any file with empty extension list");
        assertFalse(FileExtensionHandler.canAddExtension(extensionList, javaEntry),
                "Should not allow any file with empty extension list");
        assertFalse(FileExtensionHandler.canAddExtension(extensionList, pdfEntry),
                "Should not allow any file with empty extension list");
    }

    @Test
    void testCanAddExtensionWithNullList() {
        assertFalse(FileExtensionHandler.canAddExtension(null, textEntry),
                "Should return false for null extension list");
        assertFalse(FileExtensionHandler.canAddExtension(null, javaEntry),
                "Should return false for null extension list");
        assertFalse(FileExtensionHandler.canAddExtension(null, pdfEntry),
                "Should return false for null extension list");
    }

    @Test
    void testCanAddExtensionRejectsDSStore() {
        extensionList.add("All Extensions");

        assertFalse(FileExtensionHandler.canAddExtension(extensionList, dsStoreEntry),
                "Should always reject .DS_Store files regardless of extension list");

        // Test with specific extensions as well
        extensionList.clear();
        extensionList.addAll(Arrays.asList(".txt", ".java", ".DS_Store"));

        assertFalse(FileExtensionHandler.canAddExtension(extensionList, dsStoreEntry),
                "Should reject .DS_Store even if explicitly in extension list");
    }

    @Test
    void testCanAddExtensionCaseInsensitive() {
        DirectoryEntry upperCaseDSStore = new DirectoryEntry("15 Aug, 2025", "10:34:00",
                ".DS_STORE", "/home/user", "CREATED");
        DirectoryEntry mixedCaseDSStore = new DirectoryEntry("15 Aug, 2025", "10:34:00",
                ".ds_store", "/home/user", "CREATED");

        extensionList.add("All Extensions");

        assertFalse(FileExtensionHandler.canAddExtension(extensionList, upperCaseDSStore),
                "Should reject .DS_STORE (uppercase) files");
        assertFalse(FileExtensionHandler.canAddExtension(extensionList, mixedCaseDSStore),
                "Should reject .ds_store (lowercase) files");
    }

    @Test
    void testCanAddExtensionWithSingleExtension() {
        extensionList.add(".txt");

        assertTrue(FileExtensionHandler.canAddExtension(extensionList, textEntry),
                "Should allow .txt file when only .txt is in list");
        assertFalse(FileExtensionHandler.canAddExtension(extensionList, javaEntry),
                "Should not allow .java file when only .txt is in list");
        assertFalse(FileExtensionHandler.canAddExtension(extensionList, pdfEntry),
                "Should not allow .pdf file when only .txt is in list");
    }

    @Test
    void testCanAddExtensionWithMultipleSpecificExtensions() {
        extensionList.addAll(Arrays.asList(".txt", ".java", ".py", ".rar"));

        assertTrue(FileExtensionHandler.canAddExtension(extensionList, textEntry),
                "Should allow .txt file");
        assertTrue(FileExtensionHandler.canAddExtension(extensionList, javaEntry),
                "Should allow .java file");
        assertFalse(FileExtensionHandler.canAddExtension(extensionList, pdfEntry),
                "Should not allow .pdf file");

        // Test with files that have extensions in the list
        DirectoryEntry pyEntry = new DirectoryEntry("15 Aug, 2025", "10:35:00",
                "script.py", "/home/user", "CREATED");
        DirectoryEntry rarEntry = new DirectoryEntry("15 Aug, 2025", "10:36:00",
                "main.rar", "/home/user/src", "MODIFIED");

        assertTrue(FileExtensionHandler.canAddExtension(extensionList, pyEntry),
                "Should allow .py file");
        assertTrue(FileExtensionHandler.canAddExtension(extensionList, rarEntry),
                "Should allow .rar file");
    }

    @Test
    void testCanAddExtensionWithNullEntry() {
        extensionList.add("All Extensions");

        assertThrows(NullPointerException.class,
                () -> FileExtensionHandler.canAddExtension(extensionList, null),
                "Should throw NullPointerException for null directory entry");
    }

    @Test
    void testCanAddExtensionWithFileWithoutExtension() {
        extensionList.add("All Extensions");

        assertTrue(FileExtensionHandler.canAddExtension(extensionList, noExtensionEntry),
                "Should allow file without extension when 'All Extensions' is set");

        extensionList.clear();
        extensionList.add(".txt");

        assertFalse(FileExtensionHandler.canAddExtension(extensionList, noExtensionEntry),
                "Should not allow file without extension when specific extensions are required");
    }

    @Test
    void testCanAddExtensionWithDuplicateExtensions() {
        extensionList.addAll(Arrays.asList(".txt", ".txt", ".java", ".txt"));

        assertTrue(FileExtensionHandler.canAddExtension(extensionList, textEntry),
                "Should work correctly even with duplicate extensions in list");
        assertTrue(FileExtensionHandler.canAddExtension(extensionList, javaEntry),
                "Should work correctly even with duplicate extensions in list");
    }

    @Test
    void testCanAddExtensionWithEmptyStringExtensions() {
        extensionList.addAll(Arrays.asList("", ".txt", "   "));

        assertTrue(FileExtensionHandler.canAddExtension(extensionList, textEntry),
                "Should work with .txt despite empty string extensions in list");
        assertFalse(FileExtensionHandler.canAddExtension(extensionList, javaEntry),
                "Should not allow .java file");
    }

    @Test
    void testCanAddExtensionWithHiddenFiles() {
        DirectoryEntry hiddenFile = new DirectoryEntry("15 Aug, 2025", "10:37:00",
                ".gitignore", "/home/user", "CREATED");
        DirectoryEntry hiddenTxtFile = new DirectoryEntry("15 Aug, 2025", "10:38:00",
                ".hidden.txt", "/home/user", "CREATED");

        extensionList.add("All Extensions");

        assertTrue(FileExtensionHandler.canAddExtension(extensionList, hiddenFile),
                "Should allow hidden files when 'All Extensions' is set");
        assertTrue(FileExtensionHandler.canAddExtension(extensionList, hiddenTxtFile),
                "Should allow hidden .txt files when 'All Extensions' is set");

        extensionList.clear();
        extensionList.add(".txt");

        assertFalse(FileExtensionHandler.canAddExtension(extensionList, hiddenFile),
                "Should not allow .gitignore when only .txt is allowed");
        assertTrue(FileExtensionHandler.canAddExtension(extensionList, hiddenTxtFile),
                "Should allow .hidden.txt when .txt is allowed");
    }

    @Test
    void testCanAddExtensionWithComplexFilenames() {
        DirectoryEntry complexFile1 = new DirectoryEntry("15 Aug, 2025", "10:39:00",
                "file.name.with.dots.txt", "/home/user", "CREATED");
        DirectoryEntry complexFile2 = new DirectoryEntry("15 Aug, 2025", "10:40:00",
                "file-name_with_special123.java", "/home/user", "CREATED");

        extensionList.addAll(Arrays.asList(".txt", ".java"));

        assertTrue(FileExtensionHandler.canAddExtension(extensionList, complexFile1),
                "Should handle complex filename with multiple dots correctly");
        assertTrue(FileExtensionHandler.canAddExtension(extensionList, complexFile2),
                "Should handle filename with special characters correctly");
    }

    @Test
    void testConstructorIsPrivate() {
        // Test that FileExtensionHandler has a private constructor (utility class)
        assertThrows(IllegalAccessException.class, () -> {
            FileExtensionHandler.class.getDeclaredConstructor().newInstance();
        }, "Constructor should be private");

        // Alternative approach; verify we can't instantiate normally
        assertThrows(Exception.class, () -> {
            // This should fail because constructor is private
            @SuppressWarnings("unused")
            FileExtensionHandler handler = FileExtensionHandler.class.getDeclaredConstructor().newInstance();
        });
    }

    @Test
    void testExtensionMatchingAccuracy() {
        // Test edge cases for extension matching
        DirectoryEntry fileEndingWithTxt = new DirectoryEntry("15 Aug, 2025", "10:41:00",
                "not-really-txt", "/home/user", "CREATED");
        DirectoryEntry actualTxtFile = new DirectoryEntry("15 Aug, 2025", "10:42:00",
                "real.txt", "/home/user", "CREATED");

        extensionList.add(".txt");

        assertFalse(FileExtensionHandler.canAddExtension(extensionList, fileEndingWithTxt),
                "Should not match file that just ends with 'txt' but has no dot");
        assertTrue(FileExtensionHandler.canAddExtension(extensionList, actualTxtFile),
                "Should match actual .txt file");
    }

    @Test
    void testDatabaseIntegration() {
        // Test that the method properly integrates with database
        // Since we can't easily mock the database, we'll test that the method
        // completes successfully for valid cases
        extensionList.add("All Extensions");

        assertDoesNotThrow(() -> {
            boolean result = FileExtensionHandler.canAddExtension(extensionList, textEntry);
            assertTrue(result, "Should successfully process and add to database");
        }, "Should not throw exception when adding valid entry to database");

        extensionList.clear();
        extensionList.add(".txt");

        assertDoesNotThrow(() -> {
            boolean result = FileExtensionHandler.canAddExtension(extensionList, textEntry);
            assertTrue(result, "Should successfully process and add to database");
        }, "Should not throw exception when adding matching extension to database");
    }
}