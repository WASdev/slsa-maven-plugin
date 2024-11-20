/*
 * Copyright 2023, 2024 International Business Machines Corp..
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
package com.ibm.slsa.maven.plugin;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.ibm.slsa.maven.plugin.exceptions.ProvenanceGenerationException;
import jakarta.json.JsonObject;

/**
 * This is the Javadoc for the GenerateProvenanceMojo class.
 */
@Mojo(name = "generate-provenance")
public class GenerateProvenanceMojo extends AbstractMojo {

    public static final String BUILD_TYPE_DEFAULT = "https://github.com/WASdev/slsa-maven-plugin/tree/main/v1.0";

    public static final String PROVENANCE_FILE_DEFAULT_OUTPUT_PATH = "target/slsa";
    public static final String PROVENANCE_FILE_DEFAULT_NAME = "slsa_provenance.json";

    /**
     * The Maven project executing this plugin.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * The Maven session executing this plugin.
     */
    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession mavenSession;

    /**
     * URI indicating the transitive closure of the trusted build platform. This is intended to be the sole determiner of the SLSA
     * Build level. The {@code builder.id} URI SHOULD resolve to documentation explaining:
     * <ul>
     * <li>The scope of what this ID represents.
     * <li>The claimed SLSA Build level.
     * <li>The accuracy and completeness guarantees of the fields in the provenance.
     * <li>Any fields that are generated by the tenant-controlled build process and not verified by the trusted control plane,
     * except for the subject.
     * <li>The interpretation of any extension fields.
     * </ul>
     */
    @Parameter(property = "builderId", required = true)
    private String builderId;

    /**
     * Identifies the template for how to perform the build and interpret the parameters and dependencies.
     * <p>
     * The URI SHOULD resolve to a human-readable specification that includes: overall description of the build type; schema for
     * {@code externalParameters} and {@code internalParameters}; unambiguous instructions for how to initiate the build given
     * this {@code BuildDefinition}, and a complete example. Example:
     * https://slsa-framework.github.io/github-actions-buildtypes/workflow/v1.
     */
    @Parameter(property = "buildType", defaultValue = BUILD_TYPE_DEFAULT)
    private String buildType;

    /**
     * The directory path to which the provenance file is written.
     */
    @Parameter(property = "provenanceFilePath", defaultValue = PROVENANCE_FILE_DEFAULT_OUTPUT_PATH)
    private String provenanceFilePath;

    /**
     * The name of the provenance file.
     */
    @Parameter(property = "provenanceFileName", defaultValue = PROVENANCE_FILE_DEFAULT_NAME)
    private String provenanceFileName;

    public void execute() throws MojoExecutionException {
        createProvenanceFile();
    }

    private void createProvenanceFile() throws MojoExecutionException {
        new File(provenanceFilePath).mkdirs();
        File newFile = new File(provenanceFilePath + File.separator + provenanceFileName);
        try {
            FileWriter writer = new FileWriter(newFile);
            newFile.createNewFile();
            writer.write(getFileContents().toString());
            writer.close();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed creating the provenance file or writing the provenance file contents: " + e.getMessage(), e);
        }
    }

    private JsonObject getFileContents() throws ProvenanceGenerationException {
        ProvenanceGenerator generator = new ProvenanceGenerator(builderId, buildType, project, mavenSession, getLog());
        return generator.generateProvenanceFileData();
    }

}
