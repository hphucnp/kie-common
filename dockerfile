# Start with a base image containing Java runtime
FROM maven:3.8.1-openjdk-11-slim as build

# Make source folder
RUN mkdir -p /workspace
WORKDIR /workspace

# Copy maven executable to the image
# COPY mvnw .
# COPY .mvn .mvn
#
# # Copy the POM
# COPY pom.xml .
#
# # Copy the source code
# COPY src src
COPY . .

# Package the application
RUN mvn install -DskipTests