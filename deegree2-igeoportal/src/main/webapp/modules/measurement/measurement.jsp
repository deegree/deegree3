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
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.deegree.i18n.Messages" %>
<%@ page import="java.util.Locale" %>
<%
Locale loc = request.getLocale();
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>measurement</title>
        <meta name="author" content="lat/lon GmbH" />
        <meta name="KeyWords" content="deegree iGeoPortal lat/lon" />
        <link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
        <link rel="stylesheet" type="text/css" href="../../javascript/ext-3.3.1/resources/css/ext-all.css" />
        <link rel="stylesheet" type="text/css" title="blue"  href="../../javascript/ext-3.3.1/resources/css/xtheme-blue.css" /> 
        <link rel="stylesheet" type="text/css" title="gray"  href="../../javascript/ext-3.3.1/resources/css/xtheme-gray.css" />
        <link rel="stylesheet" type="text/css" title="black" href="../../javascript/ext-3.3.1/resources/css/xtheme-access.css" />
        <script type="text/javascript" src="measurement.js"></script>
        <script type="text/javascript" src="../../javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="../../javascript/ext-3.3.1/ext-all.js"></script>
        <script type="text/javascript">
        <!-- 

            var diItems = new Array();
        
            function register() {
                if ( parent.controller == null ) {
                    parent.controller = new parent.Controller();
                    parent.controller.init();
                }
            }

            function init() {
                parent.controller.initMeasurement( document );
                initGUI();
            }

            function resetStates(button) {
                for (var i = 0; i < diItems.length; i++ ) {
                   if ( diItems[i] != button && diItems[i].toggle ) { 
                    diItems[i].toggle( false, true );
                   }
               }
           }

            function initGUI() {
            	Ext.QuickTips.init();   

            	// add GUI elements
                diItems.push( new Ext.Button({                          
                    enableToggle: true,
                    x: 10,
                    y: 10,
                    tooltip: 'Streckenlänge messen',
                    icon: './images/measure_length.png',
                    handler: function(toggled){
	                	if (toggled) {
	                        resetStates( this );
	                    }
	                    parent.controller.vMeasurement.startMeasureLength();
                    }
                }) );
                
                diItems.push( new Ext.Button({                           
                    enableToggle: true,
                    x: 40,
                    y: 10,
                    tooltip: 'Fläche messen',
                    icon: './images/measure_areas.png',
                    handler: function(toggled){
	                	if (toggled) {
	                        resetStates( this );
	                    }
	                	parent.controller.vMeasurement.startMeasureArea();
                    }
                }));

                diItems.push( new Ext.Button({
                    x: 70,
                    y: 10,
                    tooltip: 'Eingaben zurücksetzten',
                    icon: './images/refresh.gif',
                    handler: function(toggled){
	                	if (toggled) {
	                        resetStates( this );
	                    }
	                	parent.controller.vMeasurement.kill();
                    }
                }));

                var viewport = new Ext.Viewport({
                    id: 'VIEWPORT',
       			 layout:'fit',
       	       height: 600,
       				 items : {
       	          layout: 'absolute',
       	          height: 600,
       	          items: diItems
       	          }
                  })
            }
            
        // -->
        </script>              
    </head>
    <body onload="register(); init();" >
	   <div id="measureTB"></div>
       <div style="position:absolute;  visibility:hidden; left:10px; heigth:80px; top:50px;">
		   <table id='tbLength'  border="0" cellspacing="4">
		       <tr>
		           <td width="40px"><%=Messages.get( loc,"IGEO_STD_MEASURE_LENGTH" )%></td>
		           <td width="90px"><input id='inpLength' type="text" size="11"></td>
		            <td width="5px"></td>
                   <td>m</td>
		       </tr>
              <tr height="10px"></tr>
		   </table>
		  
		   <table id='tbArea' border="0" cellspacing="4">
               <tr>
                   <td width="40px"><%=Messages.get( loc,"IGEO_STD_MEASURE_AREA" )%></td>
                   <td width="90px"><input id='inpArea' type="text" size="11"></td>
                   <td width="5px"></td>
                   <td>m²</td>
               </tr>
           </table>
	   </div>
	   <div id="buttonArea" style="position:absolute; visibility:hidden; left:10px; heigth:30px; top:115px;" 
            title='<%=Messages.get( loc, "IGEO_STD_MEASURE_SEG_INFO_TT" )%>'>
	       <a class="buttonArea" style="position:absolute; width:120px;" href="javascript:parent.controller.vMeasurement.showSegmentInfo()">
	           <%=Messages.get( loc,"IGEO_STD_MEASURE_SEG_INFO" )%>
           </a>
	   </div>
	   <div id="segInfoArea" style="position:absolute; left:10px; heigth:200px; top:140px; width:200px; visibility:hidden" ></div>
	</body>
</html>