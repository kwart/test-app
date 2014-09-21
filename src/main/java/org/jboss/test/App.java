package org.jboss.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.shared.Utils;

/**
 * @author Josef Cacek
 */
public class App {

	public static void main(String[] args) throws Exception {
		final Set<String> argSet = new HashSet<String>(Arrays.asList(args));
		final boolean useDoPrivileged = argSet.contains("privileged");
		final boolean useNewThread = argSet.contains("threaded");

		List<String> users = null;
		if (useNewThread) {
			final ExecutorService executor = Executors
					.newSingleThreadExecutor();
			Future<List<String>> future = executor
					.submit(new Callable<List<String>>() {

						@Override
						public List<String> call() throws Exception {
							return Utils.getUsersList(useDoPrivileged);
						}

					});
			users = future.get();
		} else {
			users = Utils.getUsersList(useDoPrivileged);
		}

		System.out.println("System users: " + users);
	}

}
