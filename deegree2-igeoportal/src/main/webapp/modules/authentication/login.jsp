<%-- $HeadURL$ --%>
<%-- $Id$ --%>
<%-- 
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
            function take() {
                var name = document.getElementById("name").value;
                if ( name.length < 4 ) {
                    alert( '<%=Messages.get( loc, "IGEO_STD_LOGIN_MSG_USR" ) %>' );
                    return;
                }
                var password = document.getElementById("password").value;
                if ( password.length < 4 ) {
                    alert( '<%=Messages.get( loc, "IGEO_STD_LOGIN_MSG_PWD" ) %>' );
                    return;
                }
                var s = "<?xml version='1.0' encoding='UTF-8'?>" + 
                		"<methodCall><methodName>security:login</methodName>" +
                        "<params><param><value><struct>"+
                        "<member><name>NAME</name><value><string>" + name + "</string></value></member>"+
                        "<member><name>PASSWORD</name><value><string>" + password + "</string></value></member>"+
                        "</struct></value></param></params></methodCall>";

                document.getElementById( "hidden_login" ).value = encodeURIComponent(s);

                document.forms[0].method = "post";
                document.forms[0].action = "control"; // HTTP
                //document.forms[0].action = "https://localhost:8443/igeoportal-std/control"; // HTTPS
                document.forms[0].submit();
            }
        // -->
        </script>
    </head>
    <body class="navi10b pLogin">
        <!-- HEADER AREA -->
        <table>
            <tbody>
                <tr>
                    <td colspan="2">&nbsp;</td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td class="header"><%=Messages.get( loc, "IGEO_STD_LOGIN_HEADER" ) %></td>
                </tr>
            </tbody>
        </table>
        <form method="post" action="javascript:take();" name="login" accept-charset="UTF-8" >
            <input id="hidden_login" type="hidden" name="rpc" value="">
            <!-- TEXT AREA -->
            <table>
                <tbody>
                    <tr>
                        <td colspan="3">&nbsp;</td>
                    </tr>
                    <tr>
                        <td width="30">&nbsp;</td>
                        <td width="50" class="text"><%=Messages.get( loc, "IGEO_STD_LOGIN_NAME" ) %></td>
                        <td>
                            <input type="text" name="name" id="name" value="" size="35" />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="3">&nbsp;</td>
                    </tr>
                    <tr>
                        <td width="30">&nbsp;</td>
                        <td width="50" class="text"><%=Messages.get( loc, "IGEO_STD_LOGIN_PWD" ) %></td>
                        <td>
                            <input type="password" name="password" id="password" value="" size="35" />
                        </td>
                    </tr>
                </tbody>
            </table>
            <!-- BUTTON AREA -->
            <br /><br />
            <a href="javascript:take()" class="buttonArea1" title="<%=Messages.get( loc, "IGEO_STD_BTN_LOGIN_TT" ) %>"><%=Messages.get( loc, "IGEO_STD_BTN_LOGIN" ) %></a>
            <a href="javascript:window.close()" class="buttonArea2" title="<%=Messages.get( loc, "IGEO_STD_BTN_CANCEL_TT" ) %>"><%=Messages.get( loc, "IGEO_STD_BTN_CANCEL" ) %></a>
            <a href="./register.html"  class="buttonArea3" title="<%=Messages.get( loc, "IGEO_STD_BTN_REGISTER_TT" ) %>"><%=Messages.get( loc, "IGEO_STD_BTN_REGISTER" ) %></a>
        </form>
    </body>
</html>
