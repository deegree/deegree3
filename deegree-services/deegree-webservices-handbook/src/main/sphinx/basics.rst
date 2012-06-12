.. _anchor-configuration-basics:

====================
Configuration basics
====================

In the previous chapter, you learned how to access and login to the deegree services console and how to download and activate workspaces. In order to be able to create your own workspace configuration, you need to have some understanding about the different workspace aspects (e.g. data access) and how to configure them. This chapter describes the overall structure of a deegree workspace, while the remaining chapters describe the configuration aspects in detail.

----------------------------
What is a deegree workspace?
----------------------------

A deegree workspace is a set of configuration files organized in a well-defined directory structure. The files in this directory structure define the different aspects of a deegree configuration:

.. figure:: images/workspace-overview.png
   :figwidth: 90%
   :width: 90%
   :target: _images/workspace-overview.png

   Configuration aspects of a deegree workspace

Each configuration aspect 

^^^^^^^^^^^^
Web services
^^^^^^^^^^^^

^^^^^^^^^^^
Data stores
^^^^^^^^^^^

^^^^^^^^^^
Map layers
^^^^^^^^^^

^^^^^^^^^^^^^^^^^^
Server connections
^^^^^^^^^^^^^^^^^^

^^^^^^^^^
Processes
^^^^^^^^^

The configuration files that control the available web services


* **web services**: OGC webservices
* **datastores**: Datastores for geospatial data (raster, vector, metadata, tiles)
* **layer configuration**: Map layers, themes and styles
* **processes**: Geospatial processes

For each aspect, there is an associated directory:





