= deegree 3 core =

This is a list of the dependencies of the deegree 3 core module.

@author last edited by: $Author$
@version $Revision$, $Date$

== modules ===

This part lists all deegree 3 modules that the core module depends on:

The core module does not depend on any other deegree module. It is the root of the module hierarchy.

== libraries ==

This part describes all libraries the core module depends on. If you use one of these
libs please add your package to the "Used for:" list.

NOTE: Necessary libraries that are also used by one of the dependent modules are not listed here again.

=== JTS ===
Files: jts-1.10.jar
URL: http://tsusiatsoftware.net/jts/main.html
Version 1.1.0
Description:
Used for: Implementation of geometry model (org.deegree.geometry.standard).
License: LGPL 2.1
Origin of files: http://sourceforge.net/projects/jts-topo-suite/files/
Responsible: schneider

=== slf4j ===
Files: sl4fj-api-1.5.8.jar,slf4j-log4j12-1.5.8.jar,jcl-over-slf4j-1.5.8.jar
URL: http://www.slf4j.org/
Version: 1.5.8
Description: The Simple Logging Facade for Java or (SLF4J) is intended to serve as a simple facade for various logging APIs allowing to the end-user to plug in the desired implementation at deployment time.
Used for: everything
License: MIT (aka X11 License), compatible with GNU GPL, see http://www.slf4j.org/license.html
Origin of files: http://www.slf4j.org/dist/slf4j-1.5.8.tar.gz
Responsible: schneider

=== log4j ===
Files: log4j-1.2.15.jar
URL: http://logging.apache.org/log4j/1.2/index.html
Version: 1.2.15
Description: Logging implementation.
Comments: deegrees default logging implementation used by slf4j
Used for: everything (indirectly by slf4j)
Origin of files: http://logging.apache.org/log4j/1.2/download.html
License: The Apache Software License, Version 2.0
Responsible: otonnhofer

=== jai ===
Files: jai_codec.jar, jai_core.jar, mlibwrapper_jar.jar, jai_imageio.jar
URL: https://jai.dev.java.net/
Version: 1.1.3
Description: Java Advanced Imaging, high-level imaging API
Comments: mlibwrapper_jar contains native implementation of parts of jai
Used for: - org.deegree.crs.transformation (polynomial transformation)
          - org.deegree.dataaccess.jai (image/raster reading and writing)
Origin of files: https://jai.dev.java.net/binary-builds.html
License: Java Distribution License (JDL)
Responsible: rbezema, otonnhofer

=== commons httpclient ===
Files: commons-httpclient-3.1.jar
URL: http://hc.apache.org/httpclient-3.x/
Version: 3.1
Description: Jakarta Commons HTTP client
Used for: everything that accesses the web
Origin of files: http://hc.apache.org/downloads.cgi
License: The Apache Software License, Version 2.0
Responsible: aschmitz

=== commons pool ===
Files: commons-pool-1.4.jar
URL: http://commons.apache.org/pool/
Version: 1.4
Description: Apache Commons Pool
Used for: JDBC connection pooling (org.deegree.commons.jdbc.ConnectionPool)
Origin of files: http://commons.apache.org/pool/download_pool.cgi
License: The Apache Software License, Version 2.0
Responsible: mschneider

=== commons dbcp ===
Files: commons-dbcp-1.2.2.jar
URL: http://commons.apache.org/dbcp/
Version: 1.2.2
Description: Apache Commons DBCP
Used for: JDBC connection pooling (org.deegree.commons.jdbc.ConnectionPool)
Origin of files: http://commons.apache.org/downloads/download_dbcp.cgi
License: The Apache Software License, Version 2.0
Responsible: mschneider

=== commons math ===
Files: commons-math-2.0.jar
URL: http://commons.apache.org/math/
Version: 2.0
Description: Apache Commons Math
Used for: solving equations of linear systems (org.deegree.geometry.linearization.CurveLinearizer)
Origin of files: http://commons.apache.org/downloads/download_math.cgi
License: The Apache Software License, Version 2.0
Responsible: aionita

=== jaxb ===
Files: jaxb-impl-2.1.10.jar,jaxb-xjc-2.1.10.jar
URL: https://jaxb.dev.java.net/
Version: 2.1.10
Description: Jaxb is a tool which binds schema files to java classes and vice versa
Used for: binding of configuration schema files
Origin of files: jaxb distribution: https://jaxb.dev.java.net/2.1.10/JAXB2_20090206.jar
Licence: https://jaxb.dev.java.net/
Responsible: rbezema

=== ehcache ===
Files: ehcache-1.6.0.jar
URL: http://ehcache.sourceforge.net/
Version: 1.6.0
Description: Ehcache is a widely used java distributed cache for general purpose caching[..].
             It features memory and disk stores, replicate by copy and invalidate, listeners,
             cache loaders, cache extensions, cache exception handlers, a gzip caching servlet
             filter and much more...
Comments:
Used for: org.deegree.dataaccess.raster (caching of raster tiles)
License: The Apache Software License, Version 2.0
Responsible: rbezema

=== cup ===
Files: cup-0.10k.jar
URL: http://www2.cs.tum.edu/projects/cup/
Version: 0.10k
Description: CUP is a LALR Parser Generator
Used for: generation of small parsers
Origin of files: CUP distribution or files included in Debian 5.0.1: http://www2.cs.tum.edu/projects/cup/java_cup_v10k.tar.gz
Licence: Custom license
Responsible: aschmitz

=== derby ===

Files: derby-10.4.2.0.jar
URL: derby-10.4.2.0.jar
Version: 10.4.2
Description: Open source relational database implemented entirely in Java
Used for: CRS database, feature locking database
Origin of files: http://db.apache.org/derby/releases/release-10.4.2.0.html
Licence: The Apache Software License, Version 2.0
Responsible: mschneider

=== jogl/jogl-1.1.2-pre-20080523.jar ===
Files: jogl-1.1.2-pre-20080523.jar
URL: https://jogl.dev.java.net/#NIGHTLY
Version: 1.1.2
Description: Used for the rendering of opengl code in java
Used for: org.deegree.services.wpvs
Responsible: bezema
License: BSD licence
Origin of files: https://jogl.dev.java.net/#NIGHTLY

=== jogl/gluegen-rt-1.1.2-pre-20080523.jar ===
Files: jgluegen-rt-1.1.2-pre-20080523.jar
URL: https://jogl.dev.java.net/#NIGHTLY
Version: 1.1.2
Description: Used for the rendering of opengl code in java
Used for: org.deegree.services.wpvs
Responsible: bezema
License: BSD licence
Origin of files: https://jogl.dev.java.net/#NIGHTLY

=== Batik ===
Files: batik-anim.jar,batik-awt-util.jar,batik-bridge.jar,batik-css.jar,batik-dom.jar,batik-ext.jar,batik-gvt.jar,batik-parser.jar,batik-script.jar,batik-svg-dom.jar,batik-util.jar,batik-xml.jar,xml-apis-ext.jar
URL: http://xmlgraphics.apache.org/batik/download.cgi
Version: 1.7
Description: Used for the reading/rendering of SVG images
Used for: org.deegree.core.rendering.r2d
Responsible: schmitz
License: Apache License 2.0
Origin of files: http://xmlgraphics.apache.org/batik/download.cgi
