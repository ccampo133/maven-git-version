package me.ccampo.maven.git.version.plugin.util;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelProviderTest {

    private Map<MavenProject, Model> models;

    private ModelProvider provider;

    @Before
    public void setUp() {
        models = new HashMap<>();
        provider = new ModelProvider(models);
    }

    @Test
    public void getModel_AlreadyExists() throws Exception {
        final MavenProject mavenProject = new MavenProject();
        final Model existing = new Model();
        models.put(mavenProject, existing);
        final Model model = provider.getModel(mavenProject);
        assertThat(model).isEqualTo(existing);
    }
}
