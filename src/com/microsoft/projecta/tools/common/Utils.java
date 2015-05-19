package com.microsoft.projecta.tools.common;

import java.nio.file.Path;

public class Utils {

    /**
     * Getting file name without extension.
     * 
     * @param path Path to the file
     * @return
     */
    public static String getNameWithoutExtension(Path path) {
        String fileName = path.getFileName().toString();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            fileName = fileName.substring(0, pos);
        }
        return fileName;
    }
}
