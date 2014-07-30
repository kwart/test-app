/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

/**
 * Custom {@link SocketFactory} implementation, which only prints debug information to {@link System#out}.
 * 
 * @author Josef Cacek
 */
public class CustomSocketFactory extends SocketFactory {

  public CustomSocketFactory() {
    debug();
  }

  public static SocketFactory getDefault() {
    debug();
    return new CustomSocketFactory();
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    debug();
    return new Socket(host, port);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException,
      UnknownHostException {
    debug();
    return new Socket(host, port, localHost, localPort);
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    debug();
    return new Socket(host, port);
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
    debug();
    return new Socket(address, port, localAddress, localPort);
  }

  private static void debug() {
    StackTraceElement st = new Exception().getStackTrace()[1];
    System.out.println(">>> " + CustomSocketFactory.class.getName() + "." + st.getMethodName() + " : " + st.getLineNumber());
  }

  public static void main(String[] args) throws UnknownHostException, IOException {
    getDefault().createSocket("localhost", 8080).close();
  }
}