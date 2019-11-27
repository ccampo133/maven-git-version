package me.ccampo.maven.git.version.plugin.util;

import org.apache.maven.project.MavenProject;

import java.util.Objects;

public class GroupArtifactVersion {

    public final String groupId;
    public final String artifactId;
    public final String version;

    GroupArtifactVersion(final String groupId, final String artifactId, final String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public static GroupArtifactVersion of(final String groupId, final String artifactId, final String version) {
        return new GroupArtifactVersion(groupId, artifactId, version);
    }

    public static GroupArtifactVersion fromMavenProject(final MavenProject mavenProject) {
        return GroupArtifactVersion.of(
                mavenProject.getGroupId(),
                mavenProject.getArtifactId(),
                mavenProject.getVersion()
        );
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupArtifactVersion)) return false;
        final GroupArtifactVersion that = (GroupArtifactVersion) o;
        final boolean groupIdEquals = groupId == null ? that.groupId == null : groupId.equalsIgnoreCase(that.groupId);
        final boolean artifactIdEquals =
                artifactId == null ? that.artifactId == null : artifactId.equalsIgnoreCase(that.artifactId);
        final boolean versionEquals = version == null ? that.version == null : version.equalsIgnoreCase(that.version);
        return groupIdEquals && artifactIdEquals && versionEquals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
