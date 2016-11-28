package org.jboss.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;

import org.junit.Test;

/**
 * Simple reproducer for issue described in https://issues.jboss.org/browse/JBEAP-7523
 * @author Josef Cacek
 */
public class KeyManagerTest {

	static volatile Exception exception;
	static volatile long endtime;

	@Test
	public void testIfIbmJdk() throws InterruptedException {
		assertTrue(System.getProperty("java.vendor").startsWith("IBM"));
	}

	@Test
	public void test() throws InterruptedException {
		TestThread ksThread1 = new TestThread("keystore1");
		TestThread ksThread2 = new TestThread("keystore2");

		// 30s should be enough to reproduce the issue
		endtime = System.currentTimeMillis() + 30L * 1000L;

		ksThread1.start();
		ksThread2.start();

		ksThread1.join();
		ksThread2.join();

		if (exception != null) {
			fail(exception.getMessage());
		}
	}

	private static class TestThread extends Thread {

		private final String ksName;
		private final KeyStore keyStore;

		TestThread(String ksName) {
			this.ksName = ksName;
			// load the test keystore
			try (FileInputStream fis = new FileInputStream("target/" + ksName)) {
				keyStore = KeyStore.getInstance("jks");
				keyStore.load(fis, ksName.toCharArray());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void run() {
			try {
				while (exception == null && System.currentTimeMillis() < endtime) {
					KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
					kmf.init(keyStore, ksName.toCharArray());
					kmf.getKeyManagers();
				}
			} catch (Exception e) {
				e.printStackTrace();
				exception = e;
			}
		}

	}

}
