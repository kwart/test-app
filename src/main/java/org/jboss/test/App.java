package org.jboss.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jboss.as.test.integration.logging.syslogserve.TCPSyslogServerConfig;
import org.jboss.as.test.integration.logging.syslogserve.TLSSyslogServerConfig;
import org.jboss.as.test.integration.logging.syslogserve.UDPSyslogServerConfig;
import org.productivity.java.syslog4j.SyslogRuntimeException;
import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerConfigIF;

/**
 * Hello world!
 * 
 * @author Josef Cacek
 */
public class App {

  public static final int SYSLOG_PORT = 9898;

  public static void main(String[] args) throws SyslogRuntimeException, UnknownHostException {

    // clear created server instances (TCP/UDP)
    SyslogServer.shutdown();

    String syslogProtocol = "tls";
    if (args.length > 0) {
      syslogProtocol = args[0];
    }

    SyslogServerConfigIF config = getSyslogConfig(syslogProtocol);
    if (config == null) {
      System.err.println("Unsupported Syslog protocol: " + syslogProtocol);
      System.exit(1);
    }
    config.setUseStructuredData(true);
    config.setHost(InetAddress.getByName(null).getHostAddress());
    config.setPort(SYSLOG_PORT);

    System.out.println("Starting Syslog server");
    System.out.println("Protocol: " + syslogProtocol);
    System.out.println("Host:     " + config.getHost());
    System.out.println("Port:     " + config.getPort());

    SyslogServer.createThreadedInstance(syslogProtocol, config);
    // start syslog server

  }

  private static SyslogServerConfigIF getSyslogConfig(String syslogProtocol) {
    SyslogServerConfigIF config = null;
    if ("udp".equals(syslogProtocol)) {
      config = new UDPSyslogServerConfig();
    } else if ("tcp".equals(syslogProtocol)) {
      config = new TCPSyslogServerConfig();
    } else if ("tls".equals(syslogProtocol)) {
      config = new TLSSyslogServerConfig();
    }
    return config;
  }

}
