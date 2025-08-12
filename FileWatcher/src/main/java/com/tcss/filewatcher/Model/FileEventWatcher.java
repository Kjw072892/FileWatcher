package com.tcss.filewatcher.Model;

import com.tcss.filewatcher.Common.Properties;
import com.tcss.filewatcher.Viewer.FileWatcherSceneController;
import com.tcss.filewatcher.Viewer.MainSceneController;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.io.Serializable;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

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
    @Serial
    private static final long serialVersionUID = 1L;

    // PropertyChangeSupport for notifying listeners
    private transient PropertyChangeSupport myChanges = new PropertyChangeSupport(this);

    // Core fields that will be serialized
    private String myAbsolutePath;
    private final Set<String> myWatchedExtensions;
    private boolean myIsWatching;
    private final List<FileEvent> myCurrentEvents;
    private boolean myHasUnsavedEvents;

    // transient fields that won't be serialized
    private transient WatchService myWatchService;

    // Database integration
    private transient DataBaseManager myDBManager;
    private transient ExecutorService myExecutorService;
    private transient AtomicBoolean myShouldStop;
    private transient Map<WatchKey, Path> myWatchKeys;
    private final transient Set<Path> myWatchedPaths = new HashSet<>();


    private Properties myProperties;

    // Default Constructor
    public FileEventWatcher() {
        myWatchedExtensions = new HashSet<>();
        myCurrentEvents = new ArrayList<>();
        myIsWatching = false;
        myHasUnsavedEvents = false;
        initializeTransientFields();
        addPropertyChangeListener(this);

    }

    // Constructor with file path
    public FileEventWatcher(final String theFilePath) {
        this();
        setWatchPath(theFilePath);
    }


    public void connectToControllers(final MainSceneController theMain,
                                     final FileWatcherSceneController theFileWatcherScene) {

        if (theMain != null) {
            theMain.addPropertyChangeListener(this);
        }

        if (theFileWatcherScene != null) {
            addPropertyChangeListener(theFileWatcherScene);
        }
    }

    // initialize transient fields after deserialization or construction
    private void initializeTransientFields() {
        if (myChanges == null) {
            myChanges = new PropertyChangeSupport(this);
        }
        if (myWatchKeys == null) {
            myWatchKeys = new HashMap<>();
        }
        if (myDBManager == null) {
            myDBManager = new DataBaseManager();
        }
        if (myShouldStop == null) {
            myShouldStop = new AtomicBoolean(false);
        }
    }

    public void addWatchPath(final String theFilePath) {

        if (theFilePath == null || theFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        final Path path = Path.of(theFilePath).toAbsolutePath().normalize();

        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must exist and be a directory: ");
        }

        myWatchedPaths.add(path);

        try {
            if (myWatchService == null) {
                myWatchService = FileSystems.getDefault().newWatchService();
            }
            walkAndRegisterDirectories(path);
        } catch (final IOException theIOE) {
            System.err.println("Failed to register directory: " + path + " - " + theIOE.getMessage());
            myWatchedPaths.remove(path);
        }

        myProperties = Properties.WATCH_PATH_SET;
        myChanges.firePropertyChange(myProperties.toString(), null, path.toString());
    }

    // sets path to watch for file events
    public void setWatchPath(final String theFilePath) {
        // THESE ARE JUST DEBUG STATEMENTS TO SEE IF THE FILE PATH IS BEING WATCHED, IF THAT FILE EXISTS, AND CHECKS IF
        // THE PATH IS A DIRECTORY (NOT A FILE)
        System.out.println("Trying to watch: " + theFilePath);
        System.out.println("Exists: " + new File(theFilePath).exists());
        System.out.println("Is directory: " + new File(theFilePath).isDirectory());
        if (theFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        final String oldPath = myAbsolutePath;
        myAbsolutePath = theFilePath;
        myWatchedPaths.add(Path.of(theFilePath));
        myProperties = Properties.WATCH_PATH_SET;
        myChanges.firePropertyChange(myProperties.toString(), oldPath, myAbsolutePath);
    }

    // adds a file extension to watch
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

    // Removes a file extension from watching
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

    // start watching for file events using the WatchService pattern from tutorials
    public void startWatching() throws IOException {
        if (myIsWatching) {
            return;
        }

        for (Path thePath : myWatchedPaths) {

            if (!Files.exists(thePath)) {
                throw new IllegalArgumentException("Watch path must be set and exist before " +
                        "starting");
            }
            if (!Files.isDirectory(thePath)) {
                throw new IllegalArgumentException("Watch path must be a directory");
            }

        }

        initializeWatchService();
        final boolean oldWatching = myIsWatching;
        myIsWatching = true;
        myShouldStop.set(false);

    }

    // Starts watching in a background thread to keep the UI responsive.
    // This is the method that should be called from JavaFX
    // applications
    public void startBackgroundWatching() throws IOException {
        startWatching();

        // Start the background thread for processing events
        if (myExecutorService == null || myExecutorService.isShutdown()) {
            myExecutorService = Executors.newSingleThreadExecutor(r -> {
                final Thread thread = new Thread(r, "FileWatcher-Thread");
                thread.setDaemon(true); // make it a daemon thread so it doesn't prevent app shutdown
                return thread;
            });
        }
        myExecutorService.submit(this::processEvents);
    }

    // Stop watching for file events
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
            } catch (final IOException e) {
                System.out.println("Error closing watch services: " + e.getMessage());
            }
        }

        myProperties = Properties.STOPPED_WATCHING;
        myChanges.firePropertyChange(myProperties.toString(), true, myIsWatching);
    }

    // Initialize the watch service following the tutorial pattern
    private void initializeWatchService() throws IOException {
        myWatchService = FileSystems.getDefault().newWatchService();
        myWatchKeys.clear();

        for (Path start : myWatchedPaths) {
            walkAndRegisterDirectories(start);
        }
    }

    // register the given directory with the WatchService
    private void registerDirectory(final Path theDir) throws IOException {
        final WatchKey key = theDir.register(myWatchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        myWatchKeys.put(key, theDir);
    }

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


            System.out.println("Directory has been successfully removed!");

        } else {

            System.out.println("The directory could not be removed!");
        }

    }

    // register the given directory, and all its subdirectories with the WatchService
    private void walkAndRegisterDirectories(final Path theStart) throws IOException {
        Files.walkFileTree(theStart, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path theDir, final BasicFileAttributes theAttrs) throws IOException {
                registerDirectory(theDir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // Process all events for keys queued to the watcher
    public void processEvents() {
        if (!myIsWatching || myWatchService == null) {
            return;
        }
        try {
            WatchKey key;
            while ((key = myWatchService.take()) != null && !myShouldStop.get()) {
                final Path dir = myWatchKeys.get(key);
                if (dir == null) {
                    System.out.println("WatchKey not recognized!!");
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

        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Watch service interrupted: " + e.getMessage());
        }
    }

    // Processes individual watch events
    private void processWatchEvent(final WatchEvent<?> theEvent, final Path theDir) {

        final WatchEvent.Kind<?> kind = theEvent.kind();
        if (kind == OVERFLOW) {
            return;
        }

        @SuppressWarnings("unchecked") final WatchEvent<Path> pathEvent = (WatchEvent<Path>) theEvent;
        final Path filename = pathEvent.context();
        final Path child = theDir.resolve(filename);

        // if a directory is created and watching recursively, then trigger it and it's subdirectories
        if (kind == ENTRY_CREATE) {
            try {
                if (Files.isDirectory(child)) {
                    walkAndRegisterDirectories(child);
                }
            } catch (final IOException x) {
                System.out.println("Error registering new directory: " + x.getMessage());

            }
        }
        // check if file matches watched extensions
        if (!matchesWatchedExtensions(filename.toString())) {
            return;
        }

        // Create file event
        final String eventType = getEventTypeString(kind, theDir);
        final String currentDate =
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, " +
                        "yyyy"));
        final String currentTime =
                java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));

        final FileEvent fileEvent = new FileEvent(
                filename.toString(),
                child.toString(),
                eventType,
                currentTime
        );


        addFileEvent(fileEvent);

        // print out event
        System.out.format("%s: %s\n", theEvent.kind().name(), child);

        DirectoryEntry entry = new DirectoryEntry(currentDate, currentTime,
                filename.toString(), theDir.toString(), theEvent.kind().name());

        myChanges.firePropertyChange(Properties.NEW_FILE_EVENT.toString(), null, entry);
    }

    // check if a filename matches any of the watched extensions
    private boolean matchesWatchedExtensions(final String theFilename) {
        if (myWatchedExtensions.isEmpty()) {
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

    // converts WatchEvent.Kind to string
    private String getEventTypeString(final WatchEvent.Kind<?> theKind, final Path theDir) {
        if (theKind == ENTRY_CREATE) {

            return "CREATED";

        } else if (theKind == ENTRY_DELETE) {

            return "DELETED";

        } else if (theKind == ENTRY_MODIFY) {

            return "MODIFIED";
        }
        return "UNKNOWN";
    }

    // adds a file event to the current events list
    private synchronized void addFileEvent(final FileEvent theFileEvent) {
        myCurrentEvents.add(theFileEvent);
        setHasUnsavedEvents(true);
    }


    // Clears current events list
    public synchronized void clearCurrentEvents() {
        final int oldSize = myCurrentEvents.size();
        myCurrentEvents.clear();
        setHasUnsavedEvents(false);
    }

    // Sets the unsaved events flag
    private void setHasUnsavedEvents(final boolean theHasUnsaved) {

        myHasUnsavedEvents = theHasUnsaved;
    }


    // PropertyChangeSupport methods
    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        myChanges.addPropertyChangeListener(theListener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener theListener) {
        myChanges.removePropertyChangeListener(theListener);
    }

    public void addPropertyChangeListener(final String thePropertyName, final PropertyChangeListener theListener) {
        myChanges.addPropertyChangeListener(thePropertyName, theListener);
    }

    public void removePropertyChangeListener(final String thePropertyName, final PropertyChangeListener theListener) {
        myChanges.removePropertyChangeListener(thePropertyName, theListener);
    }

    // Serialization support methods
    @Serial
    private void writeObject(final ObjectOutputStream theOut) throws IOException {
        theOut.defaultWriteObject();
    }

    @Serial
    private void readObject(final ObjectInputStream theIn) throws IOException, ClassNotFoundException {
        theIn.defaultReadObject();
        initializeTransientFields();
    }

    // serializes this fileEventWatcher to a file
    public void saveToFile(final String theFilename) throws IOException {
        try (final FileOutputStream fileOut = new FileOutputStream(theFilename);
             final ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(this);

        }
    }

    // deserializes a FileEventWatcher from a file
    public static FileEventWatcher loadFromFile(final String theFilename) throws IOException, ClassNotFoundException {
        try (final FileInputStream fileIn = new FileInputStream(theFilename);
             final ObjectInputStream in = new ObjectInputStream(fileIn)) {

            return (FileEventWatcher) in.readObject();
        }
    }

    @Override
    public String toString() {
        return String.format("FileEventWatcher[path=%s, extensions=%s, watching = %s, events=%d, unsaved = %s]",
                myAbsolutePath, myWatchedExtensions, myIsWatching, myCurrentEvents.size(), myHasUnsavedEvents);

    }

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

                    System.out.println("Error: The directory path is null!");
                }
            }
            case REMOVED_EXTENSION -> removeWatchedExtension(theEvent.getNewValue().toString());

            case START -> {
                try {
                    startBackgroundWatching();

                } catch (final IOException theException) {
                    System.out.println("The thread could not be initialized for background " +
                            "processes!");
                    throw new RuntimeException(theException);
                }
            }
            case STOP -> stopWatching();

        }
    }

    // FileEvent inner class representing a file system event
    public static class FileEvent implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final String myFileName;
        private final String myAbsolutePath;
        private final String myEventType;
        private final String myEventTime;

        public FileEvent(final String theFileName, final String theAbsolutePath, final String theEventType, final String theEventTime) {
            myFileName = theFileName;
            myAbsolutePath = theAbsolutePath;
            myEventType = theEventType;
            myEventTime = theEventTime;
        }


        @Override
        public String toString() {
            return String.format("[%s] %s - %s (%s)", myEventTime, myEventType, myFileName, myAbsolutePath);
        }

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

        @Override
        public int hashCode() {
            return Objects.hash(myFileName, myAbsolutePath, myEventType, myEventTime);
        }

    }

}
