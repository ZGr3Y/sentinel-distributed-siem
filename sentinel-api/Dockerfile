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
COPY sentinel-common/pom.xml sentinel-common/
COPY sentinel-agent/pom.xml sentinel-agent/
COPY sentinel-core/pom.xml sentinel-core/
COPY sentinel-api/pom.xml sentinel-api/

COPY sentinel-common/src sentinel-common/src
COPY sentinel-core/src sentinel-core/src
COPY sentinel-api/src sentinel-api/src

RUN chmod +x mvnw

# Build sentinel-common + sentinel-core + sentinel-api (dependency chain)
RUN --mount=type=cache,id=maven-v3,target=/root/.m2,sharing=locked ./mvnw -pl sentinel-common,sentinel-core,sentinel-api -am package -DskipTests

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S USER -G appgroup

WORKDIR /app

COPY --from=builder /workspace/sentinel-api/target/*.jar app.jar

RUN chown USER:appgroup app.jar

USER USER

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
