<%-- $HeadURL$ --%>
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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.deegree.framework.version.*" %>
<%@ page import="org.deegree.portal.Constants" %>
<%@ page import="org.deegree.portal.context.ViewContext" %>
<%
    ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT ); 
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="author" content="lat/lon GmbH" />
        <meta name="KeyWords" content="deegree iGeoPortal lat/lon" />
        <title>deegree iGeoPortal standard edition</title>
        <link href="css/deegree.css" rel="stylesheet" type="text/css" />
    </head>
    <body class="pHeader">
        <table border="0">
            <tr>
                <td>
                    <a href="http://deegree.org" target="_blank">
                        <img border="0" src="./images/logo-deegree.png" alt="deegree" align="top" border="0" />
                    </a> 
                </td>
                <td width="150">&nbsp;</td>
                <td>
                    <span style="color:#555555;font-size:14pt;font-weight:bold;">
                        <%= vc.getGeneral().getTitle( )%> v<%=Version.getVersionNumber()%>
                    </span>
                </td>
            </tr>
        </table>
    </body>
</html>
