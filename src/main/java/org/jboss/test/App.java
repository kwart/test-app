package org.jboss.test;

import org.wildfly.security.auth.client.AuthenticationConfiguration;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.MatchRule;

/**
 * Hello world!
 *
 * @author Josef Cacek
 */
public class App {

	public static void main(String[] args) {
		AuthenticationContext.empty()
				.with(MatchRule.ALL, AuthenticationConfiguration.EMPTY.useName("admin").usePassword("admin"))
				.run(() -> {
					System.out.println("Hello");
				});
	}

}
