package com.ibm.slsa.maven.plugin.utils.war.exceptions;

import java.io.File;

public class MultiplePackagesFoundException extends PackageFileException {

    private static final String ERROR_MSG = "There were multiple .war files found in the %s directory: %s";

    public MultiplePackagesFoundException(File buildDirectory, File[] packageFiles) {
        super(String.format(ERROR_MSG, buildDirectory.getAbsolutePath(), getFileArrayString(packageFiles)));
    }

    private static String getFileArrayString(File[] packageFiles) {
        String result = "";
        for (File file : packageFiles) {
            result += file.getName() + ", ";
        }
        result = result.replaceAll(", $", "");
        return result;
    }

}
