package com.tcss.filewatcher.Common;


public enum Properties {
    /**
     * Stores the report in the SQLite database
     */
    SAVE,

    /**
     * Stops the filewatcher program, stores the report in the SQL database, clears the table
     */
    STOP,

    /**
     * Starts the filewatcher program if it hasn't started yet
     */
    START,


    /**
     * The type of modification detected
     */
    TYPE,

    /**
     * The directory path that was added by the user
     */
    ADDED_DIRECTORY,

    /**
     * The directory path that was removed by the user
     */
    REMOVED_DIRECTORY,

    /**
     * The extension that was added by the user
     */
    ADDED_EXTENSION,

    /**
     * The extension that the user wants removed
     */
    REMOVED_EXTENSION,

    /**
     * The new event type.
     */
    EVENT_TYPE,

    /**
     * The event has been cleared
     */
    EVENT_CLEARED,

    /**
     * The event has unsaved events.
     */
    HAS_UNSAVED_EVENTS,

    /**
     * The event saved to file
     */
    SAVED_TO_FILE,

    /**
     * The event was loaded from the file
     */
    LOADED_FROM_FILE,

    /**
     * A new File was created.
     */
    CREATE,

    /**
     * A file was deleted.
     */
    DELETE,
    /**
     * A file was modified.
     */
    MODIFY,

    /**
     * A new file event occurred.
     */
    NEW_EVENT,

    /**
     * The watcher is watching
     */
    WATCHING,

    /**
     * The Watch path is set
     */
    WATCH_PATH_SET,

    /**
     * The watcher stopped watching
     */
    STOPPED_WATCHING,

    /**
     * A new DataEntry to add to the table
     */
    NEW_ENTRY,

    /**
     * The name of the file that was modified
     */
    FILE_NAME,

    /**
     * A new entry was added.
     */
    NEW_FILE_EVENT,

}
