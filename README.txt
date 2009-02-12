= deegree 3 commons =

This is a list of the dependencies of the deegree 3 commons module.

@author last edited by: $Author$
@version $Revision$, $Date$

== modules ===

This part lists all deegree 3 modules that the commons module depends on:

The commons module does not depend on any other deegree module. It is the root of the module hierarchy.

== libraries ==

This part describes all libraries the commons module depends on. If you use one of these
libs please add your package to the "Used for:" list.

NOTE: Necessary libraries that are also used by one of the dependent modules are not listed here again.


=== slf4j ===
Files: sl4fj-api-1.5.2.jar,slf4j-log4j12-1.5.2.jar,jcl-over-slf4j-1.5.2.jar
URL: http://www.slf4j.org/
Version: 1.5.2
Description: The Simple Logging Facade for Java or (SLF4J) is intended to serve as a simple facade for various logging APIs allowing to the end-user to plug in the desired implementation at deployment time.
Used for: everything
License: MIT (aka X11 License), compatible with GNU GPL, see http://www.slf4j.org/license.html
Origin of files: http://www.slf4j.org/dist/slf4j-1.5.2.tar.gz
Responsible: schneider

=== log4j ===
Files: log4j-1.2.15.jar
URL: http://logging.apache.org/log4j/1.2/index.html
Version: 1.2.15
Description: Logging implementation.
Comments: deegrees default logging implementation used by slf4j
Used for: everything (indirectly by slf4j)
Origin of Files: http://logging.apache.org/log4j/1.2/download.html
License: The Apache Software License, Version 2.0
Responsible: otonnhofer

=== jai ===
Files: jai_codec.jar, jai_core.jar, mlibwrapper_jar.jar
URL: https://jai.dev.java.net/
Version: 1.1.3
Description: Java Advanced Imaging, high-level imaging API
Comments: mlibwrapper_jar contains native implementation of parts of jai
Used for: - org.deegree.model.crs.transformation (polynomial transformation)
          - org.deegree.dataaccess.jai (image/raster reading and writing)
Origin of Files: https://jai.dev.java.net/binary-builds.html
License: Java Distribution License (JDL)
Responsible: rbezema, otonnhofer

=== commons ===
Files: commons-httpclient-3.1.jar
URL: http://hc.apache.org/httpclient-3.x/
Version: 3.1
Description: Jakarta Commons HTTP client
Used for: everything that accesses the web
Origin of Files: http://hc.apache.org/downloads.cgi
License: The Apache Software License, Version 2.0
Responsible: aschmitz

=== jaxb ===
Files: jaxb-impl-2.1.10.jar,jaxb-xjc-2.1.10.jar
URL: https://jaxb.dev.java.net/
Version: 2.1.10
Description: Jaxb is a tool which binds schema files to java classes and vice versa
Used for: binding of configuration schema files
Origin of Files: jaxb distribution: https://jaxb.dev.java.net/2.1.10/JAXB2_20090206.jar
Licence: https://jaxb.dev.java.net/
Responsible: rbezema
