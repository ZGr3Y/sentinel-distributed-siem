# ============================================
# sentinel-agent — Multi-Stage Dockerfile
# ============================================
# Build context: project root (not this directory)
# Usage: docker build -f sentinel-agent/Dockerfile .

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
COPY sentinel-agent/src sentinel-agent/src

RUN chmod +x mvnw

# Build sentinel-common + sentinel-agent
RUN --mount=type=cache,id=maven-v3,target=/root/.m2,sharing=locked ./mvnw -pl sentinel-common,sentinel-agent -am package -DskipTests

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S USER -G appgroup

WORKDIR /app

COPY --from=builder /workspace/sentinel-agent/target/*.jar app.jar

RUN chown USER:appgroup app.jar

USER USER

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
