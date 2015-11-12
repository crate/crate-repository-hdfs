=======
Develop
=======

This project uses ``gradle`` as build system.

To compile simply use::

    ./gradlew clean compileJava

In order to run the tests use::

    ./gradlew testHadoop2

or::

    ./gradlew testHadoop12

depending on the Hadoop version you want to test against.

    
=======================
Manual integration test
=======================

In order to test against a host that is really running HDFS it is possible to
to use docker to quickly launch a container that is running HDFS::
    
    docker run --net=host --rm -it -p 8020:8020 -p 8022:8022 -p 9000:9000 -p 50070:50070 -p 50075:50075 sequenceiq/hadoop-docker:2.7.0 /etc/bootstrap.sh -bash
    cd /usr/local/hadoop-2.7.0/
    bin/hdfs dfs -mkdir /data
    bin/hdfs dfs -chmod -R 777 /data

This uses the ``-net=host`` option because the namenode would otherwise
advertise the datanode with it's internal ip within the container and it
wouldn't be possible to access that IP from outside the container (unless a
custom route is added).
