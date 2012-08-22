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
<%@ page import="org.deegree.framework.version.*" %>
<%@ page import="org.deegree.ogcwebservices.wfs.capabilities.*" %>
<%@ page import="java.net.URL" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>deegree featureService</title>
        <link rel="stylesheet" type="text/css" href="css/deegree.css" />
    </head>
    <body>
        <table cellpadding="5" cellspacing="5" background="#CECECE">
            <tr>
                <td>
                    <a name="MENU"></a>
                    <table border="0" cellspacing="0" cellpadding="2" width="100%">
                        <tr valign="top">
                            <td colspan="3">
                                <p>Below is a list of all available featuretypes.</p>
                            </td>
                        </tr>
                        <tr valign="top">
                            <td width="30%"><strong><em>WFS FeatureTypes</em></strong></td>
                            <td>
                                <ul>
                                    <li><a href="#ftList">List of available FeatureTypes in deegree WFS</a></li>
                                </ul>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        <table border="1" cellspacing="0" cellpadding="2" width="100%">
            <colgroup>
                <col width="30%">
                <col width="65%">
                <col width="5%">
            </colgroup>
            <tr valign="top"  bgcolor="#d7deed">
                <td colspan="2">
                    <a name="ftList"></a>
                    <b>List of FeatureTypes</b><br />
                </td>
                <td><a href="#MENU"><b>^&nbsp;up</b></a></td>
            </tr>
            <tr valign="top">
                <td colspan="3">
                    <blockquote>
                    <%
                        WFSCapabilitiesDocument capsDoc = new WFSCapabilitiesDocument();
                        String u = "http://" + request.getServerName() + ":" + request.getServerPort() +
                                   "/" + request.getContextPath() +
                                   "/services?service=WFS&version=1.1.0&request=GetCapabilities";
        
                        capsDoc.load( new URL( u ) );
                        WFSCapabilities caps =  (WFSCapabilities)capsDoc.parseCapabilities();
                        WFSFeatureType[] types = caps.getFeatureTypeList().getFeatureTypes();
        
                        for ( int i = 0; i < types.length; i++ ) {
                            org.deegree.datatypes.QualifiedName qn = types[i].getName();
                            out.println( "<a href='" );
                            out.println( "services?service=WFS&version=1.1.0&request=DescribeFeatureType&typename=" );
                            out.println( qn.getPrefix() + ":" + qn.getLocalName() + "&namespace=xmlns(" + qn.getPrefix() + "=" +
                                         qn.getNamespace() + ")&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.1.1'>" );
                            out.println( qn.getLocalName() + "</a><br/>" );
                        }
                    %>
                    </blockquote>
                </td>
            </tr>
        </table>
    </body>
</html>