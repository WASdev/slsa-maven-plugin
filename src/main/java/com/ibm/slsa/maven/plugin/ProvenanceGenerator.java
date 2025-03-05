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

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.ibm.intoto.attestation.Statement;
import com.ibm.intoto.attestation.Subject;
import com.ibm.intoto.attestation.custom.resource.descriptors.file.WarResourceDescriptor;
import com.ibm.intoto.attestation.custom.resource.descriptors.file.exceptions.WarFileException;
import com.ibm.intoto.attestation.custom.resource.descriptors.git.GitRepositoryResourceDescriptor;
import com.ibm.intoto.attestation.exceptions.DigestCalculationException;
import com.ibm.intoto.attestation.exceptions.StatementValueNullException;
import com.ibm.intoto.attestation.utils.Utils;
import com.ibm.slsa.BuildDefinition;
import com.ibm.slsa.BuildMetadata;
import com.ibm.slsa.Builder.BuilderBuilder;
import com.ibm.slsa.RunDetails;
import com.ibm.slsa.SlsaPredicate;
import com.ibm.slsa.maven.plugin.exceptions.BuildDefinitionGenerationException;
import com.ibm.slsa.maven.plugin.exceptions.GitRepositoryException;
import com.ibm.slsa.maven.plugin.exceptions.ProvenanceGenerationException;
import com.ibm.slsa.maven.plugin.exceptions.SlsaPredicateGenerationException;
import com.ibm.slsa.maven.plugin.exceptions.StatementException;
import com.ibm.slsa.maven.plugin.utils.git.GitUtils;
import com.ibm.slsa.maven.plugin.utils.maven.MavenUtils;
import com.ibm.slsa.maven.plugin.utils.war.PackageTypeUtils;
import com.ibm.slsa.maven.plugin.utils.war.exceptions.PackageFileException;
import com.ibm.slsa.maven.plugin.utils.war.exceptions.PackageFileNotFoundException;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 * Generates provenance in the form of an in-toto attestation Statement (see https://github.com/in-toto/attestation/blob/main/spec/v1/statement.md).
 * The Statement's Predicate uses the SLSA v1 predicate format (see https://slsa.dev/provenance/v1). The subject of the statement
 * is limited to a single Resource Descriptor describing a single .war file located in the Maven project's build directory where
 * all files generated by the project are placed.
 */
public class ProvenanceGenerator {

    public static final String KEY_EXT_PARAMS_REPOSITORY = "repository";
    public static final String KEY_EXT_PARAMS_REF = "ref";
    public static final String KEY_EXT_PARAMS_USER_PROPERTIES = "userProperties";

    private String builderId;
    private String buildType;
    private MavenSession mavenSession;
    private GitUtils gitUtils;
    private PackageTypeUtils warUtils;
    private MavenUtils mavenUtils;

    public ProvenanceGenerator(String builderId, String buildType, MavenProject project, MavenSession mavenSession, Log log) {
        this.builderId = builderId;
        this.buildType = buildType;
        this.mavenSession = mavenSession;
        this.gitUtils = new GitUtils();
        this.warUtils = new PackageTypeUtils(project, log);
        this.mavenUtils = new MavenUtils(project, mavenSession);
    }

    public JsonObject generateProvenanceFileData() throws ProvenanceGenerationException {
        try {
            Subject subject = buildSubject();
            SlsaPredicate predicate = buildSlsaPredicate();
            Statement statement = buildStatement(subject, predicate);
            return statement.toJson();
        } catch (PackageFileNotFoundException e) {
            // Allow for now
            return JsonObject.EMPTY_JSON_OBJECT;
        } catch (Exception e) {
            throw new ProvenanceGenerationException(e.getMessage(), e);
        }
    }

    private Subject buildSubject() throws DigestCalculationException, PackageFileException, WarFileException {
        // Subject reflects only a single .war file located in the Maven project's build directory
        WarResourceDescriptor warResourceDescriptor = new WarResourceDescriptor(warUtils.getBuiltPackage());
        Subject.Builder subjectBuilder = new Subject.Builder();
        subjectBuilder.resourceDescriptor(warResourceDescriptor);
        return subjectBuilder.build();
    }

    private SlsaPredicate buildSlsaPredicate() throws SlsaPredicateGenerationException {
        try {
            BuildDefinition buildDefinition = buildBuildDefinition();
            RunDetails runDetails = buildRunDetails();
            return new SlsaPredicate(buildDefinition, runDetails);
        } catch (BuildDefinitionGenerationException e) {
            throw new SlsaPredicateGenerationException(e.getMessage(), e);
        }
    }

    private BuildDefinition buildBuildDefinition() throws BuildDefinitionGenerationException {
        try {
            GitRepositoryResourceDescriptor gitRepositoryResourceDescriptor = gitUtils.getGitRepositoryResourceDescriptor();
            JsonObject externalParameters = populateExternalParameters(gitRepositoryResourceDescriptor);

            BuildDefinition.Builder buildDefinitionBuilder = new BuildDefinition.Builder(buildType, externalParameters);
            JsonArray resolvedDependencies = populateResolvedDependencies(gitRepositoryResourceDescriptor);
            buildDefinitionBuilder.resolvedDependencies(resolvedDependencies);
            return buildDefinitionBuilder.build();
        } catch (GitRepositoryException e) {
            throw new BuildDefinitionGenerationException(e.getMessage(), e);
        }
    }

    private JsonObject populateExternalParameters(GitRepositoryResourceDescriptor repoResourceDescriptor) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(KEY_EXT_PARAMS_REPOSITORY, repoResourceDescriptor.getGitRepoUrl());
        builder.add(KEY_EXT_PARAMS_REF, repoResourceDescriptor.getRef());
        Utils.addIfNonNullAndNotEmpty(mavenUtils.getMavenSessionUserProperties(), KEY_EXT_PARAMS_USER_PROPERTIES, builder);
        return builder.build();
    }

    private JsonArray populateResolvedDependencies(GitRepositoryResourceDescriptor repoResourceDescriptor) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        builder.add(repoResourceDescriptor.toJson());
        mavenUtils.addMavenProjectDependencies(builder);
        return builder.build();
    }

    private RunDetails buildRunDetails() {
        BuildMetadata.Builder buildMetadataBuilder = new BuildMetadata.Builder();
        buildMetadataBuilder.startedOn(getStartTime().toString());

        BuilderBuilder builderBuilder = new BuilderBuilder(builderId);

        RunDetails.Builder runDetailsBuilder = new RunDetails.Builder(builderBuilder.build());
        runDetailsBuilder.metadata(buildMetadataBuilder.build());
        return runDetailsBuilder.build();
    }

    private ZonedDateTime getStartTime() {
        return ZonedDateTime.ofInstant(mavenSession.getStartTime().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
    }

    private Statement buildStatement(Subject subject, SlsaPredicate predicate) throws StatementException {
        Statement.Builder statementBuilder;
        try {
            statementBuilder = new Statement.Builder(Statement.TYPE_IN_TOTO_STATEMENT, subject, SlsaPredicate.PREDICATE_TYPE_SLSA_PROVENANCE_V1);
            return statementBuilder.predicate(predicate).build();
        } catch (StatementValueNullException e) {
            throw new StatementException(e.getMessage(), e);
        }
    }

}
