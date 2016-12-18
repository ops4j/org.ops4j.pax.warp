Pax Warp
========

Thanks for looking into Pax Warp, a tool for managing database schemas
and data sets.

This is the official source repository of the OPS4J Pax Warp project.
It is licensed under the Apache Software License 2.0 by the OPS4J community.

## Documentation

* [User Manual](http://ops4j.github.io/pax/warp/latest/)

## Build

You'll need a machine with Java 8 or higher.

Checkout:

    git clone git://github.com/ops4j/org.ops4j.pax.warp.git

Build with Maven wrapper (no Maven installation required):

    ./mvnw clean install

If Maven 3.2.5 or higher is already installed:

    mvn clean install

Run additional integration tests (requires Docker):

    mvn -Pdocker clean install
    
Run all integration tests including Oracle (requires Oracle account and additional Maven settings):

    mvn -Pdocker,oracle clean install    

## Releases

Releases go to Maven Central.

Pax Warp is incubating. The current milestone release is *Pax Warp 0.7.0*.

## Continuous Integration Builds

We have continuous integration builds set up here:

* <http://ci.ops4j.org/jenkins/job/org.ops4j.pax.warp>
* <https://travis-ci.org/ops4j/org.ops4j.pax.warp>

The Travis CI build supports Docker and includes the full set of integration tests, with the required database
servers running as Docker containers.

Snapshot artifacts are published to:

* <https://oss.sonatype.org/content/repositories/ops4j-snapshots>


The OPS4J Team.

