------------- INFO -------------
This file is part of deegree.
For copyright/license information, please visit http://www.deegree.org/license.
--------------------------------

deegree iGeoPortal standard edition ${deegree.version}  - demo

Requirements
- Tomcat 5.5.x (apart from 5.5.26)
- Java 5

The iGeoPortal demo is preconfigured with references to a demo data WMS accessible 
under http://testing.deegree.org/deegree-wms/serivces and therefore it works directly out 
of the box, in case your tomcat runs on localhost and the port 8080 is used. 

Just put the igeoportal-std.war archive into your $tomcat_home$/webapps directory and 
restart tomcat, in case it doesn't deploy the service automatically.
Afterwards the service can be addressed via your default webapplication url:

${default.webapp.url} 

(normally above url should be something like http://localhost:8080/igeoportal-std.)

Please refer to the documentation for further details. In case of questions etc. contact 
the user's list of the deegree project. Further information on the list can be found at 
www.deegree.org.

Bonn, ${release.date}



Please note:
If you have checked out this application from svn trunk, you need to download the latest 
deegree2.jar and add it to the WEB-INF/lib/ folder.

The current nightly build is available for download at 
http://download.deegree.org/deegree2_nightly/deegree2.jar

This nightly build is highly recommended, as new developments for this application may rely on 
the newest deegree2 library.
