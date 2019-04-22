#
# Copyright (c) 2019 LG Electronics Inc.
# SPDX-License-Identifier: Apache-2.0
#

FROM openjdk:latest as builder

RUN \
  curl -L -o sbt-1.2.3.deb http://dl.bintray.com/sbt/debian/sbt-1.2.3.deb && \
  dpkg -i sbt-1.2.3.deb && \
  rm sbt-1.2.3.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion


WORKDIR /facade

ADD . /facade

RUN sbt assembly

FROM gcr.io/distroless/java

WORKDIR /facade

COPY --from=builder /facade/target/scala-2.12/facade-assembly-1.0-SNAPSHOT.jar ./facade.jar


ENTRYPOINT ["java","-Dplay.http.secret.key=abcdefghijk","-jar","facade.jar"]  
