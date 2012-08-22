deegree CSW ${deegree.version} - demo

Requirements
- Tomcat 5.5.x (apart from 5.5.26)
- Java 5

Additional libraries are needed if you plan to use Oracle as data backend. 
You need to add at least

- ojdbc*.jar
- sdoapi.jar

to the WEB-INF/lib directory.

The demo is preconfigured to run right out of the box and to support the ISO 19115 schema. 
In case your tomcat runs on localhost and the port 8080 is used you have to adapt nothing 
but to set up a database (postgis and oracle; as described in documentation in detail) using 
the delivered create scipts. And to insert the example datasets via the generic client 
${default.webapp.url}/client/client.html 
'Request' -> 'Transaction' -> 'insert_*.xml.

Just put the deegree-csw.war archive into your $tomcat_home/webapps directory and restart 
tomcat, in case it doesn't deploy the service automatically.
Afterward the service can be tested via browser
${default.webapp.url}
The service itself is accessible under:
${default.online.resource}


Please refer to the documentation for further details. In case of questions etc. contact 
the user's list of the deegree project. Further information on the list can be found at 
www.deegree.org.
  

Bonn, ${release.date}
