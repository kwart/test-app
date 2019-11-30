package b_jcache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

public class AppJCache {

    public static void main(String[] args) {
        // Retrieve the CachingProvider which is automatically backed by the implementation

        CachingProvider cachingProvider = Caching.getCachingProvider();
        // CachingProvider cachingProvider = Caching.getCachingProvider("com.hazelcast.cache.HazelcastCachingProvider");
        // CachingProvider cachingProvider = Caching.getCachingProvider("org.infinispan.jcache.embedded.JCachingProvider");
        // CachingProvider cachingProvider = Caching.getCachingProvider("org.apache.ignite.cache.CachingProvider");

        // Create a CacheManager.
        CacheManager cacheManager = cachingProvider.getCacheManager();

        // Request an existing cache:
        Cache<String, Integer> cache = cacheManager.getCache("cityInhabitants", String.class, Integer.class);

        if (cache == null) {
            System.out.println("Cache doesn't exist yet");

            // Let's create a typesafe configuration for the cache.
            CompleteConfiguration<String, Integer> config = new MutableConfiguration<String, Integer>().setTypes(String.class,
                    Integer.class);

            // Create and get the cache.
            cache = cacheManager.createCache("cityInhabitants", config);

            cache.put("Istanbul", 15_067_724);
            cache.put("London", 9_126_366);
            cache.put("Prague", 1_308_632);
            // ...
        }
        System.out.println("London population: " + cache.get("London"));
        cachingProvider.close();
    }
}
