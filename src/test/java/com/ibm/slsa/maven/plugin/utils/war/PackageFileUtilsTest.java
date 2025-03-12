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
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
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
    @Mock private Build projectBuild;
    @Mock private Log log;

    private String builderId = "myBuilderId";
    private String buildType = "myBuildType";
    DefaultArtifactHandler artifactHandler = new DefaultArtifactHandler(Constants.PACKAGE_TYPE);
    DefaultArtifact artifact = new DefaultArtifact("com.example", builderId, "1.0", builderId, buildType, buildType, artifactHandler);

    @Test
    public void test_onePackageFile() {
        PackageTypeUtils utils = new PackageTypeUtils(project, log);

        when(project.getBuild()).thenReturn(projectBuild);
        when(projectBuild.getDirectory()).thenReturn(Constants.RESOURCES_DIR + File.separator + "one-package");
        when(project.getArtifact()).thenReturn(artifact);
        when(project.getBuild().getFinalName()).thenReturn(Constants.FINAL_NAME_APP);

        try {
            File packageFile = utils.getBuiltPackage();
            assertEquals("app.ear", packageFile.getName(), "Package file name did not match expected value.");
        } catch (PackageFileException e) {
            fail("Encountered unexpected exception: " + e);
        }
    }

}
