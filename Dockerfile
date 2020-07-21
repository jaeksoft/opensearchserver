FROM adoptopenjdk:11-jre-hotspot

MAINTAINER OpenSearchServer

ADD target/opensearchserver-*-app.jar /opt/opensearchserver/opensearchserver.jar
ADD parsers/*-shaded.jar /opt/opensearchserver/parsers/
ADD src/main/docker/config.properties /opt/opensearchserver/config.properties

VOLUME /var/lib/opensearchserver

EXPOSE 9090/tcp

WORKDIR /var/lib/opensearchserver/

CMD ["java", "-Dfile.encoding=UTF-8", "-Dcom.opensearchserver.config=/opt/opensearchserver/config.properties", "-jar", "/opt/opensearchserver/opensearchserver.jar"]
