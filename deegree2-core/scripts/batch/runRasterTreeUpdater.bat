rem set environment variables
set root=D:\java\projekte\testwcs\lib

cd d:\java\source\deegree2\classes
C:\Programme\Java\jdk1.5.0_06\bin\jar cf %root%\deegree2.jar org

cd D:\java\projekte\testwcs

C:\Programme\Java\jdk1.5.0_06\bin\java -Xms300m -Xmx1000m -classpath .;%root%\deegree2.jar;%root%\acme.jar;%root%\batik-awt-util.jar;%root%\commons-beanutils-1.5.jar;%root%\commons-codec-1.3.jar;%root%\commons-collections-3.1.jar;%root%\commons-digester-1.7.jar;%root%\commons-discovery-0.2.jar;%root%\commons-logging.jar;%root%\jai_codec.jar;%root%\jai_core.jar;%root%\mlibwrapper_jai.jar;%root%\j3dcore.jar;%root%\j3dutils.jar;%root%\vecmath.jar;%root%\jts-1.8.jar;%root%\log4j-1.2.9.jar;%root%\axis.jar;%root%\jaxen-1.1-beta-8.jar;%root%\ehcache-1.2.0_03.jar org.deegree.tools.raster.RasterTreeUpdater -wcs D:\java\projekte\testwcs\data\tiles\wcs_utah_configuration.xml -verbose -mapFiles D:\java\projekte\testwcs\data\test_update.jpg
pause