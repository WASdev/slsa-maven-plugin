package com.ibm.slsa.maven.plugin.utils.war;

import java.io.File;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.ibm.slsa.maven.plugin.utils.war.exceptions.PackageFileException;

public class PackageTypeUtils {

    private MavenProject project;
    private Log log;

    public PackageTypeUtils(MavenProject project, Log log) {
        this.project = project;
        this.log = log;
    }

    public File getBuiltPackage() throws PackageFileException {
        return new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + "." + project.getArtifact().getArtifactHandler().getExtension());
    }

}
