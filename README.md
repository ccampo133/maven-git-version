# maven-git-version 

[![Download](https://api.bintray.com/packages/ccampo133/public/maven-git-version/images/download.svg)](https://bintray.com/ccampo133/public/maven-git-version/_latestVersion)
[![](https://github.com/ccampo133/maven-git-version/workflows/Build%20master/badge.svg)](https://github.com/{owner}/{repo}/actions) 

# Description

Set your Maven project verion at build time, or automatically generate it from your Git tags!

Once you've enabled the Maven plugin, using it is dead simple. Just execute your build as normal, for example:

```
# Our repo is one commit ahead of tag 0.1.0
$ mvn clean package
[INFO] Scanning for projects...
[INFO] Inferred project version: 0.1.0-dev.1+9bb4708f
[INFO] Inferred project.build.finalName: simple-module-0.1.0-dev.1+9bb4708f
[INFO]
[INFO] ---------------------< com.example:simple-module >----------------------
[INFO] Building simple-module 0.1.0-dev.1+9bb4708f
[INFO] --------------------------------[ jar ]---------------------------------
...
[INFO] BUILD SUCCESS
```

In general, `maven-git-version` is an API and Maven plugin for performing automatic version inference in your POM. 

Various version inference strategies are provided for the Maven plugin, including one to infer the version based on the Git repository metadata.

The main use-case of the extension plugin is to allow the POM versions to be updated during a build and NOT require
the pom.xml file to be modified (causing potential merge conflicts down the road, or untracked changes).

The main functionality of this repository has been forked and adapted from [maven-external-version](https://github.com/bdemers/maven-external-version).

Continue reading below for more detailed usage scenarios, or dive straight into the [examples](https://github.com/ccampo133/maven-git-version/tree/master/example)!

# Usage

In your POM plugins section, simply add:

```xml
<plugin>
    <groupId>me.ccampo</groupId>
    <artifactId>git-version-maven-plugin</artifactId>
    <version>0.1.0</version> <!-- Use the latest stable version if possible -->
    <extensions>true</extensions>
    <configuration>
        <strategy hint="git">
            <!-- Strategy specific configuration goes here -->
        </strategy>
    </configuration>
</plugin>
```

This example shows using the Git strategy mentioned above. There should be no required strategy configuration, as opinionated defaults are chosen out of the box. However, see below for various configuration parameters.

If you are interested in other possible strategies, read below and take a look at the `version-inference-strategies`
module for more options. Some other strategies include setting the version from the command line, reading from a file, 
or executing a script.

# Plugin Configuration & Parameters

```xml
<plugin>
    <groupId>me.ccampo</groupId>
    <artifactId>git-version-maven-plugin</artifactId>
    <version>0.1.0</version> <!-- Use the latest stable version if possible -->
    <extensions>true</extensions>
    <configuration>
        <!-- See available strategies below -->
        <strategy hint="git">
            <!-- Strategy specific configuration goes here -->
        </strategy>
        <!-- See parameters below; default values are included here -->
        <generateTemporaryFile>false</generateTemporaryFile>
        <retainTemporaryFile>false</generateTemporaryFile>
        <updateDependencies>false</updateDependencies>
    </configuration>
</plugin>
```

- `strategy#hint` key defining which strategy implementation will be used, one of
  - git: infers the version from Git metadata, following the versioning strategy described above
  - file: read the version from the first line of a given file
  - script: version is given by the first line of the output execution of a given command
  - sysprop: allows to define project version & qualifier from system properties
- `generateTemporaryFile` if _true_, the generated pom files will be created as temporary files inside the directory pointed by system property `java.io.tmpdir`. If omitted it defaults to  _false_. When false, a file called `pom.xml.new-version` will be generated in the root project directory. 
- `deleteTemporaryFile` if _true_, the generated pom files created by this extension will be deleted after execution. Set this parameter to _false_ to explicitly retain the files. Value is optional and defaults to _false_.
- `updateDependencies` if _true_, any dependencies within sub-projects that are sub-projects themselves will have their versions updated to the latest version, if applicable. See the example `multi-module-update-dependencies` for more information. Defaults to _false_. 

## Strategy: `git`

This strategy uses the Git tags and metadata to infer a project version. Utilizes [jgitver](https://jgitver.github.io/)
to do most of the heavy lifting.

The version generated will be a [Semantic Version](https://semver.org/) of the form:

    <MAJOR>.<MINOR>.<PATCH>[-<PRE-RELEASE>+<BUILD META>]
    
The `MAJOR`, `MINOR`, and `PATCH` components are integers. The combination of these three components into 
`<MAJOR>.<MINOR>.<PATCH>` is known as the _normal version_.

The `PRE-RELEASE` component is a string composed of dot separated identifiers of the following form:

    <STAGE>.<COMMIT DISTANCE>[.uncommitted]
    
The value `STAGE` is a string representing the stage of development, e.g. `dev`.

The value `COMMIT DISTANCE` is the integer number of commits since the last tagged version.

The literal value `uncommitted` appears when there are uncommitted changes in the repository (i.e. the repo is dirty).

Finally, the `BUILD META` portion of the version is a string representing build metadata. By default, it is the 
abbreviated, eight character SHA1 hash of the HEAD commit.

For example, a full version may be 

    1.2.3-dev.5.uncommitted+ef7ac902
   
Or an equally valid version may simply be a normal version, e.g.

    4.5.6
 
See `example/simple-module-git` for a more detailed example on usage of this strategy.

### Usage

```xml
<plugin>
    <groupId>me.ccampo</groupId>
    <artifactId>git-version-maven-plugin</artifactId>
    <version>0.1.0</version> <!-- Use the latest stable version if possible -->
    <extensions>true</extensions>
    <configuration>
        <strategy hint="git">
            <!-- Strategy specific configuration goes here -->
            <nonQualifierBranches>master</nonQualifierBranches>
            <preReleaseStage>dev</preReleaseStage>
            <dirtyQualifier>uncommitted</dirtyQualifier>
            <snapshot>false</snapshot>
        </strategy>
    </configuration>
</plugin>
```

### Parameters

- `nonQualifierBranches`: A comma separated list of branches for which no branch name qualifier will be used. Default "master". Example: `master, integration`
- `preReleaseStage`: The pre-release stage, as defined above. Default `dev`.
- `dirtyQualifier`: If the repository is dirty (has uncommitted changes), this is the string value used to represent it. Default: `uncommitted`.
- `snapshot`: If true, use the SNAPSHOT versioning strategy, which will replace all pre-release components with the literal word `SNAPSHOT`, and will omit all build metadata. Tagged versions will still be normal versions. Default: `false`

### Available Properties

As of 0.1.0, the following Maven project properties are set when using this strategy:

- `project.normalVersion`: The computed [normal version](https://semver.org/#spec-item-2)
- `project.preReleaseVersion`: The computed [pre-release version](https://semver.org/#spec-item-9)
- `project.buildMetadata`: The computed [build metadata](https://semver.org/#spec-item-10)
- `project.dockerSafeVersion`: Equivalent to the fully computed semantic version, however the `+` character is replaced with `-` since [Docker does not support it (yet?)](https://github.com/docker/distribution/issues/1201)
- `project.fullInferredVersion`: The the fully computed semantic version, i.e. `project.version`, however maybe useful if the Maven lifecycle hasn't updated `project.version` correctly at the time you need it.

## Strategy: `file`

This strategy reads the first line of a given file to extract the version to use. 

### Usage

```xml
<plugin>
    <groupId>me.ccampo</groupId>
    <artifactId>git-version-maven-plugin</artifactId>
    <version>0.1.0</version> <!-- Use the latest stable version if possible -->
    <extensions>true</extensions>
    <configuration>
        <strategy hint="file">
            <!-- Strategy specific configuration goes here -->
            <versionFilePath>SOME_FILE</versionFilePath>
        </strategy>
    </configuration>
</plugin>
```

### Parameters

- `versionFilePath`: denotes the file which first line will be read to extract the version from. Can be a fully qualified path or a path relative to the project directory. The parameter is optional, it defaults to `VERSION`, meaning that if not provided, a file called `VERSION` will be read from the project root. 

## Strategy: `script`

This strategy allows to execute a given command ; the first line of stdout output will be used as version. 

### Usage

```xml
<plugin>
    <groupId>me.ccampo</groupId>
    <artifactId>git-version-maven-plugin</artifactId>
    <version>0.1.0</version> <!-- Use the latest stable version if possible -->
    <extensions>true</extensions>
    <configuration>
        <strategy hint="script">
            <!-- Strategy specific configuration goes here -->
            <script>SOME_COMMAND</script>
        </strategy>
    </configuration>
</plugin>
```

### Parameters

- `script`: a command to execute. The parameter is optional and defaults to `./version.sh`, meaning that if not provided a file called `version.sh` in the project root will be executed. 

## Strategy: `sysprop`

This strategy uses two system properties to define the new project version:

- `external.version`: the main version to use. If omitted, then it defaults to the current `project.version`.
- `external.version-qualifier`: an optional qualifier that will be appended to the given version

### Usage

```xml
<plugin>
    <groupId>me.ccampo</groupId>
    <artifactId>git-version-maven-plugin</artifactId>
    <version>0.1.0</version> <!-- Use the latest stable version if possible -->
    <extensions>true</extensions>
    <configuration>
        <strategy hint="sysprop">
            <!-- Strategy specific configuration goes here -->
        </strategy>
    </configuration>
</plugin>
```

# Development

To build and install to your local Maven repository

    mvn clean install

## Updating the Changelog

We use the [Conventional Changelog](https://conventionalcommits.org/) format for our git commits to generate our changelog (particularly, the angular.js preset).

First, install the [conventional-changelog-cli](https://github.com/conventional-changelog/conventional-changelog/tree/master/packages/conventional-changelog-cli)

    npm install -g conventional-changelog-cli

Then run

    conventional-changelog -p angular -i CHANGELOG.md -s

Or if you want to regenerate the entire changelog from scratch...

    conventional-changelog -p angular -i CHANGELOG.md -s -r 0

# Notes

Unfortunately, this plugin cannot be used to version itself due to some Maven quirks, so we're stuck with the 
old-fashioned manual versioning process (e.g. `git commit -m "bump version"`).
