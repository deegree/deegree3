set clp="D:\java\source\deegree2"

C:\Programme\Java\jdk1.5.0_06\bin\java -Xms500m -Xmx1200m -classpath .;%clp%\classes;%clp%\lib\jai\jai_codec.jar;%clp%\lib\jai\jai_core.jar;%clp%\lib\jai\mlibwrapper_jai.jar;%clp%\lib\batik\batik-transcoder.jar;%clp%\lib\batik\batik-util.jar;%clp%\lib\batik\batik-gvt.jar;%clp%\lib\batik\batik-extension.jar;%clp%\lib\batik\batik-ext.jar;%clp%\lib\batik\batik-awt-util.jar;%clp%\lib\log4j\log4j-1.2.9.jar org.deegree.tools.raster.RasterSplitter -tileWidth 2000 -tileHeight 2000 -format png -outDir e:/temp -rootDir D:/data/raster/europe -subDirs true
pause