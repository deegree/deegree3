.. _anchor-configuration-basics:

====================
Configuration basics
====================

In the previous chapter, you learned how to access and login to the deegree services console and how to download and activate workspaces. In order to be able to create your own workspace configuration, you need to have some understanding about the different workspace aspects (e.g. data access) and how to configure them. This chapter describes the overall structure of a deegree workspace, while the remaining chapters describe the configuration aspects in full detail.

----------------------------
What is a deegree workspace?
----------------------------

A deegree workspace is a set of configuration files organized in a well-defined directory structure. The files in this directory structure define the different aspects of a deegree configuration:

.. figure:: images/workspace-overview.png
   :figwidth: 90%
   :width: 90%
   :target: _images/workspace-overview.png

   Configuration aspects of a deegree workspace

The following table gives an overview of the individual workspace aspects:

.. table:: Workspace aspects

+---------------------------------+------------------------------------------------------------------------------+
| Configuration aspect            | Description                                                                  |
+=================================+==============================================================================+
| Web Services                    | Web services (WFS, WMS, WMTS, CSW, WPS)                                      |
+---------------------------------+------------------------------------------------------------------------------+
| Data Stores (Coverage)          | Coverage (raster) data backends (GeoTIFFs, raster pyramids, etc.)            |
+---------------------------------+------------------------------------------------------------------------------+
| Data Stores (Feature)           | Feature (vector) data backends (Shapefiles, PostGIS, Oracle Spatial, etc.)   |
+---------------------------------+------------------------------------------------------------------------------+
| Data Stores (Metadata)          | Metadata record backends (ISO records stored in PostGIS, Oracle, etc.)       |
+---------------------------------+------------------------------------------------------------------------------+
| Data Stores (Tile)              | Pre-rendered map tiles (GeoTIFF, image hierarchies in the file system, etc.) |
+---------------------------------+------------------------------------------------------------------------------+
| Map Layers (Layer)              | Map Layers based on data stores and styles                                   |
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

-------------------------------------------------------
Using the service console to manage workspace resources
-------------------------------------------------------

The service console has a corresponding menu entry for every workspace aspect. For example, if you would like to add/remove/edit a coverage store, you would click on "data stores -> coverage". This opens a view with a list of all configured coverage stores. If you activated the Utah demo, you should see the following list:

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Configuring coverage stores

As we can see, the Utah demo workspace contains configurations for three coverage stores in total. Each configured coverage store (and every deegree workspace resource in general) has a corresponding XML file, which you can edit by clicking the "Edit" button:

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Editing a coverage store configuration

The details of the individual configuration formats are described in the later chapters. The built-in XML editor allows to edit the content of the configuration file, save the changes ("Save") or discard them ("Cancel"). Additionally, you may turn on syntax highlighting and look at the XML schema of the configuration ("Display Schema").

Deleting a workspace resource is straight-forward ("Delete"). You can also turn off a workspace resource temporarily ("Deactivate").

---------------------------------------------------------
Using the service console to add a new workspace resource
---------------------------------------------------------

In order to add a new workspace resource, use the "Create new" link. Note that you have always have to specify an identifier for every new resource. 

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Adding a new WPS with the identifier "mywps"

.. attention::  

-----------------------------------------------
Managing workspace resources on the file system
-----------------------------------------------

The service console allows to configure workspace resources without knowing their location. However, in some cases it can be very handy to edit the workspace configuration files directly.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Where is the deegree workspace directory?
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
File structure of a deegree workspace
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^




