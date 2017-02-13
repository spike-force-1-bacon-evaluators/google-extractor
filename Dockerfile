FROM agapito/centos7-scala:latest

COPY . /google-extractor/

WORKDIR /google-extractor

ENTRYPOINT ["sbt", "-Djava.util.logging.config.file=./src/main/resources/logging.properties", "-Dsbt.log.noformat=true", "clean", "scalastyle", "test:scalastyle", "test"]
