package org.jboss.test;

import java.io.File;
import java.io.PrintWriter;

import org.jboss.as.cli.CommandContextFactory;

/**
 * Reproducer for JBoss CLI memory leak.
 *
 * @author Josef Cacek
 */
public class App {

  public static void main(String[] args) throws Exception {
    // workaround for WARN messages on System.err
    final File tmpJBossCliFile = File.createTempFile("jboss-cli", ".xml");
    try (PrintWriter pw = new PrintWriter(tmpJBossCliFile)) {
      pw.print("<jboss-cli xmlns='urn:jboss:cli:1.0'></jboss-cli>");
    }
    System.setProperty("jboss.cli.config", tmpJBossCliFile.getAbsolutePath());

    long counter = 0L;
    long nextPrint = 1L;
    while (true) {
      CommandContextFactory.getInstance().newCommandContext().terminateSession();
      counter++;
      if (counter >= nextPrint) {
        System.out.println("CommandContext clients created: " + counter);
        nextPrint = nextPrint * 2L;
      }
    }
  }
}
