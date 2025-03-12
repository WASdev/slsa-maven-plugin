package com.ibm.slsa.maven.plugin.utils.war.exceptions;

import java.io.File;

public class PackageFileNotFoundException extends PackageFileException {

    private static final String ERROR_MSG = "There were no package files found in the %s directory.";

    public PackageFileNotFoundException(File buildDirectory) {
        super(String.format(ERROR_MSG, buildDirectory.getAbsolutePath()));
    }

}
