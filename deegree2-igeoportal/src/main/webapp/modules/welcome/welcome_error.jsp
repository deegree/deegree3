<%-- $HeadURL$ --%>
<%-- $Id$ --%>
<%-- 
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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%
	if ( exception == null && request.getAttribute( "javax.servlet.jsp.jspException" ) != null ) {
        exception = (Exception) request.getAttribute( "javax.servlet.jsp.jspException" );
    }
    if ( exception == null ) {
        exception = new Exception( "Exception UNAVAILABLE: Tracing Stack..." );
    }
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta http-equiv="expires" content="0" />
        <title>Error - <%=exception.getMessage()%></title>
        <link rel="stylesheet" href="../../css/deegree.css" />		
	</head>
	<body bgcolor="#FFFFFF" text="#000000" leftmargin="10" rightmargin="10" topmargin="0" 
		marginwidth="0" marginheight="0" link="#2D2A63" vlink="#2D2A63" alink="#E31952">
		
	    <jsp:include page="welcome_header.jsp" flush="true" />
		<table align="center" width="75%" cellpadding="5" cellspacing="5">
			<tr>
				<td bgcolor="lightgrey" align="center">
					<h1><font color="red">Error</font></h1>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td>
					<b>The following error has occured: <%=exception.getMessage() %></b>
				</td>
			</tr>
			<tr>
				<td>
					<p>Sorry, your request cannot be completed. The server run into the 
					following error:</p>
					<h2>Stack Trace:</h2>
					<pre>
		                  <% exception.printStackTrace( ); %>
					</pre>
					<p>Please notify the administrator. Thank you.</p>
				</td>
			</tr>
		</table>
	</body>
</html>
