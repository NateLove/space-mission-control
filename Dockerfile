####
# Multi-stage build for Space Mission Control
# Stage 1: Build the Quarkus fast-jar
# Stage 2: Minimal runtime image for OpenShift
####

## ── Stage 1: Build ───────────────────────────────────────────────────────────
FROM registry.access.redhat.com/ubi8/openjdk-17:1.18 AS builder

USER root
WORKDIR /workspace

# Copy Maven wrapper and pom first for layer caching
COPY --chown=185 mvnw .
COPY --chown=185 .mvn .mvn
COPY --chown=185 pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -q

# Copy source and build
COPY --chown=185 src ./src
RUN ./mvnw package -DskipTests -q

## ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM registry.access.redhat.com/ubi8/openjdk-17-runtime:1.18

ENV LANGUAGE='en_US:en'

# Copy fast-jar
COPY --chown=185 --from=builder /workspace/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=builder /workspace/target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=builder /workspace/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=builder /workspace/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185

ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
