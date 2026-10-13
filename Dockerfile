# One Dockerfile for every module.
# Build one service:  docker build --build-arg MODULE=sample-service -t sample-service .

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /workspace

# Copy pom files first so Maven dependencies are cached between builds
COPY pom.xml .
COPY common/pom.xml common/
COPY sample-service/pom.xml sample-service/
COPY gateway/pom.xml gateway/
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -pl ${MODULE} -am dependency:resolve || true

COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -pl ${MODULE} -am package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:21-jre
ARG MODULE
WORKDIR /app
RUN useradd --system --uid 1001 app
COPY --from=build /workspace/${MODULE}/target/app.jar app.jar
USER app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
