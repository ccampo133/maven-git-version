package me.ccampo.maven.git.version.strategy;

import com.github.zafarkhaja.semver.Version;
import fr.brouillard.oss.jgitver.GitVersionCalculator;
import fr.brouillard.oss.jgitver.metadata.Metadatas;
import me.ccampo.maven.git.version.core.VersionException;
import me.ccampo.maven.git.version.core.strategy.VersionStrategy;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

@Component(role = VersionStrategy.class, hint = "git")
public class GitVersionStrategy implements VersionStrategy {

    public static final String DEFAULT_NON_QUALIFIED_BRANCH = "master";
    public static final String DEFAULT_PRE_RELEASE_STAGE = "dev";
    public static final String DEFAULT_DIRTY_QUALIFIER = "uncommitted";
    public static final String PROPERTY_PREFIX = "project.";
    public static final String NORMAL_VERSION_PROPERTY = PROPERTY_PREFIX + "normalVersion";
    public static final String PRE_RELEASE_VERSION_PROPERTY = PROPERTY_PREFIX + "preReleaseVersion";
    public static final String BUILD_METADATA_PROPERTY = PROPERTY_PREFIX + "buildMetadata";
    public static final String DOCKER_SAFE_VERSION_PROPERTY = PROPERTY_PREFIX + "dockerSafeVersion";
    public static final String FULL_INFERRED_VERSION_PROPERTY = PROPERTY_PREFIX + "fullInferredVersion";

    @Configuration(name = "nonQualifierBranches", value = DEFAULT_NON_QUALIFIED_BRANCH)
    private String nonQualifierBranches;

    @Configuration(name = "preReleaseStage", value = DEFAULT_PRE_RELEASE_STAGE)
    private String preReleaseStage;

    @Configuration(name = "dirtyQualifier", value = DEFAULT_DIRTY_QUALIFIER)
    private String dirtyQualifier;

    @Configuration(name = "snapshot", value = "false")
    private Boolean snapshot;

    @Override
    public String getVersion(final MavenProject mavenProject) throws VersionException {
        final File rootDir = mavenProject.getBasedir();
        try (final GitVersionCalculator calculator = GitVersionCalculator.location(rootDir)) {
            final Version semVer = getVersionInternal(calculator);
            setProjectProperties(mavenProject.getProperties(), semVer);
            return semVer.toString();
        } catch (final Exception e) {
            throw new VersionException("Cannot close GitVersionCalculator object for project: " + rootDir, e);
        }
    }

    protected void setProjectProperties(final Properties properties, final Version semVer) {
        properties.setProperty(NORMAL_VERSION_PROPERTY, semVer.getNormalVersion());
        properties.setProperty(PRE_RELEASE_VERSION_PROPERTY, semVer.getPreReleaseVersion());
        properties.setProperty(BUILD_METADATA_PROPERTY, semVer.getBuildMetadata());
        properties.setProperty(FULL_INFERRED_VERSION_PROPERTY, semVer.toString());
        properties.setProperty(DOCKER_SAFE_VERSION_PROPERTY, semVer.toString().replace('+', '-'));
    }

    // This method exists solely to facilitate easier unit testing
    protected Version getVersionInternal(final GitVersionCalculator calculator) {
        configureGitVersionCalculator(calculator);

        final fr.brouillard.oss.jgitver.Version calculatedVersion = calculator.getVersionObject();

        final Version normalVersion = Version.forIntegers(
                calculatedVersion.getMajor(),
                calculatedVersion.getMinor(),
                calculatedVersion.getPatch()
        );

        final Version.Builder semVerBuilder = new Version.Builder()
                .setNormalVersion(normalVersion.toString());

        final int commitDistance = calculator.meta(Metadatas.COMMIT_DISTANCE)
                .map(Integer::valueOf)
                .orElse(0);

        final StringBuilder preReleaseVersion = new StringBuilder();

        // SNAPSHOT builds don't care if repo is dirty, so we don't have to go any further.
        if (snapshot != null && snapshot && commitDistance > 0) {
            preReleaseVersion.append("SNAPSHOT");
            semVerBuilder.setPreReleaseVersion(preReleaseVersion.toString());
            return semVerBuilder.build();
        }

        preReleaseVersion.append(preReleaseStage).append(".").append(commitDistance);

        final boolean isDirty = calculator.meta(Metadatas.DIRTY)
                .map(Boolean::valueOf)
                .orElse(false);

        if (isDirty) {
            preReleaseVersion.append(".").append(dirtyQualifier);
        }

        if (isDirty || commitDistance > 0) {
            semVerBuilder.setPreReleaseVersion(preReleaseVersion.toString());
            calculator.meta(Metadatas.GIT_SHA1_8).ifPresent(semVerBuilder::setBuildMetadata);
        }

        return semVerBuilder.build();
    }

    private void configureGitVersionCalculator(final GitVersionCalculator calculator) {
        calculator.setUseDistance(true);
        calculator.setUseGitCommitId(true);
        calculator.setAutoIncrementPatch(true);
        calculator.setNonQualifierBranches(Optional.ofNullable(nonQualifierBranches)
                .orElse(DEFAULT_NON_QUALIFIED_BRANCH));
    }

    /*
     * Setters are mostly intended to facilitate testing. Probably not used in reality.
     */
    public void setNonQualifierBranches(final String nonQualifierBranches) {
        this.nonQualifierBranches = nonQualifierBranches;
    }

    public void setPreReleaseStage(final String preReleaseStage) {
        this.preReleaseStage = preReleaseStage;
    }

    public void setDirtyQualifier(final String dirtyQualifier) {
        this.dirtyQualifier = dirtyQualifier;
    }

    public void setSnapshot(final Boolean snapshot) {
        this.snapshot = snapshot;
    }
}
