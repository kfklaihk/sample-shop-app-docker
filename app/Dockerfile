FROM eclipse-temurin:17-jdk-alpine 
RUN apk update && apk add --no-cache curl
RUN  adduser -Dh /home/gordon gordon 
WORKDIR /static
COPY react-app/build/ .
WORKDIR /app
COPY target/AtSea-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "/app/AtSea-0.0.1-SNAPSHOT.jar"]
CMD ["--spring.profiles.active=postgres"]
