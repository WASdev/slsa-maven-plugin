# Build Type: SLSA Provenance Generation

This page describes a build type as outlined and recommended by https://slsa.dev/provenance/v1.

## Description

```jsonc
"buildType" : "https://github.com/WASdev/slsa-maven-plugin/tree/main/v1.0"
```

This document describes the build type for the SLSA Maven Plugin. The plugin generates a SLSA Provenance file based on a Maven project using Git.

## Build Definition

### External parameters

| Parameter | Type | Description |
| --------- | ---- | ----------- |
| repository | string | The remote origin URL of the Git repository. |
| ref | string | The full name of the current Git branch. E.g., `refs/heads/main` |
| userProperties | object | An object that maps the keys and respective values from the Maven session parameters. |

### Internal parameters

| Parameter | Type | Description |
| --------- | ---- | ----------- |
| N/A | N/A | N/A |

### Resolved dependencies

#### Git repository

| Parameter | Type | Description |
| --------- | ---- | ----------- |
| uri | [ResourceURI](https://github.com/in-toto/attestation/blob/main/spec/v1/field_types.md#ResourceURI) | The URI of the Git repository and ref using the `git+https` SPDX scheme. |
| digest | [DigestSet](https://github.com/in-toto/attestation/blob/main/spec/v1/digest_set.md) | The DigestSet which contains the Git commit. |
| digest.gitCommit | string | The commit of the current Git branch HEAD in SHA-1 format. |

#### Maven artifact

| Parameter | Type | Description |
| --------- | ---- | ----------- |
| name | string | The coordinates of the Maven artifact. This string contains the groupId, artifactId, and version of the artifact, seperated by colons. |
| uri | [ResourceURI](https://github.com/in-toto/attestation/blob/main/spec/v1/field_types.md#ResourceURI) | The URI to the Maven artifact on Maven Central. |
| annotations | object | An object that contains additional information about the Maven artifact such as the type and scope. |
| annotations.type | string | The packaging type of the Maven artifact. E.g., `pom`, `jar`, etc. |
| annotations.scope | string | The scope of the Maven artifact. E.g., `provided`, `compile`, etc. |

## Run Details

### Builder

| Parameter | Type | Description |
| --------- | ---- | ----------- |
| id | [TypeURI](https://github.com/in-toto/attestation/blob/main/spec/v1/field_types.md#typeuri) | A URI which should point to documentation pertinent to the build platform running this plugin. See https://slsa.dev/spec/v1.0/provenance#builder.id for more information. |

### Metadata

| Parameter | Type | Description |
| --------- | ---- | ----------- |
| startedOn | string | A string representing the date and time at which the Maven session started. E.g., `2024-02-28T22:49:19.695Z` |

## Example

See [slsa_provenance.json](slsa_provenance.json) for an example output.

This example was run using https://github.com/ayoho/sample-app with the command:

```
$ mvn clean package com.ibm.websphere.appserver.features:slsa-maven-plugin:1.0:generate-provenance -DbuilderId="https://example.com/builder_id" -DbuildType="https://example.com/build_type"
```