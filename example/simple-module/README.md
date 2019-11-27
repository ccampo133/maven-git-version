This is a simple example of using the version inference plugin with a simple 
Maven project

Currently it is using the `sysprop` strategy for version inference, and you can
build it with the command:

    mvn clean package -Dexternal.version=<some version>

