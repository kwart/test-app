package cz.cacek.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Simple HTTPs client
 */
public class App {

    public static void main(String[] args) throws Exception {
        URL url = new URL("https://github.com/");
        URLConnection con = url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
        String line;
        while (null != (line=br.readLine())) {
            System.out.println(line);
        }
    }
}
