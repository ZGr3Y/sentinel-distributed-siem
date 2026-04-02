# ============================================
# sentinel-core — Multi-Stage Dockerfile
# ============================================
# Build context: project root (not this directory)
# Usage: docker build -f sentinel-core/Dockerfile .

# --- Stage 1: Build ---
# Full JDK image with Maven for compilation.
# This stage is discarded after the JAR is produced.
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

# Copy Maven wrapper and project structure
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY sentinel-common/pom.xml sentinel-common/
COPY sentinel-agent/pom.xml sentinel-agent/
COPY sentinel-core/pom.xml sentinel-core/
COPY sentinel-api/pom.xml sentinel-api/

COPY sentinel-common/src sentinel-common/src
COPY sentinel-core/src sentinel-core/src

# Ensure Maven wrapper is executable
RUN chmod +x mvnw

# Build only sentinel-common + sentinel-core, skip tests
# Use BuildKit cache mount to persist ~/.m2 between builds and across modules (locked to prevent concurrent corruption)
RUN --mount=type=cache,id=maven-v3,target=/root/.m2,sharing=locked ./mvnw -pl sentinel-common,sentinel-core -am package -DskipTests

# --- Stage 2: Runtime ---
# Minimal JRE-only Alpine image. No compiler, no source code.
FROM eclipse-temurin:21-jre-alpine

# Security: create non-root user (POLP)
RUN addgroup -S appgroup && adduser -S USER -G appgroup

WORKDIR /app

# Copy only the compiled executable JAR from the builder stage
COPY --from=builder /workspace/sentinel-core/target/*-exec.jar app.jar

# Set ownership to non-root user
RUN chown USER:appgroup app.jar

# Switch to non-root user for all subsequent commands
USER USER

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
