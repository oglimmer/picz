FROM eclipse-temurin:21.0.8_9-jdk AS builder

#RUN apt install findutils # needed for gradle xargs

WORKDIR /app/backend

# layer 1 - cache all dependencies, update only if build.gradle or gradle-wrapper.properties changes
COPY backend/gradle/wrapper/gradle-wrapper.properties backend/gradle/wrapper/gradle-wrapper.jar ./gradle/wrapper/
COPY backend/gradlew backend/build.gradle backend/settings.gradle ./

# Pre-download dependencies and plugins, cache Gradle directory
RUN --mount=type=cache,target=/root/.m2 --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon build --dry-run

# layer 2 - build if any file changed
COPY backend/src ./src

# we need to copy the .git directory to get the commit hash
WORKDIR /app
COPY .git/ .git/

WORKDIR /app/backend
RUN --mount=type=cache,target=/root/.m2 --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon build

FROM eclipse-temurin:21.0.8_9-jre-noble

RUN apt-get update && apt-get install -y curl imagemagick && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

ARG APP_USER=appuser
ARG APP_UID=10001
ARG APP_GID=10001

RUN groupadd  -g "${APP_GID}" "${APP_USER}" \
     && useradd   -m -u "${APP_UID}" -g "${APP_GID}" "${APP_USER}"

COPY --chown=${APP_USER}:${APP_USER} --from=builder /app/backend/build/libs/picz-0.0.1-SNAPSHOT.jar picz.jar

EXPOSE 8080

USER ${APP_USER}

CMD ["java", "-jar", "picz.jar"]
