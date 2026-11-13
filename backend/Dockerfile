# Hotel Booking backend — production image.
# Build context: backend/ (see ../render.yaml and ../docker-compose.prod.yml).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Free-tier friendly defaults (Render/Railway Free = 512 MB); override via JAVA_OPTS env.
ENV JAVA_OPTS="-Xmx380m -XX:+UseSerialGC"
EXPOSE 8082
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
