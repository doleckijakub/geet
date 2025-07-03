FROM eclipse-temurin:21-jdk-alpine as build

WORKDIR /geet

COPY gradlew settings.gradle.kts .
COPY gradle/ ./gradle

RUN ./gradlew build --no-daemon --dry-run

COPY app/ ./app

RUN ./gradlew build --no-daemon

RUN cp app/build/libs/app-0.1.0.jar geet.jar

FROM eclipse-temurin:21-jre-alpine as runtime

WORKDIR /geet

RUN apk update
RUN apk add --no-cache git

COPY --from=build /geet/geet.jar ./geet.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "geet.jar"]
