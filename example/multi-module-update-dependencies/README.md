This is an example of using the version inference plugin with a multi-tiered
Maven project, where one of the subprojects depends on another. The project
layout looks like this:

    parent
      - subproj-one
      - subproj-two (depends on subproj-one)

Currently it is using the `sysprop` strategy for version inference, and you can
build it with the command:

    mvn clean install -Dexternal.version=<some version>
    
Note that the `package` goal _will not work_ with this example unless you 
run the `install` goal first. This is due to Maven limitations; see:

 * [Maven doesn't recognize sibling modules when running mvn dependency:tree](https://stackoverflow.com/questions/1677473/maven-doesnt-recognize-sibling-modules-when-running-mvn-dependencytree)
 * [Maven cannot resolve dependency for module in same multi-module project](https://stackoverflow.com/questions/29712865/maven-cannot-resolve-dependency-for-module-in-same-multi-module-project)

Also note that in the parent pom, we have the following plugin property set:

    <updateDependencies>true</updateDependencies>

This will cause the version inference plugin to actually update the versions of
dependencies which are also projects themselves. In this example, it will 
update the dependency version of `subproj-one` in `subproj-two` to the latest
inferred version. Of course, this property is optional, if you prefer to stick
to static versions.
