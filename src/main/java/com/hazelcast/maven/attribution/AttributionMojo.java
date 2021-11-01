package com.hazelcast.maven.attribution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "attribution", threadSafe = true)
public class AttributionMojo extends AbstractMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        Path sourcesJarDir = Paths.get("target/dependency");
        Log logger = getLog();
        if (!Files.isDirectory(sourcesJarDir)) {
            throw new MojoExecutionException("Configured sources JAR directory doesn't exist: " + sourcesJarDir);
        }
        logger.info("Sources JAR directory: " + sourcesJarDir);
        List<Path> jars;
        try {
            jars = Files.list(sourcesJarDir).filter(p->Files.isRegularFile(p) && p.toString().endsWith("-sources.jar")).collect(Collectors.toList());
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read list of sources jars", e);
        }
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService jarReaderService = Executors.newFixedThreadPool(threads);
        BlockingQueue<byte[]> javaSrcQueue = new LinkedBlockingQueue<>(1024);
        for (Path jar: jars) {
            jarReaderService.submit(createJarReadTask(jar, javaSrcQueue));
        }
        ExecutorService javaSrcReaderService = Executors.newFixedThreadPool(threads);
        Executors.newWorkStealingPool()
    }
}
