package com.ibm.slsa.maven.plugin.utils.war.exceptions;

import java.io.File;

public class MultipleWarsFoundException extends PackageFileException {

    private static final String ERROR_MSG = "There were multiple .war files found in the %s directory: %s";

    public MultipleWarsFoundException(File buildDirectory, File[] wars) {
        super(String.format(ERROR_MSG, buildDirectory.getAbsolutePath(), getFileArrayString(wars)));
    }

    private static String getFileArrayString(File[] wars) {
        String result = "";
        for (File war : wars) {
            result += war.getName() + ", ";
        }
        result = result.replaceAll(", $", "");
        return result;
    }

}
