.. _anchor-configuration-basics:

====================
Configuration basics
====================

In the previous chapter, you learned how to access and log in to the deegree service console and how to download and activate example workspaces. This chapter introduces the basic concepts of deegree webservices configuration:

* The deegree workspace and the active workspace directory
* Workspace files and resources
* Workspace directories and resource types
* Resource identifiers and dependencies
* Usage of the service console for workspace configuration

The final section of this chapter describes recommended practices for creating your own workspace. The remaining chapters of the documentation describe the individual workspace resource formats in detail.

---------------------
The deegree workspace
---------------------

The deegree workspace is the modular, resource-oriented and extensible configuration concept used by deegree webservices. The following diagram shows the different types of resources that it contains:

.. figure:: images/workspace-overview.png
   :target: _images/workspace-overview.png

   Configuration aspects of deegree workspaces

The following table provides a short description of the different types of workspace resources:

.. table:: Workspace resource types

+---------------------------------+------------------------------------------------------------------------------+
| Resource type                   | Description                                                                  |
+=================================+==============================================================================+
| Web Services                    | Web services (WFS, WMS, WMTS, CSW, WPS)                                      |
+---------------------------------+------------------------------------------------------------------------------+
| Data Stores (Coverage)          | Coverage (raster) data access (GeoTIFFs, raster pyramids, etc.)              |
+---------------------------------+------------------------------------------------------------------------------+
| Data Stores (Feature)           | Feature (vector) data access (Shapefiles, PostGIS, Oracle Spatial, etc.)     |
+---------------------------------+------------------------------------------------------------------------------+
| Data Stores (Metadata)          | Metadata record access (ISO records stored in PostGIS, Oracle, etc.)         |
+---------------------------------+------------------------------------------------------------------------------+
| Data Stores (Tile)              | Pre-rendered map tiles (GeoTIFF, image hierarchies in the file system, etc.) |
+---------------------------------+------------------------------------------------------------------------------+
| Map Layers (Layer)              | Map layers based on data stores and styles                                   |
+---------------------------------+------------------------------------------------------------------------------+
| Map Layers (Style)              | Styling rules for features and coverages                                     |
+---------------------------------+------------------------------------------------------------------------------+
| Map Layers (Theme)              | Layer trees based on individual layers                                       |
+---------------------------------+------------------------------------------------------------------------------+
| Processes                       | Geospatial processes for the WPS                                             |
+---------------------------------+------------------------------------------------------------------------------+
| Server connections (JDBC)       | Connections to SQL databases                                                 |
+---------------------------------+------------------------------------------------------------------------------+
| Server connections (remote OWS) | Connections to remote OGC web services                                       |
+---------------------------------+------------------------------------------------------------------------------+

Physically, every configured resource corresponds to an XML configuration file in the active workspace directory.

-------------------------------------------
Location of the deegree workspace directory
-------------------------------------------

The active deegree workspace is part of the ``.deegree`` directory which stores a few global configuration files along with the workspace. The location of this directory depends on your operating system.

^^^^^^^^^^^^^^^^^^^^^^
Linux/Solaris/Mac OS X
^^^^^^^^^^^^^^^^^^^^^^

On UNIX-like systems (Linux/Solaris/MacOS X), deegree's configuration files are located in folder ``$HOME/.deegree/``. Note that ``$HOME`` is determined by the user that started the web application container that runs deegree. If you started the ZIP version of deegree as user "kelvin", then the directory will be something like ``/home/kelvin/.deegree``.

.. tip::
  In order to use a different folder for deegree's configuration files, you can set the system environment variable ``DEEGREE_WORKSPACE_ROOT``. Note that the user running the web application container must have read/write access to this directory.

^^^^^^^
Windows
^^^^^^^

On Windows, deegree's configuration files are located in folder ``%USERPROFILE%/.deegree/``. Note that ``%USERPROFILE%`` is determined by the user that started the web application container that runs deegree. If you started the ZIP version of deegree as user "kelvin", then the directory will be something like ``C:\Users\kelvin\.deegree`` or ``C:\Dokumente und Einstellungen\kelvin\.deegree``.

