package org.jboss.test;

import org.jboss.logmanager.Logger;

/**
 * Sample usage of JBoss LogManager and JBoss Logging
 * 
 * @author Josef Cacek
 */
public class App {

	static {
		System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
	}

	private static final org.jboss.logging.Logger LOGGER = org.jboss.logging.Logger.getLogger(App.class);
	private static final Logger LOGGER2 = Logger.getLogger(App.class.getName());

	public static void main(String[] args) {
		LOGGER.debug("Test debug");
		LOGGER.info("Test info");
		LOGGER.warn("Test warn");

		LOGGER2.fine("test fine");
		LOGGER2.info("test info");
		LOGGER2.warning("test warning");
	}
}
