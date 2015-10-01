.. _anchor-configuration-service:

============
Web services
============

This chapter describes the configuration of web service resources. You can access this configuration level by clicking the **web services** link in the administration console. The corresponding configuration files are located in the ``services/`` subdirectory of the active deegree workspace directory.

.. figure:: images/workspace-overview-services.png
   :figwidth: 80%
   :width: 80%
   :target: _images/workspace-overview-services.png

   Web services are the top-level resources of the deegree workspace

.. tip::
  The identifier of a web service resource has a special purpose. If your deegree instance can be reached at ``http://localhost:8080/deegree-webservices``, the common endpoint for connecting to your services is ``http://localhost:8080/deegree-webservices/services``. However, if you define multiple service resources of the same type in your workspace (e.g. two WMS instances with identifiers ``wms1`` and ``wms2``), you cannot use the common URL, as deegree cannot determine the targeted WMS instance from the request. In this case, simply append the resource identifier to the common endpoint URL (e.g. ``http://localhost:8080/deegree-webservices/services/wms2``) to choose the service resource that you want to connect to explicitly.

.. _anchor-configuration-wfs:

-------------------------
Web Feature Service (WFS)
-------------------------

A deegree WFS setup consists of a WFS configuration file and any number of feature store configuration files. Feature stores provide access to the actual data (which may be stored in any of the supported backends, e.g. in shapefiles or spatial databases such as PostGIS or Oracle Spatial). In transactional mode (WFS-T), feature stores are also used for modification of stored features:

.. figure:: images/workspace-wfs.png
   :figwidth: 80%
   :width: 80%
   :target: _images/workspace-wfs.png

   A WFS resource is connected to any number of feature store resources

^^^^^^^^^^^^^^^
Minimal example
^^^^^^^^^^^^^^^

The only mandatory option is ``QueryCRS``, therefore, a minimal WFS configuration example looks like this:

.. topic:: WFS config example 1: Minimal configuration

   .. literalinclude:: xml/wfs_basic.xml
      :language: xml

This will create a deegree WFS with the feature types from all configured feature stores in the workspace and ``urn:ogc:def:crs:EPSG::4258`` as coordinate system for returned GML geometries.

^^^^^^^^^^^^^^^^^^^^
More complex example 
^^^^^^^^^^^^^^^^^^^^

A more complex configuration example looks like this:

.. topic:: WFS config example 2: More complex configuration

   .. literalinclude:: xml/wfs_complex.xml
      :language: xml

^^^^^^^^^^^^^^^^^^^^^^
Configuration overview
^^^^^^^^^^^^^^^^^^^^^^

The deegree WFS config file format is defined by schema file http://schemas.deegree.org/services/wfs/3.4.0/wfs_configuration.xsd. The root element is ``deegreeWFS`` and the config attribute must be ``3.4.0``. The following table lists all available configuration options (complex ones contain nested options themselves). When specifiying them, their order must be respected.

.. table:: Options for ``deegreeWFS``

+-------------------------+-------------+---------+------------------------------------------------------------------+
| Option                  | Cardinality | Value   | Description                                                      |
+=========================+=============+=========+==================================================================+
| SupportedVersions       | 0..1        | Complex | Activated OGC protocol versions, default: all                    |
+-------------------------+-------------+---------+------------------------------------------------------------------+
| FeatureStoreId          | 0..n        | String  | Feature stores to attach, default: all                           |
+-------------------------+-------------+---------+------------------------------------------------------------------+
| EnableTransactions      | 0..1        | Complex | Enable transactions (WFS-T operations), default: false           |
+-------------------------+-------------+---------+------------------------------------------------------------------+
| EnableResponseBuffering | 0..1        | Boolean | Enable response buffering (expensive), default: false            |
+-------------------------+-------------+---------+------------------------------------------------------------------+
| QueryCRS                | 1..n        | String  | Announced CRS, first element is the default CRS                  |
+-------------------------+-------------+---------+------------------------------------------------------------------+
| QueryMaxFeatures        | 0..1        | Integer | Limit of features returned in a response, default: 15000         |
+-------------------------+-------------+---------+------------------------------------------------------------------+
| QueryCheckAreaOfUse     | 0..1        | Boolean | Check spatial query constraints against CRS area, default: false |
+-------------------------+-------------+---------+------------------------------------------------------------------+
| StoredQuery             | 0..n        | String  | File name of StoredQueryDefinition                               |
+-------------------------+-------------+---------+------------------------------------------------------------------+
| GMLFormat               | 0..n        | Complex | GML format configuration                                         |
+-------------------------+-------------+---------+------------------------------------------------------------------+
| CustomFormat            | 0..n        | Complex | Custom format configuration                                      |
+-------------------------+-------------+---------+------------------------------------------------------------------+

The remainining sections describe these options and their sub-options in detail.

^^^^^^^^^^^^^^^
General options
^^^^^^^^^^^^^^^

* ``SupportedVersions``: By default, all implemented WFS protocol versions (1.0.0, 1.1.0 and 2.0.0) will be activated. You can control offered WFS protocol versions using element ``SupportedVersions``. This element allows any combination of the child elements ``<Version>1.0.0</Version>``, ``<Version>1.1.0</Version>`` and ``<Version>2.0.0</Version>``.
* ``FeatureStoreId``: By default, all feature stores in your deegree workspace  will be used for serving feature types. In some cases, this may not be what you want, e.g. because you have two different WFS instances running, or you don't want all feature types used in your WMS for rendering to be available via your WFS. Use the ``FeatureStoreId`` option to explicitly set the feature stores that this WFS should use.
* ``EnableResponseBuffering``: By default, WFS responses are directly streamed to the client. This is very much recommended and even a requirement for transferring large responses efficiently. The only drawback happens if exceptions occur, after a partial response has already been transferred. In this case, the client will receive part payload and part exception report. By specifying ``false`` here, you can explicitly force buffering of the full response, before it is written to the client. Only if the full response could be generated successfully, it will be transferred. If an exception happens at any time the buffer will be discarded, and an exception report will be sent to the client. Buffering is performed in memory, but switches to a temp file in case the buffer grows bigger than 1 MiB.
* ``QueryCRS``: Coordinate reference systems for returned geometries. This element can be specified multiple times, and the WFS will announce all CRS in the GetCapabilities response (except for WFS 1.0.0 which does not officially support using multiple coordinate reference systems). The first element always specifies the default CRS (used when no CRS parameter is present in a request).
* ``QueryMaxFeatures``: By default, a maximum number of 15000 features will be returned for a single ``GetFeature`` request. Use this option to override this setting. A value of ``-1`` means unlimited.
* ``QueryCheckAreaOfUse``: By default, spatial query constraints are not checked with regard to the area of validity of the CRS. Set this option to ``true`` to enforce this check.

