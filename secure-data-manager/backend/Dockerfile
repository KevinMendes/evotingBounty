ARG DOCKER_REGISTRY
ARG VERSION
FROM ev/config-tools:$VERSION as config-tools
FROM $DOCKER_REGISTRY/ev/java:1.8

COPY ./src/main/resources/certificateProperties/ /home/evote/sdm/sdmConfig/certificateProperties
COPY ./src/main/resources/langs/ /home/evote/sdm/langs
COPY ./src/test/resources/keystore.properties /home/evote/sdm/sdmConfig/keystore.properties
COPY ./src/main/resources/plugin.xml /home/evote/sdm/plugin.xml
COPY ./target/sdm-backend.jar /home/evote/sdm-backend.jar
COPY --from=config-tools /home/evote/file-converter.jar /home/evote/sdm/integration/file_converter_tool/

ENV HTTP_PORT=8090 \
 APP_CONTEXT=sdm-backend

RUN chown -R evote:evote /home/evote/

WORKDIR /home/evote

USER evote
CMD java -jar sdm-backend.jar
HEALTHCHECK --timeout=3s --retries=4 CMD curl --noproxy localhost -fk http://localhost:${HTTP_PORT}/${APP_CONTEXT}/status || exit 1
