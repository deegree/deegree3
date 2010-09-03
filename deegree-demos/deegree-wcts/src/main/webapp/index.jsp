<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- $HeadURL$
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
--%>
<%@ page import="java.util.*"%>
<%@ page import="org.deegree.commons.utils.DeegreeAALogoUtils"%>
<%@ page import="org.deegree.commons.version.DeegreeModuleInfo"%>
<%@ page import="org.deegree.services.controller.*"%>
<html>
    <head>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <title>deegree 3 services</title>
        <link rel="stylesheet" href="styles.css" />
    </head>
    <body><pre><%= DeegreeAALogoUtils.getAsString() %></pre>
...has been successfully installed on this server.

<br/><br/><br/>Version information<br/><br/>
  <%
  for (DeegreeModuleInfo moduleInfo : DeegreeModuleInfo.getRegisteredModules()) {
    out.println (" - " + moduleInfo  + "<br/>");
  }
%>

<br/><br/>Active services<br/><br/>
<%
  Map<String, AbstractOGCServiceController> nameToController = OGCFrontController.getServiceControllers();
  for (String name : nameToController.keySet()) {    
    out.print (" - " + name);
    if ("WPS".equals (name)) {
        out.print (" [<a href=" + name.toLowerCase() + "/index.jsp>details</a>]");
    }
    out.println ("<br/>");
  }
%>

<br/><br/>Request statistics<br/><br/>
- Dispatched: <%= FrontControllerStats.getDispatchedRequests()%> <br/>
- Active: <%= FrontControllerStats.getActiveRequests()%> <br/>
- Average: <%= FrontControllerStats.getAverageResponseTime()%> ms<br/>
- Maximum: <%= FrontControllerStats.getMaximumResponseTime()%> ms<br/>
<br/>[<a href="client/client.html">Send requests</a>]
    </body>
</html>
