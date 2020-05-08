# Examples

There are currently four examples, each showcasing different uses of `maven-git-version`. See each example's README for more details.

## `simple-module`

An example of how to manually set the project version at build time (does not utilize Git tagging information).

## `simple-module-git`

An example of how to automatically infer and set the version using Git tags.

## `multi-module-simple`

Mostly the same as `simple-module`, except now the Maven project consists of multiple modules.

## `multi-module-update-dependencies`

Mostly the same as `multi-module-simple`, except with inter-module dependencies.