==============================
HDFS Snapshot & Restore Plugin
==============================

.. image:: https://travis-ci.org/crate/crate-repository-hdfs.svg?branch=master
    :target: https://travis-ci.org/crate/crate-repository-hdfs
    :alt: Test Status

|

A CrateDB_ plugin that enables `Apache Hadoop HDFS`_ support for `snapshots and
restore`_.

This plugin is bundled with any CrateDB 0.53 and higher.

Prerequisites
=============

You will need:

- CrateDB 0.53 or higher
- A HDFS compatible file system accessible from the CrateDB class path

Supported File Systems
======================

Any HDFS compatible file system (like Amazon S3 or `Google Cloud Storage`_) can
be used as long as the proper Hadoop configuration is passed to the plugin.

Make sure the correct Hadoop configuration files (``core-site.xml`` and
``hdfs-site.xml``) and its JAR files are available on the plugin class path,
just as you would with any other Hadoop client or job. Otherwise, the plugin
will only read the default configuration of Hadoop and will not recognize the
plugged in file-system.

Contributing
============

This project is primarily maintained by Crate.io_, but we welcome community
contributions!

See the `developer docs`_ and the `contribution docs`_ for more information.

Help
====

Looking for more help?

- Read `the project documentation`_
- Chat with us via our `support channel`_
- Get `paid support`_

.. _Amazon S3: https://aws.amazon.com/s3/
.. _Apache Hadoop HDFS: https://hortonworks.com/apache/hdfs/
.. _contribution docs: CONTRIBUTING.rst
.. _Crate.io: http://crate.io/
.. _CrateDB: https://github.com/crate/crate
.. _developer docs: DEVELOP.rst
.. _elasticsearch-repository-hdfs: https://github.com/elastic/elasticsearch-hadoop/tree/2.1/repository-hdfs
.. _Google Cloud Storage: https://cloud.google.com/storage/
.. _support channel: https://crate.io/support/
.. _paid support: https://crate.io/pricing/
.. _snapshots and restore: https://crate.io/docs/en/latest/sql/backup_restore.html
.. _the project documentation: https://crate.io/docs/en/latest/sql/backup_restore.html
