========================
Webservice configuration
========================

This chapter describes the deegree webservices configuration files. You can access this configuration level by clicking on the ``web services`` link in the administration console. The configuration files have to be created or edited in the ``services/`` directory of the deegree workspace.

.. _anchor-configuration-wfs:

-------------------------
Web Feature Service (WFS)
-------------------------

A deegree WFS configuration consists of a WFS configuration file and any number of feature store configuration files. Feature stores provide access to the actual feature data (which may be stored in any of the supported backends, e.g. in shapefiles or spatial databases such as PostGIS or Oracle Spatial). In transactional mode (WFS-T), feature stores are also used for modification of stored features:

.. figure:: images/workspace-wfs.png
   :figwidth: 90%
   :width: 90%
   :target: _images/workspace-wfs.png

   Workspace components involved in a deegree WFS configuration

.. tip::
  In order to fully understand deegree WFS configuration, you will have to read chapter :ref:`anchor-configuration-featurestore` as well.

The deegree WFS config file format is defined by schema file http://schemas.deegree.org/services/wfs/3.1.0/wfs_configuration.xsd. The root element is ``deegreeWFS`` and the config attribute must be ``3.1.0``. The only mandatory option is ``QueryCRS``, therefore, a minimal WFS configuration example looks like this:

.. topic:: WFS config example 1: Minimal configuration

   .. literalinclude:: xml/wfs_basic.xml
      :language: xml

This will setup a deegree WFS with the feature types from all configured feature stores in the workspace and ``urn:ogc:def:crs:EPSG::4258`` as the coordinate system for returned GML geometries. A more complex configuration that restricts the offered WFS protocol versions, enables transactions, has multiple coordinate reference systems and limits GML output to 3.2 looks like this:

.. topic:: WFS config example 2: More complex configuration

   .. literalinclude:: xml/wfs_complex.xml
      :language: xml

The following table lists all available configuration options (the complex ones contain nested options themselves). When specifiying them, their order must be respected.

.. table:: Options for ``deegreeWFS``

+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| Option                   | Cardinality  | Value   | Description                                                                  |
+==========================+==============+=========+==============================================================================+
| SupportedVersions        | 0..1         | Complex | Limits active OGC protocol versions                                          |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| FeatureStoreId           | 0..n         | String  | Limits feature stores to use                                                 |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| EnableTransactions       | 0..1         | Boolean | Enables transactions (WFS-T operations)                                      |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| DisableResponseBuffering | 0..1         | Boolean | Controls response buffering                                                  |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| QueryCRS                 | 1..n         | String  | Announced CRS, first element is the default CRS                              |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| QueryMaxFeatures         | 0..1         | Integer | Limits maximum number of features returned by a GetFeature request           |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| QueryCheckAreaOfUse      | 0..1         | Boolean | Enforces checking of spatial query constraints against CRS area              |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| GMLFormat                | 0..n         | Complex | GML format configuration                                                     |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| CustomFormat             | 0..n         | Complex | Custom format configuration                                                  |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| MetadataURLTemplate      | 0..1         | String  | Template for generating URLs to feature type metadata                        |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| FeatureTypeMetadata      | 0..n         | Complex | Metadata for feature types reported in GetCapabilities response              |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| ExtendedCapabilities     | 0..n         | Complex | Extended Metadata reported in GetCapabilities response                       |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+

The remainder of this section describes these options and their sub-options in detail.

^^^^^^^^^^^^^
Basic options
^^^^^^^^^^^^^

