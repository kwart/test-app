package a_map;

import java.io.IOException;
import java.util.Map;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

public class AppInfinispan {

    public static void main(String[] args) throws IOException {

        GlobalConfiguration managerCfg = GlobalConfigurationBuilder.defaultClusteredBuilder().build();
        try (DefaultCacheManager manager = new DefaultCacheManager(managerCfg)) {
            Configuration configuration = new ConfigurationBuilder().clustering().cacheMode(CacheMode.DIST_SYNC).build();
            manager.defineConfiguration("cityInhabitants", configuration);

            Map<String, Integer> cityInhabitants = manager.getCache("cityInhabitants");

            cityInhabitants.put("Istanbul", 15_067_724);
            cityInhabitants.put("London", 9_126_366);
            cityInhabitants.put("Prague", 1_308_632);

            //...

            System.out.println("London population: " + cityInhabitants.get("London"));
        }
    }
}
