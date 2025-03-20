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
                MavenArtifactResourceDescriptor artifact = new MavenArtifactResourceDescriptor(d);
                String scope = d.getScope();
                if (!isMavenArtifactScopeToIgnore(scope)) {
                    builder.add(artifact.toJson());
                }
            });
        }
    }

    private boolean isMavenArtifactScopeToIgnore(String scope) {
        // Only add non-test dependencies
        return scope == null || "test".equalsIgnoreCase(scope);
    }

}
