set clp=D:\java\projekte\deegree2


java -Xms500m -Xmx1200m -classpath .;%clp%\lib\deegree2;%clp%\lib\jai\jai_codec.jar;%clp%\lib\jai\jai_core.jar;%clp%\lib\jai\mlibwrapper_jai.jar;%clp%\lib\batik\batik-transcoder.jar;%clp%\lib\batik\batik-util.jar;%clp%\lib\batik\batik-gvt.jar;%clp%\lib\batik\batik-extension.jar;%clp%\lib\batik\batik-ext.jar;%clp%\lib\batik\batik-awt-util.jar org.deegree.tools.raster.SimpleText2Tiff -rootDir D:\java\projekte\resources\data\dgm -extension XYZ -resolution 10 -offset 300 -scaleFactor 10
pause