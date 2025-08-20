package com.tcss.filewatcher.Common;


public enum Properties {

    /**
     * Stops the filewatcher program, stores the report in the SQL database, clears the table
     */
    STOP,

    /**
     * Closed the main window when program wasnt running
     */
    CLOSED_MAIN,

    /**
     * Starts the filewatcher program if it hasn't started yet
     */
    START,

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
     * A new entry was added.
     */
    NEW_FILE_EVENT,

    /**
     * Query Directory.
     */
    QUERY_DIRECTORY,

    /**
     * Query Extension.
     */
    QUERY_EXTENSION,

    /**
     * QUERY ALL.
     */
    QUERY_ALL,

    /**
     * Query a specific directory with a specific extension.
     */
    QUERY_DIRECTORY_EXTENSION,

    /**
     * Passes the email of the admin. (CSV file)
     */
    USERS_EMAIL,

    /**
     * Used to denote that the user successfully logged in.
     */
    LOGGED_IN,

    /**
     * Denotes in the process of stoping services.
     */
    STOPPING,

    /**
     * Stopped services
     */
    STOPPED,

    /**
     * the stage closed
     */
    CLOSED,

    /**
     * The table reset button was pressed
     */
    RESET_TABLE,

}
