.. _anchor-configuration-layers:

====================================
Layer configuration
====================================

A layer is the description on how to combine a data source and a style resource into a map. Each layer configuration can be used to define one or more layers. The layers can be used in theme definitions, and depend on various data source and style resources. This chapter assumes you've already configured a data source and a style for your layer (although a style is not strictly needed; some layer types can do without, and others can render in a default style when none is given).

--------------------
Common configuration
--------------------

Most layer configurations follow a similar structure. That's why some of the common components are exactly the same across configurations (they're even in common namespaces). In this section these common elements are described first, the subsequent chapters describe the different layer types.


.. _description:

~~~~~~~~~~~~~~~~~~~~
Description metadata
~~~~~~~~~~~~~~~~~~~~

The description section is used to describe textual metadata which occurs in almost all objects. This includes elements such as title, abstract and so on. The format which is being described here is capable of multilingualism, but processing multilingual strings is not supported yet (you can still define it, though).

The commonly used prefix for these elements is ``d``. Let's have a look at an example::

  <d:Title>My Roads Layer</d:Title>
  <d:Abstract>This is my roads layer, which I configured myself. I had no help but the deegree webservices handbook.</d:Abstract>
  <d:Keywords>
    <d:Keyword>deegree</d:Keyword>
    <d:Keyword>transportation</d:Keyword>
    <d:Type codeSpace='none'>unknown</d:Type>
  </d:Keywords>

All elements support the ``lang`` attribute to specify the language, and all elements may occur multiple times (including the ``Keywords`` element).

.. _spatial:

~~~~~~~~~~~~~~~~
Spatial metadata
~~~~~~~~~~~~~~~~

The spatial metadata is used to describe coordinate systems and envelopes. Typically, the layers can retrieve the native coordinate system and envelope from the data source, but sometimes it may be desirable to define a special extent, or add more coordinate systems. In the example configurations, the prefix ``s`` is used for spatial metadata elements, so it is used here as well::

  <s:Envelope crs='EPSG:25832'>
    <s:LowerCorner>204485 5204122</s:LowerCorner>
    <s:UpperCorner>1008600 6134557</s:UpperCorner>
  </s:Envelope>
  <s:CRS>EPSG:25832 EPSG:31466 EPSG:4326</s:CRS>

As you can see, the envelope is specified in a specific CRS. If the attribute is omitted, EPSG:4326 is assumed. The CRS element may include multiple codes, separated by whitespace.

~~~~~~~~~~~~~~~~~~~~
Common layer options
~~~~~~~~~~~~~~~~~~~~

This sections describes a set of common layer options. Not all options make sense for all layers, but most of them do.

The namespace for the elements (newly) defined in this section is commonly bound to the ``l`` character. Let's have a look at the options available:

.. table:: Common layer options

+-----------------------+---------------+--------------------+-----------------------------------------------------+
| Option                | Cardinality   | Value              | Description                                         |
+-----------------------+---------------+--------------------+-----------------------------------------------------+
| Name                  | 1             | String             | The unique identifier of the layer                  |
+-----------------------+---------------+--------------------+-----------------------------------------------------+
| *Description*         | 0..1          | Several            | The description_ elements described above           |
+-----------------------+---------------+--------------------+-----------------------------------------------------+
| *Spatial metadata*    | 0..1          | Several            | The spatial_ metadata elements described above      |
+-----------------------+---------------+--------------------+-----------------------------------------------------+
| MetadataSetId         | 0..1          | String             | A metadata set id by which this layer is identified |
+-----------------------+---------------+--------------------+-----------------------------------------------------+
| ScaleDenominators     | 0..1          | Empty              | Used to define scale constraints on the layer       |
+-----------------------+---------------+--------------------+-----------------------------------------------------+
| Dimension             | 0..n          | Complex            | Used to configure extra dimensions for the layer    |
+-----------------------+---------------+--------------------+-----------------------------------------------------+
| StyleRef              | 0..n          | Complex            | Used to reference one or more styles                |
+-----------------------+---------------+--------------------+-----------------------------------------------------+
| LayerOptions          | 0..1          | Complex            | Used to configure rendering behaviour               |
+-----------------------+---------------+--------------------+-----------------------------------------------------+

The ``MetadataSetId`` is used in the WMS to export a MetadataURL based on a template. Please refer to the WMS configuration for details on how to configure this.

The ``ScaleDenominators`` element has ``min`` and ``max`` attributes which define the constraints in WMS 1.3.0 scale denominators (based on 0.28mm pixel size).

^^^^^^^^^^^^^^^^
Layer dimensions
^^^^^^^^^^^^^^^^

The WMS specification supports extra dimensions (besides the spatial extent) for layers, such as elevation, time or other custom dimensions. Since the support must be present at the layer level, this must be configured on the layer in deegree. The ``Dimension`` element can have the attributes ``isTime`` and ``isElevation`` to indicate that you're defining the standard time/elevation dimension. If none is given, you'll have to specify the ``Name`` element. Let's see what you can configure here:

.. table:: Dimension configuration

+-----------------------+---------------+--------------------+---------------------------------------------------------------------+
| Option                | Cardinality   | Value              | Description                                                         |
+-----------------------+---------------+--------------------+---------------------------------------------------------------------+
| Name                  | 0..1          | String             | The dimension name, if not elevation or time                        |
+-----------------------+---------------+--------------------+---------------------------------------------------------------------+
| Source                | 1             | String/QName       | The data source of the dimension                                    |
+-----------------------+---------------+--------------------+---------------------------------------------------------------------+
| DefaultValue          | 0..1          | String             | Specify a default value to be used, default is none                 |
+-----------------------+---------------+--------------------+---------------------------------------------------------------------+
| MultipleValues        | 0..1          | Boolean            | Whether multiple values are supported, default is false             |
+-----------------------+---------------+--------------------+---------------------------------------------------------------------+
| NearestValue          | 0..1          | Boolean            | Whether jumping to the nearest value is supported, default is false |
+-----------------------+---------------+--------------------+---------------------------------------------------------------------+
| Current               | 0..1          | Boolean            | Whether ``current`` is supported for time, default is false         |
+-----------------------+---------------+--------------------+---------------------------------------------------------------------+
| Units                 | 0..1          | String             | What units this dimension uses. Mandatory for non time/elevation    |
+-----------------------+---------------+--------------------+---------------------------------------------------------------------+
| UnitSymbol            | 0..1          | String             | What unit symbol to use. Mandatory for non time/elevation           |
+-----------------------+---------------+--------------------+---------------------------------------------------------------------+
| Extent                | 1             | String             | The extent of the dimension                                         |
+-----------------------+---------------+--------------------+---------------------------------------------------------------------+

Please note that for feature layers, the ``Source`` element content must be a qualified property name.

To understand how the omission or specification of the various optional elements here affect the WMS protocol behaviour, it is recommended to read up on the WMS 1.3.0 specification. The deegree WMS is going to behave according to what the spec says it must do (what to do in case a default value is available or not etc.). The format for the values and the extent is also identical to that used for requests/in the spec.

^^^^^^^^^^^^
Layer styles
^^^^^^^^^^^^

^^^^^^^^^^^^^^^^^
Rendering options
^^^^^^^^^^^^^^^^^









