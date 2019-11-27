package me.ccampo.maven.git.version.plugin.util;

import me.ccampo.maven.git.version.core.strategy.VersionStrategy;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.expression.DefaultExpressionEvaluator;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.util.Optional;

public class PluginConfig {

    public static final String PROPERTY_PREFIX = "version-inference";
    public static final String GENERATE_TEMPORARY_FILE = "generateTemporaryFile";
    public static final String DELETE_TEMPORARY_FILE = "deleteTemporaryFile";
    public static final String UPDATE_DEPENDENCIES = "updateDependencies";
    public static final String FULL_PLUGIN_NAME = "me.ccampo:git-version-maven-plugin";
    public static final String STRATEGY_NODE_NAME = "strategy";
    public static final String STRATEGY_HINT = "hint";

    public final boolean shouldGenerateTemporaryFile;
    public final boolean shouldDeleteTemporaryFile;
    public final boolean shouldUpdateDependencies;
    public final VersionStrategy versionStrategy;

    public PluginConfig(
            final boolean shouldGenerateTemporaryFile,
            final boolean shouldDeleteTemporaryFile,
            final boolean shouldUpdateDependencies,
            final VersionStrategy versionStrategy
    ) {
        this.shouldGenerateTemporaryFile = shouldGenerateTemporaryFile;
        this.shouldDeleteTemporaryFile = shouldDeleteTemporaryFile;
        this.shouldUpdateDependencies = shouldUpdateDependencies;
        this.versionStrategy = versionStrategy;
    }

    public static PluginConfig of(final MavenProject mavenProject, final PlexusContainer container)
            throws MavenExecutionException {
        // Lookup this plugin's configuration from the project
        final Plugin plugin = mavenProject.getPlugin(FULL_PLUGIN_NAME);
        if (plugin != null) {
            final Xpp3Dom pluginConfigDom = (Xpp3Dom) plugin.getConfiguration();
            return new PluginConfig(
                getBooleanConfigValue(pluginConfigDom, GENERATE_TEMPORARY_FILE),
                getBooleanConfigValue(pluginConfigDom, DELETE_TEMPORARY_FILE),
                getBooleanConfigValue(pluginConfigDom, UPDATE_DEPENDENCIES),
                getStrategy(pluginConfigDom, mavenProject.getFile(), container)
            );
        }
        return null;
    }

    private static boolean getBooleanConfigValue(final Xpp3Dom pluginConfigDom, final String nodeName) {
        final Xpp3Dom n = pluginConfigDom.getChild(nodeName);
        return n != null && Boolean.parseBoolean(n.getValue());
    }

    private static VersionStrategy getStrategy(
            final Xpp3Dom configDom,
            final File pomFile,
            final PlexusContainer container
    ) throws MavenExecutionException {
        // Get the requested strategy from the POM config
        final Xpp3Dom strategyNode = Optional.ofNullable(configDom.getChild(STRATEGY_NODE_NAME))
                .orElseThrow(() -> new MavenExecutionException(
                        "Missing configuration, " + STRATEGY_NODE_NAME + " is required. ",
                        pomFile
                ));

        final String hint = Optional.ofNullable(strategyNode.getAttribute(STRATEGY_HINT))
                .orElseThrow(() -> new MavenExecutionException(
                        "Missing config; " + STRATEGY_NODE_NAME + " " + STRATEGY_HINT + " attribute is required.",
                        pomFile
                ));

        try {
            // Get and configure the strategy
            final VersionStrategy strategy = container.lookup(VersionStrategy.class, hint);
            final ComponentConfigurator configurator = container.lookup(ComponentConfigurator.class, "basic");
            configurator.configureComponent(
                    strategy,
                    new XmlPlexusConfiguration(strategyNode),
                    new DefaultExpressionEvaluator(),
                    null,
                    null
            );
            return strategy;
        } catch (final ComponentLookupException | ComponentConfigurationException e) {
            throw new MavenExecutionException(e.getMessage(), e);
        }
    }
}
