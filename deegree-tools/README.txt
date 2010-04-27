= deegree 3 tools =

This is a list of the dependencies of the deegree 3 tools module.

@author last edited by: $Author$
@version $Revision$, $Date$ 

== modules ===

This part describes all deegree 3 modules that the tools module depends on:

- deegree-core
- deegree-services

== libraries ==

This part describes all libraries the tools module depends on. If you use one of these
libs please add your package to the "Used for:" list.

NOTE: Necessary libraries that are also used by one of the dependent modules are not listed here again.

=== commons-cli ===
Lib: commons-cli-1.1+CLI-149.jar
URL: http://commons.apache.org/logging/
Version: 1.1
Description: The Apache Commons CLI library provides an API for processing command line interfaces.
Comments: This lib is the plain 1.1 version plus the official fix for CLI-149 
          (https://issues.apache.org/jira/browse/CLI-149)
Used for: command line tools like org.deegree.tools.raster.RTBClient
Responsible: otonnhofer

=== jd3 ===
Lib: j3dcore-1.5.2.jar and j3dutils-1.5.2.jar
URL: https://java3d.dev.java.net/binary-builds.html
Version: 1.5.2
Description: 3d support in java
Used for: needed for the import of vrml-buildings
licence: (utils) Berkeley Software Distribution (BSD) License, j3d-core  project is licensed under the open source GNU General Public License (GPL), version 2, with the CLASSPATH exception. 
Responsible: rbezema

=== j3d-vrml97 ===
Lib: j3d-vrml97.jar 
update: rb: j3d-vrml97_06.04.20_deegree.jar, patched against latest stable version https://j3d-vrml97.dev.java.net/files/documents/2124/33195/j3d-vrml97-06-04-20.tar.gz
find patches in the META-INF dir of the jar. I filed an issue (number 28 on https://j3d-vrml97.dev.java.net/issues/) as well
URL: https://j3d-vrml97.dev.java.net/
Version: 20 04 2006, latest stable
Description: 3d support in java
Used for: import of vrml-buildings
licence: Berkeley Software Distribution (BSD) License 
Responsible: rbezema