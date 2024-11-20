# SLSA Provenance Maven Plugin

- [Usage](#usage)
  - [Install this plugin into your local Maven repository](#install-this-plugin-into-your-local-maven-repository)
  - [Adding the plugin to an existing Maven project](#adding-the-plugin-to-an-existing-maven-project)
  - [Run the plugin](#run-the-plugin)
  - [Generate the documentation site locally](#generate-the-documentation-site-locally)
- [Example output](#example-output)

## Usage

### Install this plugin into your local Maven repository

```
$ mvn clean install
```

### Adding the plugin to an existing Maven project

Add the following to the pom.xml for the existing project:

```XML
<build>
    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>com.ibm.websphere.appserver.features</groupId>
                <artifactId>slsa-maven-plugin</artifactId>
                <version>1.0</version>
            </plugin>
        </plugins>
    </pluginManagement>
    <plugins>
        <plugin>
            <groupId>com.ibm.websphere.appserver.features</groupId>
            <artifactId>slsa-maven-plugin</artifactId>
            <version>1.0</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>generate-provenance</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

The new plugin goals will run as part of the `package` goal of the existing Maven project.

### Run the plugin

The plugin runs as part of the `package` goal of the project. The plugin needs a builder ID, a URI, which has requirements that can be found at https://slsa.dev/spec/v1.0/provenance#builder.id. The command will therefore look something like:

```
$ mvn package -DbuilderId="https://example.com/builder_id"
```

By default the command will output the provenance data into `target/slsa_provenance.json`.

Optional parameters:
- `-DprovenanceFilePath`: Path to which the provenance file will be written. The default value is `target`.

    Example:

    `$ mvn package -DbuilderId="https://example.com/builder_id" -DprovenanceFilePath="/path/to/provenance/output/"`

- `-DprovenanceFileName`: Name of the generated provenance file. The default value is `slsa_provenance.json`.

    Example:

    `$ mvn package -DbuilderId="https://example.com/builder_id" -DprovenanceFileName="provenance.json"`

- `-DbuildType`: A URI, which has requirements that can be found at https://slsa.dev/spec/v1.0/provenance#buildType. The default value is `https://github.com/WASdev/slsa-maven-plugin/tree/main/v1.0`. It is recommended to update the build type using this parameter if this repository is forked.

    Example:

    `$ mvn package -DbuilderId="https://example.com/builder_id" -DbuildType="https://example.com/build_type"`

### Generate the documentation site locally

```
$ mvn site
```

This generates HTML pages in `target/site/` that contain information about the plugin.

## Example output

```JSON
{
    "_type": "https://in-toto.io/Statement/v1",
    "subject": [
        {
            "name": "sample-app.war",
            "digest": {
                "sha256": "f394b637eeb729346f6d203bc367c6d36a74af1d3e1d84ebdc758224e7548616"
            }
        }
    ],
    "predicateType": "https://slsa.dev/provenance/v1",
    "predicate": {
        "buildDefinition": {
            "buildType": "https://example.com/build_type",
            "externalParameters": {
                "repository": "git@github.com:ayoho/sample-app.git",
                "ref": "refs/heads/main",
                "userProperties": {
                    "builderId": "https://example.com/builder_id",
                    "buildType": "https://example.com/build_type"
                }
            },
            "resolvedDependencies": [
                {
                    "uri": "git+https://github.com/ayoho/sample-app@refs/heads/main",
                    "digest": {
                        "gitCommit": "549ec9c0333446e0d31d1c8103d822867c65ce01"
                    }
                },
                {
                    "name": "org.eclipse.microprofile:microprofile:4.0.1",
                    "uri": "https://central.sonatype.com/artifact/org.eclipse.microprofile/microprofile/4.0.1",
                    "annotations": {
                        "type": "pom",
                        "scope": "provided"
                    }
                },
                {
                    "name": "jakarta.platform:jakarta.jakartaee-api:8.0.0",
                    "uri": "https://central.sonatype.com/artifact/jakarta.platform/jakarta.jakartaee-api/8.0.0",
                    "annotations": {
                        "type": "jar",
                        "scope": "provided"
                    }
                },
                {
                    "name": "com.ibm.websphere.appserver.api:com.ibm.websphere.appserver.api.basics:1.4.60",
                    "uri": "https://central.sonatype.com/artifact/com.ibm.websphere.appserver.api/com.ibm.websphere.appserver.api.basics/1.4.60",
                    "annotations": {
                        "type": "jar",
                        "scope": "provided"
                    }
                },
                {
                    "name": "org.apache.derby:derby:10.15.2.0",
                    "uri": "https://central.sonatype.com/artifact/org.apache.derby/derby/10.15.2.0",
                    "annotations": {
                        "type": "jar",
                        "scope": "provided"
                    }
                },
                {
                    "name": "org.apache.derby:derbytools:10.15.2.0",
                    "uri": "https://central.sonatype.com/artifact/org.apache.derby/derbytools/10.15.2.0",
                    "annotations": {
                        "type": "jar",
                        "scope": "provided"
                    }
                },
                {
                    "name": "org.apache.derby:derbyshared:10.15.2.0",
                    "uri": "https://central.sonatype.com/artifact/org.apache.derby/derbyshared/10.15.2.0",
                    "annotations": {
                        "type": "jar",
                        "scope": "provided"
                    }
                },
                {
                    "name": "com.ibm.websphere.appserver.api:com.ibm.websphere.appserver.api.jwt:1.1.62",
                    "uri": "https://central.sonatype.com/artifact/com.ibm.websphere.appserver.api/com.ibm.websphere.appserver.api.jwt/1.1.62",
                    "annotations": {
                        "type": "jar",
                        "scope": "provided"
                    }
                }
            ]
        },
        "runDetails": {
            "builder": {
                "id": "https://example.com/builder_id"
            },
            "metadata": {
                "startedOn": "2024-02-29T20:00:38.400Z"
            }
        }
    }
}
```