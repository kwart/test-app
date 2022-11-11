FROM hazelcast/hazelcast:5.2.0-slim

USER root

RUN apk update && apk add socat

COPY *.sh /
RUN chmod +x /*.sh

COPY hazelcast.xml /opt/hazelcast/config/hazelcast-docker.xml

CMD ["/run.sh"]