.. tip::
  In order to use a different folder for deegree's configuration files, you can set the system environment variable ``DEEGREE_WORKSPACE_ROOT``.  Note that the user running the web application container must have read/write access to this directory.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Global configuration files and the active workspace
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

If you downloaded all four example workspaces (as described in :ref:`anchor-lightly`), set a console password and the proxy parameters, your ``.deegree`` directory will look like this:

.. figure:: images/workspace-root.png
   :target: _images/workspace-root.png

   Example ``.deegree`` directory

As you see, this ``.deegree`` directory contains four subdirectories. Every subdirectory corresponds to a deegree workspace. Besides the configuration files inside the workspace, three global configuration files exist:

.. raw:: latex

   \begin{table}
   \begin{center}

.. table:: Global configuration files and workspace directories
+------------------------+------------------------------------------+
| File name              | Function                                 |
+========================+==========================================+
| <subdirectory>         | Workspace directory                      |
+------------------------+------------------------------------------+
| console.pw             | Password for services console            |
+------------------------+------------------------------------------+
| proxy.xml              | Proxy settings                           |
+------------------------+------------------------------------------+
| webapps.properties     | Selects the active workspace             |
+------------------------+------------------------------------------+

.. raw:: latex

   \end{center}
   \caption{Global configuration files and workspace directories}
   \end{table}

Note that only a single workspace can be active at a time. The information on the active one is stored in file ``webapps.properties``.

.. tip::
  Usually, you don't need to care about the three files that are located at the top level of this directory. The service console creates and modifies them as required (e.g. when switching to a different workspace). In order to create a deegree webservices setup, you will need to create or edit resource configuration files in the active workspace directory. The remaining documentation will always refer to files in the (active) workspace directory.

.. tip::
  When multiple deegree webservices instances run on a single machine, every instance can use a different workspace. The file ``webapps.properties`` stores the active workspace for every deegree webapp separately.

--------------------------------------------
Structure of the deegree workspace directory
--------------------------------------------

The workspace directory is a container for resource files with a well-defined directory structure. When deegree starts up, the active workspace directory is determined and the following subdirectories are scanned for XML resource configuration files:

.. table:: Well-known workspace resource directories
+------------------------+---------------------------------+
| Directory              | Resource type                   |
+========================+=================================+
| services/              | Web services                    |
+------------------------+---------------------------------+
| datasources/coverage/  | Coverage Stores                 |
+------------------------+---------------------------------+
| datasources/feature/   | Feature Stores                  |
+------------------------+---------------------------------+
| datasources/metadata/  | Metadata Stores                 |
+------------------------+---------------------------------+
| datasources/tile/      | Tile Stores                     |
+------------------------+---------------------------------+
| layers/                | Map Layers (Layer)              |
+------------------------+---------------------------------+
| styles/                | Map Layers (Style)              |
+------------------------+---------------------------------+
| themes/                | Map Layers (Theme)              |
+------------------------+---------------------------------+
| processes/             | Processes                       |
+------------------------+---------------------------------+
| jdbc/                  | Server Connections (JDBC)       |
+------------------------+---------------------------------+
| datasources/remoteows/ | Server Connections (Remote OWS) |
+------------------------+---------------------------------+

A workspace directory may contain additional directories to provide additional files along with the resource configurations. The major difference is that these directories are not scanned for resource files. Some common ones are:

.. table:: Additional workspace directories
+-----------------------+-------------------------------------------+
| Directory             | Used for                                  |
+=======================+===========================================+
| appschemas/           | GML application schemas                   |
+-----------------------+-------------------------------------------+
| data/                 | Datasets (GML, GeoTIFF, ...)              |
+-----------------------+-------------------------------------------+
| manager/              | Example requests (for the generic client) |
+-----------------------+-------------------------------------------+

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Workspace files and resources
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In order to clarify the relation between workspace files and resources, let's have a look at an example:

.. figure:: images/workspace-example.png
   :target: _images/workspace-example.png

   Example workspace directory

As noted, deegree scans the well-known resource directories for XML files (``*.xml``) on startup (note that it will omit directory ``manager``, as it is not a well-known resource directory). For every file found, deegree will check the type of configuration format (by determining the name of the XML root element). If it is a recognized format, deegree will try to create and initialize a corresponding resource. For the example, this results in the following setup:

* A metadata store with id ``iso19115``
* A JDBC connection pool with id ``conn1``
* A web service with id ``csw``

The individual XML resource formats and their options are described in the later chapters of the documentation.

.. tip::
  You may wonder why the ``main.xml`` and ``metadata.xml`` files are not considered as web service resource files. These two filenames are reserved and treated specifically. See :ref:`anchor-configuration-service` for details.

.. tip::
  The configuration format has to match the workspace subdirectory, e.g. metadata store configuration files are only considered when they are located in ``datasources/metadata``.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Resource identifiers and dependencies
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

It has already been hinted that resources have an identifier, e.g. for file ``jdbc/conn1.xml`` a JDBC connection pool with identifier ``conn1`` is created. You probably have guessed that the identifier is derived from the file name (file name minus suffix), but you may wonder what purpose the identifier serves. The identifier is used for wiring resources. For example, an ISO metadata store resource requires a JDBC pool, because it provides the actual connections to the SQL database. Therefore, the corresponding resource configuration format has an element to specify it:

.. topic:: Example for wiring workspace resources

   .. literalinclude:: xml/workspace_dependencies.xml
      :language: xml

In this example, the ISO metadata store is wired to JDBC connection pool ``conn1``. Many deegree resource configuration files contain such references to dependent resources. Some resources perform auto-wiring. For example, every CSW instance needs to connect to a metadata store for accessing stored metadata records. If the CSW configuration omits the reference to the metadata store, it is assumed that there's exactly one metadata store defined in the workspace and deegree will automatically connect the CSW to this store.

.. tip::
  The required dependencies are specific to every type of resource and are documented for each resource configuration format.

------------------------------------------------
Using the service console for managing resources
------------------------------------------------

As an alternative to dealing with the workspace resource configuration files directly on the filesystem, you can also use the service console for this task. The service console has a corresponding menu entry for every type of workspace resource. All resource menu entries are grouped in the lower menu on the left:

.. figure:: images/console_resources.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_resources.jpg

   Workspace resource menu entries

Although the console offers additional functionality for some resource types, the basic management of resources is always identical.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Displaying configured resources
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In order to display the configured workspace resources of a certain type, click on the corresponding menu entry. The following screenshot shows the metadata store resources in deegree-workspace-csw:

.. figure:: images/console_metadata_stores.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_metadata_stores.jpg

   Displaying metadata store resources

The right part of the window displays a table with all configured metadata store resources. In this case, the workspace contains a single resource with identifier "iso19115" which is in status "On".

^^^^^^^^^^^^^^^^^^^^^^^
Deactivating a resource
^^^^^^^^^^^^^^^^^^^^^^^

The "Deactivate" link allows to turn off a resource temporarily (while keeping the configuration):

.. figure:: images/console_deactivate.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_deactivate.jpg

   Deactivate action

After clicking on "Deactivate", the status of the resource will be "Off", and the "Deactivate" link will change to "Activate". Also, the "Reload" link at the top will turn red to notify that there may be changes that need to be propagated to dependent resources:

.. figure:: images/console_deactivated.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_deactivated.jpg

   Deactivated a resource

.. tip::
  When a resource is being deactivated, the suffix of the corresponding configuration file is changed to ".ignore". Reactivating changes the suffix back to ".xml".

^^^^^^^^^^^^^^^^^^
Editing a resource
^^^^^^^^^^^^^^^^^^

By clicking on the "Edit" link, you can edit the corresponding XML configuration inside your browser:

