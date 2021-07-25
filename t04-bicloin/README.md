# T04-Bicloin

Distributed Systems 2020-2021, 2nd semester project


## Authors

**Group T04**

93692 [Bernardo Quinteiro](mailto:bernardo.quinteiro@tecnico.ulisboa.pt) ![photo](https://i.imgur.com/9KTTJuD.png)

93700 [Diogo Lopes](mailto:diogo.andre.fulgencio.lopes@tecnico.ulisboa.pt) ![photo](https://i.imgur.com/1ZnaJ3h.png)

93713 [Gonçalo Mateus](mailto:goncalo.filipe.mateus@tecnico.ulisboa.pt) ![photo](https://i.imgur.com/InQ3Lw4.png)


### Module leaders

For each module, the README file must identify the lead developer and the contributors.
The leads should be evenly divided among the group members.

### Code identification

In all the source files (including POMs), please replace __CXX__ with your group identifier.  
The group identifier is composed by Campus - A (Alameda) or T (Tagus) - and number - always with two digits.

This change is important for code dependency management, to make sure that your code runs using the correct components and not someone else's.


## Getting Started

The overall system is composed of multiple modules.

See the project statement for a full description of the domain and the system.

### Prerequisites

Java Developer Kit 11 is required running on Linux, Windows or Mac.
Maven 3 is also required.

To confirm that you have them installed, open a terminal and type:

```
javac -version

mvn -version
```

### Installing

To compile and install all modules:

```
mvn clean install -DskipTests
```

The integration tests are skipped because they require theservers to be running.


## Built With

* [Maven](https://maven.apache.org/) - Build Tool and Dependency Management
* [gRPC](https://grpc.io/) - RPC framework


## Versioning

We use [SemVer](http://semver.org/) for versioning. 