^^^^^^^^^^^^
Transactions
^^^^^^^^^^^^

By default, WFS-T requests will be rejected. Setting the ``EnableTransactions`` option to ``true`` will enable transaction support. This option has the optional attribute ``idGenMode`` which controls how ids of inserted features (the values in the gml:id attribute) are treated. There are three id generation modes available:

* **UseExisting**: The original gml:id values from the input are stored. This may lead to errors if the provided ids are already in use.
* **GenerateNew** (default): New and unique ids are generated. References in the input GML (xlink:href) that point to a feature with an reassigned id are fixed as well, so reference consistency is maintained.
* **ReplaceDuplicate**: The WFS will try to use the original gml:id values that have been provided in the input. In case a certain identifier already exists in the backend, a new and unique identifier will be generated. References in the input GML (xlink:href) that point to a feature with an reassigned id are fixed as well, so reference consistency is maintained.

.. hint::
  Currently, transactions can only be enabled if your WFS is attached to a single feature store.

.. hint::
  Not every feature store implementation supports transactions, so you may encounter that transactions are rejected, even though you activated them in the WFS configuration.

.. hint::
  The details of the id generation depend on the feature store implementation/configuration.

.. hint::
   In a WFS 1.1.0 insert, the id generation mode can be overridden by attribute *idGenMode* of the ``Insert`` element. WFS 1.0.0 and WFS 2.0.0 don't support to specify the id generation mode on a request basis.


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
| GeometryLinearization        | 0..1         | Complex | Activates/controls the linearization of exported geometries                  |
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
| ContainerElement         | 0..1         | QName     | Qualified root element name, default: wfs:FeatureCollection                  |
+--------------------------+--------------+-----------+------------------------------------------------------------------------------+
| FeatureMemberElement     | 0..1         | QName     | Qualified feature member element name, default: gml:featureMember            |
+--------------------------+--------------+-----------+------------------------------------------------------------------------------+
| AdditionalSchemaLocation | 0..1         | String    | Added to xsi:schemaLocation attribute of wfs:FeatureCollection               |
+--------------------------+--------------+-----------+------------------------------------------------------------------------------+
| DisableDynamicSchema     | 0..1         | Complex   | Controls DescribeFeatureType strategy, default: regenerate schema            |
+--------------------------+--------------+-----------+------------------------------------------------------------------------------+
| DisableStreaming         | 0..1         | Boolean   | Disables output streaming, include numberOfFeature information/gml:boundedBy |
+--------------------------+--------------+-----------+------------------------------------------------------------------------------+
| PrebindNamespace         | 0..n         | Complex   | Pre-bind namespaces in the root element                                      |
+--------------------------+--------------+-----------+------------------------------------------------------------------------------+

* ``ContainerElement``: By default, the container element of a GetFeature response is ``wfs:FeatureCollection``. Using this option, you can specify an alternative element name. In order to bind the namespace prefix, use standard XML namespace mechanisms (xmlns attribute). This option is ignored for WFS 2.0.0.
* ``FeatureMemberElement``: By default, the member features are included in ``gml:featureMember`` (WFS 1.0.0/1.1.0) or ``wfs:member`` elements (WFS 2.0.0). Using this option, you can specify an alternative element name. In order to bind the namespace prefix, use standard XML namespace mechanisms (xmlns attribute). This option is ignored for WFS 2.0.0.
* ``AdditionalSchemaLocation``: By default, the ``xsi:schemaLocation`` attribute in a GetFeature response is auto-generated and refers to all schemas necessary for validation of the response. Using this option, you can add additional namespace/URL pairs for adding additional schemas. This may be required when you override the returned container or feature member elements in order to achieve schema-valid output.
* ``DisableDynamicSchema``: By default, the GML application schema returned in DescribeFeatureType reponses (and referenced in the ``xsi:schemaLocation`` of query responses) will be generated dynamically from the internal feature type representation. This allows generation of application schemas for different GML versions and is fine for simple feature models (e.g. feature types served from shapefiles or flat database tables). However, valid re-encoding of complex GML application schema (such as INSPIRE Data Themes) is technically not feasible. In these cases, you will have to set this option to ``false``, so the WFS will produce a response that refers to the original schema files used for configuring the feature store. If you want the references to point to an external copy of your GML application schema files (instead of pointing back to the deegree WFS), use the optional attribute ``baseURL`` that this element provides.
* ``DisableStreaming``: By default, returned features are not collected in memory, but directly streamed from the backend (e.g. an SQL database) and individually encoded as GML. This enables the querying of huge numbers of features with only minimal memory footprint. However, by using this strategy, the number of features and their bounding box is not known when the WFS starts to write out the response. Therefore, this information is omitted from the response (which is perfectly valid according to WFS 1.0.0 and 1.1.0, and a change request for WFS 2.0.0 has been accepted). If you find that your WFS client has problems with the response, you may set this option to ``false``. Features will be collected in memory first and the generated response will include numberOfFeature information and gml:boundedBy for the collection. However, for huge response and heavy server load, this is not recommended as it introduces significant overhead and may result in out-of-memory errors.
* ``PrebindNamespace``: By default, XML namespaces are bound when they are needed. This will result in valid output, but may lead to the same namespace being bound again and again in different parts of the response document. Using this option, namespaces can be bound in the root element, so they are defined for the full scope of the response document and do not need re-definition at several positions in the document. This option has the required attributes ``prefix`` and ``uri``.

"""""""""""""""""""""
Coordinate formatters
"""""""""""""""""""""

By default, GML geometries will be encoded using 6 decimal places for CRS with degree axes and 3 places for CRS with metric axes. In order to override this, two options are available:

* ``DecimalCoordinatesFormatter``: Empty element, attribute ``places`` specifies the number of decimal places.
* ``CustomCoordinateFormatter``: By specifiying this element, an implementation of Java interface ``org.deegree.geometry.io.CoordinateFormatter`` can be instantiated. Child element ``JavaClass`` contains the qualified name of the Java class (which must be on the classpath).

""""""""""""""""""""""
Geometry linearization
""""""""""""""""""""""

Some feature stores (e.g. the SQL feature store when connected to an Oracle Spatial database) can deliver non-linear geometries (e.g. arcs). Here's an example for the GML 3.1.1 encoding of such a geometry as it would be returned by the WFS:

.. topic:: Example for a non-linear GML geometry

   .. literalinclude:: xml/wfs_linearization_curve1.xml
      :language: xml

This is perfectly valid GML, but there are two reasons why you may not want your WFS to return non-linear GML geometries:

* There's no encoding for non-linear GML geometries in GML version 2
* Currently available WFS clients (e.g. QGIS, uDig, ...) cannot cope with them

