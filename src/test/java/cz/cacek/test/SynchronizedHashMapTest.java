package cz.cacek.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests synchronized view on {@link HashMap} for the thread (un)safety.
 *
 * #see {@link Collections#synchronizedMap(Map)}
 */
public class SynchronizedHashMapTest extends AbstractMapTestBase {

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.cacek.test.AbstractMapTestBase#createMapInstance()
	 */
	@Override
	protected <K, V> Map<K, V> createMapInstance() {
		return Collections.synchronizedMap(new HashMap<>());
	}
}
