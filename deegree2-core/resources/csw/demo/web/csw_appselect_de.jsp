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
if (userName == null  || "cmUser".equals( userName ) || "cmEditor".equals( userName ))  {
%>            
            <tr>
                <td valign="top" >
                    <a href="md_search.jsp" target="_top">catalogueManager Suche:</a>
                </td>
                <td>&nbsp;</td>
                <td align="left" valign="top">
                    Client-Applikation zur Suche nach und Darstellung von Metadaten
                </td>
            </tr>
            <tr>
                <td colspan="31">&nbsp;</td>
            </tr>
<%
}
%>            
<%
if ( "cmEditor".equals( userName ) && session.getAttribute( "LOGGEDIN" ) != null)  {
%> 
            <tr>
                <td valign="top" >
                    <a href="md_editor.jsp" target="_top">catalogueManager Editor:</a>
                </td>
                <td>&nbsp;</td>
                <td align="left" valign="top">
                    Client-Applikation zur Erfassung und Verwaltung von Metadaten
                </td>
            </tr>
            <tr>
                <td colspan="31">&nbsp;</td>
            </tr>
<%
}
%>            
<%
if ( "cmAdmin".equals( userName ) && session.getAttribute( "LOGGEDIN" ) != null)  {
%>            
            <tr>
                <td valign="top" >
                    <a href="./client/client.html" target="_top">Generischer Client:</a>
                </td>
                <td>&nbsp;</td>
                <td align="left" valign="top">
                     Generische Client-Applikation zum direkten Ausführen von XML-kodierten Anfragen gegen einen Katalogdienst. 
                </td>
            </tr>
             <tr>
                <td colspan="31">&nbsp;</td>
            </tr>
            <!-- 
            <tr>
                <td valign="top" >
                    <a href="javascript:alert( 'TODO' )" target="_top">catalogueManager Konfiguration:</a>
                </td>
                <td>&nbsp;</td>
                <td align="left" valign="top">
                     Client-Applikation zur Konfiguration des deegree catalogueManagers ... (TODO) 
                </td>
            </tr>
            <tr>
                <td colspan="31">&nbsp;</td>
            </tr>
             -->     
             
             <tr>
                <td valign="top" >
                    <a href="cswsetup.jsp" target="_top">catalogueManager Setup:</a>
                </td>
                <td>&nbsp;</td>
                <td align="left" valign="top">
                    Ermöglicht grundlegende Einstellung zur Datenbank und zum Katalogdienst.
                </td>
            </tr>
            <tr>
                <td colspan="31">&nbsp;</td>
            </tr>
<%
}
%>            
        </table>
        <br></br>
        <p>Bitte beachten Sie die folgenden Rollen und Benutzer. Abhängig von der Rolle sind die <b>deegree catalogueManager</b> Funktionen 
        Suche, Editieren und Setup freigeschaltet. 
        </p>
        <table id="overview" border="solid">
          <tr align="center">
            <th width="60px" align="center">Rolle</th>
            <th width="80px">Benutzer</th>
            <th width="100px">Passwort</th>
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
            <td align="center">Benutzer</td>
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
        Der <b>deegree catalogueManager</b> besteht aus Client und Server Anwendungen, die dem Management von ISO und INSPIRE 
        konformen Metadatensätzen dienen. Suchen, Zugreifen, Harvesten und Ändern von Daten- und Service-Metadaten erfolgt
        über HTTP/SOAP Schnittstellen. Metadaten können auf konfigurierbare Eigenschaften (Properties) durchsucht werden, 
        wie z.B. Title, Beschreibung, Thema etc.. Ferner können zur Eingrenzung der Ergebnismenge räumliche Einschränkungen 
        gemacht werden. Der enthaltene deegree CSW unterstützt Dublin Core und INSPIRE konforme ISO 19115/19119/19139 Metadaten Encodings. 
        Bei Verwendung des ISO Metadatenformats besteht die Möglichkeit, Daten- und Service-Metadaten zu koppeln. Dadurch kann 
        nach dem Finden eines Daten-Metadatensatzes ein entsprechende Service-Metadatensatz ermittelt werden, um die beschriebenen
        Daten mittels eines WMS anzuzeigen oder über einen WFS/WCS/SOS direkt auf sie zuzugreifen. 
        </p>
        <p>
        Die OGC CSW Spezifikation liegt in unterschiedlichen Versionen vor. Der deegree catalogueService unterstützt die Version 2.0.0, 
        2.0.1 und 2.0.2. Für CSW 2.0.2 wurde durch das OGC ein Applikationsprofil für ISO Metadaten definiert (OGC ISO Application 
        Profile for CSW 2.0.2 Specification), welches ebenfalls durch den deegree CSW unterstützt wird. Darüber hinaus werden INSPIRE 
        Spezifikationen für Metadaten und Suchdienste (discovery services) unterstützt.
        </p>
        <p>
        Als Datenhaltungkomponenten werden von deegree catalogueService die relationalen Datenbankmanagementsysteme Oracle und 
        PostGIS unterstützt. Metadaten werden hier objekt-relational in einem Datenbankschema abgebildet. 
        </p>
        <p>
        Der catalogueManager enthält zudem einen Client zur Suche nach Metadaten und zur Erfassung derselben. Beide Client
        sind intuitiv bedienbar, so dass ein Nutzer auch ohne vertiefte Kenntnisse von ISO 19xxx und INSPIRE in wenigen 
        Minuten in der Lage ist, entsprechende Metadaten zu erfassen.
        Neben dem in diesem Paket enthaltenen Metadateneditor kann auch ein deutlich mächtigerer im deegree Framework realisierter 
        Metadateneditor (tibesti) eingesetzt werden, der das rechte- und rollenabhängige, verteilte Erfassen, Ändern und 
        Verwalten von Metadatenbeständen erlaubt. 
        (<a target="_blank" href="http://wald.intevation.org/plugins/scmsvn/viewcvs.php/contrib/tibesti/trunk/?root=deegree">to SVN</a>.)
        </p>
    </body>
</html>