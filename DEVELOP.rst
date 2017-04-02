===============
Developer Guide
===============

Building
========

This project uses Gradle_ as build tool.

Gradle can be invoked like so::

    $ ./gradlew clean compileJava

The first time this command is executed, Gradle is downloaded and bootstrapped
for you automatically.

Testing
=======

Run the unit tests for Hadoop 2, like so::

    $ ./gradlew testHadoop2

Or for Hadoop 1.2::

    $ ./gradlew testHadoop12

Manual Integration Testing
==========================

You can use Docker to quickly launch a container that is running HDFS::

    $ docker run --net=host --rm -it \
        -p 8020:8020 -p 8022:8022 -p 9000:9000 -p 50070:50070 -p 50075:50075 \
        sequenceiq/hadoop-docker:2.7.0 /etc/bootstrap.sh -bash
    $ cd /usr/local/hadoop-2.7.0/
    $ bin/hdfs dfs -mkdir /data
    $ bin/hdfs dfs -chmod -R 777 /data

We use ``-net=host`` because otherwise the NameNode advertises the DataNode
with its internal IP and it isn't possible to access that IP from outside
the container unless a custom route is added.

.. _Gradle: https://gradle.org/
