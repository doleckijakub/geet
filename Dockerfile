FROM node:20-alpine AS frontend-builder

WORKDIR /frontend

COPY frontend/ ./

RUN npm install -g @angular/cli

RUN npm install

RUN ng build --configuration production

FROM eclipse-temurin:21-jdk-alpine AS backend-builder

WORKDIR /geet

COPY gradlew settings.gradle.kts ./
COPY gradle/ ./gradle/

RUN ./gradlew --no-daemon --parallel --refresh-dependencies dependencies

COPY app/ ./app

COPY --from=frontend-builder /frontend/dist/frontend/browser/ ./app/src/main/resources/static/

RUN ./gradlew --no-daemon --parallel --max-workers=$(nproc) build
RUN cp app/build/libs/app-0.1.0.jar geet.jar

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /geet

RUN apk update
RUN apk add --no-cache git
RUN rm -rf /var/cache/apk/*

COPY --from=backend-builder /geet/geet.jar ./geet.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "geet.jar"]
