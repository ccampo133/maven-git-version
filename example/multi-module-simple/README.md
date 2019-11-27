This is a simple example of using the version inference plugin with a 
multi-tiered Maven project

Currently it is using the `sysprop` strategy for version inference, and you can
build it with the command:

    mvn clean package -Dexternal.version=<some version>
    
Note that none of the sub-projects have dependencies on each-other. See the 
example `multi-module-update-dependencies` for that sort of project.
