package com.tcss.filewatcher.Controller;

import com.tcss.filewatcher.Model.DataBaseManager;
import com.tcss.filewatcher.Model.DirectoryEntry;
import com.tcss.filewatcher.Model.EmailClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;

/**
 * Generates the cvs and handles the sending of the email with the cvs attachment.
 *
 * @author Kassie Whitney
 * @version 8.13.25
 */
public class EmailFileController {

    /**
     * Logger Object for debugging
     */
    private static final Logger MY_LOGGER = Logger.getLogger("Email File Controller");
    private static final AtomicBoolean started = new AtomicBoolean(false);

    /**
     * Prevents instantiation of the utility class.
     */
    private EmailFileController() {
    }

    /**
     * User defined query information csv generator.
     * Adds a csv file to users tmp folder.
     *
     * @param theNewTable  The current table.
     * @param theQueryInfo The query Result.
     * @throws IOException Thrown if path generation is broken.
     */
    public static void send(final ObservableList<DirectoryEntry> theNewTable,
                            final Path theTmpPath,
                            final String theQueryInfo) throws IOException {

        CSVExporter.exportToCSV(theNewTable, theTmpPath, theQueryInfo);
    }

    /**
     * Starts the email automation where it emails all the events to the registered address.
     */
    public static void start5Pm(final String theEmailAddress, final Path tmpFilePath) {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        EmailFrequencyManager.startDailyAt5(() -> {
            EmailClient.start(theEmailAddress, tmpFilePath);
        });

    }


    public static void stop() {
        started.set(false);
        EmailFrequencyManager.shutdown();
    }


    /**
     * Adds the csv file as a temporary file based on the users OS temp folder location.
     *
     * @return the path of the temporary file.
     */
    public static Path getTmpFilePath() {
        Path tmp = null;

        try {
            String tmpDir = System.getProperty("java.io.tmpdir");
            tmp = Path.of(tmpDir, "csvFile.csv");

            if (Files.exists(tmp)) {
                Files.delete(tmp);
            }

            Files.createFile(tmp);
            tmp.toFile().deleteOnExit();

        } catch (final IOException theIO) {
            MY_LOGGER.log(Level.SEVERE, "Unable to generate path: " + theIO.getMessage() + "\n");
        }

        return tmp;
    }
}
