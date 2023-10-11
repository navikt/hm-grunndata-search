FROM navikt/java:17
USER root
USER apprunner
COPY build/libs/hm-grunndata-search-all.jar ./app.jar
