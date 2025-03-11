package cz.cacek.test;

import org.eclipse.sisu.launch.Main;
import org.eclipse.sisu.space.BeanScanning;

import com.google.inject.Guice;

/**
 * The App!
 */
public class App {

    public static void main(String args[]) throws Exception {
        // https://stackoverflow.com/questions/5776519/how-to-parse-unzip-unpack-maven-repository-indexes-generated-by-nexus
        final com.google.inject.Module app = Main.wire(BeanScanning.INDEX);
        Guice.createInjector(app).getInstance(LastModifiedIdx.class).perform();
//        Guice.createInjector(app).getInstance(BasicUsageExample.class).perform();
    }
}
