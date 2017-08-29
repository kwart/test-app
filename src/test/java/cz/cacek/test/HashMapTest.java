package cz.cacek.test;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests {@link HashMap} thread (un)safety.
 */
public class HashMapTest extends AbstractMapTestBase {

	/* (non-Javadoc)
	 * @see cz.cacek.test.AbstractMapTestBase#createMapInstance()
	 */
	@Override
	protected <K, V> Map<K, V> createMapInstance() {
		return new HashMap<>();
	}
}
