package cz.cacek.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hazelcast.core.MapStore;

public class FooMapStore implements MapStore<String, Integer>{

    @Override
    public Integer load(String key) {
        return key.length();
    }

    @Override
    public Map<String, Integer> loadAll(Collection<String> keys) {
        Map<String, Integer> map = new HashMap<>();
        for (String key: keys) {
            map.put(key, key.length());
        }
        return map;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        return Arrays.asList(new String[]{"Test", "tvrdych", "kokosovych", "orechu", "."});
    }

    @Override
    public void delete(String arg0) {
    }

    @Override
    public void deleteAll(Collection<String> arg0) {
    }

    @Override
    public void store(String arg0, Integer arg1) {
    }

    @Override
    public void storeAll(Map<String, Integer> arg0) {
    }

}
