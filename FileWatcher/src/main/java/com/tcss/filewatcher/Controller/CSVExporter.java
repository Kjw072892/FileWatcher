package com.tcss.filewatcher.Controller;

import com.opencsv.CSVWriter;
import com.tcss.filewatcher.Model.DirectoryEntry;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class responsible for exporting file watcher data to CSV format.
 * Handles the creation of CSV files with proper formatting and query information.
 *
 * @author salimahafurova
 * @version 08/12/2025
 */

public class CSVExporter {

    /**
     * Date formatter for CSV file headers
     */
    private static final DateTimeFormatter HEADER_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM, yyyy HH:mm:ss");

    /**
     * Exports query results to a CSV file with query information header.
     *
     * @param theEntries   List of DirectoryEntry objects to export
     * @param thePath  Name of the output CSV file
     * @param theQueryInfo Information about the query that generated these results
     * @throws IOException if an error occurs while writing the file
     */
    public static void exportToCSV(final List<DirectoryEntry> theEntries,
                                final Path thePath,
                            final String theQueryInfo) throws IOException {
        final Logger logger = Logger.getLogger("Export to CSV Logger");

        if (theEntries == null) {
            throw new IllegalArgumentException("Entries list cannot be null");
        }

        if (thePath == null) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        try (final FileWriter fileWriter = new FileWriter(thePath.toFile());
             final CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            // Write header information about the query
            writeQueryHeader(csvWriter, theQueryInfo, theEntries.size());

            // Write column headers
            writeColumnHeaders(csvWriter);

            // Write data rows
            writeDataRows(csvWriter, theEntries);
            logger.log(Level.INFO, "Successfully exported " + theEntries.size()
                    + " entries to " + thePath.getFileName());
        }
    }

    /**
     * Writes the query header to the CSV file.
     *
     * @param theCsvWriter   CSVWriter instance to write to
     * @param theQueryInfo   Information about the query
     * @param theRecordCount Number of records in the result
     */
    private static void writeQueryHeader(final CSVWriter theCsvWriter,
                                        final String theQueryInfo,
                                   final int theRecordCount) {
        // Write query information
        theCsvWriter.writeNext(new String[]{"File Watcher Query Results"});
        theCsvWriter.writeNext(new String[]{"Generated on: " + LocalDateTime.now().format(HEADER_DATE_FORMAT)});
        String queryDisplay;
        queryDisplay = Objects.requireNonNullElse(theQueryInfo, "All Records");
        theCsvWriter.writeNext(new String[]{"\nQuery: \n" + queryDisplay});
        theCsvWriter.writeNext(new String[]{"\nTotal Records: \n" + theRecordCount});
        theCsvWriter.writeNext(new String[]{""}); // Empty row for separation
    }

    /**
     * Writes the column headers to the CSV file.
     *
     * @param theCsvWriter CSVWriter instance to write to
     */
    private static void writeColumnHeaders(final CSVWriter theCsvWriter) {
        String[] headers = {
                "Date",
                "Time",
                "File Name",
                "File Extension",
                "Directory Path",
                "Change Type"
        };
        theCsvWriter.writeNext(headers);
    }

    /**
     * Writes the data rows to the CSV file.
     *
     * @param theCsvWriter CSVWriter instance to write to
     * @param theEntries   List of DirectoryEntry objects to write
     */
    private static void writeDataRows(final CSVWriter theCsvWriter,
                                final List<DirectoryEntry> theEntries) {
        for (DirectoryEntry entry : theEntries) {
            String dateValue;
            if (entry.getDate() != null) {
                dateValue = entry.getDate();
            } else {
                dateValue = "";
            }

            String timeValue;
            if (entry.getTime() != null) {
                timeValue = entry.getTime();
            } else {
                timeValue = "";
            }

            String fileNameValue;
            if (entry.getFileName() != null) {
                fileNameValue = entry.getFileName();
            } else {
                fileNameValue = "";
            }

            String fileExtensionValue;
            if (entry.getFileExtension() != null) {
                fileExtensionValue = entry.getFileExtension();
            } else {
                fileExtensionValue = "";
            }

            String directoryValue;
            if (entry.getDirectory() != null) {
                directoryValue = entry.getDirectory();
            } else {
                directoryValue = "";
            }

            String modificationTypeValue;
            if (entry.getModificationType() != null) {
                modificationTypeValue = entry.getModificationType();
            } else {
                modificationTypeValue = "";
            }


            String[] row = {
                    dateValue,
                    timeValue,
                    fileNameValue,
                    fileExtensionValue,
                    directoryValue,
                    modificationTypeValue

            };
            theCsvWriter.writeNext(row);
        }

    }


    /**
     * Validates the file name to ensure it has a .csv extension.
     *
     * @param theFileName The file name to validate
     * @return true if the file name is valid, false otherwise
     */

    public static boolean isValidFileName(final String theFileName) {
        if (theFileName == null || theFileName.trim().isEmpty()) {
            return false;
        }
        // Check for invalid characters in file names
        String invalidChars = "<>:\"|?*";
        for (char c : invalidChars.toCharArray()) {
            if (theFileName.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Generates a default file name based on query type and current timestamp.
     *
     * @param theQueryType The type of query performed
     * @return Default file name for the CSV export
     */

    public static String generateDefaultFileName(final String theQueryType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM, " +
                "yyyy HH:mm:ss"));
        return theQueryType + "_export_" + timestamp + ".csv";
    }
}
