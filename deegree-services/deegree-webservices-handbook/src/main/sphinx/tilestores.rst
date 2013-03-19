.. _anchor-configuration-tilestore:

===========
Tile stores
===========

Tile stores are resources that provide access to pre-rendered map tiles. The common use case for tile stores is to provide data for tile layers.

The remainder of this chapter describes some relevant terms and the tile store configuration files in detail. You can access this configuration level by clicking on the **tile stores** link in the administration console. The configuration files are located in the **datasources/tile/** directory of the deegree workspace.

.. figure:: images/workspace-overview-tile.png
   :figwidth: 80%
   :width: 80%
   :target: _images/workspace-overview-tile.png

   Tile store resources provide access to pre-rendered map tiles

------------------------------------------------
Tile stores, tile data sets and tile matrix sets
------------------------------------------------

A tile store is what you configure in a single tile store configuration file. It defines one or more (stored) tile data sets. Other resources such as the tile layer configuration usually refer to a specific tile data set from a tile store.

The structure of a tile data set is determined by specifying the identifier of a tile matrix set. Most often, one wants to define tile data sets that conform to a pre-defined tile matrix set. In that case, one only has to provide the tile store configuration file.

The term tile matrix set has been coined deliberately to coincide with the same term from the `WMTS specification <http://www.opengeospatial.org/standards/wmts>`_ and refers to structure and spatial properties of the tile matrix. The tile matrix sets (or "quads") from WMTS 1.0.0 and INSPIRE ViewService 3.1 specifications are already predefined, but additional tile matrix sets may be defined as well (see below).

Take note that it is not necessary to provide actual tiles for all tiles defined within the tile matrix set, a tile data set may contain a subset. The only requirement is that you need to fulfill the structure requirements (CRS, size of tiles, position of tiles in world coordinates, scale).

^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Pre-defined tile matrix sets
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The following table lists the tile matrix sets that are pre-defined in deegree:

+-------------------------+-----------------------+-----------------------------------------------+----------------------------------------+
| Workspace identifier    | Name in specification | URN                                           | Specification document                 |
+=========================+=======================+===============================================+========================================+
| globalcrs84scale        | GlobalCRS84Scale      | urn:ogc:def:wkss:OGC:1.0:GlobalCRS84Scale     | OGC WMTS 1.0.0                         |
+-------------------------+-----------------------+-----------------------------------------------+----------------------------------------+
| globalcrs84pixel        | GlobalCRS84Pixel      | urn:ogc:def:wkss:OGC:1.0:GlobalCRS84Pixel     | OGC WMTS 1.0.0                         |
+-------------------------+-----------------------+-----------------------------------------------+----------------------------------------+
| googlecrs84quad         | GoogleCRS84Quad       | urn:ogc:def:crs:OGC:1.3:CRS84                 | OGC WMTS 1.0.0                         |
+-------------------------+-----------------------+-----------------------------------------------+----------------------------------------+
| googlemapscompatible    | GoogleMapsCompatible  | urn:ogc:def:wkss:OGC:1.0:GoogleMapsCompatible | OGC WMTS 1.0.0                         |
+-------------------------+-----------------------+-----------------------------------------------+----------------------------------------+
| inspirecrs84quad        | InspireCRS84Quad      | n/a                                           | INSPIRE View Service Specification 3.1 | 
+-------------------------+-----------------------+-----------------------------------------------+----------------------------------------+

You can override these standard definitions by placing an appropriately named file into the ``datasources/tile/tilematrixset/`` directory of your workspace. It is recommended to always use lower case file names to avoid confusion.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
User-defined tile matrix sets
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

There are currently two ways to configure tile matrix sets. The first way is to state the structure of the matrices explicitly (described here), the second will extract the structure from a tiled GeoTIFF (BIGTIFF) file (possibly with overlays, described in the GeoTIFF section).

Like everything else in the deegree workspace, defining a tile matrix set means placing a configuration file into a standard location, in this case the ``datasources/tile/tilematrixset`` directory.

Let's have a look at an example for the explicit configuration:

.. code-block:: xml

  <TileMatrixSet xmlns="http://www.deegree.org/datasource/tile/tilematrixset" configVersion="3.2.0">

    <CRS>urn:ogc:def:crs:OGC:1.3:CRS84</CRS>

    <TileMatrix>
      <Identifier>1e6</Identifier>
      <ScaleDenominator>1e6</ScaleDenominator>
      <TopLeftCorner>-180 84</TopLeftCorner>
      <TileWidth>256</TileWidth>
      <TileHeight>256</TileHeight>
      <MatrixWidth>60000</MatrixWidth>
      <MatrixHeight>50000</MatrixHeight>
    </TileMatrix>
    <TileMatrix>
      <Identifier>2.5e6</Identifier>
      <ScaleDenominator>2.5e6</ScaleDenominator>
      <TopLeftCorner>-180 84</TopLeftCorner>
      <TileWidth>256</TileWidth>
      <TileHeight>256</TileHeight>
      <MatrixWidth>9000</MatrixWidth>
      <MatrixHeight>7000</MatrixHeight>
    </TileMatrix>

  </TileMatrixSet>

As you can see, the format is almost identical to the one from the WMTS capabilities documents. A tile matrix set is always defined for a single coordinate system, and contains one or more tile matrices. Each tile matrix has an identifier, a specific scale, an origin (the top left corner in world coordinates), defines a tile width/height in pixels and specifies how many tiles there are in x and y direction.

You do not need to explicitly specify the envelope, it will be calculated automatically from the values you provide. Keep in mind that the conversion between scale and resolution uses the WMTS conversion factor of approx. 111319 in case of degree based coordinate systems (that's important so the envelope is calculated correctly).

------------------
GeoTIFF tile store
------------------

The GeoTIFF tile store can be used to configure tile data sets based on GeoTIFF/BIGTIFF files. The tile store is currently read-only. The requirements for the GeoTIFFs are:

* it must be created as BIGTIFF (eg. with GDAL using the ``-co BIGTIFF=YES`` option)
* it must be created as a tiled tiff (eg. with GDAL using the ``-co TILED=YES`` option)
* it can contain overviews (it is best to use a recent GDAL version >= 1.8.0, where you can use ``GDAL_TIFF_OVR_BLOCKSIZE`` to specify the overview tile size)
* it is recommended that the overviews contain the same tile size as the main level
* it must contain the envelope as GeoTIFF tags in the tiff (don't use world files)
* it is recommended that the CRS is contained as GeoTIFF tag (but can be overridden in the tile matrix set config, see below)

To make it easy to create a WMTS based on a GeoTIFF, a tile matrix set can be generated from the GeoTIFF structure, using the method described further down. But if you manage to generate your TIFF files to fit the structure of another matrix set it is just as well (the envelope of the GeoTIFF can be a subset of the tile matrix set's envelope).

Let's have a look at an example configuration:

.. code-block:: xml

  <GeoTIFFTileStore xmlns="http://www.deegree.org/datasource/tile/geotiff" configVersion="3.2.0">

    <TileDataSet>
      <Identifier>test</Identifier>
      <TileMatrixSetId>utah</TileMatrixSetId>
      <File>../../data/test.tif</File>
      <ImageFormat>image/png</ImageFormat>
    </TileDataSet>
  ...
  </GeoTIFFTileStore>

(You can define multiple tile data sets within one tile store.)

* The identifier is optional, and defaults to the base name of the file (in this example test.tif)
* The tile matrix set id references the tile matrix set
* obviously you need to point to the GeoTIFF file
* The image format specifies the *output* image format, this is relevant if you use the tile store for a WMTS. The default is image/png.

To generate a tile matrix set from the GeoTIFF, put a file into the datasources/tile/tilematrixset/ directory. See how it must look like:

.. code-block:: xml

  <GeoTIFFTileMatrixSet xmlns="http://www.deegree.org/datasource/tile/tilematrixset/geotiff" configVersion="3.2.0">
    <StorageCRS>EPSG:26912</StorageCRS>
    <File>../../../data/utah.tif</File>
  </GeoTIFFTileMatrixSet>

The storage crs is optional if the file contains an appropriate GeoTIFF tag, but can be used to override it.

----------------------
File system tile store
----------------------

The file system tile store can be used to provide tiles from `tile cache <http://tilecache.org>`_ like directory hierarchies. This tile store is read-write.

Let's explain the configuration using an example:

.. code-block:: xml

  <FileSystemTileStore xmlns="http://www.deegree.org/datasource/tile/filesystem" configVersion="3.2.0">

    <TileDataSet>
      <Identifier>layer1</Identifier>
      <TileMatrixSetId>inspirecrs84quad</TileMatrixSetId>
      <TileCacheDiskLayout>
        <LayerDirectory>../../data/tiles/layer1</LayerDirectory>
        <FileType>png</FileType>
      </TileCacheDiskLayout>
    </TileDataSet>
  ...
  </FileSystemTileStore>

(You can define multiple tile data sets within one tile store.)

* The identifier is optional, default is the layer directory base name
* The tile matrix set id references the tile matrix set
* Currently only the tile cache disk layout is supported. Just point to the layer directory and specify the file type of the images (png is recommended, but most image formats are supported)

Please note that if you use external tools to seed the tile store, you need to make sure the resulting structure is compatible. The ``00`` directory corresponds to the *first* tile matrix of the referenced tile matrix set, ``01`` to the second tile matrix and so on.

---------------------
Remote WMS tile store
---------------------

The remote WMS tile store can be used to generate tiles on-the-fly from a WMS service. This tile store is read-only.

While you can configure multiple tile data sets in one remote WMS tile store configuration, they will all be based on one WMS.

Let's have a look at an example:

.. code-block:: xml

  <RemoteWMSTileStore xmlns="http://www.deegree.org/datasource/tile/remotewms" configVersion="3.2.0">

    <RemoteWMSId>wms1</RemoteWMSId>

    <TileDataSet>
      <Identifier>satellite</Identifier>
      <TileMatrixSetId>inspirecrs84quad</TileMatrixSetId>
      <OutputFormat>image/png</OutputFormat>
      <RequestParams>
        <Layers>SatelliteProvo</Layers>
        <Styles>default</Styles>
        <Format>image/png</Format>
        <CRS>EPSG:4326</CRS>
      </RequestParams>
    </TileDataSet>
  ...
  </RemoteWMSTileStore>

* The remote wms id is mandatory, and must point to a WMS type remote ows resource
* The identifier for the tile data sets is mandatory
* The tile matrix set id references the tile matrix set
* The output format is relevant if you use this tile data set in a WMTS
* The request params section specifies parameters to be used in the GetMap requests sent to the WMS:
 * The layers parameter can be used to specify one or more (comma separated) layers to request
 * The styles parameter must correspond to the layers parameter (works the same like GetMap)
 * The format parameter specifies the image format to request from the WMS
 * The CRS parameter specifies which CRS to use when requesting

Additionally you can specify default and override values for request parameters within the request params block. Just add ``Parameter`` tags as described in the :ref:`anchor-configuration-layer-request-options` layer chapter. The replacing/defaulting currently only works when you configure a WMTS on top of this tile store. ``GetTile`` parameters are then mapped to ``GetMap`` requests to the backend, and ``GetFeatureInfo`` WMTS parameters to ``GetFeatureInfo`` WMS parameters on the backend.

----------------------
Remote WMTS tile store
----------------------

The remote WMTS tile store can be used to generate tiles on-the-fly from a WMTS service. This tile store is read-only.

While you can configure multiple tile data sets in one remote WMTS tile store configuration, they will all be based on one WMTS.

Let's have a look at an example:

.. code-block:: xml

  <RemoteWMTSTileStore xmlns="http://www.deegree.org/datasource/tile/remotewmts" configVersion="3.2.0">

    <RemoteWMTSId>wmts1</RemoteWMTSId>

    <TileDataSet>
      <Identifier>satellite</Identifier>
      <OutputFormat>image/png</OutputFormat>
      <TileMatrixSetId>EPSG:4326</TileMatrixSetId>
      <RequestParams>
        <Layer>SatelliteProvo</Layer>
        <Style>default</Style>
        <Format>image/png</Format>
        <TileMatrixSet>EPSG:4326</TileMatrixSet>
      </RequestParams>
    </TileDataSet>

  </RemoteWMTSTileStore>

* The remote WMTS id is mandatory, and must point to a WMTS type remote OWS resource
* The identifier for the tile data sets is optional, defaults to the value of the Layer request parameter
* The output format is relevant if you want to use this tile data set in a WMTS, defaults to the value of the Format request parameter
* The tile matrix set id references the local tile matrix set you want to use, defaults to the value of the TileMatrixSet request parameter
* The request params section specifies parameters to be used in the GetTile requests sent to the WMTS:
 * The layer parameter specifies the layer name to request
 * The style parameter specifies the style name to request
 * The format parameter specifies the image format to request
 * The tile matrix set parameter specifies the tile matrix set to request

Please note that you need a locally configured tile matrix set that corresponds exactly to the tile matrix set of the remote WMTS. They need not have the same identifier(s) (just configure the TileMatrixSetId option if they differ), but the structure (coordinate system, tile size, number of tiles per matrix etc.) needs to be identical.

Additionally you can specify default and override values for request parameters within the request params block. Just add ``Parameter`` tags as described in the :ref:`anchor-configuration-layer-request-options` layer chapter. The replacing/defaulting currently only works when you configure a WMTS on top of this tile store. Please note that the ``scope`` attribute cannot be configured here, since remote WMTS currently only handles ``GetTile`` requests anyway.
