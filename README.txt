= deegree 3 commons =

This is the documentation for the deegree 3 commons module.

@author last edited by: $Author: $
@version $Revision: $, $Date: $ 

== libraries ==

This part describes all libraries the commons module depends on. If you use one of these
libs please add your package to the "Used for:" list.


=== commons-logging ===
Lib: commons-loggin-1.1.1.jar
URL: http://commons.apache.org/logging/
Version: 1.1.1
Description: The Logging package is an ultra-thin bridge between different logging implementations.
Used for: everything
Responsible: otonnhofer

=== log4j ===
Lib: log4j-1.2.15.jar
URL: http://logging.apache.org/log4j/1.2/index.html
Version: 1.2.15
Description: Logging implementation.
Comments: deegrees default logging impelemtation used by commons-logging
Used for: everything (indirect by commons-logging)
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