.. _anchor-configuration-javamodules:

==========================
Java modules and libraries
==========================

deegree webservices is a Java web application and based on code written in the Java programming language. As a user, you usually don't need to care about this, unless you want to extend the default functionality available in a deegree webservices setup. This chapter provides some basic knowledge of JAR (Java archive) files, the Java classpath and describes how deegree webservices finds JARs. Additionally, it provides precise instructions for adding JARs so your deegree webservices instance can connect to Oracle Spatial and Microsoft SQL Server databases.

.. hint::
   The terms JAR, module and library are used interchangeably in this chapter.

.. _anchor-adding-jars:

^^^^^^^^^^^^^^^^^^^^^^^^^^^
Java code and the classpath
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Java code is usually packaged in JAR files. If you want to extend deegree's codebase, you will have to add one or more JAR files to the so-called classpath [#f1]_. Basically, there are two different types of classpaths that determine which JAR files are available to deegree webservices:

* The web application classpath
* The workspace classpath

The full classpath used by deegree webservices consists of the web application classpath and the workspace classpath. If conflicting files exist on both classpaths, the file on the workspace classpath takes precedence.

.. tip::
   If you're not familiar with classpath concepts and don't have any special requirements, simply add your JAR files to the workspace classpath and ignore the web application classpath.

"""""""""""""""""""""""""
Web application classpath
"""""""""""""""""""""""""

As deegree webservices is a Java web application, standard paths apply:

* Directory ``WEB-INF/lib`` of the deegree web application (for JARs)
* Directory ``WEB-INF/classes`` of the deegree web application (for Java class files)
* Global directories for all web applications running in the container (depends on the actual web container)

When you add files to the web application claspath, you have to restart the web application or the web application container to make the new code available to deegree webservices.

.. hint::
   All Java libraries shipped with deegree webservices are located in the ``WEB-INF/lib`` directory of the deegree webservices webapp. If you downloaded the ZIP version, this directory is located in ``webapps/ROOT/WEB-INF/lib``.

"""""""""""""""""""
Workspace classpath
"""""""""""""""""""

When deegree webservices initializes the workspace, it scans directory ``modules/`` of the active deegree workspace for files ending with ``.jar`` and adds them to the classpath. This can be very handy, as it allows to create self-contained workspaces (no fiddling with other directories required) and also has the benefit the you can reload the deegree workspace only after adding your libraries (instead of restarting the deegree webapp or the whole web application container).

.. hint::
  In addition to workspace directory ``modules/``, directory ``classes/`` can be used to add individual Java classes (and other files) to the classpath. This is usually not required.

^^^^^^^^^^^^^^^^^^^^^^^
Checking available JARs
^^^^^^^^^^^^^^^^^^^^^^^

In order to see which JARs are available to your deegree webservices instance/workspace, use the "module info" link in the general section of the service console:

.. figure:: images/module_info.png
   :figwidth: 60%
   :width: 50%
   :target: _images/module_info.png

   Displaying available JARs using the service console

The list of JARs section displays the JARs found on the web application classpath, while the lower section displays the JARs found on the workspace classpath.

.. hint:
   Actually, not all JARs are displayed in this view. Only deegree modules and JDBC drivers are displayed (see below).

.. _anchor-db-libraries:

^^^^^^^^^^^^^^^^^^^^^^^
Adding database modules
^^^^^^^^^^^^^^^^^^^^^^^

By default, deegree webservices includes everything that is needed for connecting to PostgreSQL/PostGIS and Derby databases. If you want to connect to an Oracle Spatial or Microsoft SQL Server instance, you need to add additional Java libraries manually, as the required JDBC libraries are not included in the deegree webservices download (for license reasons).

"""""""""""""""""""""
Adding Oracle support
"""""""""""""""""""""

The following deegree resources support Oracle Spatial databases (10g, 11g):

* SimpleSQLFeatureStore
* SQLFeatureStore
* ISOMetadataStore

In order to enable Oracle connectivity for these resources, you need to add two JAR files (see :ref:`anchor-adding-jars`):

* A compatible Oracle JDBC6-type driver (e.g. ``ojdbc6-11.2.0.2.jar``) [#f2]_
* Module deegree-sqldialect-oracle [#f3]_

"""""""""""""""""""""""""""""""""""
Adding Microsoft SQL server support
"""""""""""""""""""""""""""""""""""

The following deegree resources support Microsoft SQL Server (2008, 2012):

* SimpleSQLFeatureStore
* SQLFeatureStore
* ISOMetadataStore

In order to enable Microsoft SQL Server connectivity for these resources, you need to add two JAR files (see :ref:`anchor-adding-jars`):

* A compatible Microsoft JDBC driver (e.g. ``sqljdbc4-3.0.jar``) [#f4]_
* Module deegree-sqldialect-mssql [#f5]_

.. rubric:: Footnotes

.. [#f1] The term classpath describes the set of files or directories which are used to find the available Java code (JARs and class files).
.. [#f2] http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html (registration required)
.. [#f3] http://repo.deegree.org/content/repositories/public/org/deegree/deegree-sqldialect-oracle/${project.version}/deegree-sqldialect-oracle-${project.version}.jar
.. [#f4] http://msdn.microsoft.com/en-us/sqlserver/aa937724.aspx
.. [#f5] http://repo.deegree.org/content/repositories/public/org/deegree/deegree-sqldialect-mssql/${project.version}/deegree-sqldialect-mssql-${project.version}.jar