* ``SupportedVersions``: By default, all implemented WFS protocol versions (1.0.0, 1.1.0 and 2.0.0) are activated. You can control offered WFS protocol versions using element ``SupportedVersions``. This element allows any of the child elements ``<Version>1.0.0</Version>``, ``<Version>1.1.0</Version>`` and ``<Version>2.0.0</Version>``.
* ``FeatureStoreId``: By default, all feature stores in your deegree workspace  will be used for serving feature types. In some cases, this may not be what you want, e.g. because you have two different WFS instances running, or you don't want all feature types used in your WMS for rendering to be available via your WFS. Use the ``FeatureStoreId`` option to explicitly set the feature stores that this WFS should use.
* ``DisableResponseBuffering``: By default, generated responses are directly written to the WFS client. This is usually fine and even a requirement for transferring large responses efficiently. The only drawback occurs when exceptions occur, after a partial response has already been transferred. In such case, the response will contain part payload and part exception report. By specifying ``false`` here, you can explicitly force buffering of the full response, before it is written to the client. Only if the full response was generated successfully, it will be transferred. Otherwise, only an exception report will be generated.
* ``EnableTransactions``: By default, WFS-T requests will be rejected. Setting this element to ``true`` will enable support for transactions in the WFS. Note that not all feature store implementations implement transactions, so you may encounter that transactions are rejected, even though you activated them in the WFS configuration.
* ``QueryCRS``: Coordinate reference systems for returned geometries. This element can be specified multiple times, and the WFS will announce all CRS in the GetCapabilities response (except for WFS 1.0.0 which does not officially support using multiple coordinate reference systems). The first element always specifies the default CRS (used when no CRS parameter is present in a request).
* ``QueryMaxFeatures``: By default, a maximum number of 15000 features will be returned for a single ``GetFeature`` request. Use this option to override this setting. A value of ``-1`` means unlimited.
* ``QueryCheckAreaOfUse``: By default, spatial query constraints are not checked with regard to the area of validity of the CRS. Set this option to ``true`` to enforce this check.

^^^^^^^^^^^^^^^^^^^^^^^^^^^
Adapting GML output formats
^^^^^^^^^^^^^^^^^^^^^^^^^^^

By default, a deegree WFS will offer GML 2, 3.0, 3.1, and 3.2 as output formats and announce those formats in the GetCapabilities responses (except for WFS 1.0.0, as this version of the standard has no means of announcing other formats than GML 2). The element for GetFeature responses is ``wfs:FeatureCollection``, as mandated by the WFS specification.

In some cases, you may want to alter aspects of the offered output formats. For example, if you want your WFS to serve a specific application schema (e.g. INSPIRE Data Themes), you should restrict the announced GML versions to the one used for the application schema. These and other output-format related aspects can be controlled by element ``GMLFormat``.

.. topic:: Example for WFS config option ``GMLFormat``

   .. literalinclude:: xml/wfs_gmlformat.xml
      :language: xml

The ``GMLFormat`` option has the following sub-options:

+------------------------------+--------------+---------+------------------------------------------------------------------------------+
| Option                       | Cardinality  | Value   | Description                                                                  |
+==============================+==============+=========+==============================================================================+
| @gmlVersion                  | 1..1         | String  | GML version (GML_2, GML_30, GML_31 or GML_32)                                |
+------------------------------+--------------+---------+------------------------------------------------------------------------------+
| MimeType                     | 1..n         | String  | Mime types associated with this format configuration                         |
+------------------------------+--------------+---------+------------------------------------------------------------------------------+
| GenerateBoundedByForFeatures | 0..1         | Boolean | Forces output of gml:boundedBy property for every feature                    |
+------------------------------+--------------+---------+------------------------------------------------------------------------------+
| GetFeatureResponse           | 0..1         | Complex | Options for controlling GetFeature responses                                 |
+------------------------------+--------------+---------+------------------------------------------------------------------------------+
| DecimalCoordinateFormatter/  | 0..1         | Complex | Controls the formatting of geometry coordinates                              |
| CustomCoordinateFormatter    |              |         |                                                                              |
+------------------------------+--------------+---------+------------------------------------------------------------------------------+

""""""""""""""""""""""""
Basic GML format options
""""""""""""""""""""""""

* ``@gmlVersion``: This attribute defines the GML version (GML_2, GML_30, GML_31 or GML_32)
* ``MimeType``: Mime types associated with this format configuration (and announced in GetCapabilities)
* ``GenerateBoundedByForFeatures``: By default, the ``gml:boundedBy`` property will only be exported for the member features if the feature store provides it. By setting this option to ``true``, the WFS will calculate the envelope and include it as a ``gml:boundedBy`` property. Please note that this setting does not affect the inclusion of the ``gml:boundedBy`` property for on the feature collection level (see DisableStreaming for that).

