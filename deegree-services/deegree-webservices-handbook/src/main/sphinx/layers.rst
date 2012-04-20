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

.. _common:

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

You can configure any number of ``StyleRef`` elements. Each corresponds to exactly one style store configuration, specified by the subelement ``StyleStoreId``. The only other allowed subelement is the ``Style`` element, which can be used to extract/rename specific styles from the style store. If omitted, all styles matching the layers' name are used. Let's have a look at an example snippet::

  <l:StyleRef>
    <l:StyleStoreId>roads_style</l:StyleStoreId>
  </l:StyleRef>

Here's a snippet with ``Style`` elements::

  <l:StyleRef>
    <l:StyleStoreId>road_styles</l:StyleStoreId>
    <l:Style>
    ...
    </l:Style>
    <l:Style>
    ...
    </l:Style>
  </l:StyleRef>

If a ``Style`` element is specified, you must first specify what style you want extracted::

  <l:Style>
    <l:StyleName>highways</l:StyleName>
    <l:LayerNameRef>highways</l:LayerNameRef>
    <l:StyleNameRef>highways</l:StyleNameRef>
    ...
  </l:Style>

The ``StyleName`` specifies the name under which the style will be known in the WMS. The ``LayerNameRef`` and ``StyleNameRef`` are used to extract the style from the style store.

The next part to configure within the ``Style`` element is the legend generation, if you don't want to use the default legend generated from the rendering style. You can either specify a different style from the style store to use for legend generation, or you can specify an external graphic (which is unfortunately not supported yet). Referencing a different legend style is straightforward::

  <l:Style>
  ...
    <l:LegendStyle>
      <l:LayerNameRef>highways</l:LayerNameRef>
      <l:StyleNameRef>highways_legend</l:StyleNameRef>
    </l:LegendStyle>
  </l:Style>

^^^^^^^^^^^^^^^^^
Rendering options
^^^^^^^^^^^^^^^^^

The rendering options are basically the same as the WMS layer options. Here's a copy of the corresponding table for reference:

+------------------------+-------------------+-----------+---------------------------------------------------------------------------------------------------+
| Option                 | Cardinality       | String    | Description                                                                                       |
+========================+===================+===========+===================================================================================================+
| AntiAliasing           | 0..1              | String    | Whether to antialias NONE, TEXT, IMAGE or BOTH, default is BOTH                                   |
+------------------------+-------------------+-----------+---------------------------------------------------------------------------------------------------+
| RenderingQuality       | 0..1              | String    | Whether to render LOW, NORMAL or HIGH quality, default is HIGH                                    |
+------------------------+-------------------+-----------+---------------------------------------------------------------------------------------------------+
| Interpolation          | 0..1              | String    | Whether to use BILINEAR, NEAREST_NEIGHBOUR or BICUBIC interpolation, default is NEAREST_NEIGHBOUR |
+------------------------+-------------------+-----------+---------------------------------------------------------------------------------------------------+
| MaxFeatures            | 0..1              | Integer   | Maximum number of features to render at once, default is 10000                                    |
+------------------------+-------------------+-----------+---------------------------------------------------------------------------------------------------+
| FeatureInfoRadius      | 0..1              | Integer   | Number of pixels to consider when doing GetFeatureInfo, default is 1                              |
+------------------------+-------------------+-----------+---------------------------------------------------------------------------------------------------+

Here is an example snippet::

  <l:LayerOptions>
    <l:AntiAliasing>TEXT</l:AntiAliasing>
  </l:LayerOptions>


--------------
Feature layers
--------------

Feature layers are layers based on a feature store. You can have multiple layers defined in a feature layers configuration, each based on feature types from the same feature store.

You have two choices to configure feature layers. One option is to try to have deegree figure out what layers to configure by itself, the other is to manually define all the layers you want. Having deegree do the configuration automatically has the obvious advantage that the configuration is minimal, with the disadvantage of lacking flexibility.

~~~~~~~~~~~
Auto layers
~~~~~~~~~~~

This configuration only involves to specify what feature store to use, and optionally, what styles. Let's have a look at an example::

  <FeatureLayers xmlns='http://www.deegree.org/layers/feature' 
                 xmlns:d='http://www.deegree.org/metadata/description'
                 xmlns:s='http://www.deegree.org/metadata/spatial'
                 xmlns:l='http://www.deegree.org/layers/base'
                 configVersion='3.2.0'>
  
    <AutoLayers>
      <FeatureStoreId>myfeaturestore</FeatureStoreId>
      <StyleStoreId>style1</StyleStoreId>
      <StyleStoreId>style2</StyleStoreId>
    </AutoLayers>
  
  </FeatureLayers>

This will create one layer for each (concrete) feature type in the feature store. If no style stores are configured, the default style will be used for all layers. If style stores are configured, matching styles will be automatically used if available. So if you have a feature type with (local) name ``Autos``, deegree will check all configured style stores for styles identified by layer name ``Autos`` and use them, if available. The name ``Autos`` will be used as name and title as appropriate, and spatial metadata will be used as available from the feature store.

~~~~~~~~~~~~~~~~~~~~
Manual configuration
~~~~~~~~~~~~~~~~~~~~

The basic structure of a manual configuration looks like this::

  <FeatureLayers xmlns='http://www.deegree.org/layers/feature' 
                 xmlns:d='http://www.deegree.org/metadata/description'
                 xmlns:s='http://www.deegree.org/metadata/spatial'
                 xmlns:l='http://www.deegree.org/layers/base'
                 configVersion='3.2.0'>
    <FeatureStoreId>myfeaturestore</FeatureStoreId>
    <FeatureLayer>
    ...
    </FeatureLayer>
    <FeatureLayer>
    ...
    </FeatureLayer>
  </FeatureLayers>

As you can see, the first thing to do is to bind the configuration to a feature store. After that, you can define one or more feature layers.

A feature layer configuration has two optional elements besides the common elements. The ``FeatureTypeName`` can be used to restrict a layer to a specific feature type (use a qualified name). The ``Filter`` element can be used to specify a filter that applies to the layer globally (use standard OGC filter encoding 1.1.0 ``ogc:Filter`` element within)::

  <FeatureLayer>
    <FeatureTypeName xmlns:app='http://www.deegree.org/app'>app:Roads</FeatureTypeName>
    <Filter>
      <Filter xmlns='http://www.opengis.net/ogc'>
        <PropertyIsEqualTo>
          <PropertyName xmlns:app='http://www.deegree.org/app'>app:type</PropertyName>
          <Literal>123</Literal>
        </PropertyIsEqualTo>
      </Filter>
    </Filter>
    ...
  </FeatureLayer>

After that the standard options follow, as outlined in the common_ section.

-----------
Tile layers
-----------

Tile layers are based on a tile pyramid of a tile store. You can configure an unlimited number of tile layers each based on a different tile store within one configuration file.

As you might have guessed, most of the common parameters are ignored for this layer type. Most notably, the style and dimension configuration is ignored.

In most cases, a configuration like the following is sufficient::

  <TileLayers xmlns="http://www.deegree.org/layers/tile"
              xmlns:d="http://www.deegree.org/metadata/description" 
              xmlns:l="http://www.deegree.org/layers/base"
              configVersion="3.2.0">
    <TileLayer>
      <TileStoreId>pyramid</TileStoreId>
      <l:Name>example</l:Name>
      <d:Title>Example INSPIRE layer</d:Title>
    </TileLayer>
  </TileLayers>

Just repeat the ``TileLayer`` element once for each layer you wish to configure.

