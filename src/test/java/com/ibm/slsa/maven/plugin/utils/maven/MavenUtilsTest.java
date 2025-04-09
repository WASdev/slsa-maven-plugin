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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ibm.slsa.test.CommonTestUtils;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

@ExtendWith(MockitoExtension.class)
public class MavenUtilsTest {

    @Mock private MavenProject project;
    @Mock private MavenSession mavenSession;

    private CommonTestUtils testUtils = new CommonTestUtils();

    MavenUtils utils;

    @BeforeEach
    public void beforeEach() {
        utils = new MavenUtils(project, mavenSession);
    }

    @Test
    public void test_getMavenSessionUserProperties_noProps() {
        final Properties props = new Properties();

        when(mavenSession.getUserProperties()).thenReturn(props);

        JsonObject result = utils.getMavenSessionUserProperties();

        assertEquals(JsonObject.EMPTY_JSON_OBJECT, result, "Should not have found any properties but did.");
    }

    @Test
    public void test_getMavenSessionUserProperties_withProps() {
        final Properties props = new Properties();
        props.setProperty("special.property", "isSet");
        props.setProperty("maven.repo.local", "/");
        props.setProperty("number", "123");

        when(mavenSession.getUserProperties()).thenReturn(props);

        JsonObject result = utils.getMavenSessionUserProperties();

        assertEquals(props.keySet(), result.keySet(), "Did not have the same set of keys in the result as expected.");
        for (Object key : props.keySet()) {
            String expectedValue = props.getProperty((String) key);
            assertEquals(expectedValue, result.getString((String) key), "Value for \"" + key + "\" did not match expected value.");
        }
    }

    @Test
    public void test_addMavenProjectDependencies_noDependencies() {

        ProjectDependencyGraph pdg = mock(ProjectDependencyGraph.class);
        when(mavenSession.getProjectDependencyGraph()).thenReturn(pdg);

        MavenProject mp1 = createProjectParent();
        MavenProject mp2 = createProjectChild1(mp1);
        MavenProject mp3 = createProjectChild2(mp1);

        List<MavenProject> projectList = Arrays.asList(mp1, mp2, mp3);
        when(pdg.getSortedProjects()).thenReturn(projectList);
        JsonArrayBuilder builder = Json.createArrayBuilder();

        utils.addMavenProjectDependencies(builder);

        JsonArray result = builder.build();
        assertEquals(result, JsonArray.EMPTY_JSON_ARRAY, "Should not have added any dependencies but did.");
    }

    @Test
    public void test_addMavenProjectDependencies_emptyDependency() {

        ProjectDependencyGraph pdg = mock(ProjectDependencyGraph.class);
        when(mavenSession.getProjectDependencyGraph()).thenReturn(pdg);

        MavenProject mp1 = createProjectParent();
        MavenProject mp2 = createProjectChild1(mp1);
        MavenProject mp3 = createProjectChild2(mp1);

        List<MavenProject> projectList = Arrays.asList(mp1, mp2, mp3);
        when(pdg.getSortedProjects()).thenReturn(projectList);

        JsonArrayBuilder builder = Json.createArrayBuilder();

        Dependency dependency = new Dependency();
        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(dependency);
        when(mp2.getDependencies()).thenReturn(dependencies);

        utils.addMavenProjectDependencies(builder);

        JsonArray result = builder.build();
        assertEquals(result, JsonArray.EMPTY_JSON_ARRAY, "Should not have added any dependencies but did.");
    }

    @Test
    public void test_addMavenProjectDependencies_oneDependency_testScope() {

        ProjectDependencyGraph pdg = mock(ProjectDependencyGraph.class);
        when(mavenSession.getProjectDependencyGraph()).thenReturn(pdg);

        MavenProject mp1 = createProjectParent();
        MavenProject mp2 = createProjectChild1(mp1);
        MavenProject mp3 = createProjectChild2(mp1);

        List<MavenProject> projectList = Arrays.asList(mp1, mp2, mp3);
        when(pdg.getSortedProjects()).thenReturn(projectList);

        JsonArrayBuilder builder = Json.createArrayBuilder();

        final Dependency dependency = testUtils.createDependency("com.example", "code-api", "1.0.0", "test", "jar");

        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(dependency);
        when(mp2.getDependencies()).thenReturn(dependencies);

        utils.addMavenProjectDependencies(builder);

        JsonArray result = builder.build();
        assertEquals(result, JsonArray.EMPTY_JSON_ARRAY, "Should not have added any dependencies but did.");
    }