""""""""""""""""""""""""""""
GetFeature response settings
""""""""""""""""""""""""""""

Option ``GetFeatureResponse`` has the following sub-options:

+--------------------------+--------------+-----------+------------------------------------------------------------------------------+
| Option                   | Cardinality  | Value     | Description                                                                  |
+==========================+==============+===========+==============================================================================+
| ContainerElement         | 0..1         | QName     | Qualified root element name                                                  |
+--------------------------+--------------+-----------+------------------------------------------------------------------------------+
| FeatureMemberElement     | 0..1         | QName     | Qualified feature member element name                                        |
+--------------------------+--------------+-----------+------------------------------------------------------------------------------+
| AdditionalSchemaLocation | 0..1         | String    | Value to add to xsi:schemaLocation attribute                                 |
+--------------------------+--------------+-----------+------------------------------------------------------------------------------+
| DisableDynamicSchema     | 0..1         | Complex   |                                                                              |
+--------------------------+--------------+-----------+------------------------------------------------------------------------------+
| DisableStreaming         | 0..1         | Boolean   | Disables output streaming, include numberOfFeature information/gml:boundedBy |
+--------------------------+--------------+-----------+------------------------------------------------------------------------------+

* ``ContainerElement``: By default, the container element of a GetFeature response is ``wfs:FeatureCollection``. Using this option, you can specify an alternative element name. In order to bind the namespace prefix, use standard XML namespace mechanisms (xmlns attribute). This option is ignored for WFS 2.0.0.
* ``FeatureMemberElement``: By default, the member features are included in ``gml:featureMember`` (WFS 1.0.0/1.1.0) or ``wfs:member`` elements (WFS 2.0.0). Using this option, you can specify an alternative element name. In order to bind the namespace prefix, use standard XML namespace mechanisms (xmlns attribute). This option is ignored for WFS 2.0.0.
* ``AdditionalSchemaLocation``: By default, the ``xsi:schemaLocation`` attribute in a GetFeature response is auto-generated and refers to all schemas necessary for validation of the response. Using this option, you can add additional namespace/URL pairs for adding additional schemas. This may be required when you override the returned container or feature member elements in order to achieve schema-valid output.
* ``DisableDynamicSchema``: By default, the GML application schema referenced in the ``xsi:schemaLocation`` (and returned in DescribeFeature reponses) will be generated dynamically from the internal feature type representation. This allows generation of application schemas for the different GML versions and is fine for most simple feature models (e.g. feature types served from shapefiles or flat database tables). However, valid re-encoding of complex GML application schema (such as INSPIRE Data Themes) is technically not possible. In these cases, you will have to set this option to ``false``, so the WFS will return the original schema files used for configuring the feature store. If you want to make the xsi:schemaLocation refer to an external copy of your GML application schema files (instead of pointing back to the deegree WFS), use the optional attribute ``baseURL`` that this element provides.
* ``DisableStreaming``: By default, returned features are not collected in memory, but directly streamed from the backend (e.g. an SQL database) and individually encoded as GML. This enables the querying of huge numbers of features with only minimal memory footprint. However, by using this strategy, the number of features and their bounding box is not known when the WFS starts to write out the response. Therefore, this information is omitted from the response (which is perfectly valid according to WFS 1.0.0 and 1.1.0, and a change request for WFS 2.0.0 has been accepted). If you find that your WFS client has problems with the response, you may set this option to ``false``. Features will be collected in memory first and the generated response will include numberOfFeature information and gml:boundedBy for the collection. However, for huge response and heavy server load, this is not recommended as it introduces significant overhead and may result in out-of-memory errors.

"""""""""""""""""""""
Coordinate formatters
"""""""""""""""""""""

By default, GML geometries will be encoded using 6 decimal places for CRS with degree axes and 3 places for CRS with metric axes. In order to override this, two options are available:

