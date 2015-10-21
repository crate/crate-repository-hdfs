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
file-system as a repository for `snapshot/restore`_. It is bundled
with any `Crate`_ distribution starting with version *0.53*.

This plugin is derived from the `elasticsearch-repository-hdfs`_ plugin.


Requirements
============

-  `Crate`_ (version *0.53* or higher).
-  HDFS accessible file-system (from the Crate classpath)


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
.. _snapshot/restore: https://crate.io/docs/en/latest/sql/backup_restore.html
.. _Freenode: http://freenode.net
.. _elasticsearch-repository-hdfs: https://github.com/elastic/elasticsearch-hadoop/tree/2.1/repository-hdfs
