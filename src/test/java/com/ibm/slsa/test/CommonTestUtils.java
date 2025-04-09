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
package com.ibm.slsa.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Dependency;

import com.ibm.intoto.attestation.custom.resource.descriptors.maven.MavenArtifactResourceDescriptor;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

public class CommonTestUtils {

    public Dependency createDependency(String groupId, String artifactId, String version, String scope, String type) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        dependency.setScope(scope);
        dependency.setType(type);
        return dependency;
    }

    public void assertExceptionMatchesPattern(Throwable e, String errorMsgRegex) {
        String errorMsg = e.toString();
        Pattern pattern = Pattern.compile(errorMsgRegex);
        Matcher m = pattern.matcher(errorMsg);
        assertTrue(m.find(), "Exception message did not match expected expression. Expected: [" + errorMsgRegex + "]. Message was: [" + errorMsg + "]");
    }

    public void assertJsonContainsOnlyExpectedStringEntry(String objectClass, JsonObject json, String expectedKey, String expectedValue) {
        assertEquals(1, json.size(), objectClass + " did not have the expected number of entries. " + objectClass + " was: " + json);
        assertJsonStringEntryMatches(objectClass, json, expectedKey, expectedValue);
    }

    public void assertJsonStringEntryMatches(String objectClass, JsonObject json, String expectedKey, String expectedValue) {
        assertTrue(json.containsKey(expectedKey), objectClass + " was missing the expected \"" + expectedKey + "\" key. " + objectClass + " was: " + json);
        assertEquals(expectedValue, json.getString(expectedKey), "\"" + expectedKey + "\" entry did not match the expected value.");
    }

    public void assertJsonEntryMatches(String objectClass, JsonObject json, String expectedKey, JsonValue expectedValue) {
        assertTrue(json.containsKey(expectedKey), objectClass + " was missing the expected \"" + expectedKey + "\" key. " + objectClass + " was: " + json);
        assertEquals(expectedValue, json.get(expectedKey), "\"" + expectedKey + "\" entry did not match the expected value.");
    }

    public void assertJsonOnlyContainsKeys(String objectClass, JsonObject json, String... keys) {
        if (keys == null) {
            assertEquals(JsonObject.EMPTY_JSON_OBJECT, json, objectClass + " was not expected to have any entries, but it did.");
            return;
        }
        JsonObjectBuilder remainingEntriesBuilder = Json.createObjectBuilder(json);
        for (String key : keys) {
            assertTrue(json.containsKey(key), objectClass + " did not contain a \"" + key + "\" key but should have. " + objectClass + " was: " + json);
            remainingEntriesBuilder.remove(key);
        }
        JsonObject remainingEntries = remainingEntriesBuilder.build();
        assertTrue(remainingEntries.isEmpty(), objectClass + " contained unexpected keys: " + remainingEntries.keySet() + ". " + objectClass + " was: " + json);
    }

    public void assertJsonContainsKey(String objectClass, JsonObject json, String key) {
        assertTrue(json.containsKey(key), objectClass + " did not contain a \"" + key + "\" key but should have. " + objectClass + " was: " + json);
    }

    public void assertJsonDoesNotContainKey(String objectClass, JsonObject json, String key) {
        assertFalse(json.containsKey(key), objectClass + " contained a \"" + key + "\" key when it shouldn't. " + objectClass + " was: " + json);
    }

    public void assertStringMatchesRegex(String expectedRegex, String valueToVerify) {
        assertTrue(valueToVerify.matches(expectedRegex), "String \"" + valueToVerify + "\" did not match the expected regex [" + expectedRegex + "].");
    }

    public void assertDependencyJsonMatchesValues(Dependency dependency, JsonObject dependencyJson) {
        String expectedGroupId = dependency.getGroupId();
        String expectedArtifactId = dependency.getArtifactId();
        String expectedVersion = dependency.getVersion();
        String expectedScope = dependency.getScope();
        String expectedType = dependency.getType();

        assertJsonContainsKey("MavenArtifactResourceDescriptor", dependencyJson, MavenArtifactResourceDescriptor.KEY_NAME);
        assertJsonContainsKey("MavenArtifactResourceDescriptor", dependencyJson, MavenArtifactResourceDescriptor.KEY_URI);
        assertJsonContainsKey("MavenArtifactResourceDescriptor", dependencyJson, MavenArtifactResourceDescriptor.KEY_ANNOTATIONS);

        String expectedName = expectedGroupId + ":" + expectedArtifactId + ":" + expectedVersion;
        assertEquals(expectedName, dependencyJson.getString(MavenArtifactResourceDescriptor.KEY_NAME), "Name did not match the expected value.");
        String expectedUri = String.format(MavenArtifactResourceDescriptor.URI_FORMAT, expectedGroupId.replace(".","/"), expectedArtifactId, expectedVersion);
        assertEquals(expectedUri, dependencyJson.getString(MavenArtifactResourceDescriptor.KEY_URI), "URI did not match the expected value.");

        JsonObjectBuilder annotationsBuilder = Json.createObjectBuilder();
        annotationsBuilder.add(MavenArtifactResourceDescriptor.KEY_ANNOTATION_SCOPE, expectedScope);
        annotationsBuilder.add(MavenArtifactResourceDescriptor.KEY_ANNOTATION_TYPE, expectedType);
        JsonObject expectedAnnotations = annotationsBuilder.build();
        assertEquals(expectedAnnotations, dependencyJson.getJsonObject(MavenArtifactResourceDescriptor.KEY_ANNOTATIONS), "Annotations did not match the expected value.");

        assertJsonDoesNotContainKey("MavenArtifactResourceDescriptor", dependencyJson, MavenArtifactResourceDescriptor.KEY_DIGEST);
        assertJsonDoesNotContainKey("MavenArtifactResourceDescriptor", dependencyJson, MavenArtifactResourceDescriptor.KEY_CONTENT);
        assertJsonDoesNotContainKey("MavenArtifactResourceDescriptor", dependencyJson, MavenArtifactResourceDescriptor.KEY_DOWNLOAD_LOCATION);
        assertJsonDoesNotContainKey("MavenArtifactResourceDescriptor", dependencyJson, MavenArtifactResourceDescriptor.KEY_MEDIA_TYPE);
    }

}
