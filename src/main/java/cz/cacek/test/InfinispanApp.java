package cz.cacek.test;

import java.io.IOException;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

/**
 * Hazelcast Hello world!
 */
public class InfinispanApp {

    public static void main(String[] args) throws IOException {
        DefaultCacheManager manager1 = new DefaultCacheManager(
                GlobalConfigurationBuilder.defaultClusteredBuilder()
//              .clusteredDefault()
//              .transport()
//              .nodeName("" + System.currentTimeMillis())
//              .addProperty("configurationFile", "jgroups.xml")
              .build());
        DefaultCacheManager manager2 = new DefaultCacheManager(
                GlobalConfigurationBuilder.defaultClusteredBuilder()
//              .clusteredDefault()
//              .transport()
//              .nodeName("" + System.currentTimeMillis())
//              .addProperty("configurationFile", "jgroups.xml")
              .build());

        Configuration configuration = new ConfigurationBuilder().clustering().cacheMode(CacheMode.DIST_SYNC)
                .build();
        manager1.defineConfiguration("test", configuration);
        manager2.defineConfiguration("test", configuration);
        Cache<String, String> cache = manager1.getCache("test");
        cache.put("key", "value");
        System.out.println(manager2.getCache("test").get("key"));
        manager1.close();
        manager2.close();
    }
}
