ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/ev/java:1.8
ENV TZ Europe/Zurich
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /data/appl/message-broker-orchestrator.jar

WORKDIR /data/appl/
USER evote
CMD java -Dloader.path="libs" -jar message-broker-orchestrator.jar

HEALTHCHECK --timeout=3s --retries=4 CMD curl -AF --noproxy localhost -fk http://localhost:${SERVER_PORT}/mo-ws-rest/check || exit 1