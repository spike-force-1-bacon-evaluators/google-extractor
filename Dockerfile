FROM agapito/centos7-scala:latest

COPY . /twitter-lm-extractor/

WORKDIR /twitter-lm-extractor

ENTRYPOINT ["sbt", "-Dsbt.log.noformat=true", "clean", "scalastyle", "test:scalastyle", "test"]
