FROM debian:12-slim
WORKDIR /app
RUN apt-get update && apt-get install -y openjdk-17-jdk libjemalloc-dev
ENV TZ="Europe/Oslo"
EXPOSE 8080
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"
COPY build/libs/hm-grunndata-search-all.jar ./app.jar
USER 1000
CMD ["java", "-jar", "app.jar"]