<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
<%@page import="org.deegree.commons.version.*"%>
<%@page import="org.deegree.services.controller.OGCFrontController"%>
<%@page import="org.deegree.services.controller.wps.WPSController"%>
<%@page import="org.deegree.services.controller.wps.ProcessletExecution"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.deegree.services.controller.wps.ProcessletExecution.ExecutionState"%>
<%@page import="java.text.SimpleDateFormat"%>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<title>WPS Process List Client</title>
<link rel="stylesheet" href="../styles.css" />
</head>
<body>
deegree 3 WPS process status
<br />
----------------------------
<br />
<br />
<%
    WPSController controller = (WPSController) OGCFrontController.getServiceController( WPSController.class );
    Collection<ProcessletExecution> allProcesses = controller.getExecutionManager().getAllProcesses();
    if ( allProcesses.size() > 0 ) {
%>
<table border="1" cellpadding="3" cellspacing="0">
	<tr>
		<th>Process</th>
		<th>Status</th>
		<th>Progress</th>
		<th>Started</th>
		<th>Finished</th>
		<th>Duration</th>
	</tr>
	<%
	    SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

	        for ( ProcessletExecution p : allProcesses ) {
	            String durationStr = "";
	            long duration = -1;
	            if ( p.getFinishTime() > 0 ) {
	                duration = p.getFinishTime() - p.getStartTime();
	            } else if ( p.getStartTime() > 0 ) {
	                duration = new java.util.Date().getTime() - p.getStartTime();
	            }
	            if ( duration >= 0 ) {
	                duration /= 1000;
	                long seconds = duration % 60;
	                long minutes = ( duration % 3600 ) / 60;
	                long hours = duration / 3600;
	                durationStr = String.format( "%02d:%02d:%02d", hours, minutes, seconds );
	            }
	%>
	<tr align="center">
		<td><%=p.getProcessId().toString()%></td>
		<td><%=p.getExecutionState().toString()%></td>
		<td><%=p.getExecutionState() == ProcessletExecution.ExecutionState.SUCCEEDED ? 100 : p.getPercentCompleted()%>%</td>
		<td><%=df.format( p.getStartTime() )%></td>
		<td><%=p.getFinishTime() > 0 ? df.format( p.getFinishTime() ) : "-"%></td>
		<td><%=durationStr%></td>
	</tr>
	<%
	    }
	%>
</table>
<%
    } else {
%>
No processes have been executed so far.
<%
    }
%>
</body>
</html>
