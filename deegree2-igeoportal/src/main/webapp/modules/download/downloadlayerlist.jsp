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
<%@ page import="org.deegree.i18n.Messages" %>
<%@ page import="org.deegree.portal.context.Layer" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%
    Locale loc = request.getLocale();
    Layer[] layers = (Layer[])request.getAttribute( "LAYERS" );
    String email = (String)request.getAttribute( "EMAIL" );
    List<String> formats = (List<String>)request.getAttribute( "FORMATS" );
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="author" content="lat/lon GmbH">
        <meta name="KeyWords" content="deegree iGeoPortal lat/lon" />
        <title>deegree iGeoPortal</title>
        <link href="./css/deegree.css" rel="stylesheet" type="text/css"/>
        <link href="./javascript/ext-3.3.1/resources/css/ext-all.css" rel="stylesheet" type="text/css">
        <script src="./javascript/ext-3.3.1/adapter/ext/ext-base.js" type="text/javascript"></script>
        <script src="./javascript/ext-3.3.1/ext-all.js" type="text/javascript"></script>
        <script LANGUAGE="JavaScript1.2" TYPE="text/javascript">
            <!--

            /**
            * initializing ExtJS GUI elements. At the moment just Ext.Button elements will be used
            * In future version this dialog will be completly reimplemented as an ExtJS to avoid
            * opening an additional browser window
            */
            function initGUI() {
            	 Ext.QuickTips.init();   
            	 new Ext.Button({
                     tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_INVERT" ) %>',
                     text: '<%=Messages.get( loc, "IGEO_STD_BTN_INVERT" ) %>',
                     renderTo: 'buttonArea0',
                     width: 100,
                     height: 25,
                     handler: function(toggled){                               
                    	 invert();
                     }
                 });
            	 
                 new Ext.Button({
                     tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_DWNLD" ) %>',
                     text: '<%=Messages.get( loc, "IGEO_STD_BTN_DWNLD" ) %>',
                     renderTo: 'buttonArea1',
                     width: 100,
                     height: 25,
                     handler: function(toggled){                               
                    	 download();
                     }
                 });

                 new Ext.Button({
                     tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_CANCEL" ) %>',
                     text: '<%=Messages.get( loc, "IGEO_STD_BTN_CANCEL" ) %>',
                     <%
                     if ( layers.length > 0 ) {
                         // just if layers available for this user to download
                     %>
                     renderTo: 'buttonArea2',
                     <%
                     } else {
                         // if layers available for this user to download another 
                         // target element for cancle button must be used
                     %>                     
                     renderTo: 'buttonArea3',
                     <%
                     }
                     %>
                     width: 100,
                     height: 25,
                     handler: function(toggled){                               
                    	 window.close();
                     }
                 });         
                
                 
            }

            function createMember( name, value, type ) {
                return "<member><name>"+ name +"</name><value><"+ type +">"+ value +"</"+ type +"></value></member>";
            }

            /**
            * Returns the current bounding box of the map
            */
            function getBBox(){
                return opener.parent.controller.mapModel.getBoundingBox();
            }
            
            /**
            * Return a list of the selected layers (whose checkboxes are checked)
            */
            function getLayerList(){
                var layerlist = '';
                var b = opener.parent.controller.mapModel.getLayerList();
                var lgroup = b.getLayerGroups();
                for( var i = 0; i < lgroup.length; i++ ){
                    var g = lgroup[i].getLayers();
                    for( var j = 0; j < g.length; j++ ){
                        var checked = document.getElementById(g[j].getName());
                        if( g[j].getDSResource() && g[j].getDSGeomType && g[j].getDSFeatureType 
                            && checked && checked.checked ) {
                        	
                            var key = g[j].getName() + "," + g[j].getTitle();
                            var value =  lgroup[i].getServiceURL();
                            
                            layerlist += createMember( key , value, "string" );
                        }
                    }
                }
                return layerlist;
            }

            function getEmail() {
                if ( document.getElementById( "email" ) == null ) {
                    return null;
                } else {                
                    return document.getElementById( "email" ).value;
            	}
            }
            
            function getFormat() {
            	var checkbox;
                var selectedFormat = "SHP"; // default value. do not change!
                <% 
                for ( String f : formats ) {
                    %>
                    checkbox = document.getElementById( "<%=f %>" );
                    if ( checkbox && checkbox.checked ) {
                        // is radio button. therefore only one can be chosen.
                        selectedFormat = checkbox.value;
                        return selectedFormat;                        
                    }
                    <%
                }
                %>
                return selectedFormat;
            }

            /**
            * Called when clicking the download button. It sends an rpc request to the server with a list of 
            * the layers that should be downloaded
            */
            function download() {
                var sessionID = null;
                if ( opener.parent.controller.vSessionKeeper != null ){
                    sessionID = opener.parent.controller.vSessionKeeper.id;
                }
                
		    	var req= "<?xml version='1.0' encoding='UTF-8'?>";
		     	req += "<methodCall><methodName>mapView:downloadFeatures</methodName>";
                req += "<params><param><value><struct>";
                req += createMember( "layerList", getLayerList(), "struct" );
                req += createMember( "sessionID", sessionID, "string" );
                req += createMember( "email", getEmail(), "string" );
                req += createMember( "format", getFormat(), "string" );

				var b = getBBox();
		      	req = req + "<member><name>boundingBox</name><value><struct>";
			    req += createMember("minx", b.minx, "double");
                req += createMember("miny", b.miny, "double");
                req += createMember("maxx", b.maxx, "double");
                req += createMember("maxy", b.maxy, "double");
                req += "</struct></value></member>";
		  		req = req + "</struct></value></param></params></methodCall>";

                //document.forms[0].action = "control?rpc=" + req;
                document.getElementById( "RPC" ).value = req;
                document.forms[0].method = "post";
                document.forms[0].action = "control";
                document.forms[0].submit();
			}

            function invert() {
                for ( var i = 0; i < document.forms[0].elements.length; i++ ) {
                    if ( document.forms[0].elements[i].type == 'checkbox' ) {
                        document.forms[0].elements[i].checked = !document.forms[0].elements[i].checked;
                    }
                }
            }
            
            // -->
        </script>
    </head>
    <body class="pDownloadLayerList" onload="initGUI();">
    <br></br>
        <table>
            <tbody>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td class="header" colspan="3"><%=Messages.get( loc, "IGEO_STD_DWNLD_HEADER" ) %></td>
                </tr>
                <tr>
                    <td colspan="4">&nbsp;</td>                    
                </tr>
                
                <%
                if ( layers.length > 0 ) {
                    // layers available for this user to download
                %>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td colspan="3"><p><%=Messages.get( loc, "IGEO_STD_DWNLD_AVAIL_LAYS" ) %></p></td>
                </tr>
                <tr>
                    <td colspan="4">&nbsp;</td>                    
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td colspan="3">
                        <form id="downloadform" method="post" action="javascript:download();">
                            <input id="RPC" type="hidden" name="rpc" value="" />
                        <%
                            for ( int i = 0; i < layers.length; i++ ) {
                                out.print( "<input type='checkbox' id='" + layers[i].getName() + "'>&nbsp;" );
                                out.print( layers[i].getTitle() );
                                out.println( "<br />" );
                            }
                        %>
                            <br />
                            <div id="buttonArea0"></div>
                            <br /><br />
                        <%
                            if ( formats.size() > 1 ) {
                                out.println( "<br />" );
                                out.println( Messages.get( loc, "IGEO_STD_DWNLD_SEL_FORMAT" ) );
                                for ( String f : formats ) {
                                    out.println( "<input type='radio' id='" + f + "' name='format' value='" + f + "'>&nbsp;" + f + "<br />" );
                                }    
                            }           
                            if ( email == null ) {
                                out.println( "<br />" );
                                out.println( Messages.get( loc, "IGEO_STD_DWNLD_EMAIL" ) );
                                out.println("<input type='text' id='email' size='60' maxlength='100' >");
                            }                        
                        %>
                        </form>
                    </td>
                </tr>
                <tr>
                    <td colspan="4">&nbsp;</td>
                </tr>
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
                <%
                } else {
                    // no layers available for this user and WebMapContext
                %>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td colspan="3">
                    <p><%=Messages.get( loc, "IGEO_STD_DWNLD_NOT_AVAILABLE" ) %></p>
                    </td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td colspan="3">
                        <div id="buttonArea3"></div>
                    </td>
                </tr>
                <%
                }
                %>
            </tbody>
        </table>
    </body>
</html>
