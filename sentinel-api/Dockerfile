# ============================================
# sentinel-api — Multi-Stage Dockerfile
# ============================================
# Build context: project root (not this directory)
# Usage: docker build -f sentinel-api/Dockerfile .
#
# NOTE: sentinel-api depends on sentinel-core (see pom.xml),
# so the Maven build must include sentinel-core in the -pl flag.

# --- Stage 1: Build ---
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY sentinel-common sentinel-common
COPY sentinel-core sentinel-core
COPY sentinel-api sentinel-api

RUN chmod +x mvnw

# Build sentinel-common + sentinel-core + sentinel-api (dependency chain)
RUN ./mvnw -pl sentinel-common,sentinel-core,sentinel-api -am package -DskipTests

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S USER -G appgroup

WORKDIR /app

COPY --from=builder /workspace/sentinel-api/target/*.jar app.jar

RUN chown USER:appgroup app.jar

USER USER

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
