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
<%@ page import="java.util.Locale" %>
<%
    Locale loc = request.getLocale();
    String name = (String)request.getAttribute( "WMSNAME" );
    String layertree = (String)request.getAttribute( "WMSLAYER" );
    String version = (String)request.getAttribute( "WMSVERSION" );
    String url = (String)request.getAttribute( "WMSURL" );
    String[] formats = (String[])request.getAttribute( "FORMATS" );
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="author" content="lat/lon GmbH" />
        <meta name="KeyWords" content="deegree iGeoPortal lat/lon" />
        <title>deegree iGeoPortal</title>
        <link href="./css/deegree.css" rel="stylesheet" type="text/css" />
        <link rel="stylesheet" type="text/css" href="./javascript/ext-3.3.1/resources/css/ext-all.css" />
        <link rel="stylesheet" type="text/css" title="blue"  href="./javascript/ext-3.3.1/resources/css/xtheme-blue.css" /> 
        <link rel="stylesheet" type="text/css" title="gray"  href="./javascript/ext-3.3.1/resources/css/xtheme-gray.css" />
        <link rel="stylesheet" type="text/css" title="black" href="./javascript/ext-3.3.1/resources/css/xtheme-access.css" />
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
                      tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_TAKE_TT" ) %>',
                      text: '<%=Messages.get( loc, "IGEO_STD_BTN_TAKE" ) %>',
                      renderTo: 'buttonArea1',
                      width: 100,
                      height: 25,
                      handler: function(toggled){                               
                          take();
                      }
                  });

                  new Ext.Button({
                      tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_INVERT_TT" ) %>',
                      text: '<%=Messages.get( loc, "IGEO_STD_BTN_INVERT" ) %>',
                      renderTo: 'buttonArea0',
                      width: 100,
                      height: 25,
                      handler: function(toggled){                               
                          invert();
                      }
                  });
                  
             }
            
            function getSelectedFormat() {
                var frms = document.getElementsByName('format');
                for (var i = 0; i < frms.length; i++) {
                    if ( frms[i].checked ) {
                        return frms[i].value;
                    }
                }
            }

            function take() {
                var version = '<%out.print(version);%>';
                var name = '<%out.print(name);%>';
                var url = '<%out.print(url);%>';
                var layers = new Array();
                var k = 0;
                for (var i = 0; i < document.forms[0].elements.length; i++) {
                    if ( document.forms[0].elements[i].type == 'checkbox') {
                        if ( document.forms[0].elements[i].checked ) {
                            var val = document.forms[0].elements[i].value;
                            var tmp = val.split("|");
                            layers[k] = new Array();
                            
                            layers[k]['name'] = tmp[0];
                            layers[k]['title'] = tmp[1];
                            layers[k]['queryable']  = (tmp[2] == 'true');
                            // add metadataURL to layer info
                            layers[k]['metadataURL'] = tmp[3];
                            // add scaleHint values (min, max) to layer info
                            layers[k]['minScale'] = tmp[4];
                            layers[k]['maxScale'] = tmp[5];
                            layers[k]['visible'] = false;
                            k++;
                        }
                    }
                }
                //In order not to cause an error with the wmsRequestFactory
                if ( layers.length > 0 ) {
                    opener.controller.addLayersToModel(name, url, version, layers, getSelectedFormat());
                }
                window.close();
            }

            function invert() {
                for (var i = 0; i < document.forms[0].elements.length; i++) {
                    if ( document.forms[0].elements[i].type == 'checkbox') {
                            document.forms[0].elements[i].checked = !document.forms[0].elements[i].checked;
                    }
                }
            }
            // -->
        </script>
    </head>
    <body class="pWMSLayerSelect" onload="initGUI()">
        <!-- HEADER AREA -->
        <table>
            <tbody>
                <tr>
                    <td colspan="2">&nbsp;</td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td class="header"><%=Messages.get( loc, "IGEO_STD_WMSLAYERSELECT_HEADER" ) %></td>
                </tr>
            </tbody>
        </table>
        <form action="">
            <!-- TEXT AREA -->
            <table>
                <tbody>
                    <tr>
                        <td colspan="2">&nbsp;</td>
                    </tr>
                    <tr>
                        <td width="30">&nbsp;</td>
                        <td width="*" class="text">
                            <%=Messages.get( loc, "IGEO_STD_WMSLAYERSELECT", name ) %>
                            <%out.print( layertree );%>
                        </td>
                    </tr>
                </tbody>
            </table>
            <br />
            <table>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td>
                        <div id="buttonArea0"></div>
                    </td>
                </tr>
            </table>      
            <br /><br /><br />
            <table>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td class="text"><%=Messages.get( loc, "IGEO_STD_WMSLAYERSELECT_FORMATS" ) %></td>
                </tr>
<%
                for (int i = 0; i < formats.length; i++) {
                    out.print( "<tr><td width='30' class='text'>&nbsp;</td><td>" );
                    out.print( "<input type='radio' name='format' value='" + formats[i] );
                    if ( i == 0) {
                        out.print( "' checked='checked" );
                    }
                    out.println( "' />" + formats[i] + "</td></tr>" );
                }
%>
                <tr>
                    <td colspan="2">&nbsp;</td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td class="text"><%=Messages.get( loc, "IGEO_STD_WMSLAYERSELECT_INFO", name ) %></td>
                </tr>
            </table>
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
            <br /><br /><br />
        </form>
    </body>
</html>
