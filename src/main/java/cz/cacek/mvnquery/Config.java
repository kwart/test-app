package cz.cacek.mvnquery;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public static class Config {
        @Parameter(names = "--groupId", description = "Filter by groupId")
        private String groupId;

        @Parameter(names = "--artifactId", description = "Filter by artifactId")
        private String artifactId;

        @Parameter(names = "--packaging", description = "Filter by packaging type", defaultValue = "jar")
        private String packaging;

        @Parameter(names = "--classifier", description = "Filter by classifier", defaultValue = "-")
        private String classifier;

        @Parameter(names = "--lastDays", description = "Filter artifacts modified in last X days", defaultValue = "14")
        private int lastDays;

        @Parameter(names = "--dataDir", description = "Set data directory for index", defaultValue = "${user.home}/.mvnindex")
        private String dataDir;

        @Parameter(names = "--repo", description = "Set repository URL", defaultValue = "https://repo1.maven.org/maven2")
        private String repo;
    }