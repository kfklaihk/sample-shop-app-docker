# Stage 1: Build the React frontend
FROM node:18-alpine AS frontend-build
WORKDIR /react-build
COPY react-app/package.json react-app/package-lock.json* ./
RUN npm install
COPY react-app/ ./
RUN npm run build

# Stage 2: Build the Java backend
FROM maven:3.8.8-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

# Stage 3: Final runtime image
FROM eclipse-temurin:17-jdk-alpine 
RUN apk update && apk add --no-cache curl
RUN  adduser -Dh /home/gordon gordon 
WORKDIR /static
COPY --from=frontend-build /react-build/build/ .
WORKDIR /app
COPY --from=backend-build /app/target/AtSea-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "/app/AtSea-0.0.1-SNAPSHOT.jar"]
CMD ["--spring.profiles.active=postgres"]
