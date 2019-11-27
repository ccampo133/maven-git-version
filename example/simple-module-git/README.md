This is a simple example of using the version inference plugin with a simple 
Maven project.

Currently it is using the `git` strategy for version inference, and you can
build it with the command:

    mvn clean package
    
Note that the version will be inferred based on the git tags and metadata.
Please refer to the main README for more information on this version inference
strategy.

