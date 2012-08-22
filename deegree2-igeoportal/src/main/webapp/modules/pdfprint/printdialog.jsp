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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.deegree.enterprise.WebUtils" %>
<%@ page import="org.deegree.i18n.Messages" %>
<%@ page import="java.util.Locale" %>
<%
    Locale loc = request.getLocale();
    String path = WebUtils.getAbsoluteContextPath( request );
%>
<html>
    <head>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
        <meta name="author" content="lat/lon GmbH" />
        <meta name="KeyWords" content="deegree iGeoPortal lat/lon" />
        <title>PDF Print</title>
        <link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
        <script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="../../javascript/rpc.js"></script>
        <script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="../../javascript/utils.js"></script>
        <script type="text/javascript" charset="UTF-8">
        <!--
            var txtDoc = null;
            var template = "template_2";
            var scaleSelection = "currentBBOX";

            function swapPrintTemplate( list ) {
                template = list.value;
                var im = document.getElementById( "IMAGE" );
                im.src = '<%=path %>/modules/pdfprint/images/' + template + ".jpg";
                var ifr = document.getElementById( "TEXTFIELDS" );
                ifr.src = '<%=path %>/modules/pdfprint/print' + template + ".jsp";
            }

            function printMap(txt, asPDF) {
                var d = document.getElementById( "wait" );
                d.style.visibility = 'visible';
                
                var userSession = -1;
                if ( opener.controller.vSessionKeeper != null ) {
                    userSession = opener.controller.vSessionKeeper.id;
                }

                var mimeType = asPDF ? "application/pdf" : "image/png";
                var s = "<?xml version='1.0' encoding='UTF-8'?><methodCall><methodName>mapView:pdfprint</methodName><params>";

                // print params
                s = s + "<param><value><string>" + mimeType + "</string></value></param><param><value><struct>" +
                        createMember( "TEMPLATE", template, "string" );
                s = s + createMember( "DPI", "150", "string" );
       
                if ( getElement( "RADIO1" ).checked ) {
                	scaleSelection = getElement( "SCALEFIELD" ).value;
                }
                s = s + createMember( "SCALE", scaleSelection, "string" );

                for ( var i = 0; i < txt.length; i++ ) {
                    s = s + createMember( "TA:" + txt[i][0], "<![CDATA[" + txt[i][1] + "]]>", "string" ) ;
                }
                s = s + "</struct></value></param>";

                 // WMC relevant params
                s = s + "<param><value><struct>";
                s = s + createMember( "sessionID", userSession, "string" );
                s = s + createMember( "layerList", getLayerList(), "struct" ) ;

                var b = getBBox();
                s = s + "<member><name>boundingBox</name><value><struct>";
                s = s + createMember( "minx", b.minx, "double" );
                s = s + createMember( "miny", b.miny, "double" );
                s = s + createMember( "maxx", b.maxx, "double" );
                s = s + createMember( "maxy", b.maxy, "double" );
                s = s +"</struct></value></member></struct></value></param>";

                s = s + "</params></methodCall>";

                document.getElementById( "RPC" ).value = encodeURIComponent(s);
                document.forms[0].method = "post";
                document.forms[0].submit();
            }

            function getBBox(){
                return opener.controller.mapModel.getBoundingBox();
            }
            
            /**
             *  copied from savecontext.jsp
             *  Creates a list of all available layers
             */
            function getLayerList(){
                var layerlist = '';
                var b = opener.controller.mapModel.getLayerList();
                var lgroup = b.getLayerGroups();
                // ATTENTION: using fist layer list only
                var MAX = lgroup.length;        // lgroup.length
                var k = 0;
                for(var i = 0; i < MAX;i++){
                    var g = lgroup[i].getLayers();
                    for(var j = 0; j < g.length;j++){
                        // index k is layer order
                        var v = g[j].isVisible() + "|" + k + "|" + lgroup[i].getServiceType() +
                                "|" + lgroup[i].getServiceName() + "|" + lgroup[i].getServiceURL().replace( /&/g, "&amp;") +
                                "|" + g[j].getTitle() + "|" + g[j].isQueryable();
                        layerlist += createMember( g[j].getName(), v, "string" );
                        k++;
                    }
                }
                return layerlist;
            }

            function setScale() {
            	var scale = opener.controller.mapModel.getScaleDenominator();
            	document.getElementById( "SCALEFIELD" ).value = scale / 1.25992;
            }

            function changeScaleSelection(radio) {
                if ( radio.value == "currentBBOX" ) {
                	scaleSelection = "currentBBOX";
                } else if ( radio.value == "currentBBOXRounded" ) {
                    scaleSelection = "currentBBOXRounded";
                } else {
                	scaleSelection = getElement( "SCALEFIELD" ).value;
                }
            }

            
        // -->
        </script>
    </head>
    <body class="pPrintDialog" onload="setScale()">
        <div id="wait" style="z-index:100; background:#FFFFFF; visibility:hidden; position:absolute; left:0px; top:0px; height:600px; width:850px; opacity: .80; filter: alpha(opacity=80); -moz-opacity: 0.8;">
            <h1><a style="position:absolute; text-decoration:none; left:200px; top:280px;"><%=Messages.get( loc, "IGEO_STD_PDF_WAIT" ) %></a></h1>
        </div>
        <form method="post" action="../../control" accept-charset="UTF-8" >
            <input id="RPC" type="hidden" name="rpc" value="">
        </form>
        <!-- choose print template -->
        <div style="position:absolute; left:30px; top:30px; height:60px; width:230px;">        
            <%=Messages.get( loc, "IGEO_STD_PDF_CHOOSE_TMPLT" ) %>
        </div>
        <div style="position:absolute; left:30px; top:50px; height:60px; width:230px;">            
            <select onchange="swapPrintTemplate( this )" style="width:200px">
                <option selected="selected" value="template_2"><%=Messages.get( loc, "IGEO_STD_PDF_TMPLT_LSCP" ) %></option>
                <option value="template_1"><%=Messages.get( loc, "IGEO_STD_PDF_TMPLT_PRTRT" ) %></option>
            </select>
        </div>
        <!-- choose print scale -->
        <div style="position:absolute; left:300px; top:30px; height:60px; width:230px;">        
            <%=Messages.get( loc, "IGEO_STD_PDF_CHOOSE_SCALE_TITLE" ) %>
        </div>
        <div style="position:absolute; left:300px; top:50px; height:60px; width:430px;">
            <input id="RADIO1" type="radio" name="scale" value="chooseScale" onclick="changeScaleSelection(this)"> <%=Messages.get( loc, "IGEO_STD_PDF_CHOOSE_SCALE" ) %> <input type="text" id="SCALEFIELD" size=10><br>  
            <input type="radio" name="scale" value="currentBBOX" onclick="changeScaleSelection(this)" checked> <%=Messages.get( loc, "IGEO_STD_PDF_CURRENT_SCALE" ) %><br>
            <input type="radio" name="scale" value="currentBBOXRounded"  onclick="changeScaleSelection(this)"> <%=Messages.get( loc, "IGEO_STD_PDF_CURRENT_ROUNDED_SCALE" ) %><br>
        </div>
        <!-- define text element for printing -->
        <div style="position:absolute; left:30px; top:130px; height:450px; width:90px;">
            <iframe id="TEXTFIELDS" src="<%=path %>/modules/pdfprint/printtemplate_2.jsp" frameborder="1" scrolling="auto" width="220" height="450" ></iframe>
        </div>
        <!-- show static image for print preview (schema) -->
        <div style="position:absolute; left:300px; top:130px; height:450px; width:450px;" >
            <img id="IMAGE" width="450" src="<%=path %>/modules/pdfprint/images/template_2.jpg" border="1"></img>
        </div>
    </body>
</html>
