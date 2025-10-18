#!/bin/bash
# Build script for Railway deployment

# Clean and package the application
./mvnw clean package -DskipTests

# The built JAR will be in target/ directory