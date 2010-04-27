= deegree 3 services =

This is a list of the dependencies of the deegree 3 services module.

@author last edited by: $Author$
@version $Revision$, $Date$ 

== modules ===

This part describes all deegree 3 modules that the services module depends on:

- deegree3_commons
- deegree3_dataaccess
- deegree3_processing
- deegree3_security

Theses modules are needed in order to compile this module.

== libraries ==

This part describes all libraries the commons module depends on. If you use one of these
libs please add your package to the "Used for:" list.

NOTE: Necessary libraries that are also used by one of the dependent modules are not listed here again.

=== servlet ===
Files: servlet-api-2.5.jar
URL: http://jcp.org/aboutJava/communityprocess/mrel/jsr154/index.html
Version: 2.5
Description: Java Servlet technology provides Web developers with a simple, consistent mechanism for extending the functionality of a Web server and for accessing existing business systems.
Used for: org.deegree.services.controller
Responsible: schneider
License: ?
Origin of files: https://cds.sun.com/is-bin/INTERSHOP.enfinity/WFS/CDS-CDS_JCP-Site/en_US/-/USD/ViewProductDetail-Start?ProductRef=servlet-2.5-mrel-eval-oth-JSpec@CDS-CDS_JCP

=== commons/apache/org/commons-codec-1.3.jar ===
Files: commons-codec-1.3.jar
URL: http://jakarta.apache.org/commons/codec/
Version: 1.3
Description: Commons Codec provides implementations of common encoders and decoders such as Base64, Hex, Phonetic and URLs.  
Used for: org.deegree.services.controller, handling multipart requests
Responsible: bezema
License: Apache License Version 2.0, January 2004 http://www.apache.org/licenses/
Origin of files: http://www.internet.bs/apache.org/commons/codec/binaries/commons-codec-1.3.tar.gz

=== org/apache/commons/commons-fileupload-1.2.1.jar ===
Files: commons-fileupload-1.2.1.jar
URL: http://commons.apache.org/fileupload/
Version: 1.2.1
Description: The Commons FileUpload package makes it easy to add robust, high-performance, file upload capability to your servlets and web applications.   
Used for: org.deegree.services.controller, handling multipart requests
Responsible: bezema
License: Apache License Version 2.0, January 2004 http://www.apache.org/licenses/
Origin of files: http://apache.eu.lucid.dk/commons/fileupload/binaries/commons-fileupload-1.2.1-bin.tar.gz

