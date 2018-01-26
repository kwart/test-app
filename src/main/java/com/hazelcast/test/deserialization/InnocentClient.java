package com.hazelcast.test.deserialization;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class InnocentClient extends AbstractEnterpriseParent {

    public static void main(String[] args) throws Exception {
        ClientConfig cfg = new ClientConfig();
        cfg.getNetworkConfig().addAddress("127.0.0.1:5701");
        HazelcastInstance client = HazelcastClient.newHazelcastClient(cfg);
        try {
            IMap<Integer, String> map = client.getMap("customers");
            System.out.println(map.get(1));
        } finally {
            client.shutdown();
        }
    }
}
