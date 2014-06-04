.. _anchor-configuration-gdal:

====
GDAL
====

The GDAL library (http://www.gdal.org/) arguably provides the most comprehensive support for geospatial raster formats. Using the GDAL tile store and layer implementations, any of these raster formats can be used to create deegree WMS or WMTS setups.

------------
Installation
------------

Before trying to set up GDAL-based deegree resources, the native GDAL library and required plugins have to be installed correctly and must be available via the standard dynamic linker path of your system.

.. note::
   Currently, the only supported GDAL library version is 1.10.1. Other versions may work as well, but this has not been tested.

In order to verify that deegree webservices can access the GDAL library, create a GDAL settings file (``gdal.xml``) in the active deegree workspace (see below). Restart deegree webservices and look for ``GDAL configuration` in the log file of the web container. If you see something like the following, you're ok.

.. code-block:: xml
  --------------------------------------------------------------------------------
  GDAL configuration.
  --------------------------------------------------------------------------------
  GDAL registered successfully.

However, if you see ``Registration of GDAL failed:[...]`` instead, deegree webservices can not access the GDAL library. In order to fix this, you may need to adapt the linker settings of your system. This is system-dependent and may involve changing environment variables (e.g. LD_LIBRARY_PATH on Linux).

-------------
GDAL settings
-------------

Before it is possible to set up any GDAL-based layers or tile stores, the GDAL library has to be installed correctly (see above) and a GDAL settings file with <name ``gdal.xml`` must be present in the main directory of the active deegree workspace. This will register the GDAL JNI adapter on workspace startup. 

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
Minimal GDAL settings example
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The only mandatory element is ``OpenDatasets``. A minimal valid configuration example looks like this:

.. topic:: GDAL settings (minimal example)

   .. literalinclude:: xml/gdal_minimal.xml
      :language: xml

This configuration will register the GDAL JNI adapter and will allow a maximum of five GDAL datasets to be kept open simultaneously.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
More complex GDAL settings example
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

A more complex example:

.. topic:: GDAL settings (more complex example)

   .. literalinclude:: xml/gdal_complex.xml
      :language: xml

This configuration will register the GDAL JNI adapter with the following settings:

* A maximum of ten GDAL datasets will be kept open simultaneously
* The parameter ``GDAL_CACHEMAX`` is set to 1000
* The parameter ``ECW_CACHE_MAXMEM`` is set to 419430400

.. hint::
   A list of general GDAL parameters is available at http://trac.osgeo.org/gdal/wiki/ConfigOptions. Some parameters (such as ``ECW_CACHE_MAXMEM``) are format specific and outlined on the respective pages in the GDAL documentation.

^^^^^^^^^^^^^^^^^^^^^
Configuration options
^^^^^^^^^^^^^^^^^^^^^

The configuration format for the GDAL settings file is defined by schema file http://schemas.deegree.org/commons/gdal/3.4.0/gdal.xsd. The following table lists all available configuration options (the complex ones contain nested options themselves). When specifiying them, their order must be respected.

.. table:: Options for the ``GDAL settings`` configuration file

+--------------+-------------+---------+----------------------------------------------------------+
| Option       | Cardinality | Value   | Description                                              |
+==============+=============+=========+==========================================================+
| OpenDatasets | 1..1        | Integer | Number cached file handles / simultaneous file accesses  |
+--------------+-------------+---------+----------------------------------------------------------+
| GDALOption   | 0..n        | Complex | Name / value of parameter to pass on to the GDAL library |
+--------------+-------------+---------+----------------------------------------------------------+

----------
GDAL Layer
----------

---------------
GDAL Tile Store
---------------
