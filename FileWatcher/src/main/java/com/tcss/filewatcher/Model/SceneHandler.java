package com.tcss.filewatcher.Model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * The handler for the different scenes
 *
 * @author Kassie Whitney
 * @version 7.16.25
 */
public abstract class SceneHandler {

    private static boolean myIsWatcherStarted = false;

    private static final Hashtable<String, List<String>> myMonitoredDirectory = new Hashtable<>();

    public static boolean watcherRunningProperty() {

        return myIsWatcherStarted;
    }

    /**
     * Tells the scene that the watcher is active.
     */
    public static void startWatcher() {

        myIsWatcherStarted = true;
    }

    /**
     * Tells the scene that the watcher is inactive.
     */
    public static void stopWatcher() {

        myIsWatcherStarted = false;
    }


    /**
     * Debugger for fileWatcher Status.
     *
     * @return true if the running, false if not.
     */
    public static boolean fileWatcherStatus() {
        return watcherRunningProperty();
    }

    /**
     * Handles the scene exit protocol dependent on if the filewatcher is running.
     *
     * @param theStage the stage that's trying to close.
     */
    public static void handleExitOnActive(final Stage theStage) {
        if (fileWatcherStatus()) {

            theStage.setOnCloseRequest(WindowEvent -> {

                WindowEvent.consume();
                theStage.setIconified(true);

            });
        } else {
            theStage.setOnCloseRequest(WindowEvent -> {
                theStage.setIconified(false);
                theStage.setOnCloseRequest(null);


                if (theStage.getTitle().equals("File Watcher")) {
                    Platform.exit();
                }
            });

        }
    }

    /**
     * Adds the directory to the internal watchlist
     *
     * @param thePath      The file's directory.
     * @param theExtension The extension that is being monitored for the directory.
     */

    public static void addMonitoredDirectory(final String thePath, final String theExtension) {

        final String path = Path.of(thePath).normalize().toString().toLowerCase();

        myMonitoredDirectory.putIfAbsent(path, new ArrayList<>());

        final List<String> extensionList = myMonitoredDirectory.get(path);
        if (!extensionList.contains(theExtension)) {
            extensionList.add(theExtension);
        }

    }

    /**
     * Removes the Extension from the WatchList.
     *
     * @param thePath      The path to the directory being monitored.
     * @param theExtension The extension that's being removed
     */
    public static void removeMonitoredExtension(final String thePath,
                                                final String theExtension) {
        final String path = Path.of(thePath).normalize().toString().toLowerCase();

        final List<String> extensionList = myMonitoredDirectory.get(path);
        if (extensionList != null) {
            extensionList.remove(theExtension);
            // Optionally remove the directory if no extensions remain
            if (extensionList.isEmpty()) {
                myMonitoredDirectory.remove(path);
            }
        }
    }

    /**
     * Removes the directory that is being monitored.
     *
     * @param thePath The path of the directory.
     */
    public static void removeMonitoredDirectory(final String thePath) {
        final String path = Path.of(thePath).normalize().toString().toLowerCase();
        myMonitoredDirectory.remove(path);
    }

    /**
     * Gets the list of the correlating extensions for the directory
     *
     * @param thePath the Directory path.
     * @return returns NULL if the path does not exist, returns the list of extensions if
     * the path exists.
     */
    public static List<String> getExtensionsFromDir(final String thePath) {

        final String path = Path.of(thePath).normalize().toString().toLowerCase();
        return myMonitoredDirectory.get(path);
    }


}
