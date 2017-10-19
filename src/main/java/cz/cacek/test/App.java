package cz.cacek.test;

/**
 * Hello world!
 *
 * @author Josef Cacek
 */
public class App {

    public static void main(String[] args) {
        System.out.println(sayHello());
        new Exception("ahoj");
    }

    protected static String sayHello() {
    	return "Hello World!";
    }
}
