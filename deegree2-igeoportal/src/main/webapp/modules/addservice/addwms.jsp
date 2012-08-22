<!-- $HeadURL$ -->
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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.deegree.i18n.Messages" %>
<%@ page import="java.util.Locale" %>
<%
    Locale loc = request.getLocale();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="author" content="lat/lon GmbH" />
        <meta name="KeyWords" content="deegree iGeoPortal lat/lon" />
        <title>deegree iGeoPortal</title>
        <link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
        <link rel="stylesheet" type="text/css" href="../../javascript/ext-3.3.1/resources/css/ext-all.css" />
        <link rel="stylesheet" type="text/css" title="blue"  href="../../javascript/ext-3.3.1/resources/css/xtheme-blue.css" /> 
        <link rel="stylesheet" type="text/css" title="gray"  href="../../javascript/ext-3.3.1/resources/css/xtheme-gray.css" />
        <link rel="stylesheet" type="text/css" title="black" href="../../javascript/ext-3.3.1/resources/css/xtheme-access.css" />
        <script type="text/javascript" src="../../javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="../../javascript/ext-3.3.1/ext-all.js"></script>
        <script LANGUAGE="JavaScript1.2" TYPE="text/javascript">
            var version = '1.1.1';
            // if version is not set, the highest version supported by the WMS is taken
            
             /**
            * initializing ExtJS GUI elements. At the moment just Ext.Button elements will be used
            * In future version this dialog will be completly reimplemented as an ExtJS to avoid
            * opening an additional browser window
            */
            function initGUI() {
                 Ext.QuickTips.init();   
                 new Ext.Button({
                     tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_CANCEL_TT" ) %>',
                     text: '<%=Messages.get( loc, "IGEO_STD_BTN_CANCEL" ) %>',
                     renderTo: 'buttonArea2',
                     width: 100,
                     height: 25,
                     handler: function(toggled){                               
                    	 window.close();
                     }
                 });
                 
                 new Ext.Button({
                     tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_CONTINUE_TT" ) %>',
                     text: '<%=Messages.get( loc, "IGEO_STD_BTN_CONTINUE" ) %>',
                     renderTo: 'buttonArea1',
                     width: 100,
                     height: 25,
                     handler: function(toggled){                               
                    	 take();
                     }
                 });
                 
            }

            function take() {
            	 var tmp = window.location.pathname.split( '/' );
                 var s = '';
                 for ( var i = 0; i < tmp.length-3; i++) {
                     s += (tmp[i] + '/' ); 
                 } 
                 var url = window.location.protocol + '//' + window.location.host + s + 'control';
                
                var wmsurl = document.getElementById( "wmsurl" ).value;
                if ( wmsurl.length < 12 ) {
                    alert( wmsurl + " is not a valid URL to access a WMS" );
                    return;
                }
                
                s = "<methodCall><methodName>GetWMSLayer</methodName>" +
                    "<params><param><value><struct>" +
                    "<member><name>VERSION</name><value><string>" + version + "</string></value></member>"+
                    "<member><name>WMSURL</name><value><string>" + wmsurl.replace( /&/g, "&amp;") + "</string></value></member>";
                        
				if(  opener.controller.vSessionKeeper != null && opener.controller.vSessionKeeper.id != null ) {
					s += "<member><name>SESSIONID</name><value><string>" + opener.controller.vSessionKeeper.id  + "</string></value></member>";
				}
				s += "</struct></value></param></params></methodCall>";

				document.getElementById( "rpc" ).value = s;
                document.forms[0].method = "post";
                document.forms[0].action = url;
                document.forms[0].submit();
            }

            function change() {
                var list = document.getElementById( "knownwms" );
                for (var i = 0; i < list.options.length;i++ ) {
                    if ( list.options[i].selected ) {
                        document.getElementById( "wmsurl" ).value = list.options[i].value;
                        break;
                    }
                }
            }

            
        </script>
    </head>
    <body class="pAddWMS" onload="initGUI()">
        <!-- HEADER AREA -->
        <table>
            <tbody>
                <tr>
                    <td colspan="2">&nbsp;</td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td class="header"><%=Messages.get( loc, "IGEO_STD_ADDWMS_HEADER" ) %></td>
                </tr>
            </tbody>
        </table>
        <form action="javascript:take();">
            <input type="hidden" id="rpc" name="rpc" value="">
            <!-- TEXT AREA -->
            <table>
                <tr>
                    <td colspan="4">&nbsp;</td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td width="120" valign="top" class="text"><%=Messages.get( loc, "IGEO_STD_ADDWMS_KNOWN" ) %></td>
                    <td width="*">
                        <select id="knownwms" onchange="change()" style="width: 340px;" size="15">
                            <jsp:include page="addwms.list" flush="true"/>
                            <!-- 
                            all wms entries made in addwms.list are displayed here
                            -->
                        </select>
                    </td>
                    <td width="30">&nbsp;</td>
                </tr>
                <tr>
                    <td colspan="4">&nbsp;</td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td width="120" class="text"><%=Messages.get( loc, "IGEO_STD_ADDWMS_URL" ) %></td>
                    <td width="*"><input style="color:#111111; width: 340px;" type="text" name="wmsurl" id="wmsurl" value="http://" /></td>
                    <td width="30">&nbsp;</td>
                </tr>
                <!--
                <tr>
                    <td colspan="4">&nbsp;</td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td width="120" class="text">WMS Version:</td>
                    <td width="*">
                        <input type="radio" name="version" value="1.1.0" checked="checked" onchange="setVersion(this)" />1.1.0
                        <input type="radio" name="version" value="1.1.1" onchange="setVersion(this)" />1.1.1
                    </td>
                    <td width="30">&nbsp;</td>
                </tr>
                -->
            </table>
            <!-- BUTTON AREA -->
            <br /><br />
            <table>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td>
                        <div id="buttonArea1"></div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td>
                        <div id="buttonArea2"></div>
                    </td>
                </tr>
            </table>
        </form>
    </body>
</html>