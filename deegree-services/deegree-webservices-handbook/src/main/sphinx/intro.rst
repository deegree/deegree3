============
Introduction
============

deegree webservices are implementations of the geospatial webservice specifications of the `Open Geospatial Consortium (OGC) <http://www.opengeospatial.org>`_ and the `INSPIRE Network Services <http://inspire.jrc.ec.europa.eu>`_. deegree webservices 3.2 includes the following services:

* `Web Feature Service (WFS) <http://www.opengeospatial.org/standards/wfs>`_: Provides access to raw geospatial data objects
* `Web Map Service (WMS) <http://www.opengeospatial.org/standards/wms>`_: Serves maps rendered from geospatial data
* `Web Map Tile Service (WMTS) <http://www.opengeospatial.org/standards/wmts>`_: Serves pre-rendered map tiles
* `Catalogue Service for the Web (CSW) <http://www.opengeospatial.org/standards/cat>`_: Performs searches for geospatial datasets and services
* `Web Processing Service (WPS) <http://www.opengeospatial.org/standards/wps>`_: Executes geospatial processes

With a single deegree webservices installation, you can set up one of the above services, all of them or even multiple services of the same type. The remainder of this chapter introduces some notable features of the different service implementations and provides learning trails for learning the configuration of each service.

------------------------------
Characteristics of deegree WFS
------------------------------

deegree WFS is an implementation of the `OGC Web Feature Service specification <http://www.opengeospatial.org/standards/wfs>`_. Notable features:

* Implements WFS standards 1.0.0, 1.1.0 and 2.0.0 [#f1]_
* Fully transactional (even for rich data models)
* Supports KVP, XML and SOAP requests
* GML 2/3.0/3.1/3.2 output/input
* Support for GetGmlObject requests and XLinks
* High performance and excellent scalability
* On-the-fly coordinate transformation
* Designed for rich data models from the bottom up
* Backends support flexible mapping of GML application schemas to relational models
* ISO 19107-compliant geometry model: Complex geometries (e.g. non-linear curves)
* Advanced filter expression support based on XPath 1.0
* Supports numerous backends, such as PostGIS, Oracle Spatial, MS SQL Server, Shapefiles or GML instance documents

.. tip::
  In order to learn the setup and configuration of a deegree-based WFS, we recommend to read chapters :ref:`anchor-installation` and :ref:`anchor-lightly` first. Check out :ref:`anchor-workspace-inspire` and :ref:`anchor-workspace-utah` for example deegree WFS configurations. Continue with :ref:`anchor-configuration-basics` and :ref:`anchor-configuration-wfs`.

------------------------------
Characteristics of deegree WMS
------------------------------

deegree WMS is an implementation of the `OGC Web Map Service specification <http://www.opengeospatial.org/standards/wms>`_. Notable features:

* Implements WMS standards 1.1.1 and 1.3.0 [#f2]_
* Extensive support for styling languages SLD/SE versions 1.0.0 and 1.1.0
* High performance and excellent scalability
* High quality rendering
* Scale dependent styling
* Support for SE removes the need for a lot of proprietary extensions
* Easy configuration of HTML and other output formats for GetFeatureInfo responses
* Uses stream-based data access, minimal memory footprint
* Nearly complete support for raster symbolizing as defined in SE (with some extensions)
* Complete support for TIME/ELEVATION and other dimensions for both feature and raster data
* Supports numerous backends, such as PostGIS, Oracle Spatial, Shapefiles or GML instance documents
* Can render rich data models directly

.. tip::
  In order to learn the setup and configuration of a deegree-based WMS, we recommend to read chapters :ref:`anchor-installation` and :ref:`anchor-lightly` first. Check out :ref:`anchor-workspace-utah` and :ref:`anchor-workspace-inspire` for example deegree WMS configurations. Continue with :ref:`anchor-configuration-basics` and :ref:`anchor-configuration-wms`.

-------------------------------
Characteristics of deegree WMTS
-------------------------------

deegree WMTS is an implementation of the `OGC Web Map Tile Service specification <http://www.opengeospatial.org/standards/wmts>`_. Notable features:

* Implements Basic WMTS standard 1.0.0 (KVP)
* High performance and excellent scalability
* Supports different backends, such as GeoTIFF, remote WMS or file system tile image hierarchies
* Supports on-the-fly caching (using EHCache)
* Supports GetFeatureInfo for remote WMS backends

.. tip::
  In order to learn the setup and configuration of a deegree-based WMTS, we recommend to read :ref:`anchor-installation` and :ref:`anchor-lightly` first. Continue with :ref:`anchor-configuration-basics` and :ref:`anchor-configuration-wmts`.

------------------------------
Characteristics of deegree CSW
------------------------------

deegree CSW is an implementation of the `OGC Catalogue Service specification <http://www.opengeospatial.org/standards/cat>`_. Notable features:

* Implements CSW standard 2.0.2
* Fully transactional
* Supports KVP, XML and SOAP requests
* High performance and excellent scalability
* ISO Metadata Application Profile 1.0.0
* Pluggable and modular dataaccess layer allows to add support for new APs and backends
* Modular inspector architecture allows to validate records to be inserted against various criteria
* Standard inspectors: schema validity, identifier integrity, INSPIRE requirements
* Handles all defined queryable properties (for Dublin Core as well as ISO profile) 
* Complex filter expressions

.. tip::
  In order to learn the setup and configuration of a deegree-based CSW, we recommend to read :ref:`anchor-installation` and :ref:`anchor-lightly` first. Check out :ref:`anchor-workspace-csw` for an example deegree CSW configuration. Continue with :ref:`anchor-configuration-basics` and :ref:`anchor-configuration-csw`.

------------------------------
Characteristics of deegree WPS
------------------------------

deegree WPS is an implementation of the `OGC Processing Service specification <http://www.opengeospatial.org/standards/wps>`_. Notable features:

* Implements WPS standard 1.0.0
* Supports KVP, XML and SOAP requests
* Pluggable process provider layer
* Easy-to-use API for implementing Java processes
* Supports all variants of input/output parameters: literal, bbox, complex (binary and xml)
* Streaming access for complex input/output parameters
* Processing of huge amounts of data with minimal memory footprint
* Supports storing of response documents/output parameters
* Supports input parameters given inline and by reference
* Supports RawDataOutput/ResponseDocument responses
* Supports asynchronous execution (with polling of process status)

.. tip::
  In order to learn the setup and configuration of a deegree-based WPS, we recommend to read:ref:`anchor-installation` and :ref:`anchor-lightly` first. Check out :ref:`anchor-workspace-wps` for an example deegree WPS configuration. Continue with :ref:`anchor-configuration-basics` and :ref:`anchor-configuration-wps`.

.. rubric:: Footnotes

.. [#f1] Passes OGC WFS CITE test suites (including all optional tests)
.. [#f2] Passes OGC WMS CITE test suites (including all optional tests)