* ``DecimalCoordinatesFormatter``: Empty element, attribute ``places`` specifies the number of decimal places.
* ``CustomCoordinateFormatter``: By specifiying this element, an implementation of Java interface ``org.deegree.geometry.io.CoordinateFormatter`` can be instantiated. Child element ``JavaClass`` contains the qualified name of the Java class (which must be on the classpath).


^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Adding custom output formats
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Using option element ``CustomFormat``, it possible to plug-in your own Java classes to generate the output for a specific mime type (e.g. a binary format)

+-----------+-------------+---------+------------------------------------------------------+
| Option    | Cardinality | Value   | Description                                          |
+===========+=============+=========+======================================================+
| MimeType  | 1..n        | String  | Mime types associated with this format configuration |
+-----------+-------------+---------+------------------------------------------------------+
| JavaClass | 1..1        | String  | Qualified Java class name                            |
+-----------+-------------+---------+------------------------------------------------------+
| Config    | 0..1        | Complex | Value to add to xsi:schemaLocation attribute         |
+-----------+-------------+---------+------------------------------------------------------+

* ``MimeType``: Mime types associated with this format configuration (and announced in GetCapabilities)
* ``JavaClass``: Therefore, an implementation of interface ``org.deegree.services.wfs.format.CustomFormat`` must be present on the classpath.
* ``Config``: 

^^^^^^^^^^^^^^^^^^^^
Controlling Metadata
^^^^^^^^^^^^^^^^^^^^

These settings affect the metadata returned in the GetCapabilities response.
 
* ``MetadataURLTemplate``:
* ``FeatureTypeMetadata``:  

* ``ExtendedCapabilities``: By default, the GetCapabilites response does not contain any extended capabilities elements in the OperationsMetadata section. The child elements of this option will be included in the OperationMetadata section to provide these extended capabilities, e.g. an ``inspire_ds:ExtendedCapabilities`` element. The attribute ``wfsVersions`` is as white-space separated list of WFS versions (1.0.0, 1.1.0 or 2.0.0) for which the extended capabilities shall be returned.

.. topic:: Example for ``ExtendedCapabilities`` option

   .. literalinclude:: xml/wfs_extendedcapabilities.xml
      :language: xml


.. _anchor-configuration-wms:

---------------------
Web Map Service (WMS)
---------------------

In deegree terminology, a deegree WMS renders maps from data stored in feature, coverage and tile stores. The WMS is configured using a layer structure, called a *theme*. A theme can be thought of as a collection of layers, organized in a tree structure. *What* the layers show is configured in a layer configuration, and *how* it is shown is configured in a style file. Supported style languages are StyledLayerDescriptor (SLD) and Symbology Encoding (SE).

.. figure:: images/workspace-wms.png
   :figwidth: 90%
   :width: 90%
   :target: _images/workspace-wms.png

   A deegree WMS configuration defines a hierarchy of layers

.. tip::
  In order to fully understand deegree WMS configuration, you will have to learn configuration of other workspace aspects as well. Chapter :ref:`anchor-configuration-renderstyles` describes the creation of layers and styling rules. Chapter :ref:`anchor-configuration-featurestore` describes the configuration of vector data access and chapter :ref:`anchor-configuration-coveragestore` describes the configuration of raster data access.

^^^^^^^^^^^^^^^^^^^^^^^^^^^
A word on layers and themes
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Readers familiar with the WMS protocol might be wondering why layers can not be configured directly in the WMS configuration file. Inspired by WMTS 1.0.0 we found the idea to separate structure and content very appealing. Thinking of a layer store that just offers a set of layers is an easy concept. Thinking of a theme as a structure that may contain layers at certain points also makes sense. But when thinking of WMS the terms begin clashing. We suggest to avoid confusion as much as possible by using the same name for each corresponding theme, layer and possibly even tile/feature/coverage data sources. We believe that once you work a little with the concept of themes, and seeing them exported as WMS layer trees, the concepts fit well enough so you can appreciate the clean cut.

^^^^^^^^^^^^^^^^^^^^^^
Configuration overview
^^^^^^^^^^^^^^^^^^^^^^

