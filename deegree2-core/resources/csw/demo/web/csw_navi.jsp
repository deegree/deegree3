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
<%
    String docPath = "http://download.deegree.org/deegree" + Version.getVersionNumber() + "/docs/csw";
    String lang = request.getLocale().getLanguage();   
    if ( lang == null || ( !(lang.equals("en"))  && !(lang.equals("de")) ) ) {
        lang = "en";
    }
    String userName = null;
    if ( request.getUserPrincipal() != null ) {
        userName = request.getUserPrincipal().getName();        
    }    
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
        <META HTTP-EQUIV="EXPIRES" CONTENT="0">
        <title>deegree catalogueService</title>
        <link rel="stylesheet" type="text/css" href="css/deegree.css" />
    </head>
    <body> 
<%
if ( session.getAttribute( "LOGGEDIN" ) == null ) {
%>         
        <table width="100%" border="1" cellspacing="0" cellpadding="3">
            <tr>
                <th>Login</th>
            </tr>
            <tr>
                <td>
                    <a href="loginRedirect.jsp" target="_top">login</a>
                </td>
            </tr>
        </table>
        <br/>
<%
} else {    
%>        
        <table width="100%" border="1" cellspacing="0" cellpadding="3">
            <tr>
                <th>Logout</th>
            </tr>
            <tr>
                <td>
                    <a href="logoutRedirect.jsp" target="_top">logout</a>
                </td>
            </tr>
        </table>
        <br/>
<%
}
%>
        <table width="100%" border="1" cellspacing="0" cellpadding="3">
            <tr>
                <th>HOME</th>
            </tr>
            <tr>
                <td>
                    <a href="csw_main.jsp" target="_top">Start Page</a>
                </td>
            </tr>
        </table>
        <br/>   
        <table width="100%" border="1" cellspacing="0" cellpadding="3">
            <tr>
                <th>Documentation online</th>
            </tr>
            <tr>
                <td class="menu">
                    <strong>CSW</strong><br />
                    <a href="<%=docPath %>/deegree_csw_2.0.2_documentation_en.pdf" target="_blank">deegree CSW</a> [PDF]<br />
                    <a href="<%=docPath %>/README.txt" target="main">readme</a> [TXT]<br />
                </td>
            </tr>
        </table>
        <br/>
        <table width="100%" border="1" cellspacing="0" cellpadding="3">
            <tr>
                <th>deegree online</th>
            </tr>
            <tr>
                <td class="menu">
                    <a href="http://deegree.org/" target="_blank">Home&nbsp;Page</a><br />
                    <a href="http://wiki.deegree.org" target="_blank">Wiki</a><br />
                    <a href="http://wiki.deegree.org/deegreeWiki/CatalogueManager" target="_blank">catalogueManager Wiki</a><br />
                    <a href="http://demo.deegree.org" target="_blank">Demo Installation</a><br />
                    <a href="http://wald.intevation.org/tracker/?group_id=27" target="_blank">Issue Tracker</a><br />
                    <a href="https://lists.sourceforge.net/lists/listinfo/deegree-users" target="_blank">Users&nbsp;Mailing&nbsp;List</a><br />
                    <a href="https://lists.sourceforge.net/lists/listinfo/deegree-devel" target="_blank">Developers&nbsp;Mailing&nbsp;List</a>
                </td>
            </tr>
        </table>        
    </body>
</html>