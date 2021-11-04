package com.hazelcast.maven.attribution;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class AttributionMojo extends AbstractMojo {

    private static final Pattern PATTERN_COPYRIGHT = Pattern.compile("(?i)^([\\s/*]*)(((\\(c\\))|(copyright))\\s+\\S[^;{}]*$)");
    private static final int PATTERN_COPYRIGHT_GRPIDX = 2;

    public void execute() throws MojoExecutionException, MojoFailureException {
        Path sourcesJarDir = Paths.get("target/dependency");
        Log logger = getLog();
        if (!Files.isDirectory(sourcesJarDir)) {
            throw new MojoExecutionException("Configured sources JAR directory doesn't exist: " + sourcesJarDir);
        }
        logger.info("Sources JAR directory: " + sourcesJarDir);
        List<Path> jars;
        try {
            jars = Files.list(sourcesJarDir).filter(p -> Files.isRegularFile(p) && p.toString().endsWith("-sources.jar"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read list of sources jars", e);
        }
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService jarReaderService = Executors.newFixedThreadPool(threads);
        final AttributionContext context = new AttributionContext();
        for (Path jar : jars) {
            jarReaderService.submit(() -> readJar(jar, context));
        }
        ExecutorService consumerExecutorService = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            consumerExecutorService.submit(() -> consumeSrc(context));
        }

        consumerExecutorService.shutdown();
        jarReaderService.shutdown();
        try {
            jarReaderService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            getLog().error(e);
            throw new MojoFailureException("JAR files processing has timed out", e);
        }
        context.producersRunning.set(false);
        try {
            consumerExecutorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            getLog().error(e);
            throw new MojoFailureException("Source files processing has timed out", e);
        }
    }

    private void readJar(Path jar, AttributionContext context) {
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(Files.newInputStream(jar)))) {
            ZipEntry zipEntry;
            while (null != (zipEntry = zip.getNextEntry())) {
                String srcName = zipEntry.getName();
                if (!zipEntry.isDirectory() && srcName.toLowerCase(Locale.ROOT).endsWith(".java")) {
                    try {
                        context.srcQueue.put(new SrcFile(jar.toString(), srcName, toByteArray(zip)));
                    } catch (InterruptedException e) {
                        getLog().warn("Putting source file to queue was interrupted", e);
                    } catch (IOException e) {
                        getLog().warn("Reading source file failed", e);
                    }
                }
                zip.closeEntry();
            }
        } catch (IOException e) {
            getLog().error("Reading archive failed: " + jar, e);
        }
    }

    private static byte[] toByteArray(InputStream in) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        }
    }

    private void consumeSrc(AttributionContext context) {
        while (context.producersRunning.get() || !context.srcQueue.isEmpty()) {
            try {
                SrcFile srcFile = context.srcQueue.poll(1, TimeUnit.SECONDS);
                getLog().debug("Processing " + srcFile.getSourceName() + " from " + srcFile.getArchiveName());
                Set<String> hitSet = context.foundAttribution.computeIfAbsent(srcFile.getArchiveName(),
                        s -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new ByteArrayInputStream(srcFile.getBytes()), StandardCharsets.UTF_8))) {
                    String line;
                    while (null != (line = reader.readLine())) {
                        Matcher m = PATTERN_COPYRIGHT.matcher(line);
                        if (m.find()) {
                            String copyrightStr = m.group(PATTERN_COPYRIGHT_GRPIDX);
                            if (hitSet.add(copyrightStr)) {
                                getLog().info("Found: " + copyrightStr);
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                getLog().debug(e);
            } catch (IOException e) {
                getLog().error(e);
            }
        }
    }
}
