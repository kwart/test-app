package cz.cacek.mvnquery;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

public class Config {

    @Parameter(names = { "--help", "-h" }, help = true, description = "Prints this help")
    private boolean printHelp;

    @Parameter(names = { "--quiet", "-q" }, description = "Don't print progress")
    private boolean quiet;

    @Parameter(names = { "--groupId", "-g" }, description = "Filter by groupId")
    private String groupId;

    @Parameter(names = { "--artifactId", "-a" }, description = "Filter by artifactId")
    private String artifactId;

    @Parameter(names = { "--packaging", "-p" }, description = "Filter by packaging type")
    private String packaging = "jar";

    @Parameter(names = { "--classifier", "-c" }, description = "Filter by classifier")
    private String classifier = "-";

    @Parameter(names = { "--lastDays", "-d" }, description = "Filter artifacts modified in last X days")
    private int lastDays = 14;

    @Parameter(names = { "--config-data-dir" }, converter = FileConverter.class, description = "Set data directory for index")
    private File configDataDir = new File(System.getProperty("user.home"), ".mvnindex");

    @Parameter(names = { "--config-repo" }, description = "Set repository URL")
    private String configRepo = "https://repo1.maven.org/maven2";

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getPackaging() {
        return packaging;
    }

    public String getClassifier() {
        return classifier;
    }

    public int getLastDays() {
        return lastDays;
    }

    public File getConfigDataDir() {
        return configDataDir;
    }

    public String getConfigRepo() {
        return configRepo;
    }

    public boolean isPrintHelp() {
        return printHelp;
    }

    public boolean isQuiet() {
        return quiet;
    }
}
