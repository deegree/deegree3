deegree WMS ${deegree.version} - demo

Requirements
- Tomcat 5.5.x (apart from 5.5.26)
- Java 5

Additional libraries are needed if you plan to use Oracle as data backend. 
You need to add at least

- ojdbc*.jar
- sdoapi.jar

to the WEB-INF/lib directory.

The demo is preconfigured with several layers originating from a local WFS, local WCS 
and remote WMS and should work directly out of the box, in case your tomcat runs on 
localhost and the port 8080 is used. 

Just put the deegree-wms.war archive into your $tomcat_home$/webapps directory and 
restart tomcat, in case it doesn't deploy the service automatically.
Afterwards the service can be checked via browser
${default.webapp.url}
The service itself is accessible under:
${default.online.resource}

Please refer to the documentation for further details. In case of questions etc. contact 
the user's list of the deegree project. Further information on the list can be found at 
www.deegree.org.

NOTE FOR OGC COMPLIANCE TESTING: To set up a deegree WMS service which runs successfully 
against the OGC reference implementation test suite, please reference the appropriate 
configuration files ($WMS_HOME/WEB-INF/conf/wms/wms_1_1_1_reference_implementation_configuration.xml
or wms_1_3_0_configuration_ref_impl.xml) in the $WMS_HOME/WEB-INF/web.xml deployment 
descriptor. 
Please also check the description at 
http://wiki.deegree.org/deegreeWiki/HowToGetCITECompliantOGCWebServices

Bonn, ${release.date}
