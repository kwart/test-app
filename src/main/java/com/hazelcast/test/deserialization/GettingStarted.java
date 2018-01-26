package com.hazelcast.test.deserialization;

import com.hazelcast.config.Config;
import com.hazelcast.config.InterfacesConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.SymmetricEncryptionConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class GettingStarted extends AbstractEnterpriseParent {

  public static void main(String[] args) {
    Config cfg = new Config();
    NetworkConfig networkConfig = cfg.getNetworkConfig();
    SymmetricEncryptionConfig symmetricEncryptionConfig= new SymmetricEncryptionConfig();
    symmetricEncryptionConfig.setEnabled(true);
    networkConfig.setSymmetricEncryptionConfig(symmetricEncryptionConfig);
    InterfacesConfig interfaces = networkConfig.getInterfaces();
    interfaces.setEnabled(true);
    interfaces.addInterface("192.168.1.105");
    HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
  }
}
