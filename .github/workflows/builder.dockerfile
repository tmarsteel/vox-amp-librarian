FROM gradle:7.4.2-jdk17-alpine

RUN apk update && \
    apk add nodejs npm