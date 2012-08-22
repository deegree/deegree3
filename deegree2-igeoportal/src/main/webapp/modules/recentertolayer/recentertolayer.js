//$HeadURL$
/*----------------------------------------------------------------------------
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
----------------------------------------------------------------------------*/

function doRecenterToLayerRequest( doc, controller ){
    
    var iframe = doc.createElement("iframe");
    iframe.setAttribute( "src", "./modules/recentertolayer/recentertolayer.html");
    iframe.setAttribute( "width", "0");
    iframe.setAttribute( "height", "0");
    iframe.setAttribute( "frameBorder", "0");
    iframe.setAttribute( "id", "r2LtmpIFrame");
    
    doc.childNodes[0].appendChild( iframe );
}
    
function requestRecenterToLayer( controller ) {
	
    var selectedRow  = null;
    
    if ( controller.vLayerListView != null ) {
    	selectedRow = controller.vLayerListView.selectedRow;		
    }
	if ( selectedRow == null ) {
        alert( 'Please choose a layer first.' );
        return;
	}  

	var index = selectedRow.indexOf("r");
	var groupIndex = parseInt(selectedRow.substring(1, index) );		
	var index2 = selectedRow.indexOf("c");
	var layerIndex = parseInt(selectedRow.substring(index+1, index2) );	
    //var layerGroup = opener.controller.mapModel.getLayerList().getLayerGroup(groupIndex);        
	var layerGroup = controller.mapModel.getLayerList().getLayerGroup(groupIndex);        
	var layer = layerGroup.getLayer( layerIndex );
    var serviceURL = layerGroup.getServiceURL()
    var capabilitiesRequest = '' ; 
    
    if ( serviceURL.charAt(serviceURL.length-1) != '?') {
        serviceURL = serviceURL + '?';
    }
	
	var serviceType = layerGroup.getServiceType();
	var tmp2 = serviceType.split(" ");
	var ser = tmp2[0];
	var tmp = ser.split(":");
	var service = tmp[1];
	var version = tmp2[1];
    
    var capabilitiesRequest =  
		"<![CDATA[" + serviceURL + "SERVICE=WMS&REQUEST=GetCapabilities&version=" + version;
	if (  controller.vSessionKeeper != null  &&  controller.vSessionKeeper.id != null ) { 
		capabilitiesRequest = capabilitiesRequest + "&SESSIONID=" + controller.vSessionKeeper.id;
	}
	capabilitiesRequest = capabilitiesRequest + "]]>";
    
	var layerName = layer.getName();					
	//var  width = opener.controller.mapModel.getWidth();
	var width = controller.mapModel.getWidth();
	var height = controller.mapModel.getHeight();
    var crs = controller.mapModel.getSrs();	
    var b = controller.mapModel.getBoundingBox();   
			    
    var rpc = "<?xml version='1.0' encoding='UTF-8'?><methodCall>" +
              "<methodName>mapView:recenterToLayer</methodName><params>"+
                  "<param><value><struct>"+
	                  createMember( 'capabilitiesRequest', capabilitiesRequest, 'string' ) +
	                  createMember( 'layerName', layerName, 'string' )+
	                  createMember( 'crs', crs, 'string' ) +                  
	                  createMember( 'minx', b.minx, 'double' ) +
	                  createMember( 'miny', b.miny, 'double' ) +
	                  createMember( 'maxx', b.maxx, 'double' ) +
	                  createMember( 'maxy', b.maxy, 'double' ) +                  
	                  createMember( 'mapWidth', parseInt(width), 'int' ) +                  
	                  createMember( 'mapHeight', parseInt(height), 'int' ) +                                    
                  "</struct></value></param>" + 
              "</params></methodCall>";		                  
              
    this.proxy.forms[0].method = "post";
	this.proxy.forms[0].action = "control?rpc=" + encodeURIComponent(rpc);                           
	this.proxy.forms[0].submit();        
}

function setProxy(vproxy){
	this.proxy = vproxy;	
}

function removeIFrame(){
    doct.removeChild( iframe );
}
