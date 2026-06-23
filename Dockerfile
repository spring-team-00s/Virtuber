FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENV JAVA_OPTS="-Xmx512m"
EXPOSE 8000
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]