package cz.cacek.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cz.cacek.test.App;

/**
 * A AppTest.
 *
 * @author Josef Cacek
 */
public class AppTest {

	@Test
	public void test() {
		assertEquals("Unexpected greeting", "Hello World!", App.sayHello());
	}

	@Test
	public void testNullToMain() {
		App.main(null);
	}

}
