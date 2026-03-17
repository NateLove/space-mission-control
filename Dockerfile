####
# Space Mission Control - Runtime image
#
# Build the jar locally first:
#   ./mvnw package -DskipTests
#
# Then build this image:
#   docker build -t space-mission-control:latest .
####

FROM registry.access.redhat.com/ubi8/openjdk-17-runtime:1.18

ENV LANGUAGE='en_US:en'
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

# Copy the pre-built Quarkus fast-jar from target/
COPY --chown=185 target/quarkus-app/lib/       /deployments/lib/
COPY --chown=185 target/quarkus-app/*.jar       /deployments/
COPY --chown=185 target/quarkus-app/app/        /deployments/app/
COPY --chown=185 target/quarkus-app/quarkus/    /deployments/quarkus/

EXPOSE 8080
USER 185

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]