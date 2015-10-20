.. image:: https://cdn.crate.io/web/2.0/img/crate-avatar_100x100.png
    :width: 100px
    :height: 100px
    :alt: Crate.IO
    :target: https://crate.io

.. image:: https://travis-ci.org/crate/crate-repository-hdfs.svg?branch=master
    :target: https://travis-ci.org/crate/crate-repository-hdfs
    :alt: Test Status

=====================================
 Hadoop HDFS Snapshot/Restore plugin
=====================================

The ``crate-repository-hdfs`` plugin enables `Crate`_ to support ``hdfs``
file-system as a repository for `snapshot/restore`_.

This plugin is derived from the `elasticsearch-repository-hdfs
<https://github.com/elastic/elasticsearch-hadoop>`__ plugin.

Requirements
============

-  `Crate`_ (version *0.53* or higher).
-  HDFS accessible file-system (from the Crate classpath)

Flavors
=======

The HDFS snapshot/restore plugin comes in three flavors:

:Default (Light): The default version contains just the plugin jar without any
          Hadoop dependencies.

:Hadoop 1.x: The ``hadoop12`` version contains the plugin jar
             alongside Hadoop 1.x (stable) dependencies

:Hadoop 2.x: The ``hadoop2`` version contains the plugin jar
                    plus the Hadoop 2.x (Yarn) dependencies.

What version to use?
--------------------

It depends on whether you have Hadoop installed on your nodes or
not. If you do, then we recommend exposing Hadoop to the Crate
classpath and using the default version. This guarantees the existing
libraries and configuration are being picked up by the plugin. If you
do not have Hadoop installed, then select either the ``hadoop12``
version (for Hadoop stable/1.x) or, if you are using Hadoop 2, the
``hadoop2`` version.

Installation
============

The HDFS Snapshot/Restore is a `Crate`_ plugin - be sure to familiarize
with what these are and how they work by reading the `plugins chapter
<https://crate.io/docs/en/latest/plugins.html>`__ in the Crate documentation.

Node restart
------------

*After* installing the plugin on *every* `Crate`_ node, be sure to
*restart* it. This applies to *all* nodes on which the plugins have
been installed - without restarting the nodes, the plugin will not
function properly.


Configuration Properties
========================

Once installed, define the configuration for the ``hdfs`` repository
through ``crate.yml`` or the `REST
API <http://www.elastic.co/guide/en/elasticsearch/reference/current/modules-snapshots.html>`__:

::

    repositories
      hdfs:
        uri: "hdfs://<host>:<port>/"    # optional - Hadoop file-system URI
        path: "some/path"               # required - path with the file-system where data is stored/loaded
        load_defaults: "true"           # optional - whether to load the default Hadoop configuration (default) or not
        conf_location: "extra-cfg.xml"  # optional - Hadoop configuration XML to be loaded (use commas for multi values)
        conf.<key> : "<value>"          # optional - 'inlined' key=value added to the Hadoop configuration
        concurrent_streams: 5           # optional - the number of concurrent streams (defaults to 5)
        compress: "false"               # optional - whether to compress the metadata or not (default)
        chunk_size: "10mb"              # optional - chunk size (disabled by default)

NOTE: Be careful when including a paths within the ``uri``
setting. Some implementations ignore them completely while others
consider them. In general, we recommend keeping the ``uri`` to a
minimum and using the ``path`` element instead.

Plugging other file-systems
===========================

Any HDFS-compatible file-systems (like Amazon ``s3://`` or Google
``gs://``) can be used as long as the proper Hadoop configuration is
passed to the Crate plugin. In practice, this means making sure the
correct Hadoop configuration files (``core-site.xml`` and
``hdfs-site.xml``) and its jars are available in plugin classpath,
just as you would with any other Hadoop client or job. Otherwise, the
plugin will only read the *default*, vanilla configuration of Hadoop
and will not be able to recognized the plugged in file-system.

Help & Contact
==============

Do you have any questions? Or suggestions? We would be very happy
to help you. So, feel free to swing by our IRC channel #crate on Freenode_.
Or for further information and official contact please
visit `https://crate.io/ <https://crate.io/>`_.

License
=======

Copyright 2013-2015 CRATE Technology GmbH ("Crate")

Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  Crate licenses
this file to you under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.  You may
obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations
under the License.

However, if you have executed another commercial license agreement
with Crate these terms will supersede the license and you may use the
software solely pursuant to the terms of the relevant commercial agreement.



.. _Crate: https://github.com/crate/crate
.. _snapshot/restore: http://www.elasticsearch.org/guide/en/elasticsearch/reference/master/modules-snapshots.html>
.. _Freenode: http://freenode.net
