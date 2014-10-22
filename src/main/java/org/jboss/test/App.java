package org.jboss.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Hello world!
 * 
 * @author Josef Cacek
 */
public class App {

  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Usage:");
      System.out.println("\tjava -jar host-test.jar <IP|hostname> ...");
      System.out.println();
      System.exit(1);
    }
    for (String arg : args) {
      System.out.println("[ " + arg + " ]");
      try {
        InetAddress[] addrs = InetAddress.getAllByName(arg);
        for (InetAddress addr : addrs) {
          System.out.println("getHostAddress():       " + addr.getHostAddress());
          System.out.println("getHostName():          " + addr.getHostName());
          System.out.println("getCanonicalHostName(): " + addr.getCanonicalHostName());
          System.out.println();
        }
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }
    }
  }

}
