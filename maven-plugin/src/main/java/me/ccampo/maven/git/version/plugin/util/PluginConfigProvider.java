package me.ccampo.maven.git.version.plugin.util;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;

import java.util.HashMap;
import java.util.Map;

public class PluginConfigProvider {

    private final Map<MavenProject, PluginConfig> projectConfigs;
    private final PlexusContainer container;

    public PluginConfigProvider(
            final Map<MavenProject, PluginConfig> projectConfigs,
            final PlexusContainer container
    ) {
        this.projectConfigs = projectConfigs;
        this.container = container;
    }

    public PluginConfigProvider(final PlexusContainer container) {
        this(new HashMap<>(), container);
    }

    public PluginConfig getForProject(final MavenProject mavenProject) throws MavenExecutionException {
        // Can't use computeIfAbsent here elegantly due to checked exceptions
        if (projectConfigs.containsKey(mavenProject)) {
            return projectConfigs.get(mavenProject);
        }
        final PluginConfig pluginConfig = PluginConfig.of(mavenProject, container);
        projectConfigs.put(mavenProject, pluginConfig);
        return pluginConfig;
    }
}
