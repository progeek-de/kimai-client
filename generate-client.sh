#!/usr/bin/env bash
openapi-generator-cli generate -i api-docs.json -o kimai-swagger-client -g kotlin \
  --additional-properties=library=multiplatform,groupId=de.progeek,artifactId=kimai-swagger-client,omitGradleWrapper=true,omitGradlePluginVersions=true,packageName=de.progeek.kimai,serializationLibrary=kotlinx_serialization