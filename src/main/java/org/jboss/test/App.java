package org.jboss.test;

import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.shared.Utils;

/**
 * @author Josef Cacek
 */
public class App implements Runnable {

  private final boolean useDoPrivileged;

  private App(boolean useDoPrivileged) {
    this.useDoPrivileged = useDoPrivileged;
  }

  public static void main(String[] args) throws Exception {
    final Set<String> argSet = new HashSet<String>(Arrays.asList(args));
    final App app = new App(argSet.contains("privileged"));
    if (argSet.contains("threaded")) {
      new Thread(app).start();
    } else {
      app.run();
    }
  }

  @Override
  public void run() {
    try {
      System.out.println("System users: " + Utils.getUsersList(useDoPrivileged));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (PrivilegedActionException e) {
      e.printStackTrace();
    }
  }

}
