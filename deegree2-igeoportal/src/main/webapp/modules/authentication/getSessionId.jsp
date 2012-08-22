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
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="author" content="lat/lon GmbH" />
        <meta name="KeyWords" content="deegree iGeoPortal lat/lon" />
        <title>deegree iGeoPortal - login</title>
        <link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
        <script LANGUAGE="JavaScript1.2" TYPE="text/javascript">
            <!--
            function gotoPortal() {
                opener.controller.vSessionKeeper.id = '<% out.print( request.getAttribute( "SESSIONID" ) ); %>';

                var tmp = window.location.pathname.split( '/' );
                var s = '';
                for ( var i = 0; i < tmp.length-3; i++) {
                    s += (tmp[i] + '/' ); 
                } 
                url = window.location.protocol + '//' + window.location.host + s + 'control';

                // copy from WUP
                var env = opener.parent.controller.mapModel.getBoundingBox();
                var req = url + "?rpc=<?xml version='1.0' encoding='UTF-8'?><methodCall>" +
                          "<methodName>mapClient:contextSwitch</methodName><params><param><value><struct>"+
                          "<member><name>mapContext</name><value><string>" +
                          '<% out.print( request.getAttribute( "STARTCONTEXT" ) ); %>' +
                          "</string></value></member></struct></value></param></params></methodCall>";
                opener.parent.window.location.replace( req );
                opener.controller.vSessionKeeper.id = '<% out.print( request.getAttribute( "SESSIONID" ) ); %>';
                window.close();
            }
            // -->
        </script>
    </head>
    <body onload="gotoPortal(); window.close(); ">
    </body>
</html>
