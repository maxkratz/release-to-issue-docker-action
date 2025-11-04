# Build the application within Docker
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY release-to-issue-java/pom.xml .
COPY release-to-issue-java/src ./src
RUN mvn clean compile assembly:single -DskipTests

# Base image and maintainer
FROM eclipse-temurin:17
LABEL maintainer="Max Kratz <github@maxkratz.com>"

COPY --from=build /app/target/release-to-issue-java-*-jar-with-dependencies.jar .

# Copy and define the entrypoint script
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
