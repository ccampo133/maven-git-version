package me.ccampo.maven.git.version.plugin.util;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class ModelProvider {

    private final Map<MavenProject, Model> models;

    public ModelProvider(final Map<MavenProject, Model> models) {
        this.models = models;
    }

    public ModelProvider() {
        this(new HashMap<>());
    }

    public Model getModel(final MavenProject mavenProject) throws MavenExecutionException {
        if (models.containsKey(mavenProject)) {
            return models.get(mavenProject);
        }

        try (final Reader fileReader = new FileReader(mavenProject.getFile())) {
            final Model model = new MavenXpp3Reader().read(fileReader);
            models.put(mavenProject, model);
            return model;
        } catch (final XmlPullParserException | IOException e) {
            throw new MavenExecutionException(e.getMessage(), e);
        }
    }
}
