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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
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
        <meta name="KeyWords" content="deegree iGeoPortal lat/lon" />
        <title>deegree iGeoPortal</title>
        <link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
        <link rel="stylesheet" type="text/css" href="../../javascript/ext-3.3.1/resources/css/ext-all.css" />
        <script type="text/javascript" src="../../javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="../../javascript/ext-3.3.1/ext-all.js"></script>
        <script language="JavaScript1.2" type="text/javascript" src="../../javascript/rpc.js"></script>
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
                  tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_SAVE_TT" ) %>',
                  text: '<%=Messages.get( loc, "IGEO_STD_BTN_SAVE" ) %>',
                  renderTo: 'buttonArea0',
                  width: 100,
                  height: 25,
                  handler: function(toggled){                               
                      save();
                  }
              });

              new Ext.Button({
                  tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_CANCEL_TT" ) %>',
                  text: '<%=Messages.get( loc, "IGEO_STD_BTN_CANCEL" ) %>',
                  renderTo: 'buttonArea1',
                  width: 100,
                  height: 25,
                  handler: function(toggled){                               
                	  window.close();
                  }
              });
           }
        
            function getBBox(){
                return opener.parent.controller.mapModel.getBoundingBox();
            }

            function getLayerList(){
                var layerlist = '';
                var b = opener.parent.controller.mapModel.getLayerList();
                var lgroup = b.getLayerGroups();
                // ATTENTION: using fist layer list only
                var MAX = lgroup.length;
                var k = 0;
                for( var i = 0; i < MAX; i++ ){
                    var g = lgroup[i].getLayers();
                    for ( var j = 0; j < g.length; j++ ) {
                    	 if ( g[j].getName() != 'highlight' ) {
	                         // index k is layer order
	                         var v = g[j].isVisible() + "|" + k + "|" + lgroup[i].getServiceType() +
	                                 "|" + lgroup[i].getServiceName() + "|" + 
	                                 encodeURIComponent( lgroup[i].getServiceURL() ) +
	                                 "|" + g[j].getTitle() + "|" + g[j].isQueryable();
	                         layerlist += createMember( g[j].getName(), v, "string" );
	                         k++;
                    	 }
                    }
                }
                return layerlist;
            }

            function save() {
                var userSession = 5555;
                if ( opener.parent.controller.vSessionKeeper != null ) {
                    userSession = opener.parent.controller.vSessionKeeper.id;
                }

                // user shouldn't be allowed to choose a name...
                var filename = document.forms[0].filename.value;
                
                if( filename.length < 1 ){
                    alert('Please enter a valid file name.');
                    document.forms[0].filename.focus();
                    return;
                }
                var req= "<?xml version='1.0' encoding='UTF-8'?><methodCall>" +
                    "<methodName>mapClient:contextSave</methodName><params><param><value><struct>"+
                    createMember( "sessionID", userSession, "string" )  +
                    createMember( "newContext", filename, "string" )  +
                    createMember( "layerList", getLayerList(), "struct" ) ;
                var b = getBBox();
                req = req + "<member><name>boundingBox</name><value><struct>"+
                    createMember( "minx", b.minx, "double" ) +
                    createMember( "miny", b.miny, "double" ) +
                    createMember( "maxx", b.maxx, "double" ) +
                    createMember( "maxy", b.maxy, "double" ) +
                    "</struct></value></member>";
                req = req + "</struct></value></param></params></methodCall>";

                var tmp = window.location.pathname.split( '/' );
                var s = '';
                for ( var i = 0; i < tmp.length-3; i++) {
                    s += (tmp[i] + '/' ); 
                } 
                var url = window.location.protocol + '//' + window.location.host + s + 'control';

                document.getElementById( "RPC" ).value = encodeURIComponent( req );
                document.forms[0].method = "post";
                document.forms[0].action = url;
                document.forms[0].submit();       
            }
            -->
        </script>
    </head>
    <body class="pSaveContext" onload="initGUI();">
        <!-- HEADER AREA -->
        <table>
            <tbody>
                <tr>
                    <td colspan="2">&nbsp;</td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td class="header"><%=Messages.get( loc, "IGEO_STD_CNTXT_SAVE_HEADER" ) %></td>
                </tr>
            </tbody>
        </table>
        <form>
            <input id="RPC" type="hidden" name="rpc" value="">
            <!-- TEXT AREA -->
            <table>
                <tr>
                    <td colspan="4">&nbsp;</td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td width="120" class="text"><%=Messages.get( loc, "IGEO_STD_CNTXT_SAVE_LABLE" ) %></td>
                    <td width="*"><input type="text" style="color:#111111" name="filename" id="filename" value="new_context.xml" size="25"></input></td>
                    <td width="30">&nbsp;</td>
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
	                <td width="30">&nbsp;</td>
	                <td>
	                    <div id="buttonArea1"></div>
	                </td>                  
	            </tr>
	        </table>
        </form>
    </body>
</html>
