FROM openjdk:8-jdk-alpine
RUN apk add openssl ca-certificates
VOLUME /tmp
ARG JAR_FILE=target/*.jar
RUN apk update \
 && apk add jq curl
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=prod","-jar","/app.jar"]
EXPOSE 80
HEALTHCHECK --start-period=15s --interval=1m --timeout=10s --retries=5 \
            CMD curl --silent --fail --request GET http://localhost:80/actuator/health \
                | jq --exit-status '.status == "UP"' || exit 1
