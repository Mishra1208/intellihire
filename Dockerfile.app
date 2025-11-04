# ---- Build stage ----
FROM gradle:8.7-jdk17 AS build
WORKDIR /workspace

# (optional) speed up dependency download by copying wrapper files first
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
RUN ./gradlew --version

# now copy the rest of the project and build
COPY . .
RUN ./gradlew clean build -x test --no-daemon

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# copy the built jar(s)
COPY --from=build /workspace/build/libs/*.jar /app/

# Render injects PORT; Spring will read it via application.yml (${PORT:8080})
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseZGC"
EXPOSE 8080

# run the first jar we find (works for SNAPSHOT or release jar names)
CMD sh -c "java $JAVA_OPTS -jar $(ls /app/*.jar | head -n 1)"
