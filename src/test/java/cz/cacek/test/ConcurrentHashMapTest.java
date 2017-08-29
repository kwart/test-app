package cz.cacek.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tests synchronized view on {@link HashMap} for the thread (un)safety.
 *
 * #see {@link Collections#synchronizedMap(Map)}
 */
public class ConcurrentHashMapTest extends AbstractMapTestBase {

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.cacek.test.AbstractMapTestBase#createMapInstance()
	 */
	@Override
	protected <K, V> Map<K, V> createMapInstance() {
		return new ConcurrentHashMap<>();
	}
}
