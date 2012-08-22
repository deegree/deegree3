set clp=D:\java\projekte\deegree2
set code=6636

C:\Programme\Java\jdk1.5.0_06\bin\java -Xms500m -Xmx1200m -classpath .;%clp%\classes;%clp%\lib\jai\jai_codec.jar;%clp%\lib\jai\jai_core.jar;%clp%\lib\jai\mlibwrapper_jai.jar;%clp%\lib\batik\batik-transcoder.jar;%clp%\lib\batik\batik-util.jar;%clp%\lib\batik\batik-gvt.jar;%clp%\lib\batik\batik-extension.jar;%clp%\lib\batik\batik-ext.jar;%clp%\lib\batik\batik-awt-util.jar org.deegree.tools.raster.Text2Tiff -r 1 -h -c 3  D:\java\projekte\lgv_3d\resources\data\%code%.xyz D:\java\projekte\lgv_3d\resources\data\%code%
pause