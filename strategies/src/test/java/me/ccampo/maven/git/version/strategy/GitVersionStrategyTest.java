package me.ccampo.maven.git.version.strategy;

import fr.brouillard.oss.jgitver.GitVersionCalculator;
import fr.brouillard.oss.jgitver.Version;
import fr.brouillard.oss.jgitver.metadata.Metadatas;
import org.junit.Test;

import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitVersionStrategyTest {

    @Test
    public void getVersionInternal_IsDirtyAndTenCommitsAhead() {
        final GitVersionStrategy strategy = new GitVersionStrategy();
        strategy.setPreReleaseStage(GitVersionStrategy.DEFAULT_PRE_RELEASE_STAGE);
        strategy.setDirtyQualifier(GitVersionStrategy.DEFAULT_DIRTY_QUALIFIER);

        final GitVersionCalculator gitVersionCalculator = mock(GitVersionCalculator.class);

        final int major = 1;
        final int minor = 2;
        final int patch = 3;
        when(gitVersionCalculator.getVersionObject())
                .thenReturn(new Version(major, minor, patch));

        final int commitDistance = 10;
        when(gitVersionCalculator.meta(Metadatas.COMMIT_DISTANCE))
                .thenReturn(Optional.of(String.valueOf(commitDistance)));

        final boolean isDirty = true;
        when(gitVersionCalculator.meta(Metadatas.DIRTY))
                .thenReturn(Optional.of(String.valueOf(isDirty)));

        final String sha = "58de199";
        when(gitVersionCalculator.meta(Metadatas.GIT_SHA1_8))
                .thenReturn(Optional.of(sha));

        final String version = strategy.getVersionInternal(gitVersionCalculator).toString();

        final String expectedVersion = major +
                "." +
                minor +
                "." +
                patch +
                "-" +
                GitVersionStrategy.DEFAULT_PRE_RELEASE_STAGE +
                "." +
                commitDistance +
                "." +
                GitVersionStrategy.DEFAULT_DIRTY_QUALIFIER +
                "+" +
                sha;

        assertThat(version).isEqualTo(expectedVersion);
    }

    @Test
    public void getVersionInternal_IsNotDirtyAndFiveCommitsAhead() {
        final GitVersionStrategy strategy = new GitVersionStrategy();
        strategy.setPreReleaseStage(GitVersionStrategy.DEFAULT_PRE_RELEASE_STAGE);
        strategy.setDirtyQualifier(GitVersionStrategy.DEFAULT_DIRTY_QUALIFIER);

        final GitVersionCalculator gitVersionCalculator = mock(GitVersionCalculator.class);

        final int major = 1;
        final int minor = 2;
        final int patch = 3;
        when(gitVersionCalculator.getVersionObject())
                .thenReturn(new Version(major, minor, patch));

        final int commitDistance = 5;
        when(gitVersionCalculator.meta(Metadatas.COMMIT_DISTANCE))
                .thenReturn(Optional.of(String.valueOf(commitDistance)));

        final boolean isDirty = false;
        when(gitVersionCalculator.meta(Metadatas.DIRTY))
                .thenReturn(Optional.of(String.valueOf(isDirty)));

        final String sha = "58de199";
        when(gitVersionCalculator.meta(Metadatas.GIT_SHA1_8))
                .thenReturn(Optional.of(sha));

        final String version = strategy.getVersionInternal(gitVersionCalculator).toString();

        final String expectedVersion = major +
                "." +
                minor +
                "." +
                patch +
                "-" +
                GitVersionStrategy.DEFAULT_PRE_RELEASE_STAGE +
                "." +
                commitDistance +
                "+" +
                sha;

        assertThat(version).isEqualTo(expectedVersion);
    }

    @Test
    public void getVersionInternal_IsNotDirtyAndZeroCommitsAhead() {
        final GitVersionStrategy strategy = new GitVersionStrategy();
        strategy.setPreReleaseStage(GitVersionStrategy.DEFAULT_PRE_RELEASE_STAGE);
        strategy.setDirtyQualifier(GitVersionStrategy.DEFAULT_DIRTY_QUALIFIER);

        final GitVersionCalculator gitVersionCalculator = mock(GitVersionCalculator.class);

        final int major = 1;
        final int minor = 2;
        final int patch = 3;
        when(gitVersionCalculator.getVersionObject())
                .thenReturn(new Version(major, minor, patch));

        final int commitDistance = 0;
        when(gitVersionCalculator.meta(Metadatas.COMMIT_DISTANCE))
                .thenReturn(Optional.of(String.valueOf(commitDistance)));

        final boolean isDirty = false;
        when(gitVersionCalculator.meta(Metadatas.DIRTY))
                .thenReturn(Optional.of(String.valueOf(isDirty)));

        final String sha = "58de199";
        when(gitVersionCalculator.meta(Metadatas.GIT_SHA1_8))
                .thenReturn(Optional.of(sha));

        final String version = strategy.getVersionInternal(gitVersionCalculator).toString();

        final String expectedVersion = major + "." + minor + "." + patch;

        assertThat(version).isEqualTo(expectedVersion);
    }

    @Test
    public void getVersionInternal_IsNotDirtyAndIsSnapshot() {
        final GitVersionStrategy strategy = new GitVersionStrategy();
        strategy.setPreReleaseStage(GitVersionStrategy.DEFAULT_PRE_RELEASE_STAGE);
        strategy.setDirtyQualifier(GitVersionStrategy.DEFAULT_DIRTY_QUALIFIER);
        strategy.setSnapshot(true);

        final GitVersionCalculator gitVersionCalculator = mock(GitVersionCalculator.class);

        final int major = 1;
        final int minor = 2;
        final int patch = 3;
        when(gitVersionCalculator.getVersionObject())
                .thenReturn(new Version(major, minor, patch));

        final int commitDistance = 9;
        when(gitVersionCalculator.meta(Metadatas.COMMIT_DISTANCE))
                .thenReturn(Optional.of(String.valueOf(commitDistance)));

        final boolean isDirty = false;
        when(gitVersionCalculator.meta(Metadatas.DIRTY))
                .thenReturn(Optional.of(String.valueOf(isDirty)));

        final String sha = "58de199";
        when(gitVersionCalculator.meta(Metadatas.GIT_SHA1_8))
                .thenReturn(Optional.of(sha));

        final String version = strategy.getVersionInternal(gitVersionCalculator).toString();

        final String expectedVersion = major + "." + minor + "." + patch + "-SNAPSHOT";

        assertThat(version).isEqualTo(expectedVersion);
    }

    @Test
    public void getVersionInternal_IsDirtyAndIsSnapshot() {
        final GitVersionStrategy strategy = new GitVersionStrategy();
        strategy.setPreReleaseStage(GitVersionStrategy.DEFAULT_PRE_RELEASE_STAGE);
        strategy.setDirtyQualifier(GitVersionStrategy.DEFAULT_DIRTY_QUALIFIER);
        strategy.setSnapshot(true);

        final GitVersionCalculator gitVersionCalculator = mock(GitVersionCalculator.class);

        final int major = 1;
        final int minor = 2;
        final int patch = 3;
        when(gitVersionCalculator.getVersionObject())
                .thenReturn(new Version(major, minor, patch));

        final int commitDistance = 9;
        when(gitVersionCalculator.meta(Metadatas.COMMIT_DISTANCE))
                .thenReturn(Optional.of(String.valueOf(commitDistance)));

        final boolean isDirty = true;
        when(gitVersionCalculator.meta(Metadatas.DIRTY))
                .thenReturn(Optional.of(String.valueOf(isDirty)));

        final String sha = "58de199";
        when(gitVersionCalculator.meta(Metadatas.GIT_SHA1_8))
                .thenReturn(Optional.of(sha));

        final String version = strategy.getVersionInternal(gitVersionCalculator).toString();

        final String expectedVersion = major + "." + minor + "." + patch + "-SNAPSHOT";

        assertThat(version).isEqualTo(expectedVersion);
    }

    @Test
    public void getVersionInternal_IsNotDirtyAndIsSnapshotButIsZeroCommitsAhead() {
        final GitVersionStrategy strategy = new GitVersionStrategy();
        strategy.setPreReleaseStage(GitVersionStrategy.DEFAULT_PRE_RELEASE_STAGE);
        strategy.setDirtyQualifier(GitVersionStrategy.DEFAULT_DIRTY_QUALIFIER);
        strategy.setSnapshot(true);

        final GitVersionCalculator gitVersionCalculator = mock(GitVersionCalculator.class);

        final int major = 1;
        final int minor = 2;
        final int patch = 3;
        when(gitVersionCalculator.getVersionObject())
                .thenReturn(new Version(major, minor, patch));

        final int commitDistance = 0;
        when(gitVersionCalculator.meta(Metadatas.COMMIT_DISTANCE))
                .thenReturn(Optional.of(String.valueOf(commitDistance)));

        final boolean isDirty = false;
        when(gitVersionCalculator.meta(Metadatas.DIRTY))
                .thenReturn(Optional.of(String.valueOf(isDirty)));

        final String sha = "58de199";
        when(gitVersionCalculator.meta(Metadatas.GIT_SHA1_8))
                .thenReturn(Optional.of(sha));

        final String version = strategy.getVersionInternal(gitVersionCalculator).toString();

        final String expectedVersion = major + "." + minor + "." + patch;

        assertThat(version).isEqualTo(expectedVersion);
    }

    @Test
    public void getVersionInternal_IsDirtyAndIsZeroCommitsAhead() {
        final GitVersionStrategy strategy = new GitVersionStrategy();
        strategy.setPreReleaseStage(GitVersionStrategy.DEFAULT_PRE_RELEASE_STAGE);
        strategy.setDirtyQualifier(GitVersionStrategy.DEFAULT_DIRTY_QUALIFIER);

        final GitVersionCalculator gitVersionCalculator = mock(GitVersionCalculator.class);

        final int major = 1;
        final int minor = 2;
        final int patch = 3;
        when(gitVersionCalculator.getVersionObject())
                .thenReturn(new Version(major, minor, patch));

        final int commitDistance = 0;
        when(gitVersionCalculator.meta(Metadatas.COMMIT_DISTANCE))
                .thenReturn(Optional.of(String.valueOf(commitDistance)));

        final boolean isDirty = true;
        when(gitVersionCalculator.meta(Metadatas.DIRTY))
                .thenReturn(Optional.of(String.valueOf(isDirty)));

        final String sha = "58de199";
        when(gitVersionCalculator.meta(Metadatas.GIT_SHA1_8))
                .thenReturn(Optional.of(sha));

        final String version = strategy.getVersionInternal(gitVersionCalculator).toString();

        final String expectedVersion = major
                + "."
                + minor
                + "."
                + patch
                + "-"
                + GitVersionStrategy.DEFAULT_PRE_RELEASE_STAGE
                + "."
                + commitDistance
                + "."
                + GitVersionStrategy.DEFAULT_DIRTY_QUALIFIER
                + "+"
                + sha;

        assertThat(version).isEqualTo(expectedVersion);
    }

    @Test
    public void projectPropertiesAreSet_FullVersion() {
        final Properties properties = new Properties();
        final GitVersionStrategy strategy = new GitVersionStrategy();

        final com.github.zafarkhaja.semver.Version semVer = com.github.zafarkhaja.semver.Version.forIntegers(1, 2, 3)
                .setPreReleaseVersion("dev.1")
                .setBuildMetadata("58de199");

        strategy.setProjectProperties(properties, semVer);

        assertThat(properties.get(GitVersionStrategy.NORMAL_VERSION_PROPERTY)).isEqualTo(semVer.getNormalVersion());
        assertThat(properties.get(GitVersionStrategy.PRE_RELEASE_VERSION_PROPERTY))
                .isEqualTo(semVer.getPreReleaseVersion());
        assertThat(properties.get(GitVersionStrategy.BUILD_METADATA_PROPERTY)).isEqualTo(semVer.getBuildMetadata());
        assertThat(properties.get(GitVersionStrategy.DOCKER_SAFE_VERSION_PROPERTY))
                .isEqualTo(semVer.toString().replace('+', '-'));
    }

    @Test
    public void projectPropertiesAreSet_SnapshotVersion() {
        final Properties properties = new Properties();
        final GitVersionStrategy strategy = new GitVersionStrategy();

        final com.github.zafarkhaja.semver.Version semVer = com.github.zafarkhaja.semver.Version.forIntegers(1, 2, 3)
                .setPreReleaseVersion("SNAPSHOT");

        strategy.setProjectProperties(properties, semVer);

        assertThat(properties.get(GitVersionStrategy.NORMAL_VERSION_PROPERTY)).isEqualTo(semVer.getNormalVersion());
        assertThat(properties.get(GitVersionStrategy.PRE_RELEASE_VERSION_PROPERTY))
                .isEqualTo(semVer.getPreReleaseVersion());
        assertThat(properties.get(GitVersionStrategy.BUILD_METADATA_PROPERTY)).asString().isEmpty();
        assertThat(properties.get(GitVersionStrategy.DOCKER_SAFE_VERSION_PROPERTY))
                .isEqualTo(semVer.toString().replace('+', '-'));
    }

    @Test
    public void projectPropertiesAreSet_NormalVersion() {
        final Properties properties = new Properties();
        final GitVersionStrategy strategy = new GitVersionStrategy();

        final com.github.zafarkhaja.semver.Version semVer = com.github.zafarkhaja.semver.Version.forIntegers(1, 2, 3);
        strategy.setProjectProperties(properties, semVer);

        assertThat(properties.get(GitVersionStrategy.NORMAL_VERSION_PROPERTY)).isEqualTo(semVer.getNormalVersion());
        assertThat(properties.get(GitVersionStrategy.PRE_RELEASE_VERSION_PROPERTY)).asString().isEmpty();
        assertThat(properties.get(GitVersionStrategy.BUILD_METADATA_PROPERTY)).asString().isEmpty();
        assertThat(properties.get(GitVersionStrategy.DOCKER_SAFE_VERSION_PROPERTY))
                .isEqualTo(semVer.toString().replace('+', '-'));
    }
}
