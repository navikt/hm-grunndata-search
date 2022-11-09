FROM navikt/java:17
USER root
RUN apt-get update && apt-get install -y curl
USER apprunner
COPY build/libs/hm-grunndata-search.jar ./app.jar
ENV JAVA_OPTS="-Xms256m -Xmx1024m"