The configuration can be split up in six sections. Readers familiar with other deegree service configurations may recognize some similarities, but we'll describe the options anyway, because there may be subtle differences. A document template looks like this::

  <?xml version='1.0'?>
  <deegreeWMS xmlns='http://www.deegree.org/services/wms'>
    <!-- actual configuration goes here -->
  </deegreeWMS>

The following table shows what top level options are available.

.. table:: Options for ``deegreeWMS``

+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| Option                   | Cardinality  | Value   | Description                                                                  |
+==========================+==============+=========+==============================================================================+
| SupportedVersions        | 0..1         | Complex | Limits active OGC protocol versions                                          |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| MetadataStoreId          | 0..1         | String  | Configures a metadata store to check if metadata ids for layers exist        |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| MetadataURLTemplate      | 0..1         | String  | Template for generating URLs to feature type metadata                        |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| ServiceConfiguration     | 1            | Complex | Configures service content                                                   |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| FeatureInfoFormats       | 0..1         | Complex | Configures additional feature info output formats                            |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| ExtendedCapabilities     | 0..n         | Complex | Extended Metadata reported in GetCapabilities response                       |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+

^^^^^^^^^^^^^
Basic options
^^^^^^^^^^^^^

* ``SupportedVersions``: By default, all implemented WMS protocol versions (1.1.1 and 1.3.0) are activated. You can control offered WMS protocol versions using the element ``SupportedVersions``. This element allows any of the child elements ``<Version>1.1.1</Version>`` and ``<Version>1.3.0</Version>``.
* ``MetadataStoreId``: If set to a valid metadata store, the store is queried upon startup with all configured layer metadata set ids. If a metadata set does not exist in the metadata store, it will not be exported as metadata URL in the capabilties. This is a useful option if you want to automatically check for configuration errors/typos. By default, no checking is done.
* ``MetadataURLTemplate``: By default, no metadata URLs are generated for layers in the capabilities. You can set this option either to a unique URL, which will be exported as is, or to a template with a placeholder. In any case, a metadata URL will only be exported if the layer has a metadata set id set. A template looks like this: http://discovery.eu/csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;id=${metadataSetId}&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full. Please note that you'll need to escape the & symbols with &amp; as shown in the example. The ${metadataSetId} will be replaced with the metadata set id from each layer.

Here is a snippet for quick copy & paste::

  <SupportedVersions>
    <SupportedVersion>1.1.1</SupportedVersion>
  </SupportedVersions>
  <MetadataStoreId>mdstore</MetadataStoreId>
  <MetadataURLTemplate>http://discovery.eu/csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;id=${metadataSetId}&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full</MetadataURLTemplate>

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Service content configuration
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

You can configure the behaviour of layers using the ``DefaultLayerOptions`` element.

Have a look at the layer options and their values:

.. table:: Layer options

+------------------------+-------------------+-----------+---------------------------------------------------------------------------------------------------+
| Option                 | Cardinality       | String    | Description                                                                                       |
+========================+===================+===========+===================================================================================================+
| Antialiasing           | 0..1              | String    | Whether to antialias NONE, TEXT, IMAGE or BOTH, default is BOTH                                   |
+------------------------+-------------------+-----------+---------------------------------------------------------------------------------------------------+
| RenderingQuality       | 0..1              | String    | Whether to render LOW, NORMAL or HIGH quality, default is HIGH                                    |
+------------------------+-------------------+-----------+---------------------------------------------------------------------------------------------------+
| Interpolation          | 0..1              | String    | Whether to use BILINEAR, NEAREST_NEIGHBOUR or BICUBIC interpolation, default is NEAREST_NEIGHBOUR |
+------------------------+-------------------+-----------+---------------------------------------------------------------------------------------------------+
| MaxFeatures            | 0..1              | Integer   | Maximum number of features to render at once, default is 10000                                    |
+------------------------+-------------------+-----------+---------------------------------------------------------------------------------------------------+
| FeatureInfoRadius      | 0..1              | Integer   | Number of pixels to consider when doing GetFeatureInfo, default is 1                              |
+------------------------+-------------------+-----------+---------------------------------------------------------------------------------------------------+

