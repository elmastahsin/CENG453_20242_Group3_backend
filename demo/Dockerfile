FROM openjdk:17-jdk AS build-stage

WORKDIR /usr/app

COPY . /usr/app

RUN ./mvnw clean package -DskipTests

# Run Stage
FROM openjdk:17-jdk-slim

WORKDIR /usr/app

COPY --from=build-stage /usr/app/target/demo-0.0.1-SNAPSHOT.jar /usr/app/

ENTRYPOINT ["java", "-jar", "demo-0.0.1-SNAPSHOT.jar"]
