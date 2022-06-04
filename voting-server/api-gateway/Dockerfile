ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/ev/tomee:8

WORKDIR $CATALINA_HOME

COPY ./resources/server_*.xml ./conf/server.xml
COPY ./resources/tomee_*.xml ./conf/tomee.xml
COPY ./target/ag-ws-rest.war ./webapps/ag-ws-rest.war

ENV TC_HTTP_PORT=8011 \
    DEBUG_PORT=6006 \
    APP_CONTEXT=ag-ws-rest

RUN chown evote:evote -R /data/appl

# REST TIME OUT PROPERTIES
ENV READ_TIME_OUT=60
ENV WRITE_TIME_OUT=60
ENV CONNECTION_TIME_OUT=60

# CRYPTO POOLS PROPERTIES
ENV ASYMMETRIC_MAX_ELEMENTS_CRYPTO_POOL=50
ENV PROOFS_MAX_ELEMENTS_CRYPTO_POOL=50
ENV PRIMITIVES_MAX_ELEMENTS_CRYPTO_POOL=50
ENV ELGAMAL_MAX_ELEMENTS_CRYPTO_POOL=50
ENV STORES_MAX_ELEMENTS_CRYPTO_POOL=50
ENV SYMMETRIC_MAX_ELEMENTS_CRYPTO_POOL=50

USER evote
ENV JAVA_OPTS="-Dorg.apache.johnzon.max-string-length=262144"

EXPOSE ${TC_HTTP_PORT} ${DEBUG_PORT}
CMD ./bin/catalina.sh run
HEALTHCHECK --timeout=3s --retries=4 CMD curl -AF --noproxy localhost -fk http://localhost:${TC_HTTP_PORT}/${APP_CONTEXT}/api/ov/monitoring/1.0/check || exit 1

