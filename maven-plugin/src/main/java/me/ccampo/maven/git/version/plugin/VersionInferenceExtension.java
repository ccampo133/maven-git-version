/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package me.ccampo.maven.git.version.plugin;

import me.ccampo.maven.git.version.core.VersionException;
import me.ccampo.maven.git.version.core.strategy.VersionStrategy;
import me.ccampo.maven.git.version.plugin.util.GroupArtifactVersion;
import me.ccampo.maven.git.version.plugin.util.ModelProvider;
import me.ccampo.maven.git.version.plugin.util.PluginConfig;
import me.ccampo.maven.git.version.plugin.util.PluginConfigProvider;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Maven Extension that will update all the projects in the reactor with an externally managed version.
 * <p/>
 * This extension MUST be configured as a plugin in order to be configured.
 * <p/>
 * 'strategy' - The configuration for an ExternalVersionStrategy.
 * 'hint' -  A component hint to load the ExternalVersionStrategy.
 *
 * @author <a href="mailto:bdemers@apache.org">Brian Demers</a>
 * @author <a href="mailto:ccampo.progs@gmail.com">Chris Campo</a>
 */
@Component(role = AbstractMavenLifecycleParticipant.class, hint = "version-inference")
public class VersionInferenceExtension extends AbstractMavenLifecycleParticipant {

    @Requirement
    private Logger logger;

    @Requirement
    private PlexusContainer container;

    private Map<GroupArtifactVersion, String> projectGavs = new HashMap<>();

    private PluginConfigProvider pluginConfigProvider;

    private ModelProvider modelProvider;

    @Override
    public void afterProjectsRead(final MavenSession session) throws MavenExecutionException {
        init();

        for (final MavenProject mavenProject : session.getAllProjects()) {
            setProjectVersion(mavenProject);
        }

        // Need to do a second pass here since our projectGavs map is populated now
        for (final MavenProject mavenProject : session.getAllProjects()) {
            final PluginConfig pluginConfig = pluginConfigProvider.getForProject(mavenProject);
            if (pluginConfig != null) {
                if (pluginConfig.shouldUpdateDependencies) {
                    setDependencyVersions(mavenProject);
                }
                setParentVersion(mavenProject);
                createNewVersionPom(mavenProject);
            }
        }
    }

    // This is a clutch to work around Plexus's complete lack of constructor injection
    private void init() {
        Objects.requireNonNull(logger);
        Objects.requireNonNull(container);

        if (pluginConfigProvider == null) {
            pluginConfigProvider = new PluginConfigProvider(container);
        }

        if (modelProvider == null) {
            modelProvider = new ModelProvider();
        }

        if (projectGavs == null) {
            projectGavs = new HashMap<>();
        }
    }

    private void setProjectVersion(final MavenProject mavenProject) throws MavenExecutionException {
        // Get the plugin config
        final PluginConfig pluginConfig = pluginConfigProvider.getForProject(mavenProject);

        if (pluginConfig != null) {
            // Store the old version before changing it
            final String oldVersion = mavenProject.getVersion();

            // Now use the strategy to figure out the new version
            final String newVersion = getNewVersion(pluginConfig.versionStrategy, mavenProject);

            logger.info("Inferred project version: " + newVersion);

            final String oldFinalName = mavenProject.getBuild().getFinalName();
            final String newFinalName = oldFinalName.replaceFirst(Pattern.quote(oldVersion), newVersion);
            logger.info("Inferred project.build.finalName: " + newFinalName);

            // Now that we have the new version, we update the project versions.
            mavenProject.setVersion(newVersion);
            mavenProject.getArtifact().setVersion(newVersion);
            VersionRange versionRange = VersionRange.createFromVersion(newVersion);
            mavenProject.getArtifact().setVersionRange(versionRange);
            mavenProject.getBuild().setFinalName(newFinalName);

            final GroupArtifactVersion oldProjectVersion =
                    GroupArtifactVersion.of(mavenProject.getGroupId(), mavenProject.getArtifactId(), oldVersion);
            projectGavs.put(oldProjectVersion, newVersion);
        }
    }

