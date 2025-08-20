package com.tcss.filewatcher.Model;

import com.tcss.filewatcher.Common.Properties;
import com.tcss.filewatcher.Viewer.FileWatcherSceneController;
import com.tcss.filewatcher.Viewer.MainSceneController;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Watches a directory for file creation, modification, and deletion events.
 * Manages watched file extensions and notifies listeners of file system changes.
 * Integrates with a database manager to store and query file event history.
 *
 * @author Salima Hafurova
 * @version 7/30/25
 */
public class FileEventWatcher extends SceneHandler implements Serializable,
        PropertyChangeListener {
    /**
     * Serial version UID for serialization compatibility.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * PropertyChangeSupport instance to manage property change listeners.
     */
    private transient PropertyChangeSupport myChanges = new PropertyChangeSupport(this);

    /**
     * The absolute path of the directory being watched.
     */
    private String myAbsolutePath;

    /**
     * Set of file extensions to watch for events.
     */
    private final Set<String> myWatchedExtensions;

    /**
     * Flag indicating whether the watcher is currently active.
     */
    private boolean myIsWatching;

    /**
     * List of currently captured file events.
     */
    private final List<FileEvent> myCurrentEvents;

    /**
     * Flag indicating whether there are unsaved events.
     */
    private boolean myHasUnsavedEvents;

    /**
     * The watch service used to monitor file system events.
     */
    private transient WatchService myWatchService;

    /**
     * The database manager for storing and querying file events.
     */
    private transient DataBaseManager myDBManager;

    /**
     * Executor service for processing file events in a background thread.
     */
    private transient ExecutorService myExecutorService;

    /**
     * Atomic boolean to control stopping the watcher.
     */
    private transient AtomicBoolean myShouldStop;

    /**
     * Map of watch keys to their corresponding paths.
     */
    private transient Map<WatchKey, Path> myWatchKeys;

    /**
     * Set of paths being watched.
     */
    private final transient Set<Path> myWatchedPaths = new HashSet<>();


    /**
     * Properties for tracking changes in the watcher state.
     */
    private Properties myProperties;

    /**
     * Logger used for debugging.
     */
    private static final Logger MY_LOGGER = Logger.getLogger("File Event Watcher");

    /**
     * Overrides constructor debugger constructor.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean myIsDebuggerOverride = true;

    /**
     * Default constructor initializes the watcher with no paths or extensions.
     */
    public FileEventWatcher(final boolean theDebuggerStatus) {
        myWatchedExtensions = new HashSet<>();
        myCurrentEvents = new ArrayList<>();
        myIsWatching = false;
        myHasUnsavedEvents = false;
        initializeTransientFields();
        addPropertyChangeListener(this);
        isDebuggerOn(theDebuggerStatus);

    }

    /**
     * Constructor that initializes the watcher with a specific file path to watch.
     *
     * @param theFilePath the absolute path of the directory to watch
     */
    public FileEventWatcher(final String theFilePath) {
        this(false);
        if (theFilePath == null || theFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        setWatchPath(theFilePath);
    }

    /**
     * Sets the debugger
     */
    private void isDebuggerOn(final boolean theDebuggerStatus) {
        if (!theDebuggerStatus || !myIsDebuggerOverride) {
            MY_LOGGER.log(Level.OFF, "\n");
        }
    }

    /**
     * Connects this watcher to the specified controllers for property change notifications.
     *
     * @param theMain             the main scene controller to connect to
     * @param theFileWatcherScene the file watcher scene controller to connect to
     */
    public void connectToControllers(final MainSceneController theMain,
                                     final FileWatcherSceneController theFileWatcherScene) {

        if (theMain != null) {
            theMain.addPropertyChangeListener(this);
        }

        if (theFileWatcherScene != null) {
            addPropertyChangeListener(theFileWatcherScene);
        }
    }

    /**
     * Initializes transient fields that are not serialized.
     */
    private void initializeTransientFields() {
        if (myChanges == null) {
            myChanges = new PropertyChangeSupport(this);
        }
        if (myWatchKeys == null) {
            myWatchKeys = new HashMap<>();
        }
        if (myDBManager == null) {
            myDBManager = new DataBaseManager(false);
        }
        if (myShouldStop == null) {
            myShouldStop = new AtomicBoolean(false);
        }
    }

    /**
     * Adds a directory to watch for file events.
     *
     * @param theFilePath the absolute path of the directory to watch
     */
    public void addWatchPath(final String theFilePath) {

        final Path path = Path.of(theFilePath).toAbsolutePath().normalize();
        myWatchedPaths.add(path);

        try {
            if (myIsWatching && myWatchService != null) {

                walkAndRegisterDirectories(path);
            }

        } catch (final ClosedWatchServiceException theEvent) {

            MY_LOGGER.log(Level.SEVERE, "WatchService closed while adding: " + path + "\n");
            MY_LOGGER.log(Level.SEVERE, theEvent.getMessage() + "\n");

        } catch (final IOException theIOE) {

            MY_LOGGER.log(Level.SEVERE, "Failed to register directory: " + path + "\n");
            MY_LOGGER.log(Level.SEVERE, theIOE.getMessage() + "\n");

            myWatchedPaths.remove(path);
        }

        myProperties = Properties.WATCH_PATH_SET;
        myChanges.firePropertyChange(myProperties.toString(), null, path.toString());
    }

    /**
     * Returns the absolute path of the file associated with this file system event.
     *
     * @return the absolute file path as a String (e.g., "/home/user/documents/example.txt")
     */
    public String getAbsolutePath() {
        return myAbsolutePath;
    }

    /**
     * Returns the set of file extensions being watched for events.
     *
     * @return a Set of file extensions (e.g., {".txt", ".jpg"})
     */
    public Set<String> getWatchedExtensions() {
        return new HashSet<>(myWatchedExtensions);
    }

    /**
     * Checks if the watcher is currently active and watching for file events.
     *
     * @return true if the watcher is active, false otherwise
     */
    public boolean isWatching() {
        return myIsWatching;
    }

    /**
     * Returns the list of currently captured file events.
     *
     * @return a List of FileEvent objects representing the current file events
     */
    public List<FileEvent> getCurrentEvents() {
        return new ArrayList<>(myCurrentEvents);
    }

    /**
     * Checks if there are any unsaved file events.
     *
     * @return true if there are unsaved events, false otherwise
     */
    public boolean hasUnsavedEvents() {
        return myHasUnsavedEvents;
    }

    /**
     * Sets the absolute path of the directory to watch.
     *
     * @param theFilePath the absolute path of the directory to watch
     */
    public void setWatchPath(final String theFilePath) {

        if (theFilePath == null || theFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        // THESE ARE JUST DEBUG STATEMENTS TO SEE IF THE FILE PATH IS BEING WATCHED, IF THAT FILE EXISTS, AND CHECKS IF
        // THE PATH IS A DIRECTORY (NOT A FILE)

        final String message = "Trying to watch: " + theFilePath +
                "\nExists: " + new File(theFilePath).exists() +
                "\nIs directory: " + new File(theFilePath).isDirectory();

        MY_LOGGER.log(Level.INFO, message + "\n");

        final String oldPath = myAbsolutePath;
        myAbsolutePath = theFilePath;
        myWatchedPaths.add(Path.of(theFilePath));
        myProperties = Properties.WATCH_PATH_SET;
        myChanges.firePropertyChange(myProperties.toString(), oldPath, myAbsolutePath);
    }

    /**
     * Adds a file extension to watch for events.
     *
     * @param theExtension the file extension to watch, e.g., ".txt"
     */
    public void addWatchedExtension(final String theExtension) {
        if (theExtension == null || theExtension.trim().isEmpty()) {
            return;
        }

        // normalize extension to use a dot
        String normalizedExtension;
        if (theExtension.startsWith(".")) {
            normalizedExtension = theExtension;
        } else {
            normalizedExtension = "." + theExtension;
        }
        final boolean added = myWatchedExtensions.add(normalizedExtension.toLowerCase());
        if (added) {

            myProperties = Properties.ADDED_EXTENSION;
            myChanges.firePropertyChange(myProperties.toString(), null, normalizedExtension);
        }
    }

    /**
     * Removes a file extension from the watched list.
     *
     * @param theExtension the file extension to remove, e.g., ".txt"
     */
    public void removeWatchedExtension(final String theExtension) {
        if (theExtension == null) {
            return;
        }
        String normalizedExtension;
        if (theExtension.startsWith(".")) {
            normalizedExtension = theExtension;
        } else {
            normalizedExtension = "." + theExtension;
        }
        final boolean removed = myWatchedExtensions.remove(normalizedExtension.toLowerCase());
        if (removed) {

            myProperties = Properties.REMOVED_EXTENSION;
            myChanges.firePropertyChange(myProperties.toString(), null, normalizedExtension);
        }
    }

    /**
     * Starts watching the specified paths for file events.
     *
     * @throws IOException if an error occurs while initializing the watch service
     */
    public void startWatching() throws IOException {
        if (myIsWatching) {
            return;
        }

        for (final Path thePath : myWatchedPaths) {
            if (!Files.exists(thePath)) {
                throw new IllegalArgumentException("Watch path must be set and exist before " +
                        "starting");
            }
            if (!Files.isDirectory(thePath)) {
                throw new IllegalArgumentException("Watch path must be a directory");
            }

        }

        initializeWatchService();
        myIsWatching = true;
        myShouldStop.set(false);

    }

    /**
     * Starts watching the specified paths in a background thread.
     *
     * @throws IOException if an error occurs while initializing the watch service
     */
    public void startBackgroundWatching() throws IOException {
        startWatching();

        // Start the background thread for processing events
        if (myExecutorService == null || myExecutorService.isShutdown()) {
            myExecutorService = Executors.newSingleThreadExecutor(theRunnable -> {
                final Thread thread = new Thread(theRunnable, "FileWatcher-Thread");
                thread.setDaemon(true); // make it a daemon thread so it doesn't prevent app shutdown
                return thread;
            });
        }
        myExecutorService.submit(this::processEvents);
    }

    /**
     * Stops watching the currently watched paths for file events.
     */
    public void stopWatching() {
        if (!myIsWatching) {
            return;
        }
        myIsWatching = false;
        myShouldStop.set(true);

        // Shutdown executor service
        if (myExecutorService != null && !myExecutorService.isShutdown()) {
            myExecutorService.shutdown();
        }
        // Close watch service
        if (myWatchService != null) {
            try {
                myWatchService.close();
                myChanges.firePropertyChange(Properties.STOPPED_WATCHING.toString(), null,
                        false);
            } catch (final IOException theEvent) {
                MY_LOGGER.log(Level.SEVERE,
                        "Error closing watch services: " + theEvent.getMessage() + "\n");
                myWatchService = null;
            }
        }

        if (myWatchKeys != null) {
            myWatchKeys.clear();
        }

        myProperties = Properties.STOPPED_WATCHING;
        myChanges.firePropertyChange(myProperties.toString(), true, myIsWatching);
    }


    /**
     * Initializes the watch service and registers all watched paths.
     *
     * @throws IOException if an error occurs while creating the watch service or registering directories
     */
    private void initializeWatchService() throws IOException {
        myWatchService = FileSystems.getDefault().newWatchService();
        myWatchKeys.clear();
        for (final Path start : myWatchedPaths) {
            walkAndRegisterDirectories(start);
        }
    }

    /**
     * Registers a directory with the watch service to monitor file events.
     *
     * @param theDir the directory to register
     * @throws IOException if an error occurs while registering the directory
     */
    private void registerDirectory(final Path theDir) throws IOException {
        final WatchKey key = theDir.register(myWatchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        myWatchKeys.put(key, theDir);
    }

    /**
     * Unregisters a directory from the watch service.
     *
     * @param theDir the directory to unregister
     */
    private void unregisterDirectory(final Path theDir) {
        WatchKey keyToRemove = null;
        for (Map.Entry<WatchKey, Path> entry : myWatchKeys.entrySet()) {
            if (entry.getValue().equals(theDir)) {
                keyToRemove = entry.getKey();
                break;
            }
        }
        if (keyToRemove != null) {
            keyToRemove.cancel();
            myWatchKeys.remove(keyToRemove);

            MY_LOGGER.log(Level.INFO, "Directory has been successfully removed!\n");

        } else {
            MY_LOGGER.log(Level.INFO, "The directory could not be removed!\n");
        }

    }

    /**
     * Walks through the directory tree starting from the specified path
     *
     * @param theStart the starting path to walk through
     * @throws IOException if an error occurs while walking the directory tree or registering directories
     */
    private void walkAndRegisterDirectories(final Path theStart) throws IOException {
        Files.walkFileTree(theStart, new SimpleFileVisitor<>() {
            @Override
            public @NotNull FileVisitResult preVisitDirectory(final @NotNull Path theDir,
                                                              final @NotNull BasicFileAttributes theAttrs) throws IOException {
                registerDirectory(theDir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Processes file system events in a background thread.
     */
    public void processEvents() {
        if (!myIsWatching || myWatchService == null) {
            return;
        }
        try {
            WatchKey key;
            while ((key = myWatchService.take()) != null && !myShouldStop.get()) {
                final Path dir = myWatchKeys.get(key);
                if (dir == null) {

                    MY_LOGGER.log(Level.WARNING, "WatchKey not recognized!!\n");
                    continue;
                }
                for (final WatchEvent<?> event : key.pollEvents()) {
                    processWatchEvent(event, dir);
                }
                // reset key and remove from set if directory no longer accessible
                final boolean valid = key.reset();
                if (!valid) {
                    myWatchKeys.remove(key);

                    // all directories are inaccessible
                    if (myWatchKeys.isEmpty()) {
                        break;
                    }
                }
            }

        } catch (final ClosedWatchServiceException ignored) {
            // normal during stop
        } catch (final InterruptedException theE) {
            Thread.currentThread().interrupt();

            MY_LOGGER.log(Level.SEVERE, "Watch service interrupted: " + theE.getMessage() +
                    "\n");
        }
    }

    /**
     * Processes a single file system event.
     *
     * @param theEvent the watch event to process
     * @param theDir   the directory where the event occurred
     */
    private void processWatchEvent(final WatchEvent<?> theEvent, final Path theDir) {

        final WatchEvent.Kind<?> kind = theEvent.kind();
        if (kind == OVERFLOW) {
            return;
        }


        final WatchEvent<Path> pathEvent = (WatchEvent<Path>) theEvent;
        final Path filename = pathEvent.context();
        final Path child = theDir.resolve(filename);

        // if a directory is created and watching recursively, then trigger it and it's subdirectories
        if (kind == ENTRY_CREATE) {
            try {
                if (Files.isDirectory(child)) {
                    walkAndRegisterDirectories(child);
                }
            } catch (final IOException theE) {
                MY_LOGGER.log(Level.SEVERE,
                        "Error registering new directory: " + theE.getMessage() + "\n");

            }
        }
        // check if file matches watched extensions
        if (!matchesWatchedExtensions(filename.toString(), theDir)) {
            return;
        }

        // Create file event
        final String eventType = getEventTypeString(kind);
        final String currentDate =
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, " +
                        "yyyy"));
        final String currentTime =
                java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        final FileEvent fileEvent = new FileEvent(
                filename.toString(),
                child.toString(),
                eventType,
                currentTime
        );

        addFileEvent(fileEvent);

        // print out event
        final String message = String.format("%s: %s\n", theEvent.kind().name(), child);
        MY_LOGGER.log(Level.INFO, message);

        final DirectoryEntry entry = new DirectoryEntry(currentDate, currentTime,
                filename.toString(), theDir.toString(), theEvent.kind().name());

        myChanges.firePropertyChange(Properties.NEW_FILE_EVENT.toString(), null, entry);
    }

    /**
     * Checks if the given filename matches any of the watched file extensions.
     *
     * @param theFilename the name of the file to check against watched extensions
     * @return true if the filename matches any watched extension, false otherwise
     */
    private boolean matchesWatchedExtensions(final String theFilename, final Path theDir) {
        if (myWatchedExtensions.isEmpty() || myWatchedPaths.contains(theDir)) {
            return true; // watch all files if no extensions specified
        }
        final String lowerFilename = theFilename.toLowerCase();
        for (final String extension : myWatchedExtensions) {
            if (lowerFilename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a string representation of the event type based on the kind of event.
     *
     * @param theKind the kind of watch event (e.g., ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
     * @return a string representing the event type
     */
    private String getEventTypeString(final WatchEvent.Kind<?> theKind) {
        if (theKind == ENTRY_CREATE) {

            return "CREATED";

        } else if (theKind == ENTRY_DELETE) {

            return "DELETED";

        } else if (theKind == ENTRY_MODIFY) {

            return "MODIFIED";
        }
        return "UNKNOWN";
    }

    /**
     * Adds a file event to the current events list and notifies listeners.
     *
     * @param theFileEvent the file event to add
     */
    private synchronized void addFileEvent(final FileEvent theFileEvent) {
        myCurrentEvents.add(theFileEvent);
        setHasUnsavedEvents(true);
    }

    /**
     * Clears the list of current file events and resets the unsaved events flag.
     */
    public synchronized void clearCurrentEvents() {
        myCurrentEvents.clear();
        setHasUnsavedEvents(false);
    }

    /**
     * Sets the flag indicating whether there are unsaved events.
     *
     * @param theHasUnsaved true if there are unsaved events, false otherwise
     */
    private void setHasUnsavedEvents(final boolean theHasUnsaved) {

        myHasUnsavedEvents = theHasUnsaved;
    }

    /**
     * Adds a property change listener to this watcher.
     *
     * @param theListener the listener to add
     */
    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        myChanges.addPropertyChangeListener(theListener);
    }

    /**
     * Removes a property change listener from this watcher.
     *
     * @param theListener the listener to remove
     */
    public void removePropertyChangeListener(final PropertyChangeListener theListener) {
        myChanges.removePropertyChangeListener(theListener);
    }

    /**
     * Adds a property change listener for a specific property name.
     *
     * @param thePropertyName the name of the property to listen for changes
     * @param theListener     the listener to add
     */
    public void addPropertyChangeListener(final String thePropertyName, final PropertyChangeListener theListener) {
        myChanges.addPropertyChangeListener(thePropertyName, theListener);
    }


    /**
     * Serializes this FileEventWatcher object to an ObjectOutputStream.
     *
     * @param theOut the ObjectOutputStream to write this object to
     * @throws IOException if an I/O error occurs during serialization
     */
    @Serial
    private void writeObject(final ObjectOutputStream theOut) throws IOException {
        theOut.defaultWriteObject();
    }

    /**
     * Deserializes this FileEventWatcher object from an ObjectInputStream.
     *
     * @param theIn the ObjectInputStream to read this object from
     * @throws IOException            if an I/O error occurs during deserialization
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    @Serial
    private void readObject(final ObjectInputStream theIn) throws IOException, ClassNotFoundException {
        theIn.defaultReadObject();
        initializeTransientFields();
    }

    /**
     * Returns a string representation of this FileEventWatcher.
     *
     * @return a string representation of this watcher
     */
    @Override
    public String toString() {
        return String.format("FileEventWatcher[path=%s, extensions=%s, watching = %s, events=%d, unsaved = %s]",
                myAbsolutePath, myWatchedExtensions, myIsWatching, myCurrentEvents.size(), myHasUnsavedEvents);

    }

    /**
     * Handles property change events by updating the watcher state based on the event source
     *
     * @param theEvent A PropertyChangeEvent object describing the event source
     *                 and the property that has changed.
     */
    @Override
    public void propertyChange(final PropertyChangeEvent theEvent) {

        myProperties = Properties.valueOf(theEvent.getPropertyName());

        switch (myProperties) {
            case ADDED_DIRECTORY -> setWatchPath(theEvent.getNewValue().toString());

            case ADDED_EXTENSION -> addWatchedExtension(theEvent.getNewValue().toString());

            case REMOVED_DIRECTORY -> {
                if (theEvent.getOldValue() != null) {
                    final Path thePath =
                            Path.of(Objects.requireNonNull(theEvent.getOldValue().toString()));
                    unregisterDirectory(thePath);
                    myWatchedPaths.remove(thePath);
                } else {

                    MY_LOGGER.log(Level.WARNING, "Error: The directory path is null!\n");
                }
            }
            case REMOVED_EXTENSION ->
                    removeWatchedExtension(theEvent.getNewValue().toString());

            case START -> {
                try {
                    startBackgroundWatching();

                } catch (final IOException theException) {

                    MY_LOGGER.log(Level.SEVERE, "The thread could not be initialized for background " +
                            "processes!\n");
                    MY_LOGGER.log(Level.SEVERE, theException.getMessage() + "\n");
                    throw new RuntimeException(theException);
                }
            }
            case STOP -> stopWatching();

        }
    }

    /**
     * Represents a file event with details about the file, its absolute path,
     *
     * @author Salima Hafurova
     * @version 8.11.25
     */
    public static class FileEvent implements Serializable {

        /**
         * Serial version UID for serialization compatibility.
         */
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * The name of the file associated with the event.
         */
        private final String myFileName;

        /**
         * The absolute path of the file associated with the event.
         */
        private final String myAbsolutePath;

        /**
         * The type of event (e.g., CREATE, MODIFY, DELETE).
         */
        private final String myEventType;

        /**
         * The time when the event occurred, formatted as a string.
         */
        private final String myEventTime;

        /**
         * Constructs a FileEvent with the specified details.
         *
         * @param theFileName     the name of the file associated with the event
         * @param theAbsolutePath the absolute path of the file associated with the event
         * @param theEventType    the type of event (e.g., CREATE, MODIFY, DELETE)
         * @param theEventTime    the time when the event occurred, formatted as a string
         */
        public FileEvent(final String theFileName, final String theAbsolutePath, final String theEventType, final String theEventTime) {
            myFileName = theFileName;
            myAbsolutePath = theAbsolutePath;
            myEventType = theEventType;
            myEventTime = theEventTime;
        }


        /**
         * Returns the name of the file associated with this event.
         *
         * @return the name of the file
         */
        @Override
        public String toString() {
            return String.format("[%s] %s - %s (%s)", myEventTime, myEventType, myFileName, myAbsolutePath);
        }

        /**
         * Checks if this FileEvent is equal to another object.
         *
         * @param theObj the object to compare with
         * @return true if the objects are equal, false otherwise
         */
        @Override
        public boolean equals(final Object theObj) {
            if (this == theObj) {
                return true;
            }
            if (theObj == null || getClass() != theObj.getClass()) {
                return false;
            }
            final FileEvent fileEvent = (FileEvent) theObj;
            return Objects.equals(myFileName, fileEvent.myFileName) &&
                    Objects.equals(myAbsolutePath, fileEvent.myAbsolutePath) &&
                    Objects.equals(myEventType, fileEvent.myEventType) &&
                    Objects.equals(myEventTime, fileEvent.myEventTime);
        }

        /**
         * Returns the hash code for this FileEvent.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            return Objects.hash(myFileName, myAbsolutePath, myEventType, myEventTime);
        }

    }

}
