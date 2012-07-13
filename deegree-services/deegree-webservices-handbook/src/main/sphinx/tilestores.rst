.. _anchor-configuration-tilestore:

========================
Tile store configuration
========================

Tile stores are data sources that provide access to pre-rendered map tiles. The common use cases for tile stores are:

* Provide data for layers in the WMTS
* Provide data for tile layers in the WMS

The remainder of this chapter describes some relevant terms and the tile store configuration files in detail. You can access this configuration level by clicking on the ``tile stores`` link in the administration console. The configuration files have to be created or edited in the ``datasources/tile/`` directory of the deegree workspace.

--------------------------
Tile stores, tile data sets and tile matrix sets
--------------------------

A tile store is what you configure with one configuration file. It can contain a bunch of tile data sets. Other resources such as the tile layer configuration usually refer to a specific tile data set from a tile store.

The structure of a tile data set is determined by a reference to a tile matrix set. This distincion allows the user to focus about where the data is coming from, without being confused about the matrix structure.

The term tile matrix set has been coined deliberately to coincide with the same term from the `WMTS specification <http://www.opengeospatial.org/standards/wmts>`. The tile matrix sets (quads) from WMTS 1.0.0 and the INSPIRE ViewService 3.1 specs are already predefined, but custom tile matrix sets may be specified (see below).

Take note that it is not necessary to provide actual tiles for all tiles defined within the tile matrix set, a tile data set may contain a subset. The only requirement is that you need to fulfill the structure requirements (CRS, size of tiles, position of tiles in world coordinates, scale).

^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Pre-defined tile matrix sets
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The predefined tile matrix sets are currently:

* GoogleCRS84Quad (from WMTS 1.0.0)
* InspireCRS84Quad (from INSPIRE ViewService 3.1)

You can override standard definitions by placing an appropriately named file into the workspaces' datasources/tile/tilematrixset/ directory. It is recommended to always use lower case file names to avoid confusion.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
User-defined tile matrix sets
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

There are currently two ways to configure tile matrix sets. The first way is to state the structure of the matrices explicitly (described here), the second will extract the structure from a tiled GeoTIFF (BIGTIFF) file (possibly with overlays, described in the GeoTIFF section).

Like everything else in the deegree workspace, defining a tile matrix set means placing a configuration file into a standard location, in this case the datasources/tile/tilematrixset directory.

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

As you can see, the format is almost identical to the one from the WMTS capabilities documents. The tile matrix set is obviously defined for one coordinate system, and contains many tile matrices. Each tile matrix has its own identifier, a specific scale, an origin (the top left corner in world coordinates), defines a tile width/height in pixels and specifies how many tiles there are in x and y direction.

You do not need to explicitly specify the envelope, it will be calculated automatically from the values you provide.

------------------
GeoTIFF tile store
------------------

The GeoTIFF tile store can be used to configure tile data sets based on GeoTIFF/BIGTIFF files. The tile store is currently read-only. The requirements for the GeoTIFFs are:

* it must be created as BIGTIFF (eg. with GDAL using the -co BIGTIFF=YES option)
* it must be created as a tiled tiff (eg. with GDAL using the -co TILED=YES option)
* it can contain overviews (it is best to use a recent GDAL version >= 1.8.0, where you can use GDAL_TIFF_OVR_BLOCKSIZE to specify the overview tile size)
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
* obviously you need to point to the file
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

The file system tile store can be used to provide tiles from `tile cache <http://tilecache.org>` like directory hierarchies. This tile store is read-write.

Let's explain the configuration using an example:

.. code-block:: xml

  <FileSystemTileStore xmlns="http://www.deegree.org/datasource/tile/filesystem" configVersion="3.2.0">

    <TileDataSet>
      <Identifier>layer1</Identifier>
      <TileMatrixSetId>InspireCrs84Quad</TileMatrixSetId>
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
      <TileMatrixSetId>InspireCrs84Quad</TileMatrixSetId>
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

----------------------
Remote WMTS tile store
----------------------

TBD

