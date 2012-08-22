deegree WFS ${deegree.version} - demo 

Requirements
- Tomcat 5.5.x (apart from 5.5.26) 
  Please set the Java memory to at least 500MB to avoid "java.lang.OutOfMemoryError: Java heap space"!
  Windows Tomcat Monitor: "Maximum memory pool" (500 MB) or for Windows add the following 
  entry to the start script of Apache Tomcat catalina.bat/catalina.sh  
  set JAVA_OPTS=-Xmx500m -Xms100m
  for Linux add 
  export JAVA_OPTS="-Xms128m -Xmx500m" 
  to catalina.sh
- Java 5

Additional libraries are needed if you plan to use Oracle as data backend. 
You need to add at least

- ojdbc*.jar
- sdoapi.jar

to the WEB-INF/lib directory.


The demo comes preconfigured with five featuretypes working directly out of the box, 
in case your tomcat runs on localhost and port 8080 is used. The datasources 
are shape file and hsqldb.

Just put the deegree-wfs.war archive into your $tomcat_home$/webapps directory and 
restart tomcat, in case it doesn't deploy the service automatically.
Afterwards the service can be tested via browser
${default.webapp.url}
The service itself is accessible at:
${default.online.resource}


For the Philosopher example (a complex gml application schema) or a WFS-G (WFS-Gazetteer)
you need a PostGIS 1.x DB. Please refer to the deegree WFS documentation for configuring 
this featuretype. 

Please refer to the documentation for further details. In case of questions etc. contact 
the user's list of the deegree project. Further information on the list can be found at 
www.deegree.org.

NOTE FOR OGC COMPLIANCE TESTING:
If you want to set up an OGC CITE compliant WFS, please check the description at 
http://wiki.deegree.org/deegreeWiki/HowToGetCITECompliantOGCWebServices


Bonn, ${release.date}
