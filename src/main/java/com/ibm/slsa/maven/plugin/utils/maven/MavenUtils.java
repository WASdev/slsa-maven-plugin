/*
 * Copyright 2023, 2025 International Business Machines Corp..
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
package com.ibm.slsa.maven.plugin.utils.maven;

import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import com.ibm.intoto.attestation.custom.resource.descriptors.maven.MavenArtifactResourceDescriptor;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class MavenUtils {

    private MavenProject project;
    private MavenSession mavenSession;

    public MavenUtils(MavenProject project, MavenSession mavenSession) {
        this.project = project;
        this.mavenSession = mavenSession;
    }

    public JsonObject getMavenSessionUserProperties() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        Properties userProps = mavenSession.getUserProperties();
        if (userProps != null) {
            for (Entry<Object, Object> userProp : userProps.entrySet()) {
                builder.add((String) userProp.getKey(), (String) userProp.getValue());
            }
        }
        return builder.build();
    }

    public void addMavenProjectDependencies(JsonArrayBuilder builder) {

        List<MavenProject> projects = mavenSession.getProjectDependencyGraph().getSortedProjects();
        for (MavenProject prj : projects) {
            List<Dependency> dependencies = prj.getDependencies();
            Stream<Dependency> dependenciesStream = dependencies.stream();
            dependenciesStream.forEach(d -> {
                // Submodule dependencies are not expected to have a separate entry in resolvedDependencies.
                if (!isSubModuleDependency(projects,d)){
                    MavenArtifactResourceDescriptor artifact = new MavenArtifactResourceDescriptor(d);
                    String scope = d.getScope();
                    if (!isMavenArtifactScopeToIgnore(scope)) {
                        builder.add(artifact.toJson());
                    }
                }
            });
        }
    }

    private boolean isMavenArtifactScopeToIgnore(String scope) {
        // Only add non-test dependencies
        return scope == null || "test".equalsIgnoreCase(scope);
    }

    private boolean isSubModuleDependency(List<MavenProject> projects, Dependency dependency) {
        if (dependency == null 
            || dependency.getGroupId() == null 
            || dependency.getArtifactId() == null 
            || dependency.getVersion() == null) {
            return false;
        }
    
        return projects.stream().anyMatch(p -> 
            dependency.getGroupId().equals(p.getGroupId()) &&
            dependency.getArtifactId().equals(p.getArtifactId()) &&
            dependency.getVersion().equals(p.getVersion())
        );
    }    

}
