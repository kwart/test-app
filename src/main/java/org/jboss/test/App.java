package org.jboss.test;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Hello world!
 *
 * @author Josef Cacek
 */
public class App {

    public static void main(String[] args) throws IOException {
    	System.out.println("Hello world");
    	new IOException("I wanna fail");
    	System.out.println("Dead code here?");
    }

}