    @Test
    public void test_addMavenProjectDependencies_oneDependency_providedScope() {
        MavenProject mp1 = createProjectParent();
        MavenProject mp2 = createProjectChild1(mp1);

        final Dependency dependency = testUtils.createDependency("com.example", "code-api", "1.0.0", "provided", "jar");

        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(dependency);
        when(mp2.getDependencies()).thenReturn(dependencies);

        MavenProject mp3 = createProjectChild2(mp1);

        List<MavenProject> projectList = Arrays.asList(mp1, mp2, mp3);
        ProjectDependencyGraph pdg = mock(ProjectDependencyGraph.class);
        when(mavenSession.getProjectDependencyGraph()).thenReturn(pdg);
        when(pdg.getSortedProjects()).thenReturn(projectList);
        JsonArrayBuilder builder = Json.createArrayBuilder();

        utils.addMavenProjectDependencies(builder);

        JsonArray result = builder.build();
        assertEquals(1, result.size(), "Should have had one entry in the result, but didn't. Result was: " + result);
        JsonObject dependencyJson = result.getJsonObject(0);
        testUtils.assertDependencyJsonMatchesValues(dependency, dependencyJson);
    }

    @Test
    public void test_addMavenProjectDependencies_multipleDependencies() {
        MavenProject mp1 = createProjectParent();
        MavenProject mp2 = createProjectChild1(mp1);

        JsonArrayBuilder builder = Json.createArrayBuilder();

        final Dependency providedDependency1 = testUtils.createDependency("com.example", "code-api", "1.0.0", "provided", "jar");
        final Dependency providedDependency2 = testUtils.createDependency("com.empire", "stardust", "0.9.9", "provided", "pom");
        final Dependency providedDependency3 = testUtils.createDependency("org.rebels", "fulcrum", "0.9.9", "provided", "jar");
        final Dependency providedDependency4 = testUtils.createDependency("org.anonymous", "tenet", "1.2.1", "provided", "jar");
        final Dependency testDependency1 = testUtils.createDependency("com.example", "stuff", "0.4.2", "test", "jar");
        final Dependency testDependency2 = testUtils.createDependency("org.example", "super-cool-test-utils", "3.5.5", "test", "jar");

        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(providedDependency1);
        dependencies.add(testDependency1);
        dependencies.add(providedDependency2);
        dependencies.add(providedDependency3);
        dependencies.add(providedDependency4);
        dependencies.add(testDependency2);
        when(mp2.getDependencies()).thenReturn(dependencies);

        MavenProject mp3 = createProjectChild2(mp1);

        List<MavenProject> projectList = Arrays.asList(mp1, mp2, mp3);
        ProjectDependencyGraph pdg = mock(ProjectDependencyGraph.class);
        when(mavenSession.getProjectDependencyGraph()).thenReturn(pdg);
        when(pdg.getSortedProjects()).thenReturn(projectList);

        utils.addMavenProjectDependencies(builder);

        JsonArray result = builder.build();
        assertEquals(4, result.size(), "Result did not have the expected number of entries. Result was: " + result);
        JsonObject dependency1Json = result.getJsonObject(0);
        testUtils.assertDependencyJsonMatchesValues(providedDependency1, dependency1Json);
        JsonObject dependency2Json = result.getJsonObject(1);
        testUtils.assertDependencyJsonMatchesValues(providedDependency2, dependency2Json);
        JsonObject dependency3Json = result.getJsonObject(2);
        testUtils.assertDependencyJsonMatchesValues(providedDependency3, dependency3Json);
        JsonObject dependency4Json = result.getJsonObject(3);
        testUtils.assertDependencyJsonMatchesValues(providedDependency4, dependency4Json);
    }

    private MavenProject createProjectParent() {
        MavenProject mp1 = mock(MavenProject.class);
        return mp1;
    }

    private MavenProject createProjectChild1(MavenProject parent) {
        MavenProject mp2 = mock(MavenProject.class);
        return mp2;
    }

    private MavenProject createProjectChild2(MavenProject parent) {
        MavenProject mp3 = mock(MavenProject.class);
        return mp3;
    }

}