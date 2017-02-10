FROM agapito/centos7-scala:latest

COPY . /google-extractor/

WORKDIR /google-extractor

ENTRYPOINT ["sbt", "-Dsbt.log.noformat=true", "clean", "scalastyle", "test:scalastyle", "test"]
