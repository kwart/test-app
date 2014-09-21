package org.jboss.shared;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Josef Cacek
 */
public class Utils {

	public static List<String> getUsersList(boolean useDoPrivileged)
			throws IOException, PrivilegedActionException {
		if (useDoPrivileged)
			return AccessController
					.doPrivileged(new PrivilegedExceptionAction<List<String>>() {
						@Override
						public List<String> run() throws IOException {
							return getUserListInternal();
						}
					});
		else
			return getUserListInternal();

	}

	private static List<String> getUserListInternal() throws IOException {
		List<String> users = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/etc/passwd"));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				int separatorPos = line.indexOf(':');
				if (separatorPos > 0) {
					users.add(line.substring(0, separatorPos));
				}
			}
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return users;
	}

}
