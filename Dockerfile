# Build Stage
FROM --platform=linux/amd64 maven:3.8-openjdk-17 AS build-stage

WORKDIR /usr/app

COPY . /usr/app

# Run Maven build
RUN mvn clean package -DskipTests

# List files to see what was actually created
RUN echo "Contents of target directory:" && \
    ls -la target/ || echo "Target directory doesn't exist"

# Run Stage
FROM --platform=linux/amd64 openjdk:17-jdk-slim

WORKDIR /usr/app

# Copy the specific JAR file with the UnoApplication name
COPY --from=build-stage /usr/app/target/UnoApplication-*.jar /usr/app/UnoApplication.jar

# Use the specific application name in the entrypoint
ENTRYPOINT ["java", "-jar", "UnoApplication.jar"]