.. figure:: images/console_edit.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_edit.jpg

   Edit action

The XML configuration will be displayed:

.. figure:: images/console_editing.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_editing.jpg

   Editing a resource configuration

You can now perform configuration changes in the text area and click on "Save". Or click any of the links:

* Display Schema: Displays the XML schema file for the resource configuration format.
* Cancel: Discards any changes.
* Turn on highlighting: Perform syntax highlighting.

If there are no (syntactical) errors in the configuration, the "Save" link will take you back to the corresponding resource view. Before actually saving the file, the service console will perform an XML validation of the file and display any syntactical errors:

.. figure:: images/console_edit_error.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_edit_error.jpg

   Displaying a syntax error

In this case, the mandatory "JDBCConnId" element was removed, which violates the configuration schema. This needs to be corrected, before "Save" will actually save the file to the workspace directory.

^^^^^^^^^^^^^^^^^^^
Deleting a resource
^^^^^^^^^^^^^^^^^^^

The "Delete" link will deactivate the resource and delete the corresponding configuration file from the workspace:

.. figure:: images/console_delete.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_delete.jpg

   Delete action

^^^^^^^^^^^^^^^^^^^^^^^
Creating a new resource
^^^^^^^^^^^^^^^^^^^^^^^

In order to add a new resource, enter a new identifier in the text field, select a resource sub-type from the drop-down and click on "Create new":

.. figure:: images/console_add.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_add.jpg

   Adding a WMS resource with identifier "mywms"

The next steps depend on the type of resource, but generally you have to choose between different options and the result will be a new resource configuration file in the workspace.

.. _anchor-console-errors:

^^^^^^^^^^^^^^^^^^^^^^^^^
Displaying error messages
^^^^^^^^^^^^^^^^^^^^^^^^^

One of the most helpful features of the console is that it can help to detect and fix errors in a workspace setup. For example, if you delete (or deactivate) JDBC connection "conn1" in deegree-workspace-csw and click "[Reload]", you will see the following:

.. figure:: images/console_error.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_error.jpg

   Errors in resource categories

The red exclamation marks near "services" and "metadata" show that these resource categories have resources with errors. Let's click on the metadata link to see what's going on:

.. figure:: images/console_error2.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_error2.jpg

   Resource "iso19115" has an error

The metadata resource view reveals that the metadata store "iso19115" has an error. Clicking on "Show errors" leads to:

.. figure:: images/console_error3.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_error3.jpg

   Details on the problem with "iso19115"

The error message gives an important hint: "No JDBC connection pool with id 'conn1' defined." deegree was unable to initialize the metadata store, because it refers to a JDBC connection pool "conn1". You may wonder what the error in the services category is about:

.. figure:: images/console_error4.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_error4.jpg

   Details on the problem with "csw"

