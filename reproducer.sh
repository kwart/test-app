#!/bin/bash

set -x

# cleanup
docker rm -f member-proxy member0 member1 member2
docker network rm toxiproxy-254

docker network create --subnet 172.18.5.0/24 toxiproxy-254

docker run -d --rm --network toxiproxy-254 --network-alias member-proxy --ip 172.18.5.2 ghcr.io/shopify/toxiproxy:2.4.

curl --data '{"name": "member0", "upstream": "member0:5701", "listen": "0.0.0.0:6000"}' http://172.18.5.2:8474/proxies
curl --data '{"name": "member1", "upstream": "member1:5701", "listen": "0.0.0.0:6001"}' http://172.18.5.2:8474/proxies
curl --data '{"name": "member2", "upstream": "member2:5701", "listen": "0.0.0.0:6002"}' http://172.18.5.2:8474/proxies

for i in {0..2}; do
  docker run -d --rm --network toxiproxy-254 --network-alias member$i --ip 172.18.5.1$i \
    --volume "`pwd`/hazelcast-config.xml:/opt/hazelcast/config/hazelcast-docker.xml" --env HZ_PHONE_HOME_ENABLED=false \
    --env JAVA_OPTS="-DproxyPort=600$i" hazelcast/hazelcast:5.1.2-slim
done

sleep 30

# TODO the rest

# For each proxy:
#toxics().bandwidth(CUT_CONNECTION_DOWNSTREAM, ToxicDirection.DOWNSTREAM, 0);
#toxics().bandwidth(CUT_CONNECTION_UPSTREAM, ToxicDirection.UPSTREAM, 0);

sleep 60

# For each proxy
#toxics().get(CUT_CONNECTION_DOWNSTREAM).remove();
#toxics().get(CUT_CONNECTION_UPSTREAM).remove();
