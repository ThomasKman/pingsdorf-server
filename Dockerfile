# --- build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache dependencies first
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

# --- runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Non-root user
RUN groupadd -r app && useradd -r -g app app && mkdir -p /app/data/uploads && chown -R app:app /app
USER app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080
ENV PINGSDORF_STORAGE_DIR=/app/data/uploads

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
