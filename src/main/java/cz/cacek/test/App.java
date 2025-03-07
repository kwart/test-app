package cz.cacek.test;

import org.eclipse.sisu.launch.Main;
import org.eclipse.sisu.space.BeanScanning;

import com.google.inject.Guice;

/**
 * The App!
 */
public class App {

    public static void main(String args[]) throws Exception {
        final com.google.inject.Module app = Main.wire(BeanScanning.INDEX);
        Guice.createInjector(app).getInstance(BasicUsageExample.class).perform();
    }
}