Option ``GeometryLinearization`` will ensure that GML responses will only contain linear geometries. Curves with non-linear segments and surfaces with non-linear boundary segments will be converted before they are encoded to GML. Here's an example usage of this GML format option:

.. topic:: Example config snippet for activating geometry linearization

   .. literalinclude:: xml/wfs_linearization_example.xml
      :language: xml

``GeometryLinearization`` has a single mandatory option ``Accuracy``. It defines the numerical accuracy of the linear approximation in units of the coordinate reference system used by the feature store. If the coordinate reference system is based on meters, a value of 0.1 will ensure that the maximum error between the original and the linearized geometry does not exceed 10 centimeters.

Here's an example of a linearized version of the example geometry as it would be generated by the WFS:

.. topic:: Example for linearized GML output

   .. literalinclude:: xml/wfs_linearization_curve2.xml
      :language: xml

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

^^^^^^^^^^^^^^
Stored queries
^^^^^^^^^^^^^^

Besides standard ('ad hoc') queries, WFS 2.0.0 introduces so-called stored queries. When WFS 2.0.0 support is activated, your WFS will automatically support the well-known stored query ``urn:ogc:def:storedQuery:OGC-WFS::GetFeatureById`` (defined in the WFS 2.0.0 specification). It can be used to query a feature instance by specifying it's gml:id (similar to GetGmlObject requests in WFS 1.1.0). In order to define custom stored queries, use the ``StoredQuery`` element to specify the file name of a StoredQueryDefinition file. The given file name (can be relative) must point to a valid WFS 2.0.0 StoredQueryDefinition file. Here's an example:

.. topic:: Example for a WFS 2.0.0 StoredQueryDefinition file

   .. literalinclude:: xml/wfs_storedquerydefinition.xml
      :language: xml

This example is actually usable if your WFS is set up to serve the ad:Address feature type from INSPIRE Annex I. It defines the stored query ``urn:x-inspire:storedQuery:GetAddressesForStreet`` for retrieving ad:Address features that are located in the specified street. The street name is passed using parameter ``streetName``. If your WFS instance can be reached at ``http://localhost:8080/services``, you could use the request ``http://localhost:8080/services?request=GetFeature&storedquery_id=urn:x-inspire:storedQuery:GetAddressesForStreet&streetName=Madame%20Curiestraat`` to fetch the ad:Address features in street Madame Curiestraat.

The attribute returnFeatureTypes of QueryExpressionText can be left empty. If this is the case, the element will be filled with all feature types served by the WFS when executing a DescribeStoredQueries request. The same applies for the value ${deegreewfs:ServedFeatureTypes}. If a value is set for returnFeatureTypes, the user is responsible to configure it as expected: Usually values of the typeNames of the Query-Elements should be used. An exception is thrown as DescribeStoredQueries response, if the configured feature type is not served by the WFS.

.. tip::
  deegree WFS supports the execution of stored queries using ``GetFeature`` and ``GetPropertyValue`` requests. It also implements the ``ListStoredQueries`` and the ``DescribeStoredQueries`` operations. However, there is no support for ``CreateStoredQuery`` and ``DropStoredQuery`` at the moment.

.. _anchor-configuration-wms:

---------------------
Web Map Service (WMS)
---------------------

In deegree terminology, a deegree WMS renders maps from data stored in feature, coverage and tile stores. The WMS is configured using a layer structure, called a *theme*. A theme can be thought of as a collection of layers, organized in a tree structure. *What* the layers show is configured in a layer configuration, and *how* it is shown is configured in a style file. Supported style languages are StyledLayerDescriptor (SLD) and Symbology Encoding (SE).

.. figure:: images/workspace-wms.png
   :figwidth: 80%
   :width: 80%
   :target: _images/workspace-wms.png

   A WMS resource is connected to exactly one theme resource

.. tip::
  In order to fully understand deegree WMS configuration, you will have to learn configuration of other workspace aspects as well. Chapter :ref:`anchor-configuration-renderstyles` describes the creation of layers and styling rules. Chapter :ref:`anchor-configuration-featurestore` describes the configuration of vector data access and chapter :ref:`anchor-configuration-coveragestore` describes the configuration of raster data access.

^^^^^^^^^^^^^^^^^^^^^^^^^^^
A word on layers and themes
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Readers familiar with the WMS protocol might be wondering why layers can not be configured directly in the WMS configuration file. Inspired by WMTS 1.0.0 we found the idea to separate structure and content very appealing. Thinking of a layer store that just offers a set of layers is an easy concept. Thinking of a theme as a structure that may contain layers at certain points also makes sense. But when thinking of WMS the terms begin clashing. We suggest to avoid confusion as much as possible by using the same name for each corresponding theme, layer and possibly even tile/feature/coverage data sources. We believe that once you work a little with the concept of themes, and seeing them exported as WMS layer trees, the concepts fit well enough so you can appreciate the clean cut.

^^^^^^^^^^^^^^^^^^^^^^
Configuration overview
^^^^^^^^^^^^^^^^^^^^^^

The configuration can be split up in six sections. Readers familiar with other deegree service configurations may recognize some similarities, but we'll describe the options anyway, because there may be subtle differences. A document template looks like this:

.. code-block:: xml

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
| GetMapFormats            | 0..1         | Complex | Configures additional image output formats                                   |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| ExtendedCapabilities     | 0..n         | Complex | Extended Metadata reported in GetCapabilities response                       |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| LayerLimit               | 0..1         | Integer | Maximum number of layers in a GetMap request, default: unlimited             |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| MaxWidth                 | 0..1         | Integer | Maximum width in a GetMap request, default: unlimited                        |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| MaxHeight                | 0..1         | Integer | Maximum height in a GetMap request, default: unlimited                       |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+


^^^^^^^^^^^^^
Basic options
^^^^^^^^^^^^^

* ``SupportedVersions``: By default, all implemented WMS protocol versions (1.1.1 and 1.3.0) are activated. You can control offered WMS protocol versions using the element ``SupportedVersions``. This element allows any of the child elements ``<Version>1.1.1</Version>`` and ``<Version>1.3.0</Version>``.
* ``MetadataStoreId``: If set to a valid metadata store, the store is queried upon startup with all configured layer metadata set ids. If a metadata set does not exist in the metadata store, it will not be exported as metadata URL in the capabilties. This is a useful option if you want to automatically check for configuration errors/typos. By default, no checking is done.
* ``MetadataURLTemplate``: By default, no metadata URLs are generated for layers in the capabilities. You can set this option either to a unique URL, which will be exported as is, or to a template with a placeholder. In any case, a metadata URL will only be exported if the layer has a metadata set id set. A template looks like this: http://discovery.eu/csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;id=${metadataSetId}&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full. Please note that you'll need to escape the & symbols with &amp; as shown in the example. The ${metadataSetId} will be replaced with the metadata set id from each layer.

