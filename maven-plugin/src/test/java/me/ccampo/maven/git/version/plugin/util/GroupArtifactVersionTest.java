package me.ccampo.maven.git.version.plugin.util;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GroupArtifactVersionTest {

    @Test
    public void of() {
        final GroupArtifactVersion gav = GroupArtifactVersion.of("foo", "bar", "baz");
        final GroupArtifactVersion expected = new GroupArtifactVersion("foo", "bar", "baz");
        assertThat(gav).isEqualTo(expected);
    }

    @Test
    public void fromMavenProject() {
        final MavenProject mavenProject = new MavenProject();
        mavenProject.setGroupId("foo");
        mavenProject.setArtifactId("bar");
        mavenProject.setVersion("baz");
        final GroupArtifactVersion gav = GroupArtifactVersion.fromMavenProject(mavenProject);
        final GroupArtifactVersion expected = new GroupArtifactVersion("foo", "bar", "baz");
        assertThat(gav).isEqualTo(expected);
    }

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(GroupArtifactVersion.class)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void testToString() {
        final GroupArtifactVersion gav = new GroupArtifactVersion("foo", "bar", "baz");
        assertThat(gav.toString()).isEqualTo("foo:bar:baz");
    }
}
