= deegree 3 commons =

This is the documentation for the deegree 3 commons module.

@author last edited by: $Author: $
@version $Revision: $, $Date: $ 

== libraries ==

This part describes all libraries the commons module depends on. If you use one of these
libs please add your package to the "Used for:" list.


=== slf4j ===
Lib: sl4fj-api-1.5.2.jar,slf4j-log4j12-1.5.2.jar,jcl-over-slf4j-1.5.2.jar
URL: http://www.slf4j.org/
Version: 1.5.2
Description: The Simple Logging Facade for Java or (SLF4J) is intended to serve as a simple facade for various logging APIs allowing to the end-user to plug in the desired implementation at deployment time.
Used for: everything
Responsible: schneider

=== log4j ===
Lib: log4j-1.2.15.jar
URL: http://logging.apache.org/log4j/1.2/index.html
Version: 1.2.15
Description: Logging implementation.
Comments: deegrees default logging implementation used by slf4j
Used for: everything (indirectly by slf4j)
Responsible: otonnhofer

=== jai ===
Lib: jai_codec.jar, jai_core.jar, mlibwrapper_jar.jar
URL: https://jai.dev.java.net/
Version: 1.1.3
Description: Java Advanced Imaging, high-level imaging API
Comments: mlibwrapper_jar contains native implementation of parts of jai
Used for: - org.deegree.model.crs.transformation (polynomial transformation) 
          - org.deegree.dataaccess.jai (image/raster reading and writing)
Responsible: rbezema, otonnhofer