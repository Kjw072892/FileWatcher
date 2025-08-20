package com.tcss.filewatcher.Model;

import java.util.List;

public class FileExtensionHandler {
    private FileExtensionHandler() {
    }


    public static boolean canAddExtension(final List<String> theListOfDirExten,
                                          final DirectoryEntry theEntry) {

        final String fileName = theEntry.getFileName().toLowerCase();
        final String extension = theEntry.getFileExtension();

        System.out.println("Entry: " + theEntry.getDirectory());
        System.out.println("Extension: " + theEntry.getFileExtension());
        System.out.println("The List of Extensions for Dir: "+theListOfDirExten);

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
