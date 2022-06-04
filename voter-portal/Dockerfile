ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/ev/apache:2.4.37

ARG SECOND_FACTOR
RUN mkdir -p ./htdocs/vote/
COPY ./target/dist/ ./htdocs/vote/
RUN sed -i "s=\"identification\":\s*\"[yd]ob\"=\"identification\": \"${SECOND_FACTOR}\"=g" ./htdocs/vote/config.json
RUN mkdir -p ./conf/

HEALTHCHECK --timeout=3s --retries=4 CMD curl --noproxy localhost -f http://localhost:${APACHE_HTTP_PORT}/vote/ || exit 1
