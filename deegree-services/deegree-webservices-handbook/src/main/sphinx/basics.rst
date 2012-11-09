.. _anchor-configuration-basics:

====================
Configuration basics
====================

In the previous chapter, you learned how to access and login to the deegree services console and how to download and activate example workspaces. In order to be able to adapt an example workspace (or to create your own workspace from scratch), you need to understand the different types of workspace resources (e.g. data access) and how to configure them. This chapter describes the overall structure of a deegree workspace, while the remaining chapters describe the individual configuration formats in full detail.

----------------------------------------------
Where do I find deegree's configuration files?
----------------------------------------------

Although deegree webservices comes with a basic web console for configuration, it is usually necessary to edit and create XML files in order to configure deegree webservices according to your needs. The services console currently provides an interface to help with a few tasks (such as downloading example configurations), but it's main purpose is to help with creating and editing the different types of XML configuration files in the active deegree workspace.

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

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Workspaces and global configuration files
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Besides the workspace configuration files, deegree webservices support a small number of global configuration files. Subdirectories in the deegree configuration directory are workspace directories:

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

.. tip::
  Usually, you don't need to take care of any files that are located in this directory. In order to adapt deegree webservices to your needs, you will need to create or edit configuration files in the active workspace directory. Therefore, the rest of the documentation will refer to configuration files in the (active) workspace directory.

------------------------------
What is the deegree workspace?
------------------------------

The deegree workspace is the active configuration for a deegree webservices instance. It consists of XML files organized in a well-defined directory structure that control the different configuration aspects:

.. figure:: images/workspace-overview.png
   :figwidth: 90%
   :width: 90%
   :target: _images/workspace-overview.png

   Configuration aspects of deegree workspaces

The following table gives an overview of the different types of workspace aspects

.. table:: Workspace aspects

+---------------------------------+------------------------------------------------------------------------------+
| Configuration aspect            | Description                                                                  |
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
| Map Layers (Style)              | Styling rules for features and converages                                    |
+---------------------------------+------------------------------------------------------------------------------+
| Map Layers (Theme)              | Layer trees based on individual layers                                       |
+---------------------------------+------------------------------------------------------------------------------+
| Processes                       | Geospatial processes for the WPS                                             |
+---------------------------------+------------------------------------------------------------------------------+
| Server connections (JDBC)       | Connections to SQL databases                                                 |
+---------------------------------+------------------------------------------------------------------------------+
| Server connections (remote OWS) | Connections to remote OGC web services                                       |
+---------------------------------+------------------------------------------------------------------------------+

-----------------------------
What are workspace resources?
-----------------------------

A workspace directory consists of XML files organized in a well-defined directory structure. Each XML file corresponds to a "workspace resource". When the workspace is initialized, a resource will be created for every XML file. The type of created resource depends on the directory and the configuration format. Here's an example:

.. figure:: images/workspace-overview.png
   :figwidth: 90%
   :width: 90%
   :target: _images/workspace-overview.png

   Example workspace directory

When the above workspace is initialized, the following deegree resources will be created:

* A JDBC connection pool with id ````
* A Data Store for metadata with id ````
* A Web Service with id ````

As you may guess, the configuration file format has to match the workspace subdirectory, e.g. you should only put MetadataStore configuration files into ``datasources/metadata``. The following table provides an overview on the directories and the expected type of configuration file:

.. table:: Workspace directory structure
+------------------------+---------------------------------+
| Directory              | Configuration aspect            |
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
+----------------------------------------------------------+
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

.. tip::
  deegree will try to process configuration files in the well-known directories into active resources of the corresponding type. Other directories in the workspace will not be scanned for resource configurations and can be used for other purposes (e.g. providing GeoTIFF files along with the workspace).

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Dependencies between workspace resources
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The former example will result in the following setup:

.. figure:: images/workspace-csw.png
   :figwidth: 90%
   :width: 90%
   :target: _images/workspace-csw.png

   Workspace components involved in a deegree CSW configuration

-------------------------------------------------------
Using the service console to manage workspace resources
-------------------------------------------------------

The service console has a corresponding menu entry for every type of workspace resource. For example, if you would like to add/remove/edit a coverage store, you would click on "data stores -> coverage". This opens a view with a list of all configured coverage stores. If you activated the Utah workspace (see :ref:`anchor-workspace-utah`), you should see the following list:

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Configuring coverage stores

As you can see, the Utah demo workspace defines three coverage stores in total. Each configured coverage store (and every deegree workspace resource in general) has a corresponding XML file, which you can edit by clicking the "Edit" button:

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Editing a coverage store configuration

The details of the individual configuration formats are described in the later chapters. The built-in XML editor allows to edit the contents of the configuration file, which controls the behaviour of th workspace resource. In the example, it describes the raster file that this coverage store accesses. You can save the changes ("Save") or discard them ("Cancel"). Additionally, you may turn on syntax highlighting and look at the XML schema of the configuration ("Display Schema").

Deleting a workspace resource is straight-forward ("Delete"). You can also turn off a workspace resource temporarily ("Deactivate").

---------------------------------------------------------
Using the service console to add a new workspace resource
---------------------------------------------------------

In order to add a new workspace resource, use the "Create new" link. Note that you always have to specify an identifier for every new resource. 

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Adding a new WPS with identifier "mywps"

----------------------------------------
Creating your own workspace from scratch
----------------------------------------

You should have a basic understanding of the deegree workspace concepts now. If you want to configure deegree webservices for your own scenario, you will probably understand
Now you should know how to create and edit resources in the deegree workspace. But you may be wondering how to find out which exact workspace resources you need for a specific scenario.

TBD

