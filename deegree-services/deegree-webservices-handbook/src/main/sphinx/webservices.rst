========================
Webservice configuration
========================

.. _anchor-configuration-wfs:

-----------------------------------
Managing web service configurations
-----------------------------------

-------------------------
Web Feature Service (WFS)
-------------------------

A deegree WFS configuration consists of the actual WFS configuration file and any number of feature store configuration files. Feature stores are used to access the actual feature data (which may be stored in several different backends, e.g. ESRI shapefiles or spatial databases such as PostGIS or Oracle Spatial). In transactional mode (WFS-T), feature stores are also used for modification of stored features:

.. figure:: images/workspace-wfs.png
   :figwidth: 90%
   :width: 90%
   :target: _images/workspace-wfs.png

   Workspace components involved in a deegree WFS configuration

.. tip::
  In order to fully understand deegree WFS configuration, you will have to read chapter :ref:`anchor-configuration-featurestore` as well.

The WFS config file format is defined by schema file http://schemas.deegree.org/services/wfs/3.1.0/wfs_configuration.xsd. The basic structure of a WFS config always looks like this:

.. topic:: WFS config example 1: Minimal configuration

   .. literalinclude:: xml/wfs_basic.xml
      :language: xml

The root element has to be ``deegreeWFS`` and the config attribute must be ``3.1.0``. The only mandatory element is:

* ``QueryCRS``: Default coordinate reference system for geometries

The above configuration will create a deegree WFS with the feature types from all configured feature stores in the workspace and use ``urn:ogc:def:crs:EPSG::4258`` as the default coordinate system for GML responses/inputs. It will support WFS 1.0.0, 1.1.0 and 2.0.0 requests. Transactions are activated (as far as they are supported by the active feature stores).

By default, a deegree WFS supports all implemented WFS protocol versions (1.0.0, 1.1.0 and 2.0.0). In order to control the supported WFS protocol versions, use configuration element ``SupportedVersions``:

* ``SupportedVersions``: Control offered WFS protocol versions

.. topic:: WFS config example 2: Restricting Protocol versions

   .. literalinclude:: xml/wfs_versions.xml
      :language: xml

This configuration restricts the offered protocol versions to 1.1.0 and 2.0.0. It will not support WFS 1.0.0 requests.

By default, a deegree WFS will use all active feature stores for serving feature types. In some cases, this may not be what you want, e.g. because you have two different WFS instances running in the same workspace, or you don't want all feature types used in the WMS for rendering to be available via the WFS. Use the ``FeatureStoreId`` to explicitly set the feature stores that this WFS should use:

* ``FeatureStoreId``: Set feature stores to use

.. topic:: WFS config example 3: Restricting feature stores

   .. literalinclude:: xml/wfs_featurestores.xml
      :language: xml

* ``EnableTransactions``: Boolean-valued element (``true`` or ``false``). Set to ``false`` in order to disable transactions (Insert, Update, Delete). Default is ``true``.
* ``QueryCRS``: Coordinate systems announced in the GetCapabilities response (WFS 1.1.0 and 2.0.0). Element can be used multiple times.
* ``QueryMaxFeatures``: Limits the maximum number of features that the WFS will return for a single ``GetFeature`` request. Default is 15000. Set to ``-1`` for unlimited.
* ``GMLFormat``:

^^^^^^^^^^^^^^^^^^^^
Controlling Metadata
^^^^^^^^^^^^^^^^^^^^
 
* ``MetadataURLTemplate``:
* ``FeatureTypeMetadata``:  
* ``ExtendedCapabilities``:  

.. topic:: WFS config example 3: Restricting Feature stores

   .. literalinclude:: xml/wfs_featurestores.xml
      :language: xml

^^^^^^^^^^^^^^^^^^^^^
Additional parameters
^^^^^^^^^^^^^^^^^^^^^

* ``DisableResponseBuffering``: Boolean-valued element (``true`` or ``false``). Set to ``false`` in order to enable buffered ``GetFeature`` responses. Default is ``true``.
* ``QueryCheckAreaOfUse``:

.. _anchor-configuration-wms:

---------------------
Web Map Service (WMS)
---------------------

In deegree terminology, a deegree WMS renders maps from data stored in feature and coverage stores. Available layers are configured in the WMS configuration file, while rendering of layers is controlled by style files. Supported style languages are StyledLayerDescriptor (SLD) and Symbology Encoding (SE).

.. figure:: images/workspace-wms.png
   :figwidth: 90%
   :width: 90%
   :target: _images/workspace-wms.png

   A deegree WMS configuration defines a hierarchy of layers

.. tip::
  In order to fully understand deegree WMS configuration, you will have to learn configuration of other workspace aspects as well. Chapter :ref:`anchor-configuration-renderstyles` describes the creation of layers and styling rules. Chapter :ref:`anchor-configuration-featurestore` describes the configuration of vector data access and chapter :ref:`anchor-configuration-coveragestore` describes the configuration of raster data access.

.. _anchor-configuration-csw:

-----------------------------------
Catalogue Service for the Web (CSW)
-----------------------------------

In deegree terminology, a deegree CSW provides access to metadata records stored in a metadata store. If the metadata store is transaction-capable, CSW transactions can be used to modify the stored records.

.. figure:: images/workspace-csw.png
   :figwidth: 90%
   :width: 90%
   :target: _images/workspace-csw.png

   Workspace components involved in a deegree CSW configuration

.. _anchor-configuration-wps:

----------------------------
Web Processing Service (WPS)
----------------------------

In deegree terminology, a deegree WPS allows the execution of (usually geospatial) processes from process providers.

.. figure:: images/workspace-wps.png
   :figwidth: 90%
   :width: 90%
   :target: _images/workspace-wps.png

   Workspace components involved in a deegree WPS configuration


