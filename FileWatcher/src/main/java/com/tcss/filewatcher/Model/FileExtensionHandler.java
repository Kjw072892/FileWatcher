package com.tcss.filewatcher.Model;

import java.util.List;

/**
 * Handles whether the file extension is valid.
 *
 * @author Kassie Whitney
 * @version 8.15.25
 */
public class FileExtensionHandler {
    private FileExtensionHandler() {
    }

    /**
     * Checks if the extension can be added to the table.
     * @param theListOfDirExten the list of file extensions correlated with the directory.
     * @param theEntry the new entry event.
     * @return true if the extension in the entry event is valid with the associated directory.
     */
    public static boolean canAddExtension(final List<String> theListOfDirExten,
                                          final DirectoryEntry theEntry) {

        final String fileName = theEntry.getFileName().toLowerCase();
        final String extension = theEntry.getFileExtension();

        if (fileName.equalsIgnoreCase(".DS_Store")) {
            return false;
        }

        if (theListOfDirExten == null) {
            return false;
        }

        if (theListOfDirExten.contains("All Extensions")) {
            
            new DataBaseManager(false).insertFileEvent(theEntry.getDate(), theEntry.getTime(),
                    fileName, theEntry.getDirectory(), theEntry.getModificationType());
            return true;
        }

        if (extension != null && theListOfDirExten.contains(extension)) {
            new DataBaseManager(false).insertFileEvent(theEntry.getDate(), theEntry.getTime(),
                    fileName, theEntry.getDirectory(), theEntry.getModificationType());
            return true;
        }

        return false;
    }
}
