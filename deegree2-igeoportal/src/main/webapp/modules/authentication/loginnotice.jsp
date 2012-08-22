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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.deegree.i18n.Messages" %>
<%@ page import="java.util.Locale" %>
<%
    Locale loc = request.getLocale();
    String user = (String)request.getAttribute( "USER" );
%>
<html>
    <head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="author" content="lat/lon GmbH" />
        <meta name="KeyWords" content="OGC deegree iGeoPortal lat/lon" />
		<title>deegree iGeoPortal - login</title>
		<link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
        <script LANGUAGE="JavaScript1.2" TYPE="text/javascript">
            <!--
            function sendSessionID() {
                var s = "<methodCall><methodName>security:getSessionID</methodName>" + 
                        "<params><param><value><struct>" +
                        "<member><name>user</name><value><string><% out.print(user); %></string></value></member>" +
                        "</struct></value></param></params></methodCall>";
                        
                document.forms[0].method = "post";
                document.forms[0].action = "control?rpc=" + s; //HTTP connection all the way through
                document.forms[0].submit();
            }
            -->
        </script>
    </head>
    <body class="navi10b pLoginNotice">
        <!-- HEADER AREA -->
        <table>
            <tbody>
                <tr>
                    <td colspan="2">&nbsp;</td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td class="header"><%=Messages.get( loc, "IGEO_STD_LOGIN_NOTE_HEADER" ) %></td>
                </tr>
            </tbody>
        </table>
        <form action="javascript:sendSessionID();"></form>
        <!-- TEXT AREA -->
        <table>
            <tbody>
                <tr>
                    <td colspan="2">&nbsp;</td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td class="text"><%=Messages.get( loc, "IGEO_STD_LOGIN_NOTE", user ) %></td>
                </tr>
            </tbody>
        </table>
        <!-- BUTTON AREA -->
        <br /><br />
        <a href="javascript:sendSessionID();" class="buttonArea1" title="<%=Messages.get( loc, "IGEO_STD_BTN_PORTAL_TT" ) %>"><%=Messages.get( loc, "IGEO_STD_BTN_PORTAL" ) %></a>
    </body>
</html>
