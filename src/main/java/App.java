import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The App!
 */
public class App {

    public static void main(String[] args) throws Exception {
        String url = "https://mkyong.com/";
        if (args!=null && args.length>0) {
            url = args[0];
        }
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        try (InputStream is = conn.getInputStream(); BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            System.out.println("HTTP headers:\n" + conn.getHeaderFields());
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
