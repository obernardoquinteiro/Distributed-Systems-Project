# Server


## Authors

Group T04


### Lead developer 

93713, Gonçalo Mateus, ist193713


### Contributors

93692, Bernardo Quinteiro, ist193692

93700, Diogo Lopes, ist193700



## About

This is a gRPC server defined by the protobuf specification.

The server runs in a stand-alone process.


## Instructions for using Maven

To compile and run:

```
mvn compile exec:java -Dexec.args="arg0 arg1 arg2 arg3 arg4"
```

To run using appassembler plugin on Linux:

```
./target/appassembler/bin/spotter arg0 arg1 arg2 arg3 arg4
```

To run using appassembler plugin on Windows:

```
target\appassembler\bin\app arg0 arg1 arg2 arg3 arg4
```
or

```
sh target/appassembler/bin/app arg0 arg1 arg2 arg3 arg4
```

When running, the server awaits connections from clients.


## To configure the Maven project in Eclipse

'File', 'Import...', 'Maven'-'Existing Maven Projects'

'Select root directory' and 'Browse' to the project base folder.

Check that the desired POM is selected and 'Finish'.


----

