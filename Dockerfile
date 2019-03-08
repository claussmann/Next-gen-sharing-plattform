FROM openjdk:8-jdk-alpine as build

RUN apk add gradle

WORKDIR /app

COPY . .

RUN gradle bootjar



FROM openjdk:8-jdk-alpine

WORKDIR /app

RUN apk add bash

COPY ./src/main/resources/application-prod.properties .
COPY ./wait-for-it.sh .
RUN chmod +x wait-for-it.sh

COPY --from=build /app/build/libs/sharingplatform-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

ENTRYPOINT ./wait-for-it.sh -t 30 h2DB:1521 -- java -jar sharingplatform-0.0.1-SNAPSHOT.jar --spring.config.location=file:/app/application-prod.properties
