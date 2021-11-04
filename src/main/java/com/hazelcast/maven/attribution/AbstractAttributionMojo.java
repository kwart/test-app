package com.hazelcast.maven.attribution;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;

public abstract class AbstractAttributionMojo extends AbstractMojo {

    /**
     * The classifier for sources.
     */
    public static final String SOURCES_CLASSIFIER = "sources";

    @Component
    private ResourceResolver resourceResolver;

    /**
     * The current build session instance. This is used for toolchain manager API calls.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    /**
     * The Maven Settings.
     */
    @Parameter(defaultValue = "${settings}", readonly = true, required = true)
    private Settings settings;

    /**
     * The Maven Project Object
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojo;

    /**
     * The projects in the reactor for aggregation report.
     */
    @Parameter(property = "reactorProjects", readonly = true)
    private List<MavenProject> reactorProjects;

    @Parameter(property = "attribution.skip", defaultValue = "false")
    protected boolean skip;

    /**
     * Specifies the destination file.
     */
    @Parameter(property = "attribution.outputFile", defaultValue = "${project.build.directory}/attribution.txt", required = true)
    protected File outputFile;

    public void execute() throws MojoExecutionException, MojoFailureException {
        for (MavenProject subProject : getAggregatedProjects()) {
            getLog().info("MavenProject " + subProject.getGroupId() + ":" + subProject.getArtifactId());
        }

        final Collection<ArtifactModule> sourceJars = new ArrayList<>();

        Set<Artifact> artifacts = new HashSet<>();
        final Map<String, MavenProject> projectMap = new HashMap<>();
        if (reactorProjects != null) {
            for (final MavenProject p : reactorProjects) {
                projectMap.put(key(p.getGroupId(), p.getArtifactId()), p);
                Set<Artifact> projArtifacts = p.getArtifacts();
                getLog().info("Project artifacts: " + projArtifacts);
                artifacts.addAll(projArtifacts);
            }
        } else {
            getLog().info("Null reactorProjects");
        }

        getLog().info("Artifacts size: " + artifacts.size());

        for (final Artifact artifact : artifacts) {
            final String key = key(artifact.getGroupId(), artifact.getArtifactId());
            // exclude reactor projects
            if (projectMap.get(key) == null) {
                Path sourcePath = resolve(createResourceArtifact(artifact, SOURCES_CLASSIFIER));
                if (sourcePath == null) {
                    continue;
                }
                sourceJars.add(new ArtifactModule(key(artifact.getGroupId(), artifact.getArtifactId()), artifact.getFile(),
                        sourcePath));
            }
        }
    }

    private Path resolve(Artifact artifact) {
        if (!SOURCES_CLASSIFIER.equals(artifact.getClassifier())) {
            return null;
        }

        Artifact resolvedArtifact = null;
        try {
            resolvedArtifact = resourceResolver.getResolver().resolveArtifact(getProjectBuildingRequest(project), artifact)
                    .getArtifact();
            getLog().info(" > resolved " + resolvedArtifact.getFile());
        } catch (ArtifactResolverException e1) {
            getLog().info("Resolving failed for " + artifact);
        }
        return resolvedArtifact == null ? null : resolvedArtifact.getFile().toPath();
    }

    private Artifact createResourceArtifact(final Artifact artifact, final String classifier) {
        final DefaultArtifact a = (DefaultArtifact) resourceResolver.getArtifactFactory().createArtifactWithClassifier(
                artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), "jar", classifier);

        a.setRepository(artifact.getRepository());

        return a;
    }

    /**
     *
     * @return List of projects to be part of aggregated javadoc
     */
    private List<MavenProject> getAggregatedProjects() {
        if (this.reactorProjects == null) {
            return Collections.emptyList();
        }
        Map<Path, MavenProject> reactorProjectsMap = new HashMap<>();
        for (MavenProject reactorProject : this.reactorProjects) {
            reactorProjectsMap.put(reactorProject.getBasedir().toPath(), reactorProject);
        }

        return new ArrayList<>(modulesForAggregatedProject(project, reactorProjectsMap));
    }

    /**
     * Recursively add the modules of the aggregatedProject to the set of aggregatedModules.
     *
     * @param aggregatedProject the project being aggregated
     * @param reactorProjectsMap map of (still) available reactor projects
     */
    private Set<MavenProject> modulesForAggregatedProject(MavenProject aggregatedProject,
            Map<Path, MavenProject> reactorProjectsMap) {
        // Maven does not supply an easy way to get the projects representing
        // the modules of a project. So we will get the paths to the base
        // directories of the modules from the project and compare with the
        // base directories of the projects in the reactor.

        if (aggregatedProject.getModules().isEmpty()) {
            return Collections.singleton(aggregatedProject);
        }

        Path basePath = aggregatedProject.getBasedir().toPath();
        List<Path> modulePaths = new LinkedList<>();
        for (String module : aggregatedProject.getModules()) {
            modulePaths.add(basePath.resolve(module).normalize());
        }

        Set<MavenProject> aggregatedModules = new LinkedHashSet<>();

        for (Path modulePath : modulePaths) {
            MavenProject module = reactorProjectsMap.remove(modulePath);
            if (module != null) {
                aggregatedModules.addAll(modulesForAggregatedProject(module, reactorProjectsMap));
            }
        }

        return aggregatedModules;
    }

    private static Collection<Path> resolveFromProject(final MavenProject reactorProject, final Artifact artifact) {
        final List<String> dirs = new ArrayList<>();

        final List<String> srcRoots = reactorProject.getCompileSourceRoots();
        dirs.addAll(srcRoots);

        return pruneDirs(reactorProject, dirs);
    }

    /**
     * Method that removes the invalid directories in the specified directories. <b>Note</b>: All elements in <code>dirs</code>
     * could be an absolute or relative against the project's base directory <code>String</code> path.
     *
     * @param project the current Maven project not null
     * @param dirs the collection of <code>String</code> directories path that will be validated.
     * @return a List of valid <code>String</code> directories absolute paths.
     */
    public static Collection<Path> pruneDirs(MavenProject project, Collection<String> dirs) {
        final Path projectBasedir = project.getBasedir().toPath();

        Set<Path> pruned = new LinkedHashSet<>(dirs.size());
        for (String dir : dirs) {
            if (dir == null) {
                continue;
            }

            Path directory = projectBasedir.resolve(dir);

            if (Files.isDirectory(directory)) {
                pruned.add(directory.toAbsolutePath());
            }
        }

        return pruned;
    }

    private ProjectBuildingRequest getProjectBuildingRequest(MavenProject currentProject) {
        return new DefaultProjectBuildingRequest(session.getProjectBuildingRequest())
                .setRemoteRepositories(currentProject.getRemoteArtifactRepositories());
    }

    private static String key(final String gid, final String aid) {
        return gid + ":" + aid;
    }

}
