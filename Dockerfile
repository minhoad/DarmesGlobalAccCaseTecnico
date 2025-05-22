FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

COPY src ./src

RUN ./mvnw clean package -DskipTests

ENTRYPOINT ["java","-jar","target/app-3.4.5.jar"]