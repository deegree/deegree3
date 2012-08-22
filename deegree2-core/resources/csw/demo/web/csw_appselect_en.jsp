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
<%
String userName = null;
if ( request.getUserPrincipal() != null ) {
    userName = request.getUserPrincipal().getName();
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
        <META HTTP-EQUIV="EXPIRES" CONTENT="0">
        <title>deegree <%=Version.getVersionNumber()%></title>
        <link rel="stylesheet" type="text/css" href="css/metadataclient.css" />

         <style type="text/css">
          table {
          font-size: 12px;
          border-color: #c1cde5;
          }
        </style>
        
        
    </head>
    <body leftmargin="10" rightmargin="10" topmargin="0" marginwidth="0" marginheight="0" link="#2D2A63" vlink="#2D2A63" alink="#E31952">
        <table width="100%">
            <colgroup>
                <col width="200">
                <col width="20">
                <col width="*">
            </colgroup>
<%
if ( userName == null || "cmUser".equals( userName ) || "cmEditor".equals( userName ))  {
%>            
            <tr>
                <td valign="top" >
                    <a href="md_search.jsp" target="_top">catalogueManager search:</a>
                </td>
                <td>&nbsp;</td>
                <td align="left" valign="top">
                    client for searching metadata
                </td>
            </tr>
            <tr>
                <td colspan="31">&nbsp;</td>
            </tr>
<%
}
%>            
<%
if ( "cmEditor".equals( userName ) && session.getAttribute( "LOGGEDIN" ) != null )  {
%>             
            <tr>
                <td valign="top" >
                    <a href="md_editor.jsp" target="_top">catalogueManager editor:</a>
                </td>
                <td>&nbsp;</td>
                <td align="left" valign="top">
                    client for creating and managing metadata
                </td>
            </tr>
            <tr>
                <td colspan="31">&nbsp;</td>
            </tr>
<%
}
%>   
<%
if ( "cmAdmin".equals( userName ) && session.getAttribute( "LOGGEDIN" ) != null )  {
%>            
             <tr>
                <td valign="top" >
                    <a href="./client/client.html" target="_top">generic client:</a>
                </td>
                <td>&nbsp;</td>
                <td align="left" valign="top">
                    generic client application for performing XML encode requests against catalogue services. 
                </td>
            </tr>
            <tr>
                <td colspan="31">&nbsp;</td>
            </tr>            
            <tr>
                <td valign="top" >
                    <a href="cswsetup.jsp" target="_top">catalogueManager setup:</a>
                </td>
                <td>&nbsp;</td>
                <td align="left" valign="top">
                    basic database and application settings ...
                </td>
            </tr>
<%
}
%>            
            <tr>
                <td colspan="31">&nbsp;</td>
            </tr>
<!--             
            <tr>
                <td valign="top" >
                    <a href="javascript:alert( 'TODO' )" target="_top">catalogueManager configuration:</a>
                </td>
                <td>&nbsp;</td>
                <td align="left" valign="top">
                     configuration of deegree catalogueManager ... (TODO) 
                </td>
            </tr>
            <tr>
                <td colspan="31">&nbsp;</td>
            </tr>
-->                        
           
        </table>
        <br></br>
        
        <p>Please notice the following roles and users with <b>deegree catalogueManager</b>. Depending on the roles 
        the search, edit or setup functions will be available.
        </p>
       <table id="overview" border="1">
          <tr align="center">
            <th width="60px" align="center">Rolle</th>
            <th width="80px">User</th>
            <th width="100px">Password</th>
            <th width="60px">Search</th>
            <th width="40px">Edit</th>
            <th width="50px">Setup</th>
          </tr>
          <tr>
            <td align="center">Editor</td>
            <td align="center">cmEditor</td>
            <td align="center">-</td>
            <td align="center">x</td>
            <td align="center">x</td>
            <td align="center">-</td>
          </tr>
          <tr>
            <td align="center">User</td>
            <td align="center">cmUser</td>
            <td align="center">-</td>
            <td align="center">x</td>
            <td align="center">-</td>
            <td align="center">-</td>
          </tr>
          <tr>
            <td align="center">Admin</td>
            <td align="center">cmAdmin</td>
            <td align="center">not available with online demo</td>
            <td align="center">x</td>
            <td align="center">-</td>
            <td align="center">x</td>
          </tr>
        </table>
        
        <br></br>
        <p>
        <b>deegree catalogueManager</b> is a collection of client and server applications for managing ISO and INSPIRE 
        compliant metadata. It provides HTTP/SOAP interfaces for searching, accessing, harvesting and 
        manipulating metadata for geographic data and services. Metadata can be searched by their title, topic, date etc. 
        and the spatial extent of the described data/service. deegree supports Dublin Core and INSPIRE compliant ISO 19115/19119/19139 
        metadata encoding. If the ISO metadata format is used, data metadata and service metadata can be coupled. This 
        enables a user to first search for data metadata matching, for example, a specific topic and area and then 
        finding a WMS, WFS etc. serving the data described by the data metadata sets matching the initial search.
        </p>
        <p>
        The OGC CSW specification is available in different versions. deegree catalogueService supports versions 2.0.0, 
        2.0.1 and 2.0.2. For CSW 2.0.2 OGC has specified an application profile for ISO metadata (OGC ISO Application 
        Profile for CSW 2.0.2 Specification) which is also supported by deegree CSW. Another important topic covers the 
        INSPIRE specifications for metadata and discovery services. This is supported as far as specifications are 
        completed. There are no compliance tests made available for CSW by the OGC, yet.
        </p>
        <p>       
        deegree catalogueService comes with support for Oracle and PostGIS database backend.
        </p>
        <p> 
        The catalogueManager also contains a metadata editor as well as a web frontend for searching and displaying metadata. 
        Both components are designed to be easy to use without the requirement of detailed knowledge of ISO 19xxx or INSPIRE standards. 
        Beside the contained editor deegree also offers a more powerful but even more complex editor (tibesti) to create, 
        edit, update and  manage metadata directly on top of a database backend. (<a target="_blank" 
        href="http://wald.intevation.org/plugins/scmsvn/viewcvs.php/contrib/tibesti/trunk/?root=deegree">to SVN</a>.)        
        </p>
    </body>
</html>