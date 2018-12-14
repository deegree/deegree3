.. _anchor-configuration-coveragestore:

===============
Coverage stores
===============

Coverage stores are resources that provide access to raster data. The most common use case for coverage stores is to provide data for coverage layers. You can access this configuration level by clicking the **coverage stores** link in the service console. The corresponding resource configuration files are located in subdirectory **datasources/coverage/** of the active deegree workspace directory.

.. figure:: images/workspace-overview-coverage.png
   :figwidth: 80%
   :width: 80%
   :target: _images/workspace-overview-coverage.png

   Coverage store resources provide access to raster data

For raster data there are three different possible configurations. One is for <Raster> and one is for <MultiResolutionRaster>. The third possibility is for <Pyramid>. If you are not sure which one to use, you probably want the <Raster> configuration. 

------
Raster
------

The most common method to provide coverages with deegree, is to use Raster.
With the Raster configuration it is possible to provide single RasterFiles or a complete RasterDirectory directly.

Here are two examples showing RasterFile and RasterDirectory configuration:

.. code-block:: xml

  <Raster xmlns="http://www.deegree.org/datasource/coverage/raster" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.deegree.org/datasource/coverage/raster http://schemas.deegree.org/datasource/coverage/raster/3.0.0/raster.xsd" configVersion="3.4.0" originLocation="outer">
    <StorageCRS>EPSG:26912</StorageCRS>
    <RasterFile>../../../data/utah/raster/dem.tiff</RasterFile>
  </Raster>

.. code-block:: xml

  <Raster xmlns="http://www.deegree.org/datasource/coverage/raster" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.deegree.org/datasource/coverage/raster http://schemas.deegree.org/datasource/coverage/raster/3.0.0/raster.xsd" configVersion="3.4.0" originLocation="outer">
    <StorageCRS>EPSG:26912</StorageCRS>
    <RasterDirectory>../../../data/utah/raster/Satellite_Provo/</RasterDirectory>
  </Raster>

A Raster can have several attributes:

* The originLocation attribute can have the values center or outer to declare the pixel origin of the coverage.
* The nodata attribute can be optionally used to declare a nodata value.
* The readWorldFiles parameter can have the values true or false to indicate if worlfiles will be read. Default value is true.
* The StorageCRS paramter is optional but recommended. It contains the EPSG code of the coverage sources.
* The RasterFile and RasterDirectory parameters contain the path to your coverage sources. The RasterDirectory paramter can additionally have the recursive attribute with true and false as value to declare subdirectories to be included.

---------------------
MultiResolutionRaster
---------------------

A <MultiResolutionRaster> wraps single raster elements and adds a resolution for each raster. This means, depending on the resolution of the map a different raster source is used.

Here is an example for a MultiResolutionRaster:

.. code-block:: xml

  <MultiResolutionRaster xmlns="http://www.deegree.org/datasource/coverage/raster" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.deegree.org/datasource/coverage/raster http://schemas.deegree.org/datasource/coverage/raster/3.0.0/raster.xsd" configVersion="3.4.0" originLocation="outer">
    <StorageCRS>EPSG:26912</StorageCRS>
    <Resolution>
      <Raster configVersion="3.4.0" originLocation="outer" res="1.0">
        <StorageCRS>EPSG:26912</StorageCRS>
        <RasterFile>../../../data/utah/raster/dem.tiff</RasterFile>
      </Raster>
    </Resolution>
    <Resolution>
      <Raster configVersion="3.4.0" res="2.0">
        <StorageCRS>EPSG:26912</StorageCRS>
        <RasterDirectory>../../../data/utah/raster/Satellite_Provo/</RasterDirectory>
      </Raster>
    </Resolution>
  </MultiResolutionRaster>

* A MultiResolustionRaster contains at least one Resolution
* The Raster parameter has a res attribute. Its value is related to the provided resolution.
* The StorageCRS paramter is optional but recommended. It contains the EPSG code of the coverage sources.
* All elements and attributes from the Raster configuration can be used for the resolutions.
  
-------
Pyramid
-------

A <Pyramid> is used for deegree's support for raster pyramids. For this, it is required that the raster pyramid must be a GeoTIFF, containing the extent and coordinate system of the data. Overlays must be multiples of 2. This is best tested with source data being processed with GDAL.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Prerequisities for Pyramids
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

* Must be a GeoTiff as BigTiff
* Must be RGB or RGBA
* CRS must be contained
* Must be tiled
* Should have overviews where each overview must consist of 1/2 resolution

The following example shows, how to configure a coverage pyramid:

.. code-block:: xml

  <Pyramid xmlns="http://www.deegree.org/datasource/coverage/pyramid" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.deegree.org/datasource/coverage/pyramid http://schemas.deegree.org/datasource/coverage/raster/3.1.0/pyramid.xsd" configVersion="3.4.0">
    <PyramidFile>data/example.tif</PyramidFile>
    <CRS>EPSG:4326</CRS>
  </Pyramid>

* A Pyramid contains a PyramidFile parameter with the path to the pyramid as its value.
* A Pyramid contains a CRS parameter describing the source CRS of the pyramid as EPSG code.
* As in Raster, the nodata attribute can be optionally used to declare a nodata value.

----------------
Oracle GeoRaster
----------------

A <OracleGeoraster> is used to wrap a connection information to a singe Oracle GeoRaster element inside a Oracle Database.

To be able to use the module it is required that the Oracle GeoRaster libraries are available, see :ref:`anchor-db-libraries` for details.

The following example shows, how to configure a GeoRaster coverage (minmal required options):

.. code-block:: xml
  <OracleGeoraster configVersion="3.4.0"
    xmlns="http://www.deegree.org/datasource/coverage/oraclegeoraster"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.deegree.org/datasource/coverage/oraclegeoraster http://schemas.deegree.org/datasource/coverage/oraclegeoraster/3.4.0/oraclegeoraster.xsd">

    <JDBCConnId>oracle</JDBCConnId>
    <StorageCRS>EPSG:25832</StorageCRS>
    
    <Raster id="17" />
  
  </OracleGeoraster>

The second example shows a complete configuration, which will load faster because no database lookups are required to initiate the coverage store.

.. code-block:: xml

  <OracleGeoraster configVersion="3.4.0"
    xmlns="http://www.deegree.org/datasource/coverage/oraclegeoraster"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.deegree.org/datasource/coverage/oraclegeoraster http://schemas.deegree.org/datasource/coverage/oraclegeoraster/3.4.0/oraclegeoraster.xsd">

    <JDBCConnId>oracle</JDBCConnId>
    <StorageCRS>EPSG:31468</StorageCRS>
    
    <StorageBBox>
      <LowerCorner>4508000.0 5652000.0</LowerCorner>
      <UpperCorner>4518000.0 5642000.0</UpperCorner>
    </StorageBBox>
    
    <Raster id="17" maxLevel="7" rows="10000" columns="10000">
      <Table>RASTER</Table>
      <RDTTable>RASTER_RDT</RDTTable>
      <Column>IMAGE</Column>
    </Raster>
    
    <Bands>
      <RGB red="1" green="2" blue="3" />
    </Bands>
  </OracleGeoraster>

If your GeoRaster coverage only consist in a greyscale coverage or you only want to server a single band you could specifiy the following:

.. code-block:: xml
    <Bands>
      <Single>1</Single>
    </Bands>

.. table:: Options for ``<Raster>``

+-----------------------+-------------+---------+------------------------------------------------------------------------------+
| Option                | Cardinality | Value   | Description                                                                  |
+=======================+=============+=========+==============================================================================+
| ``@id``               | 1           | integer | Identifier of the specified Oracle GeoRaster object                          |
+-----------------------+-------------+---------+------------------------------------------------------------------------------+
| ``@maxLevel``         | 0..1        | integer | The number of pyramid levels, specify zero if no pyramid is available        |
+-----------------------+-------------+---------+------------------------------------------------------------------------------+
| ``@rows``             | 0..1        | integer | Number of rows of the GeoRaster                                              |
+-----------------------+-------------+---------+------------------------------------------------------------------------------+
| ``@columns``          | 0..1        | integer | Number of columns of the GeoRaster                                           |
+-----------------------+-------------+---------+------------------------------------------------------------------------------+
| ``<Table>``           | 0..1        | String  | Defines the name of table name which contains the GeoRaster object           |
+-----------------------+-------------+---------+------------------------------------------------------------------------------+
| ``<RDTTable>``        | 0..1        | String  | The name of the corresponding raster data table.                             |
+-----------------------+-------------+---------+------------------------------------------------------------------------------+
| ``<Column>``          | 0..1        | String  | The column name of the ``<Table>`` in which the ``SDO_GEORASTER`` is stored  |
+-----------------------+-------------+---------+------------------------------------------------------------------------------+