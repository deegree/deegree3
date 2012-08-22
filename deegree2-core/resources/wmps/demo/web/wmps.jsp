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
<%@ page language="java" contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="org.deegree.framework.version.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>deegree <%=Version.getVersionNumber()%></title>
        <link rel="stylesheet" type="text/css" href="css/deegree.css" />
    </head>
    <body leftmargin="10" rightmargin="10" topmargin="0" marginwidth="0" marginheight="0" link="#2D2A63" vlink="#2D2A63" alink="#E31952">
        <table width="100%">
            <colgroup>
                <col width="250">
                <col width="20">
                <col width="*">
            </colgroup>
            <!-- Header -->
            <tr>
                <td valign="top"colspan="3">
                    <iframe name="header" frameborder="0" src="deegree_header.jsp" scrolling="auto" width="100%" height="115px"></iframe>
                </td>
            </tr>
            <!-- Content -->
            <tr>
                <!-- Table of Contents -->
                <td valign="top" style="width:250px">
                    <iframe name="navi" frameborder="0" src="wmps_navi.jsp" scrolling="auto" width="100%" height="650"></iframe>
                </td>
                <td style="width:20px">&nbsp;</td>
                <!-- Body -->
                <td align="left" valign="top">
                    <iframe name="main" frameborder="0" src="wmps_main.html" scrolling="auto" width="100%" height="650"></iframe>
                </td>
            </tr>
        </table>
    </body>
</html>