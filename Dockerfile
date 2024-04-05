FROM openjdk:17-jdk-slim

ARG WORKDIR=/app
WORKDIR $WORKDIR

RUN groupadd -r app && useradd -r -s /bin/false -g app app
RUN chown -R app:app $WORKDIR

COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

USER app

ARG JAR_FILE=target/wordcount-*-SNAPSHOT.jar
COPY $JAR_FILE $WORKDIR/app.jar

ENTRYPOINT ["/docker-entrypoint.sh"]