    /**
     * In the case where the plugin is configured to update project dependency
     * versions, we loop through the dependencies of the project and check if
     * that each dependency is actually one of the projects contained in this
     * POM (with the exception of the project itself, since it can never depend
     * on the latest version of itself). If a dependency is a project of this
     * POM, we update its version to the latest inferred version.
     */
    private void setDependencyVersions(final MavenProject mavenProject) {
        final GroupArtifactVersion projectGav = GroupArtifactVersion.fromMavenProject(mavenProject);
        mavenProject.getDependencies().forEach(dependency -> {
            final GroupArtifactVersion dependencyGav = GroupArtifactVersion.of(
                    dependency.getGroupId(),
                    dependency.getArtifactId(),
                    dependency.getVersion()
            );
            if (!projectGav.equals(dependencyGav) && projectGavs.containsKey(dependencyGav)) {
                final String newVersion = projectGavs.get(dependencyGav);
                dependency.setVersion(newVersion);
                if (logger.isInfoEnabled()) {
                    logger.info("Setting project " + projectGav + " dependency " + dependencyGav + " to version " +
                            newVersion);
                }
            }
        });
    }

    private void setParentVersion(final MavenProject mavenProject) throws MavenExecutionException {
        // Update model version. The project's version has been updated so we can just use it here.
        final Model model = modelProvider.getModel(mavenProject);
        model.setVersion(mavenProject.getVersion());

        // Update model parent version
        if (model.getParent() != null) {
            final GroupArtifactVersion parentGav = GroupArtifactVersion.of(
                    model.getParent().getGroupId(),
                    model.getParent().getArtifactId(),
                    model.getParent().getVersion()
            );
            final String newVersionForParent = projectGavs.get(parentGav);
            if (newVersionForParent != null) {
                model.getParent().setVersion(newVersionForParent);
            }
        }

        // Nothing else to do.
        if (mavenProject.getParent() == null) {
            return;
        }

        /*
         * At this point, we've only updated the versions of the individual projects.
         * Now we need to update the references between the updated projects.
         */
        final MavenProject parent = mavenProject.getParent();
        if (projectGavs.containsKey(GroupArtifactVersion.fromMavenProject(parent))) {
            // We need to update the parent
            //TODO: implement -ccampo 2019-01-16
            logger.warn("Need to update parent (not implemented)");
        }
    }

    private String getNewVersion(final VersionStrategy strategy, final MavenProject mavenProject)
            throws MavenExecutionException {
        final Optional<String> newVersion;
        try {
            newVersion = Optional.ofNullable(strategy.getVersion(mavenProject));
        } catch (final VersionException e) {
            throw new MavenExecutionException(e.getMessage(), e);
        }

        return newVersion.orElseThrow(() -> {
            final String msg = "Unable to infer new version; strategy returned null.";
            return new MavenExecutionException(msg, mavenProject.getFile());
        }).trim();
    }

    private void createNewVersionPom(final MavenProject mavenProject) throws MavenExecutionException {
        final PluginConfig pluginConfig = pluginConfigProvider.getForProject(mavenProject);

        final File newPom;
        try {
            if (pluginConfig.shouldGenerateTemporaryFile) {
                newPom = File.createTempFile("pom", ".version-inference");
            } else {
                newPom = new File(mavenProject.getBasedir(), "pom.xml.new-version");
            }
        } catch (final IOException e) {
            throw new MavenExecutionException(e.getMessage(), e);
        }

        if (pluginConfig.shouldDeleteTemporaryFile) {
            newPom.deleteOnExit();
        }

        if (logger.isDebugEnabled()) {
            logger.debug(VersionInferenceExtension.class.getSimpleName() + ": using new pom file => " + newPom);
        }

        final Model model = modelProvider.getModel(mavenProject);

        // Write the new pom to disk
        try (final Writer fileWriter = new FileWriter(newPom)) {
            new MavenXpp3Writer().write(fileWriter, model);
        } catch (final IOException e) {
            throw new MavenExecutionException(e.getMessage(), e);
        }

        mavenProject.setFile(newPom);
    }

    /*
     * The following setters are primarily used to facilitate testing. Probably not used in practice.
     */
    @SuppressWarnings("unused")
    public void setLogger(final Logger logger) {
        this.logger = logger;
    }

    @SuppressWarnings("unused")
    public void setContainer(final PlexusContainer container) {
        this.container = container;
    }

    @SuppressWarnings("unused")
    public void setProjectGavs(final Map<GroupArtifactVersion, String> projectGavs) {
        this.projectGavs = projectGavs;
    }

    @SuppressWarnings("unused")
    public void setPluginConfigProvider(final PluginConfigProvider pluginConfigProvider) {
        this.pluginConfigProvider = pluginConfigProvider;
    }

    @SuppressWarnings("unused")
    public void setModelProvider(final ModelProvider modelProvider) {
        this.modelProvider = modelProvider;
    }
}
