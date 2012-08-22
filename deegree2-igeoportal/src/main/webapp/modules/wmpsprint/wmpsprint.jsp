<!-- $HeadURL$ -->
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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
		<title>WMPS Print</title>
		<link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
		<link rel="stylesheet" type="text/css" href="../../javascript/ext-3.3.1/resources/css/ext-all.css" />
		<link rel="stylesheet" type="text/css" title="blue"  href="../../javascript/ext-3.3.1/resources/css/xtheme-blue.css" /> 
        <link rel="stylesheet" type="text/css" title="gray"  href="../../javascript/ext-3.3.1/resources/css/xtheme-gray.css" />
        <link rel="stylesheet" type="text/css" title="black" href="../../javascript/ext-3.3.1/resources/css/xtheme-access.css" />
        
		<link href="../../javascript/ext-3.3.1/resources/css/structure/combo.css" rel="stylesheet" type="text/css" />
		<link rel="stylesheet" href="../../javascript/openlayers/theme/default/style.css" type="text/css" />
        <script src='../../javascript/openlayers/lib/OpenLayers.js'></script>
		<script type="text/javascript" src="wmpsprint.js"></script>
		<script type="text/javascript" src="../../javascript/request_handler.js"></script>
		<script type="text/javascript" src="../../javascript/json2.js"></script>
		<script type="text/javascript" src="../../javascript/utils.js"></script>
		<script type="text/javascript" src="../../javascript/validation.js"></script>
		<script type="text/javascript" src="../../javascript/envelope.js"></script>
		<script type="text/javascript" src="../../javascript/geometries.js"></script>
		<script type="text/javascript" src="../../javascript/geometryfactory.js"></script>
		<script type="text/javascript" src="../../javascript/geometryutils.js"></script>
		<script type="text/javascript" src="../../javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="../../javascript/ext-3.3.1/ext-all.js"></script>

		<script type="text/javascript">
            var url = null;
            var fields = null;
            var mapWidth = 0;
            var mapHeight = 0;
            var dpi = 300;
            var scale = 25000;
            var template = null;
            var printBox;
            
        
	        function register() {
	            if ( parent.controller == null ) {
	                parent.controller = new parent.Controller();
	                parent.controller.init();
	            }
	            parent.setActiveStyleSheet();	            
	        }
	       
	        function initWMPSPrint() {
	            parent.controller.initWMPSPrint(document);
	            var tmp = window.location.pathname.split( '/' );
                var s = '';
                for ( var i = 0; i < tmp.length-3; i++) {
                    s += (tmp[i] + '/' ); 
                } 
                url = window.location.protocol + '//' + window.location.host + s + 'ajaxcontrol';
	            try {
	                submitGetRequest( url + "?action=loadTemplateList", handleLoadTemplateList, null, false );
	            } catch(e) {
		            alert( "1 " + e );
	            }	            
	            initGUI();
	        }

	        /**
	         * initializing ExtJS GUI elements. At the moment just Ext.Button elements will be used
	         * In future version this dialog will be completly reimplemented as an ExtJS to avoid
	         * opening an additional browser window
	         */
	         function initGUI() {
	              Ext.QuickTips.init();   
	              new Ext.Button({
	                  tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_PRINTMAP" ) %>',
	                  text: '<%=Messages.get( loc, "IGEO_STD_BTN_PRINTMAP" ) %>',
	                  renderTo: 'buttonArea0',
	                  width: 100,
	                  height: 25,
	                  handler: function(toggled){                               
	                	  printMap();
	                  }
	              });           
	           }
	        
	        /**
	         * adds list of available wmps templates as options to combobox at HTML page 
	         * 
	         * @param response 
	         */
	        function handleLoadTemplateList(response) {		        
	            var templates = JSON.parse( response, null );
	            var select = getElement( "templates" ); 
	            if ( select != 0 ) { 
		            removeChildren( select );
		            appendOption( select, '<%=Messages.get( loc,"IGEO_STD_WMPS_SELECT_TEMPLATE" )%>', "-", true );
		            for (var i = 0; i < templates.length; i++ ) {
		                appendOption( select, templates[i], templates[i], false );
		            }
		            convertDPISelect();
	                convertScaleSelect();
	                convertTemplateSelect();
	            }
	        }	 

	        function convertDPISelect() {
	        	new Ext.form.ComboBox({
                    listeners:{ 'select': switchDPI },   
                    itemId: 'dpiLst',  
                    id: 'dpiLst',             
                    typeAhead: true,
                    triggerAction: 'all',
                    editable: false,
                    transform:'dpi',
                    width:180,
                    x: 0,
                    y: 0,
                    forceSelection:true
                });
	        }

	        function switchDPI(cb, record, index) {
		        dpi = record.data['value'];
	        }

	        function convertScaleSelect() {
	        	new Ext.form.ComboBox({
                    listeners:{ 'select': initFence },   
                    itemId: 'scaleValueLst',  
                    id: 'scaleValueLst',             
                    typeAhead: true,
                    triggerAction: 'all',
                    editable: false,
                    transform:'scaleValue',
                    width:180,
                    x: 0,
                    y: 0,
                    forceSelection:true
                });
	        }

	        function convertTemplateSelect() {
                new Ext.form.ComboBox({
                    listeners:{ 'select': loadTemplateDescription },   
                    itemId: 'templatesLst',  
                    id: 'templatesLst',             
                    typeAhead: true,
                    triggerAction: 'all',
                    editable: false,
                    transform:'templates',
                    width:180,
                    x: 0,
                    y: 0,
                    forceSelection:true
                });
            }
	         

	        function loadTemplateDescription(cb, record, index) {
		        if ( record.data['value'] != '-' ) {
			        var bean = JSON.stringify( new function() {
				       this.action = 'loadTemplateDescription';
				       this.template = record.data['value'];
				       template = this.template;
			        }, null, false );
		        	try {
	                    submitPostRequest( url, handleTemplateDescription, bean );                    
	                } catch(e) {
	                    alert( "2 " +  e );
	                }
		        }
	        }      

	        /**
	         * initializes a fence on top of the map showing which area will be printed
	         */ 
	        function initFence(cb, record, index) {
	        	removeFence();
	        	
		        if ( record != null ) { 
		            scale = record.data['value'];
		        }
	            // create feature for box that will be printed
	            var bbox = createBBOX();

		        parent.controller.vOLMap.initFence( bbox, callback );
	        }

	        /**
	        * will invoked when dragging is finished
	        */
	        function callback(feature) {
		        printBox = feature.geometry.getBounds().toBBOX( 3 );
	        }

	        function createBBOX() {		        
	        	var mm = parent.controller.mapModel;
	        	var center = mm.getBoundingBox().getCentroid();
	        	var scaleDenominator = parseFloat( "" + scale );
				
				// screen -> world projection
				var pixelSize = 0.0254 / 72;
				var w2 = ( scaleDenominator * pixelSize * mapWidth ) / 2.0;
				var x1 = center.x - w2;
				var x2 = center.x + w2;
				w2 = ( scaleDenominator * pixelSize * mapHeight ) / 2.0;
				var y1 = center.y - w2;
				var y2 = center.y + w2;
				
				return new Envelope( x1, y1, x2, y2 );
			}

	        function removeFence() {	        	
		        var map = parent.controller.vOLMap.getMap();
	            var boxes = map.getLayersByName("_BOXESMARKER_" );
	            if ( boxes.length > 0 ) {
                    var controls = map.getControlsByClass( 'OpenLayers.Control.DragFeature' );
                    if ( controls != null ) {
                        for (key in controls) {
                            var control = controls[key];
                            control.deactivate();
                            map.removeControl( control );
                        } 
                    }
                    map.removeLayer( boxes[0] );
                }
	        }

	        function handleTemplateDescription(response) {	     
	        	var parameter = JSON.parse( response, null );	        	
        		var paramTable = getElement( 'parameter' );
                removeChildren( paramTable );
                fields = new Array();
                fields[0] = new Object();
                for (var i = 0; i < parameter.length; i++ ) {
                    if ( parameter[i][0] != 'SCALE' && parameter[i][0] != 'LEGEND' && 
                         parameter[i][0] != '$MAPWIDTH' && parameter[i][0] != '$MAPHEIGHT' ) {                        
                        fields[0][parameter[i][0]] = ' ';
                        var row = createElement( 'tr', null, null, null, paramTable ); 
                        var cell = createElement( 'td', null, null, parameter[i][0], row );
                        cell.width = '150px';
                        cell = createElement( 'td', null, null, null, row );                        
                        var inp = createElement( 'input', parameter[i][0], null, null, cell );
                        inp.type = 'text';
                    } else if ( parameter[i][0] == '$MAPWIDTH' ) {      
                    	mapWidth = parseInt( parameter[i][1] ); 
                    } else if ( parameter[i][0] == '$MAPHEIGHT' ) {
                    	mapHeight = parseInt( parameter[i][1] );
                    }
                }    
                initFence(); 		        
	        }

	        /**
	        * invokes print action on server
	        */
	        function printMap() {
	        	if ( fields == null ) {
                    alert('<%=Messages.get( loc,"IGEO_STD_WMPS_SELECT_TEMPLATE3" )%>' );
                    return;
                }
                for (var i = 0; i < fields.length; i++) {
                    for (var field in fields[i]) {
                        fields[i][field] = getInputValue( field );
                    }
                } 
                var print = new Print();
                print.fields = fields;                 
                print.bbox = printBox;
                
                if ( !isEmail( print.email ) ) {
                    alert( '<%=Messages.get( loc,"IGEO_STD_WMPS_VALID_EMAIL" )%>' );
                    getElement( 'email' ).focus();
                    getElement( 'email' ).select();
                    return;
                } 
                
                var s = JSON.stringify( print );
                try {
                    submitPostRequest( url, function(response) { alert( response ); }, s );                    
                } catch(e) {
                    alert( "3 " + e );
                }
            }

	        /**
	        * print bean
	        */
	        function Print() {
		        this.action = 'printWMPS';
		        this.legend = ''+getElement( 'legend' ).checked;
		        this.scale = ''+getElement( 'scale' ).checked;
		        this.email = getInputValue('email' );
		        this.scaleValue = "" + scale;
		        this.template =  template;
		        this.dpi = ""+dpi;
		        this.fields = null;
		        this.bbox = null; 
		        this.srs = parent.controller.mapModel.getSrs();
	        }
	        
        </script>
	</head>
	<body onload="register(); initWMPSPrint();" onunload="removeFence()" style="margin:5px">
		<div>
		  <table border="0" cellspacing="2">
            <tr>
                <td width="150px"><%=Messages.get( loc,"IGEO_STD_WMPS_SELECT_TEMPLATE2" )%> </td>
                <td>		
		          <select id="templates" size="1" ></select>
		        </td>
		    </tr>
		  </table>  
		</div>
		<div style="height:20px"></div>
		<div>
		  <table  border="0" cellspacing="2" cellpadding="0">
		      <tbody id="parameter"></tbody>
		  </table>		 
		</div>		
		<div style="height:10px"></div>
		<div>
		   <table id="parameter" border="0" cellspacing="2">
              <tr>
                  <td><input id='legend' type="checkbox" name='legend' value='legend'> <%=Messages.get( loc,"IGEO_STD_WMPS_PRINT_LEGEND" )%></td>
              </tr>
              <tr>
                  <td><input id='scale' type="checkbox" name='scale' value='scale'> <%=Messages.get( loc,"IGEO_STD_WMPS_PRINT_SCALE" )%></td>
              </tr>             
          </table>
		</div>
		<div style="height:10px"></div>
		<div>
		  <table border="0" cellspacing="2">
              <tr>
                  <td width="150px"><%=Messages.get( loc,"IGEO_STD_WMPS_SCALE" )%> </td>
                  <td>
                    <select id="scaleValue" size="1" >
                      <option value="100">1:100</option>
                      <option value="250">1:250</option>
                      <option value="500">1:500</option>
                      <option value="1000">1:1000</option>
                      <option value="2500">1:2500</option>
                      <option value="5000">1:5000</option>
                      <option value="10000">1:10000</option>
                      <option value="25000" selected="selected">1:25000</option>
                      <option value="50000">1:50000</option>
                      <option value="100000">1:100000</option>
                      <option value="250000">1:250000</option>
                      <option value="500000">1:500000</option>
                      <option value="1000000">1:1000000</option>
                      <option value="2500000">1:2500000</option>
                      <option value="5000000">1:5000000</option>
                  </select>
                  </td>
              </tr>              
          </table>
		</div>
		<div style="height:10px"></div>
		<div>
          <table border="0" cellspacing="2">
              <tr>
                  <td width="150px"><%=Messages.get( loc,"IGEO_STD_WMPS_DPI" )%> </td>
                  <td>
                    <select id="dpi" size="1">
                      <option value="72">72 dpi</option>
                      <option value="150">150 dpi</option>
                      <option value="300" selected="selected">300 dpi</option>
                      <option value="600">600 dpi</option>
                  </select>
                  </td>
              </tr>                           
          </table>
        </div>
        <div style="height:10px"></div>
		<div>
          <table border="0" cellspacing="2">
              <tr>
                  <td width="150px"><%=Messages.get( loc,"IGEO_STD_WMPS_EMAIL" )%> </td>
                  <td><input id='email' type="text" name='email' value="" style="width:180px"></td>
              </tr>
          </table>
        </div>          
		<div style="height:10px"></div>
		<div id="buttonArea0"></div>
	</body>
</html>