You can configure the WMS to use one or more preconfigured themes. In WMS terms, each theme is mapped to a layer in the WMS capabilities. So if you use one theme, the WMS root layer corresponds to the root theme. If you use multiple themes, a synthetic root layer is exported in the capabilities, with one child layer corresponding to each root theme. The themes are configured using the ``ThemeId`` element.

Here is an example snippet of the content section::

  <ServiceConfiguration>

    <DefaultLayerOptions>
      <Antialiasing>NONE</Antialiasing>
    </DefaultLayerOptions>

    <ThemeId>mytheme</ThemeId>

  </ServiceConfiguration>

^^^^^^^^^^^^^^^^^^^^^^^^^^^
Custom feature info formats
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Any mime type can be configured to be available as response format for GetFeatureInfo requests, although the most commonly used is probably ``text/html``. There are two alternative ways of controlling how the output is generated (besides using the default HTML output). One involves a deegree specific templating mechanism, the other involves writing an XSLT script. The deegree specific mechanism has the advantage of being considerably less verbose, making common use cases very easy, while the XSLT approach gives you all the freedom.

This is how the configuration section looks like for configuring a deegree templating based format::

  <FeatureInfoFormats>
    <GetFeatureInfoFormat>
      <File>../customformat.gfi</File>
      <Format>text/html</Format>
    </GetFeatureInfoFormat>
  </FeatureInfoFormats>

The configuration for the XSLT approach looks like this::

  <FeatureInfoFormats>
    <GetFeatureInfoFormat>
      <XSLTFile>../customformat.xsl</XSLTFile>
      <Format>text/html</Format>
    </GetFeatureInfoFormat>
  </FeatureInfoFormats>

Of course it is possible to define as many custom formats as you want, as long as you use a different mime type for each (just duplicate the ``GetFeatureInfoFormat`` element). If you use one of the default formats, the default output will be overridden with your configuration.

In order to write your XSLT script, it is best to develop it against a GetFeatureInfo output in GML 2 format, that's the input the XSLT script will get. It is not possible yet to receive a different format for XSLT input.

If you want to learn more about the templating format, http://wiki.deegree.org/deegreeWiki/deegree3/WMSConfiguration#GetFeatureInfoHTML is a starting point. (TODO describe properly)

^^^^^^^^^^^^^^^^^^^^^
Extended capabilities
^^^^^^^^^^^^^^^^^^^^^

Important for applications like INSPIRE, it is often desirable to include predefined blocks of XML in the extended capabilities section of the WMS' capabilities output. This can be achieved simply by adding these blocks to the extended capabilities element of the configuration::

  <ExtendedCapabilities>
    <MyCustomOutput xmlns="http://www.custom.org/output">
      ...
    </MyCustomOutput>
  </ExtendedCapabilities>

^^^^^^^^^^^^^^^^^^^^^^^^^^
Vendor specific parameters
^^^^^^^^^^^^^^^^^^^^^^^^^^

The deegree WMS supports a number of vendor specific parameters. Some parameters are supported on a per layer basis while some are applied to the whole request. Most of the parameters correspond to the layer options above.

The parameters which are supported on a per layer basis can be used to set an option globally, eg. ...&REQUEST=GetMap&ANTIALIAS=BOTH&..., or for each layer separately (using a comma separated list): ...&REQUEST=GetMap&ANTIALIAS=BOTH,TEXT,NONE&LAYERS=layer1,layer2,layer3&... Most of the layer options have a corresponding parameter with a similar name: ANTIALIAS, INTERPOLATION, QUALITY and MAX_FEATURES. The feature info radius can currently not be set dynamically.

The PIXELSIZE parameter can be used to dynamically adjust the resolution of the resulting image. The default is the WMS default of 0.28 mm. So to achieve a double resolution, you can double the WIDTH/HEIGHT parameter values and set the PIXELSIZE parameter to 0.14.

