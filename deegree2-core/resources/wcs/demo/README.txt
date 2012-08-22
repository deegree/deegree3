deegree WCS ${deegree.version} - demo

Requirements
- Tomcat 5.5.x (apart from 5.5.26)
- Java 5

Additional libraries are needed if you plan to use Oracle as data backend. 
You need to add at least

- ojdbc*.jar
- sdoapi.jar

to the WEB-INF/lib directory.

The demo is preconfigured with one aerial image working directly out of the box, in 
case your tomcat runs on localhost and the port 8080 is used. 

Just put the deegree-wcs.war archive into your $tomcat_home$/webapps directory and 
restart tomcat, in case it doesn't deploy the service automatically.
Afterwards the service can be checked via browser
${default.webapp.url}
The service itself is accessible under:
${default.online.resource}

Please refer to the documentation for further details. In case of questions etc. contact 
the user's list of the deegree project. Further information on the list can be found at 
www.deegree.org.

NOTE FOR OGC COMPLIANCE TESTING:
If you want to set up an OGC CITE compliant WCS, please check the description at
http://wiki.deegree.org/deegreeWiki/HowToGetCITECompliantOGCWebServices

Bonn, ${release.date}