Here is a snippet for quick copy & paste:

.. code-block:: xml

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

Here is an example snippet of the content section:

.. code-block:: xml

  <ServiceConfiguration>

    <DefaultLayerOptions>
      <Antialiasing>NONE</Antialiasing>
    </DefaultLayerOptions>

    <ThemeId>mytheme</ThemeId>

  </ServiceConfiguration>

.. _anchor-featureinfo-configuration:

^^^^^^^^^^^^^^^^^^^^^^^^^^^
Custom feature info formats
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Any mime type can be configured to be available as response format for GetFeatureInfo requests, although the most commonly used is probably ``text/html``. There are two alternative ways of controlling how the output is generated (besides using the default HTML output). One involves a deegree specific templating mechanism, the other involves writing an XSLT script. The deegree specific mechanism has the advantage of being considerably less verbose, making common use cases very easy, while the XSLT approach gives you all the freedom.

This is how the configuration section looks like for configuring a deegree templating based format:

.. code-block:: xml

  <FeatureInfoFormats>
    <GetFeatureInfoFormat>
      <File>../customformat.gfi</File>
      <Format>text/html</Format>
    </GetFeatureInfoFormat>
  </FeatureInfoFormats>

The configuration for the XSLT approach looks like this:

.. code-block:: xml

  <FeatureInfoFormats>
    <GetFeatureInfoFormat>
      <XSLTFile gmlVersion="GML_32">../customformat.xsl</XSLTFile>
      <Format>text/html</Format>
    </GetFeatureInfoFormat>
  </FeatureInfoFormats>

Of course it is possible to define as many custom formats as you want, as long as you use a different mime type for each (just duplicate the ``GetFeatureInfoFormat`` element). If you use one of the default formats, the default output will be overridden with your configuration.

In order to write your XSLT script, you'll need to develop it against a specific GML version (namespaces between GML versions may differ, GML output itself will differ). The default is GML 3.2, you can override it by specifying the ``gmlVersion`` attribute on the ``XSLTFile`` element. Valid GML version strings are ``GML_2``, ``GML_30``, ``GML_31`` and ``GML_32``.

If you want to learn more about the templating format, read the following sections.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
FeatureInfo templating format
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The templating format can be used to create text based output formats for featureinfo output. It uses a number of definitions, rules and special constructs to replace content with other content based on feature and property values. Please note that you should make sure your file is UTF-8 encoded if you're using umlauts.

""""""""""""""""""""
Introduction/Example
""""""""""""""""""""

This section gives a quick overview how the format works and demonstrates the development of a small sample HTML output.

