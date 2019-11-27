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


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import static me.ccampo.maven.git.version.plugin.util.PluginConfig.GENERATE_TEMPORARY_FILE;
import static me.ccampo.maven.git.version.plugin.util.PluginConfig.PROPERTY_PREFIX;
import static me.ccampo.maven.git.version.plugin.util.PluginConfig.DELETE_TEMPORARY_FILE;
import static me.ccampo.maven.git.version.plugin.util.PluginConfig.UPDATE_DEPENDENCIES;

/**
 * External Version extension configuration Mojo.  This mojo is ONLY used to configure the extension.
 *
 * @author <a href="mailto:bdemers@apache.org">Brian Demers</a>
 */
@Mojo(name = "version-inference")
@SuppressWarnings("unused")
public class VersionInferenceMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(property = "strategy", required = true)
    private String strategy;

    @Parameter(property = PROPERTY_PREFIX + DELETE_TEMPORARY_FILE, defaultValue = "false")
    private Boolean deleteTemporaryFile;

    @Parameter(property = PROPERTY_PREFIX + GENERATE_TEMPORARY_FILE, defaultValue = "false")
    private Boolean generateTemporaryFile;

    @Parameter(property = PROPERTY_PREFIX + UPDATE_DEPENDENCIES, defaultValue = "false")
    private Boolean updateDependencies;

    @Override
    public void execute() {
        getLog().info("This mojo is used to configure an extension, and should NOT be executed directly.");
    }
}
