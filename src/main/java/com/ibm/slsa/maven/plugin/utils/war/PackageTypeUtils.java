package com.ibm.slsa.maven.plugin.utils.war;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.ibm.slsa.maven.plugin.utils.war.exceptions.PackageFileException;

public class PackageTypeUtils {

    private MavenProject project;
    private MavenSession mavenSession;
    private Log log;

    public PackageTypeUtils(MavenProject project, MavenSession mavenSession, Log log) {
        this.project = project;
        this.mavenSession = mavenSession;
        this.log = log;
    }

    public List<File> getBuiltPackage() throws PackageFileException {

        List<File> files = new ArrayList<>();
        List<MavenProject> projects = mavenSession.getProjectDependencyGraph().getSortedProjects();

        for (MavenProject prj : projects) {
            if (prj.getArtifact().getArtifactHandler().getExtension().equals("pom")) {
                continue;
            }
            File file = new File(prj.getBuild().getDirectory(),
                    prj.getBuild().getFinalName() + "." + prj.getArtifact().getArtifactHandler().getExtension());

            files.add(file);

        }
        return files;
    }

}