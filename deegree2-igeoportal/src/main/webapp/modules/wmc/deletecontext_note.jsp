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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.deegree.i18n.Messages" %>
<%@ page import="java.util.Locale" %>
<%
    Locale loc = request.getLocale();
	String context = (String)request.getAttribute( "CONTEXT" );
%>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="author" content="lat/lon GmbH" />
        <meta name="KeyWords" content="deegree iGeoPortal lat/lon" />
        <title>deegree iGeoPortal: delete confirmation</title>
        <link href="./css/deegree.css" rel="stylesheet" type="text/css" />
        <link rel="stylesheet" type="text/css" href="./javascript/ext-3.3.1/resources/css/ext-all.css" />
        <script type="text/javascript" src="./javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="./javascript/ext-3.3.1/ext-all.js"></script>
        <script language="JavaScript1.2" type="text/javascript">
        <!--	

        /**
         * initializing ExtJS GUI elements. At the moment just Ext.Button elements will be used
         * In future version this dialog will be completly reimplemented as an ExtJS to avoid
         * opening an additional browser window
         */
         function initGUI() {
              Ext.QuickTips.init();   
              new Ext.Button({
                  tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_DELETE_CNTXT_NOTE_TT" ) %>',
                  text: '<%=Messages.get( loc, "IGEO_STD_BTN_DELETE_CNTXT_NOTE" ) %>',
                  renderTo: 'buttonArea0',
                  width: 100,
                  height: 25,
                  handler: function(toggled){                               
                	  openLoadContext();
                  }
              });
          }
              
			function openLoadContext() {		
                var sessionID = null;
                if ( opener.parent.controller.vSessionKeeper != null ) {
                    sessionID = opener.parent.controller.vSessionKeeper.id
                }
                var s = "control?rpc=<?xml version='1.0' encoding='UTF-8'?><methodCall>" +
                        "<methodName>mapClient:listContexts</methodName><params><param><value><struct>"+
                        "<member><name>sessionID</name><value><string>" + sessionID + "</string></value></member>" +
                        "</struct></value></param></params></methodCall>";

                window.location.replace( s );
            }
            // -->
		</script>
	</head>
	<body class="pDeleteContextNote" onload="initGUI();">
		<!-- HEADER AREA -->
        <table>
            <tbody>
                <tr>
                    <td colspan="2">&nbsp;</td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td class="header"><%=Messages.get( loc, "IGEO_STD_CNTXT_DELETE_HEADER" ) %></td>
                </tr>
            </tbody>
        </table>
        <!-- TEXT AREA -->
        <table>
            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>
			<tr>
				<td width="30">&nbsp;</td>
				<td class="text"><%=Messages.get( loc, "IGEO_STD_CNTXT_DELETE_MSG", context  ) %></td>
			</tr>
		</table>
        <!-- BUTTON AREA -->
        <br/><br />
        <table>
            <tr>
                <td width="30">&nbsp;</td>
                <td>
                    <div id="buttonArea0"></div>
                </td>                
            </tr>
        </table>
	</body>
</html>