Using the QUERYBOXSIZE parameter you can include features when rendering that would normally not intersect the envelope specified in the BBOX parameter. That can be useful if you have labels at point symbols out of the envelope which would be rendered partly inside the map. Normal GetMap behaviour will exclude such a label. With the QUERYBOXSIZE parameter you can specify a factor by which to enlarge the original bounding box, which is used solely for querying the data store (the actual extent returned will not be changed!). Use values like 1.1 to enlarge the envelope by 5% in each direction (this would be 10% in total).

.. _anchor-configuration-wmts:

---------------------------
Web Map Tile Service (WMTS)
---------------------------

In deegree terminology, a deegree WMTS provides access to tiles stored in tile stores. The WMTS is configured using so-called *themes*. A theme can be thought of as a collection of layers, organized in a tree structure.

.. figure:: images/workspace-wmts.png
   :figwidth: 90%
   :width: 90%
   :target: _images/workspace-wmts.png

   Workspace components involved in a deegree WMTS configuration

.. tip::
  In order to fully understand deegree WMTS configuration, you will have to learn configuration of other workspace aspects as well. Chapter :ref:`anchor-configuration-tilestore` describes the configuration of tile data access. Chapter :ref:`anchor-configuration-layers` describes the configuration of layers (only tile layers are usable for the WMTS). Chapter :ref:`anchor-configuration-themes` describes how to create a theme from layers.

The deegree WMTS config file format is defined by schema file http://schemas.deegree.org/services/wmts/3.2.0/wmts.xsd. The root element is ``deegreeWMTS`` and the config attribute must be ``3.2.0``. The only mandatory section is ``ServiceConfiguration`` (which can be empty), therefore, a minimal WMTS configuration example looks like this:

.. topic:: WMTS config example 1: Minimal configuration

   .. literalinclude:: xml/wmts_basic.xml
      :language: xml

This will setup a deegree WMTS with all configured themes in the workspace. A more complex configuration that restricts the offered themes looks like this:

.. topic:: WMTS config example 2: More complex configuration

   .. literalinclude:: xml/wmts_complex.xml
      :language: xml

The following table lists all available configuration options. When specifiying them, their order must be respected.

.. table:: Options for ``deegreeWMTS``

+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| Option                   | Cardinality  | Value   | Description                                                                  |
+==========================+==============+=========+==============================================================================+
| MetadataURLTemplate      | 0..1         | String  | Template for generating URLs to layer metadata                               |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| ThemeId                  | 0..n         | String  | Limits themes to use                                                         |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+

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

----------------------
Metadata configuration
----------------------

This section describes the configuration for the different types of metadata that a service reports in the ``GetCapabilities`` response. These options don't affect the data that the service offers or the behaviour of the service. It merely changes the descriptive metadata that the service reports.

^^^^^^^^^^^^^^^^
Service metadata
^^^^^^^^^^^^^^^^

In order to configure the service identification and service provider information that web services return in ``GetCapabilities`` responses, you have two options:

* Create a global ``metadata.xml`` file in the ``services`` directory of the workspace (options apply for all configured web services)
* Create an instance specific ``xyz_metadata.xml`` file (options apply only for web service with configuration file name ``xyz.xml``)

If both files are present, the instance specific file takes precedence. The metadata config file format is defined by schema file http://schemas.deegree.org/services/metadata/3.0.0/metadata.xsd. The root element is ``deegreeServicesMetadata`` and the config attribute must be ``3.0.0``.

.. topic:: Example for ``metadata.xml``

   .. literalinclude:: xml/service_metadata.xml
      :language: xml

^^^^^^^^^^^^^^^^
Dataset metadata
^^^^^^^^^^^^^^^^

This type of metadata is attached to the datasets that a service offers (e.g. layers for the WMS or feature types for the WFS). Please have a look at the service specific section for configuring this type of metadata.

^^^^^^^^^^^^^^^^^^^^^
Extended capabilities
^^^^^^^^^^^^^^^^^^^^^

Extended capabilities are generic metadata sections below the ``OperationsMetadata`` element in the ``GetCapabilities`` response. It is not defined by the OGC specifications, but by extensions, such as the INSPIRE service specifications. deegree treats this section as a generic XML element and does not validate it. Please have a look at the service specific section for configuring this type of metadata. 
