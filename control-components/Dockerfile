ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/ev/java:1.8

ENV TZ Europe/Zurich
ENV SPRING_RABBITMQ_SSL_ENABLED=true
ENV KEYS_KEYSTORE_DIR=file://${keystore_location}

COPY ./resources/ /data/appl/
COPY ./target/control-components.jar /data/appl/control-components.jar

RUN chown -R evote:evote /data/appl/ && chmod u+x /data/appl/libs/* && chmod u+x /data/appl/healthcheck.sh

WORKDIR /data/appl
USER evote
CMD java -Dloader.path="libs" -jar control-components.jar
HEALTHCHECK --timeout=3s --retries=4 CMD /data/appl/healthcheck.sh || exit 1