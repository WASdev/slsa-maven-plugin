/*
 * Copyright 2023 International Business Machines Corp..
 * 
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.ibm.slsa.maven.plugin.utils.war;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ibm.slsa.maven.plugin.utils.war.exceptions.PackageFileException;
import com.ibm.slsa.test.Constants;

@ExtendWith(MockitoExtension.class)
public class PackageFileUtilsTest {

    @Mock private MavenProject project;
    @Mock private MavenSession mavenSession;
    @Mock private Build projectBuild;
    @Mock private Log log;

    private String builderId = "myBuilderId";
    private String buildType = "myBuildType";
    DefaultArtifactHandler artifactHandler = new DefaultArtifactHandler(Constants.PACKAGE_TYPE);
    DefaultArtifact artifact = new DefaultArtifact("com.example", builderId, "1.0", builderId, buildType, buildType, artifactHandler);

    @Test
    public void test_onePackageFile() {
        PackageTypeUtils utils = new PackageTypeUtils(project, mavenSession, log);

        when(project.getBuild()).thenReturn(projectBuild);
        when(project.getBuild().getFinalName()).thenReturn(Constants.FINAL_NAME_APP);

        ProjectDependencyGraph pdg = mock(ProjectDependencyGraph.class);
        when(mavenSession.getProjectDependencyGraph()).thenReturn(pdg);

        MavenProject mp1 = createProjectParent();
        MavenProject mp2 = createProjectChild1(mp1);
        MavenProject mp3 = createProjectChild2(mp1);

        List<MavenProject> theList = Arrays.asList(mp1, mp2, mp3);
        when(pdg.getSortedProjects()).thenReturn(theList);

        try {
            List<File> packageFile = utils.getBuiltPackage();
            assertEquals("app.ear", packageFile.get(0).getName(), "Package file name did not match expected value.");
        } catch (PackageFileException e) {
            fail("Encountered unexpected exception: " + e);
        }
    }

    private MavenProject createProjectParent() {
        MavenProject mp1 = mock(MavenProject.class);
        when(mp1.getBuild()).thenReturn(projectBuild);
        when(mp1.getArtifact()).thenReturn(artifact);
        return mp1;
    }

    private MavenProject createProjectChild1(MavenProject parent) {
        MavenProject mp2 = mock(MavenProject.class);
        when(mp2.getBuild()).thenReturn(projectBuild);
        when(mp2.getArtifact()).thenReturn(artifact);
        return mp2;
    }

    private MavenProject createProjectChild2(MavenProject parent) {
        MavenProject mp3 = mock(MavenProject.class);
        when(mp3.getBuild()).thenReturn(projectBuild);
        when(mp3.getArtifact()).thenReturn(artifact);
        return mp3;
    }

}