As you see, the problem with the service resource ("There is no MetadataStore configured, ensure that exactly one store is available!) is actually a consequence of the other issue. Because deegree couldn't initialize the metadata store, it was also unable to start up the CSW correctly. If you add a new JDBC connection "conn1" and click on "[Reload]", both problems should disappear.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Resource type specific actions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In addition to the common management functionality, some resource views offer additional actions. This is described in the corresponding chapters, but here's a short overview:

* Web Services: Display service capabilities ("Capabilities"), edit service metadata ("Edit metadata"), edit controller configuration ("Edit global config")
* Feature Stores: Display feature types and number of stored features ("Info"), Import GML feature collections ("Loader"), Mapping wizard ("Create new" SQL feature store)
* Metadata Stores: Import metadata sets ("Loader"), create database tables ("Setup tables")
* Server Connections (JDBC): Test database connection ("Test")

--------------------------------------
Best practices for creating workspaces
--------------------------------------

This section provides some hints for creating a deegree workspace.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Start from example or from scratch
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

For creating your own workspace, you have two options. Option 1 is to use an existing workspace as a template and adapt it to your needs. Option 2 is to start from scratch, using an empty workspace. Adapting an existing workspace makes a lot of sense if your use-case is close to the scenario of the workspace. For example, if you want to set up INSPIRE View and Download Services, it is a good option to use :ref:`anchor-workspace-inspire` as a starting point.

In order to create a new workspace, simply create a new directory in the ``.deegree`` directory.

.. figure:: images/workspace-new.png
   :target: _images/workspace-new.png

   Creating the new workspace ``myscenario``

Afterwards, switch to the new workspace using the services console, as described in :ref:`anchor-downloading-workspaces`.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Find out which resources you need
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The first step is to identify the types of workspace resources that you need for your use-case. You probably know already which types of services your setup requires. The next step is to identify the dependencies for every service by having a look at the respective chapter in the documentation.  Let's say you want a setup with a transactional WFS, a WMS and a CSW:

* A WFS instance requires 1..n feature stores
* A WMS instance requires 1..n themes
* A CSW instance requires a single metadata store

Now you have to dig deeper: What kinds of feature stores exist? Maybe you will find out that what you want is an SQL feature store. So you read the respective part of the documentation and see that an SQL feature store requires a JDBC connection pool resource. Do the same research for the WMS dependencies. A WMS depends on a theme. Find out what a theme is and what it requires. In short, you have to answer the following questions for every encountered resource:

* What does resource do?
* How is it configured?
* On which resources does this resource depend?

At the end of this process you should know about the resources that you will have to configure for your setup.

.. tip::
  Alternatively, you can approach the resources question bottom-up. Let's say you have your data ready in a PostGIS database. You want to visualize it using a WMS. So you would require a JDBC resource pool that connects to your database. You need a simple SQL feature store (or an SQL feature store) that uses the new connection pool. You create one or more feature layers that are wired to the feature store and a theme based on the layers. At the end of the chain is the WMS resource which has to be configured to use the theme resource. Rendering styles can be created later (references have to be added to the layers configuration).

^^^^^^^^^^^^^^^^^^^^^^^^^^^
Use a validating XML editor
^^^^^^^^^^^^^^^^^^^^^^^^^^^

All deegree XML configuration files have a corresponding XML schema, which allows to detect syntactical errors easily. The editor built into the services console performs validation when you save a configuration file. If the contents is not valid according to the schema, the file will not be saved, but an error message will be displayed:

.. figure:: images/console_edit_error.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_edit_error.jpg

   The services console displays an XML syntax error

If you prefer to use a different editor for editing deegree's configuration files, it is highly recommended to choose a validating XML editor. Successfully tested editors are Eclipse and Altova XML Spy, but any schema-aware editor should work.

.. tip::
  In case you are able to understand XML schema, you can also use the schema file to find out about the available config options. deegree's schema files are hosted at http://schemas.deegree.org.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Check the resource status and error messages
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

As pointed out in :ref:`anchor-console-errors`, the service console indicates errors if resources cannot be initialized. Here's an example:

.. figure:: images/console_error3.jpg
   :figwidth: 60%
   :width: 50%
   :target: _images/console_error3.jpg

   Error message

In this case, it was not possible to initialize the JDBC connection (and the resources that depend on it). You can spot resource categories and resources that have errors easily, as they have a red exclamation mark. Click on the respective resource level and on "Errors" near the broken resource to see the error message. After fixing the error, click on "Reload" to re-initialize the workspace. If your fix was successful, the exclamation mark will be gone.

Additional information can be found in the deegree log. If you're running the ZIP version, switch to the terminal window. When initializing workspace resources, information on every resource will be logged, along with error messages.

.. figure:: images/terminal.png
   :figwidth: 60%
   :width: 50%
   :target: _images/terminal.png

   Log messages in the deegree log

.. tip::
  If you deployed the WAR version, the location of the deegree log depends on your web application container. For Tomcat, you will find it in file ``catalina.out`` in the ``log/`` directory.

.. tip::
  More logging can be activated by adjusting file ``log4j.properties`` in the ``/WEB-INF/classes/`` directory of the deegree webapplication.




