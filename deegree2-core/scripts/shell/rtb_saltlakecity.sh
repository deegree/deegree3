#!/bin/sh
# This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
# This script builds up an example RasterTree.
java -Xms300m -Xmx1000m -classpath .:../../classes:../../lib/deegree2.jar org.deegree.tools.raster.RasterTreeBuilder -outDir ../../data/utah/raster/saltlakecity -baseName saltlakecity -outputFormat jpg -maxTileSize 500 -noOfLevel 5 -srs EPSG:26912 -rootDir ../../data/utah/raster/orig