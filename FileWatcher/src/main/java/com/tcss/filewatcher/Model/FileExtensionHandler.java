package com.tcss.filewatcher.Model;

import java.util.List;
import java.util.Objects;

public class FileExtensionHandler {
    private FileExtensionHandler() {
    }


    public static boolean canAddExtension(final List<String> theExtensions,
                                          final DirectoryEntry theEntry) {

        final String name = theEntry.getFileName();

        if (name.equalsIgnoreCase(".DS_Store")) {
            return false;
        }

        if (theExtensions == null) {
            return false;
        }

        if (theExtensions.contains("All Extensions")) {
            new DataBaseManager().insertFileEvent(theEntry.getDate(), theEntry.getTime(),
                    name, theEntry.getDirectory(), theEntry.getModificationType());
            return true;
        }

        final String ext = theEntry.getFileExtension();
        if (ext != null && theExtensions.contains(ext)) {
            new DataBaseManager().insertFileEvent(theEntry.getDate(), theEntry.getTime(),
                    name, theEntry.getDirectory(), theEntry.getModificationType());
            return true;
        }

        return false;
    }
}
