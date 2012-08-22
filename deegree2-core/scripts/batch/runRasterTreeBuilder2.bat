rem set environment variables
set root=D:\java\projekte\testwcs\lib

cd D:\java\projekte\testwcs
java -Xms300m -Xmx1000m -classpath .;%root%\deegree2.jar;%root%\acme.jar;%root%\batik-awt-util.jar;%root%\commons-beanutils-1.5.jar;%root%\commons-codec-1.3.jar;%root%\commons-collections-3.1.jar;%root%\commons-digester-1.7.jar;%root%\commons-discovery-0.2.jar;%root%\commons-logging.jar;%root%\jai_codec.jar;%root%\jai_core.jar;%root%\mlibwrapper_jai.jar;%root%\j3dcore.jar;%root%\j3dutils.jar;%root%\vecmath.jar;%root%\jts-1.8.jar;%root%\log4j-1.2.9.jar;%root%\axis.jar;%root%\jaxen-1.1-beta-8.jar;%root%\ehcache-1.2.0_03.jar org.deegree.tools.raster.RasterTreeBuilder -outDir D:\data\raster\city\out -baseName bn -outputFormat png -maxTileSize 500 -noOfLevel 2 -interpolation "Nearest Neighbor" -baseDir D:\data\raster\city\quell -dbaseFile D:\data\raster\city\dbf\7218.dbf -sortColumn REIHE -fileColumn DATEI -worldFileType outter  -resolution 0.09 -bgColor 0xFFFF00
pause