On top level, you can have a number of *template definitions*. A template always has a name, and there always needs to be a template named ``start`` (yes, it's the one we start with).

A simple valid templating file that does not actually depend on the features coming in looks like this:

.. code-block:: xml

  <?template start>
  <html>
  <body>
    <p>Hello</p>
  </body>
  </html>

A featureinfo request will now always yield the body of this template. In order to use the features coming in, you need to define other templates, and call them from a template. So let's add another template, and call it from the ``start`` template:

.. code-block:: xml

  <?template start>
  <html>
  <body>
  <ul>
  <?feature *:myfeaturetemplate>
  </ul>
  </body>
  </html>

  <?template myfeaturetemplate>
  <li>I have a feature</li>

What happens now is that first the body of the ``start`` template is being output. In that output, the ``<?feature *:myfeaturetemplate>`` is replaced with the content of the ``myfeaturetemplate`` template for each feature in the feature collection. So if your query hits five features, you'll get five ``li`` tags like in the template. The asterisk is used to select all features, it's possible to limit the number of objects matched. See below in the reference section for a detailed explanation on how it works.

Within the ``myfeaturetemplate`` template you have switched context. In the ``start`` template your context is the feature collection, and you can call *feature templates*. In the ``myfeaturetemplate`` you 'went down' the tree and are now in a feature context, where you can call *property templates*. So what can we do in a feature context? Let's start simple by writing out the feature type name. Change the ``myfeaturetemplate`` like this:

.. code-block:: xml

  <?template myfeaturetemplate>
  <li>I have a <?name> feature</li>

What happens now is that for each use of the ``myfeaturetemplate`` the ``<?name>`` part is being replaced with the name of the feature type of the feature you hit. So if you hit two features, each of a different type, you get two different ``li`` tags in the document, each with its name written in it.

So deegree only replaces the *template call* in the ``start`` template with its replacement once the special constructs in the *called* template are all replaced, and all the special constructs/calls within *that* template are all replaced, ... and so on.

Let's take it to the next level. What's you really want to do in featureinfo responses is of course get the value of the features' properties. So let's add another template, and call it from the ``myfeaturetemplate`` template:

.. code-block:: xml

  <?template myfeaturetemplate>
  <li>I have a <?name> feature and properties: <?property *:mypropertytemplate></li>

  <?template mypropertytemplate>
  <?name>=<?value>

Now you also get all property names and values in the ``li`` item. Note that again you switched the context in the template, now you are at property level. The ``<?name>`` and ``<?value>`` special constructs yield the property name and value, respectively (remember, we're at property level here).

While that's already nice, people often put non human readable values in properties, even property names are sometimes not human readable. In order to fix that, you often have code lists mapping the codes to proper text. To use these, there's a special kind of template called a *map*. A map is like a simple property file. Let's have a look at how to define one:

.. code-block:: xml

  <?map mycodelistmap>
  code1=Street
  code2=Highway
  code3=Railway

  <?map mynamecodelistmap>
  tp=Type of way

Looks simple enough. Instead of ``template`` we use map, after that comes the name. Then we just map codes to values. So how do we use this? Instead of just using the ``<?name>`` or ``<?value>`` we push it through the map:

.. code-block:: xml

  <?template mypropertytemplate>
  <?name:map mynamecodelistmap>=<?value:map mycodelistmap>

Here the name of the property is replaced with values from the ``mynamecodelistmap``, the value is replaced with values from the ``mycodelistmap``. If the map does not contain a fitting mapping, the original value is used instead.

That concludes the introduction, the next section explains all available special constructs in detail.

"""""""""""""""""""""""""""""
Templating special constructs
"""""""""""""""""""""""""""""

This section shows all available special constructs. The selectors are explained in the table below. The validity describes in which context the construct can be used (and where the description applies). The validity can be one of *top level* (which means it's the definition of something), *featurecollection* (the ``start`` template), *feature* (a template on feature level), *property* (a template on property level) or *map* (a map definition).

+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| Construct                     | Validity          | Description                                                                                     |
+===============================+===================+=================================================================================================+
| <?template *name*>            | top level         | defines a template with name *name*                                                             |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?map *name*>                 | top level         | defines a map with name *name*                                                                  |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?feature *selector*:*name*>  | featurecollection | calls the template with name *name* for features matching the selector *selector*               |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?property *selector*:*name*> | feature           | calls the template with name *name* for properties matching the selector *selector*             |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?name>                       | feature           | evaluates to the feature type name                                                              |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?name>                       | property          | evaluates to the property name                                                                  |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?name:map *name*>            | feature           | uses the map *name* to map the feature type name to a value                                     |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?name:map *name*>            | property          | uses the map *name* to map the property name to a value                                         |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?value>                      | property          | evaluates to the property's value                                                               |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?value:map *name*>           | property          | uses the map *name* to map the property's value to another value                                |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?index>                      | feature           | evaluates to the index of the feature (in the list of matches from the previous template call)  |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?index>                      | property          | evaluates to the index of the property (in the list of matches from the previous template call) |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?gmlid>                      | feature           | evaluates to the feature's gml:id                                                               |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?odd:*name*>                 | feature           | calls the *name* template if the index of the current feature is odd                            |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?odd:*name*>                 | property          | calls the *name* template if the index of the current property is odd                           |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?even:*name*>                | feature           | calls the *name* template if the index of the current feature is even                           |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?even:*name*>                | property          | calls the *name* template if the index of the current property is even                          |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?link:*prefix*:>             | property          | if the value of the property is not an absolute link, the prefix is prepended                   |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+
| <?link:*prefix*:*text*>       | property          | the text of the link will be *text* instead of the link address                                 |
+-------------------------------+-------------------+-------------------------------------------------------------------------------------------------+

The selector for properties and features is a kind of pattern matching on the object's name.

+--------------------------+----------------------------------------------------------+
| Selector                 | Description                                              |
+==========================+==========================================================+
| \*                       | matches all objects                                      |
+--------------------------+----------------------------------------------------------+
| \* *text*                | matches all objects with names ending in *text*          |
+--------------------------+----------------------------------------------------------+
| *text* \*                | matches all objects with names starting with *text*      |
+--------------------------+----------------------------------------------------------+
| not(*selector*)          | matches all objects not matching the selector *selector* |
+--------------------------+----------------------------------------------------------+
| *selector1*, *selector2* | matches all objects matching *selector1* and *selector2* |
+--------------------------+----------------------------------------------------------+

.. _anchor-image-output-configuration:

^^^^^^^^^^^^^^^^^^^^^^^^^^^
Custom image output formats
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Any mime type of the following output formats can be configured to be available as response format for GetMap requests.

 * ``image/png``
 * ``image/png; subtype=8bit``
 * ``image/png; mode=8bit``
 * ``image/gif``
 * ``image/jpeg``
 * ``image/tiff``
 * ``image/x-ms-bmp``

If no format has been configured, all formats are supported.

This is how the configuration section looks like for configuring only ``image/png`` as image output format:

.. code-block:: xml

  <GetMapFormats>
    <GetMapFormat>image/png</GetMapFormat>
  </GetMapFormats>

^^^^^^^^^^^^^^^^^^^^^
Extended capabilities
^^^^^^^^^^^^^^^^^^^^^

Important for applications like INSPIRE, it is often desirable to include predefined blocks of XML in the extended capabilities section of the WMS' capabilities output. This can be achieved simply by adding these blocks to the extended capabilities element of the configuration:

.. code-block:: xml

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
   :figwidth: 80%
   :width: 80%
   :target: _images/workspace-wmts.png

   A WMTS resource is connected to any number of theme resources (with tile layers)

.. tip::
  In order to fully understand deegree WMTS configuration, you will have to learn configuration of other workspace aspects as well. Chapter :ref:`anchor-configuration-tilestore` describes the configuration of tile data access. Chapter :ref:`anchor-configuration-layers` describes the configuration of layers (only tile layers are usable for the WMTS). Chapter :ref:`anchor-configuration-themes` describes how to create a theme from layers.

^^^^^^^^^^^^^^^
Minimal example
^^^^^^^^^^^^^^^

The only mandatory section is ``ServiceConfiguration`` (which can be empty), therefore a minimal WMTS configuration example looks like this:

.. topic:: WMTS config example 1: Minimal configuration

   .. literalinclude:: xml/wmts_basic.xml
      :language: xml

This will create a deegree WMTS resource that connects to all configured themes of the workspace.

^^^^^^^^^^^^^^^^^^^^
More complex example 
^^^^^^^^^^^^^^^^^^^^

A more complex configuration that restricts the offered themes looks like this:

.. topic:: WMTS config example 2: More complex configuration

   .. literalinclude:: xml/wmts_complex.xml
      :language: xml

^^^^^^^^^^^^^^^^^^^^^^
Configuration overview
^^^^^^^^^^^^^^^^^^^^^^

The deegree WMTS config file format is defined by schema file http://schemas.deegree.org/services/wmts/3.2.0/wmts.xsd. The root element is ``deegreeWMTS`` and the config attribute must be ``3.2.0``.

The following table lists all available configuration options. When specifying them, their order must be respected.

.. table:: Options for ``deegreeWMTS``

+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| Option                   | Cardinality  | Value   | Description                                                                  |
+==========================+==============+=========+==============================================================================+
| MetadataURLTemplate      | 0..1         | String  | Template for generating URLs to layer metadata                               |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+
| ThemeId                  | 0..n         | String  | Limits themes to use                                                         |
+--------------------------+--------------+---------+------------------------------------------------------------------------------+

Below the ``ServiceConfiguration`` section you can specify custom featureinfo format handlers:

.. code-block:: xml
  ...
  </ServiceConfiguration>
  <FeatureInfoFormats>
  ...
  </FeatureInfoFormats>

Have a look at section :ref:`anchor-featureinfo-configuration` (in the WMS chapter) to see how custom featureinfo formats are configured. Take note that the GetFeatureInfo operation is currently only supported for remote WMS tile store backends.

.. _anchor-configuration-csw:

-----------------------------------
Catalogue Service for the Web (CSW)
-----------------------------------

In deegree terminology, a deegree CSW provides access to metadata records stored in a metadata store. If the metadata store is transaction-capable, CSW transactions can be used to modify the stored records.

.. figure:: images/workspace-csw.png
   :figwidth: 80%
   :width: 80%
   :target: _images/workspace-csw.png

   A CSW resource is connected to exactly one metadata store resource

.. tip::
  In order to fully understand deegree CSW configuration, you will have to learn configuration of other workspace aspects as well. Chapter :ref:`anchor-configuration-metadatastore` describes the configuration of metadatastores.

^^^^^^^^^^^^^^^
Minimal example
^^^^^^^^^^^^^^^

There is no mandatory element, therefore a minimal CSW configuration example looks like this:

.. topic:: CSW config example 1: Minimal configuration

   .. literalinclude:: xml/csw_basic.xml
      :language: xml

^^^^^^^^^^^^^^^^^^^^^^
Configuration overview
^^^^^^^^^^^^^^^^^^^^^^

The deegree CSW config file format is defined by schema file http://schemas.deegree.org/services/csw/3.2.0/csw_configuration.xsd. The root element is ``deegreeCSW`` and the config attribute must be ``3.2.0``.

The following table lists all available configuration options. When specifiying them, their order must be respected.

.. table:: Options for ``deegreeCSW``

+--------------------------+--------------+---------+----------------------------------------------------------------------------------------------+
| Option                   | Cardinality  | Value   | Description                                                                                  |
+==========================+==============+=========+==============================================================================================+
| SupportedVersions        | 0..1         | String  | Supported CSW Version (Default: 2.0.2)                                                       |
+--------------------------+--------------+---------+----------------------------------------------------------------------------------------------+
| MaxMatches               | 0..1         | Integer | Not negative number of matches (Default:0)                                                   |
+--------------------------+--------------+---------+----------------------------------------------------------------------------------------------+
| MetadataStoreId          | 0..1         | String  | Id of the meradatastoreId to use as backenend. By default the only configured store is used. |
+--------------------------+--------------+---------+----------------------------------------------------------------------------------------------+
| EnableTransactions       | 0..1         | Boolean | Enable transactions (CSW operations) default: disabled. (Default: false)                     |
+--------------------------+--------------+---------+----------------------------------------------------------------------------------------------+
| EnableInspireExtensions  | 0..1         |         | Enable the INSPIRE extensions, default: disabled                                             |
+--------------------------+--------------+---------+----------------------------------------------------------------------------------------------+
| ExtendedCapabilities     | 0..1         | anyURI  | Include referenced capabilities section.                                                     |
+--------------------------+--------------+---------+----------------------------------------------------------------------------------------------+
| ElementNames             | 0..1         |         |  List of configured return profiles. See following xml snippet for detailed informations.    |
+--------------------------+--------------+---------+----------------------------------------------------------------------------------------------+

   .. literalinclude:: xml/csw_elementNames.snippet
      :language: xml

^^^^^^^^^^^^^^^^^^^^^^^^^^
Extended Functionality
^^^^^^^^^^^^^^^^^^^^^^^^^^
* deegree3 CSW (up to 3.2-pre11) supports JSON as additional output format. Use *outputFormat="application/json"* in your GetRecords or GetRecordById Request to get the matching records in JSON.


.. _anchor-configuration-wps:

----------------------------
Web Processing Service (WPS)
----------------------------

A deegree WPS allows the invocation of geospatial processes. The offered processes are determined by the attached process provider resources.

.. figure:: images/workspace-wps.png
   :figwidth: 90%
   :width: 90%
   :target: _images/workspace-wps.png

   Workspace components involved in a deegree WPS configuration

.. tip::
  In order to fully master deegree WPS configuration, you will have to understand :ref:`anchor-configuration-processproviders` as well.

^^^^^^^^^^^^^^^
Minimal example
^^^^^^^^^^^^^^^

A minimal valid WPS configuration example looks like this:

.. code-block:: xml
  
  <deegreeWPS configVersion="3.1.0" xmlns="http://www.deegree.org/services/wps" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.deegree.org/services/wps http://schemas.deegree.org/services/wps/3.1.0/wps_configuration.xsd">  
  </deegreeWPS>

This will create a WPS resource with the following properties:

* All WPS protocol versions are enabled. Currently, this is only 1.0.0.
* The WPS resource will attach to all process provider resources in the workspace.
* Temporary files (e.g. for process results) are stored in the standard Java temp directory of the deegree webapp.
* The last 100 process executions are tracked.
* Memory buffers (e.g. for inline XML inputs) are limited to 1 MB each. If this limit is exceeded, buffering is switched to use a file in the storage directory.

^^^^^^^^^^^^^^^
Complex example
^^^^^^^^^^^^^^^

A more complex configuration example looks like this:

.. code-block:: xml
  
  <deegreeWPS configVersion="3.1.0" xmlns="http://www.deegree.org/services/wps" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.deegree.org/services/wps http://schemas.deegree.org/services/wps/3.1.0/wps_configuration.xsd">
  
    <SupportedVersions>
      <Version>1.0.0</Version>
    </SupportedVersions>
  
    <DefaultExecutionManager>
      <StorageDir>../var/wps/</StorageDir>
      <TrackedExecutions>1000</TrackedExecutions>
      <InputDiskSwitchLimit>1048576</InputDiskSwitchLimit>
    </DefaultExecutionManager>
  
  </deegreeWPS>

This will create a WPS resource with the following properties:

* Enabled WPS protocol versions: 1.0.0
* The WPS resource will attach to all process provider resources in the workspace.
* Storage directory for temporary files (e.g. for process results) is ``/var/wps`` inside the workspace.
* The last 1000 process executions will be tracked.
* Memory buffers (e.g. for inline XML inputs) are limited to 1 MB each. If this limit is exceeded, buffering is switched to use a file in the storage directory.

^^^^^^^^^^^^^^^^^^^^^^
Configuration overview
^^^^^^^^^^^^^^^^^^^^^^

The deegree WPS config file format is defined by schema file http://schemas.deegree.org/services/wps/3.1.0/wps_configuration.xsd. The root element is ``deegreeWPS`` and the config attribute must be ``3.1.0``. The following table lists all available configuration options (complex ones contain nested options themselves). When specifiying them, their order must be respected.

.. table:: Options for ``deegreeWPS``

+-------------------------+-------------+---------+-----------------------------------------------+
| Option                  | Cardinality | Value   | Description                                   |
+=========================+=============+=========+===============================================+
| SupportedVersions       | 0..1        | Complex | Activated OGC protocol versions, default: all |
+-------------------------+-------------+---------+-----------------------------------------------+
| DefaultExecutionManager | 0..1        | Complex | Settings for tracking process executions      |
+-------------------------+-------------+---------+-----------------------------------------------+

The remainder of this section describes these options and their sub-options in detail.

* ``SupportedVersions``: By default, all implemented WMS protocol versions are activated. Currently, this is just 1.0.0 anyway. Alternatively you can control offered WPS protocol versions using the element ``SupportedVersions``. This element allows the child element ``<Version>1.0.0</Version>`` for now.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
DefaultExecutionManager section
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This section controls aspects that are related to temporary storage (for input and output parameter values) during the execution of processes. The ``DefaultExecutionManager`` option has the following sub-options:

.. table:: Options for ``DefaultExecutionManager``

+----------------------+-------------+---------+-------------------------------------------------------------------------------+
| Option               | Cardinality | Value   | Description                                                                   |
+======================+=============+=========+===============================================================================+
| StorageDir           | 0..1        | String  | Directory for storing execution-related data, default: Java tempdir           |
+----------------------+-------------+---------+-------------------------------------------------------------------------------+
| TrackedExecutions    | 0..1        | Integer | Number of executions to track, default: 100                                   |
+----------------------+-------------+---------+-------------------------------------------------------------------------------+
| InputDiskSwitchLimit | 0..1        | Integer | Limit in bytes, before a ComplexInputInput is written to disk, default: 1 MiB |
+----------------------+-------------+---------+-------------------------------------------------------------------------------+

.. _anchor-configuration-service-metadata:

--------
Metadata
--------

This section describes the configuration for the different types of metadata that a service reports in the ``GetCapabilities`` response. These options don't affect the data that the service offers or the behaviour of the service. It merely changes the descriptive metadata that the service reports.

In order to configure the metadata for a web service instance ``xyz``, create a corresponding ``xyz_metadata.xml`` file in the ``services`` directory of the workspace. The actual service type does not matter, the configuration works for all types of service alike.

.. topic:: Example for ``deegreeServicesMetadata``

   .. literalinclude:: xml/service_metadata.xml
      :language: xml

The metadata config file format is defined by schema file http://schemas.deegree.org/services/metadata/3.2.0/metadata.xsd. The root element is ``deegreeServicesMetadata`` and the config attribute must be ``3.2.0``. The following table lists all available configuration options (complex ones contain nested options themselves). When specifiying them, their order must be respected.

.. table:: Options for ``deegreeServicesMetadata``

+-------------------------+-------------+---------+------------------------------------------------------------------+
| Option                  | Cardinality | Value   | Description                                                      |
+=========================+=============+=========+==================================================================+
| ServiceIdentification   | 1..1        | Complex | Metadata that describes the service                              |
+-------------------------+-------------+---------+------------------------------------------------------------------+
| ServiceProvider         | 1..1        | Complex | Metadata that describes the provider of the service              |
+-------------------------+-------------+---------+------------------------------------------------------------------+
| DatasetMetadata         | 0..1        | Complex | Metadata on the datasets provided by the service                 |
+-------------------------+-------------+---------+------------------------------------------------------------------+
| ExtendedCapabilities    | 0..n        | Complex | Extended Metadata reported in OperationsMetadata section         |
+-------------------------+-------------+---------+------------------------------------------------------------------+

The remainder of this section describes these options and their sub-options in detail.

^^^^^^^^^^^^^^^^^^^^^^
Service identification
^^^^^^^^^^^^^^^^^^^^^^

The ``ServiceIdentification`` option has the following sub-options:

.. table:: Options for ``ServiceIdentification``

+----------------------+-------------+---------+-------------------------------------------------------------------------------+
| Option               | Cardinality | Value   | Description                                                                   |
+======================+=============+=========+===============================================================================+
| Title                | 0..n        | String  | Title of the service                                                          |
+----------------------+-------------+---------+-------------------------------------------------------------------------------+
| Abstract             | 0..n        | String  | Abstract                                                                      |
+----------------------+-------------+---------+-------------------------------------------------------------------------------+
| Keywords             | 0..n        | Complex | Keywords that describe the service                                            |
+----------------------+-------------+---------+-------------------------------------------------------------------------------+
| Fees                 | 0..1        | String  | Fees that apply for using this service                                        |
+----------------------+-------------+---------+-------------------------------------------------------------------------------+
| AccessConstraints    | 0..n        | String  | Access constraints for this service                                           |
+----------------------+-------------+---------+-------------------------------------------------------------------------------+

^^^^^^^^^^^^^^^^
Service provider
^^^^^^^^^^^^^^^^

The ``ServiceProvider`` option has the following sub-options:

.. table:: Options for ``ServiceProvider``

+----------------+-------------+---------+-------------------------------------+
| Option         | Cardinality | Value   | Description                         |
+================+=============+=========+=====================================+
| ProviderName   | 0..1        | String  | Name of the service provider        |
+----------------+-------------+---------+-------------------------------------+
| ProviderSite   | 0..1        | String  | Website of the service provider     |
+----------------+-------------+---------+-------------------------------------+
| ServiceContact | 0..1        | Complex | Contact information                 |
+----------------+-------------+---------+-------------------------------------+

^^^^^^^^^^^^^^^^
Dataset metadata
^^^^^^^^^^^^^^^^

This type of metadata is attached to the datasets that a service offers (e.g. layers for the WMS or feature types for the WFS). The services themselves may have specific mechanisms to override this metadata, so make sure to have a look at the appropriate service sections. However, some metadata configuration can be done right here.

To start with, you'll need to add a ``DatasetMetadata`` container element:

.. code-block:: xml

  <DatasetMetadata>
  ...
  </DatasetMetadata>

Apart from the descriptive metadata (title, abstract etc.) for each dataset, you can also configure ``MetadataURL``s, external metadata links and metadata as well as external metadata IDs.

For general ``MetadataURL`` configuration, you can configure the element ``MetadataUrlTemplate``. Its content can be any URL, which may contain the pattern ``${metadataSetId}``. For each dataset (layer, feature type) the service will output a ``MetadataURL`` based on that pattern, if a ``MetadataSetId`` has been configured for that dataset (see below). The template is optional, if omitted, no ``MetadataURL`` will be produced.

Configuration for the template looks like this:

.. code-block:: xml

  <DatasetMetadata>
    <MetadataUrlTemplate>http://some.url.de/csw?request=GetRecordById&amp;service=CSW&amp;version=2.0.2&amp;outputschema=http://www.isotc211.org/2005/gmd&amp;elementsetname=full&amp;id=${metadataSetId}</MetadataUrlTemplate>
  ...
  </DatasetMetadata>

You can also configure ``ExternalMetadataAuthority`` elements, which are currently only used by the WMS. You can define multiple authorities, with the authority URL as text content and a unique ``name`` attribute. For each dataset you can define an ID for an authority by refering to that name. This will generate an ``AuthorityURL`` and ``Identifier`` pair in WMS capabilities documents (version 1.3.0 only).

Configuration for an external authority looks like this:

.. code-block:: xml

  <DatasetMetadata>
    <ExternalMetadataAuthority name="myorg">http://www.myauthority.org/metadataregistry/</ExternalMetadataAuthority>
  ...
  </DatasetMetadata>

Now follows the list of the actual dataset metadata. You can add as many as you need:

.. code-block:: xml

  <DatasetMetadata>
    <MetadataUrlTemplate>...</MetadataUrlTemplate>
    ...
    <Dataset>
    ...
    </Dataset>
    <Dataset>
    ...
    </Dataset>
    ...
  </DatasetMetadata>

For each dataset, you can configure the metadata as outlined in the following table:

.. table:: Metadata options for ``Dataset``

+-------------------------+--------------+---------------+----------------------------------------------------------------------------------------------+
| Option                  | Cardinality  | Value         | Description                                                                                  |
+=========================+==============+===============+==============================================================================================+
| Name                    | 1            | String/QName  | the layer/feature type name you refer to                                                     |
+-------------------------+--------------+---------------+----------------------------------------------------------------------------------------------+
| Title                   | 0..n         | String        | can be multilingual by using the ``lang`` attribute                                          |
+-------------------------+--------------+---------------+----------------------------------------------------------------------------------------------+
| Abstract                | 0..n         | String        | can be multilingual by using the ``lang`` attribute                                          |
+-------------------------+--------------+---------------+----------------------------------------------------------------------------------------------+
| MetadataSetId           | 0..1         | String        | is used to generate ``MetadataURL`` s, see above                                             |
+-------------------------+--------------+---------------+----------------------------------------------------------------------------------------------+
| ExternalMetadataSetId   | 0..n         | String        | is used to generate ``AuthorityURL`` s and ``Identifier`` s for WMS, see above. Refer to an  |
|                         |              |               | authority using the ``authority`` attribute.                                                 |
+-------------------------+--------------+---------------+----------------------------------------------------------------------------------------------+

^^^^^^^^^^^^^^^^^^^^^
Extended capabilities
^^^^^^^^^^^^^^^^^^^^^

Extended capabilities are generic metadata sections below the ``OperationsMetadata`` element in the ``GetCapabilities`` response. They are not defined by the OGC service specifications, but by additional guidance documents, such as the INSPIRE Network Service TGs. deegree treats this section as a generic XML element and includes it in the output. If your service supports multiple protocol versions (e.g. a WFS that supports 1.1.0 and 2.0.0), you may include multiple ``ExtendedCapabilities`` elements in the metadata configuration and use attribute ``protocolVersions`` to indicate the version that you want to define the extended capabilities for.

------------------
Service controller
------------------

The controller configuration is used to configure various global aspects that affect all services.

Since it's a global configuration file for all services, it's called ``main.xml``, and located in the ``services`` directory. All of the options are optional, and you can also omit the file completely.

An empty example file looks like follows:

.. code-block:: xml

  <?xml version='1.0'?>
  <deegreeServiceController xmlns='http://www.deegree.org/services/controller' configVersion='3.4.0'>
  </deegreeServiceController>

The following table lists all available configuration options. When specifiying them, their order must be respected.

.. table:: Options for ``deegreeServiceController``

+----------------------------+-------------+---------+---------------------------------------------+
| Option                     | Cardinality | Value   | Description                                 |
+============================+=============+=========+=============================================+
| ReportedUrls               | 0..1        | Complex | Hardcode reported URLs in service responses |
+----------------------------+-------------+---------+---------------------------------------------+
| PreventClassloaderLeaks    | 0..1        | Boolean | TODO                                        |
+----------------------------+-------------+---------+---------------------------------------------+
| RequestLogging             | 0..1        | Complex | TODO                                        |
+----------------------------+-------------+---------+---------------------------------------------+
| ValidateResponses          | 0..1        | Boolean | TODO                                        |
+----------------------------+-------------+---------+---------------------------------------------+
| RequestTimeoutMilliseconds | 0..n        | Complex | Maximum request execution time              |
+----------------------------+-------------+---------+---------------------------------------------+

The following sections describe the available options in detail.

^^^^^^^^^^^^^
Reported URLs
^^^^^^^^^^^^^

Some web service responses contain URLs that refer back to the service, for example in capabilities documents (responses to GetCapabilities requests). By default, deegree derives these URLs from the incoming request, so you don't have to think about this, even when your server has multiple network interfaces or hostnames. However, sometimes it is required to override these URLs, for example when using deegree behind a proxy or load balancer.

.. tip::
  If you don't have a proxy setup that requires it, don't configure the reported URLs. In standard setups, the default behaviour works best.

To override the reported URLs, put a fragment like the following into the ``main.xml``:

.. code-block:: xml

  <ReportedUrls>
    <Services>http://www.mygeoportal.com/ows</Services>
    <Resources>http://www.mygeoportal.com/ows-resources</Resources>
  </ReportedUrls>

For this example, deegree would report ``http://www.mygeoportal.com/ows`` as service endpoint URL in capabilities responses, regardless of the real connection details of the deegree server. If a specific service is contacted on the deegree server, for example via a request to ``http://realnameofdeegreemachine:8080/deegree-webservices/services/inspire-wfs-ad``, deegree would report ``http://www.mygeoportal.com/ows/inspire-wfs-ad``.

The URL configured by ``Resources`` relates to the reported URL of the ``resources`` servlet, which allows to access parts of the active deegree workspace via HTTP. Currently, this is only used in WFS DescribeFeatureType responses that access GML application schema directories.

^^^^^^^^^^^^^^^^
Request timeouts
^^^^^^^^^^^^^^^^

By default, the execution time of a request to a web service is not constrained. It depends on the complexity of the request and the configuration -- it's well possible to create a WMS configuration and a GetMap request that will require hours of processing time. Generally, it is the responsibility of the configuration creator to ensure that service requests will return in a reasonable time (e.g. by applying scale limitations in the layer configuration).

Nevertheless, it is sometimes desirable to enforce an execution time limit. This can be achieved by using the RequestTimeoutMilliseconds option:

.. code-block:: xml

  ...
    <RequestTimeoutMilliseconds serviceId="wms1" request="GetMap">1000</RequestTimeoutMilliseconds>
    <RequestTimeoutMilliseconds serviceId="wms2" request="GetMap">2500</RequestTimeoutMilliseconds>
  ...

This example enforces the following time-out behaviour:

* GetMap requests to WMS instance wms1 will be interrupted after an execution time of 1000 milliseconds
* GetMap requests to WMS instance wms2 will be interrupted after an execution time of 2500 milliseconds

Besides the time-out value in milliseconds, the following sub-options are supported by RequestTimeoutMilliseconds:

.. table:: Options for ``RequestTimeoutMilliseconds``

+------------+-------------+---------+------------------------------------+
| Option     | Cardinality | Value   | Description                        |
+============+=============+=========+====================================+
| @serviceId | 1           | String  | Resource identifier of the service |
+------------+-------------+---------+------------------------------------+
| @request   | 1           | String  | Service request                    |
+------------+-------------+---------+------------------------------------+

.. note::
  A time-out value can be configured for any service type and request. However, a correct termination of requests requires that the relevant Java code is actually interruptible. So far, this has only been verified for GetMap requests to WMS based on feature layers.
