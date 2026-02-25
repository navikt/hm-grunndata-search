FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get upgrade -y && apt-get install -y libjemalloc-dev && apt-get remove -y wget && apt-get autoremove -y && rm -rf /var/lib/apt/lists/*
ENV TZ="Europe/Oslo"
EXPOSE 8080
COPY build/libs/hm-grunndata-search-all.jar ./app.jar
USER 1000
CMD ["java", "-jar", "app.jar"]