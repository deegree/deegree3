//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

function FeatureInfo() {
	
	this.onDbClick = onDbClick;
	this.onClick = onClick;
	this.onClick_ = onClick_;
	this.msgResult = msgResult;
	this.findSelectedLayers = findSelectedLayers;
	this.findNotQueryableLayers = findNotQueryableLayers;
	this.findNotVisibleLayers = findNotVisibleLayers;
	this.performFeatureInfo = performFeatureInfo;
	
	/**
	 * handle click events
	 */
	function onClick(e) {
		var m1 = ' IE just supports HTTP Get requests with less than 2000 characters! ';
		var m2 = 'following layers are not visible: ';
		var m3 = 'following layers are not queryable: ';
		var m4 = 'feature info';
		var m5 = 'no layer has been selected ( directly or via a layer group ) that supports feature info';
		var m6 = ' is not queryable';
		var m7 = ' is not visible';
		var m8 = 'no layer has been selected';
		
		if ( controller.vDataTable ) {
			controller.vDataTable.resetTabs();
		} else if ( controller.dataWindowTabs != null ) {
			controller.dataWindowTabs.removeAll();
		}
	
		var lgs = controller.mapModel.getLayerList().getLayerGroups();
		
		// create and perform feature info requests
		var gfi = false;
		for ( var i = 0; i < lgs.length; i++) {
			if ( this.performFeatureInfo( lgs[i], controller.mapModel, e.xy.x, e.xy.y ) ) {
				gfi = true;
			}			
		}
		
		
		// find all layers that are selected and queryable but not visible
		var nv = new Array();
		for ( var i = 0; i < lgs.length; i++) {
			nv = this.findNotVisibleLayers( nv, lgs[i] );
		}
		var nvs = '';
		if ( nv.length  > 0 ) {						
			var nvs = m2;
			for ( var i = 0; i < nv.length; i++) {
				nvs = nvs + nv[i].getTitle() + "; ";
			}
		}
		
		// find all layers that are selected but not queryable
		var nq = new Array();
		for ( var i = 0; i < lgs.length; i++) {
			nq = this.findNotQueryableLayers( nq, lgs[i] );
		}
		var nqs = '';
		if ( nq.length  > 0 ) {
			nqs = m3;
			for ( var i = 0; i < nq.length; i++) {
				nqs = nqs + nq[i].getTitle() + "; ";
			}
		} 	
		
		// find all selected layers
		var sel = new Array();
		for ( var i = 0; i < lgs.length; i++) {
			sel = this.findSelectedLayers( sel, lgs[i] );
		}
		
		// if more than one layer has been selected and at least one of them is not queryable 
		// or not visible the user will be informed
		if ( (nv.length  > 0 || nq.length > 0)  && sel.length > 1 ) {				
			Ext.Msg.show({
				title: m4,
				msg: nqs + '<br><br>' + nvs,
				buttons: {ok: 'OK' },
				width: 500,
				fn: this.msgResult,
				icon: Ext.MessageBox.WARNING
			}); 
		}
		
		// if no feature info has been performed, create message depending on amount of selected layers
		if ( !gfi ) {
		    var message = '';
			if ( sel.length > 1 ) {
				message = m5;
			} else if ( sel.length == 1 ) {
				if ( !sel[0].isQueryable() ) {
					message = sel[0].getTitle() + m6;
				} else {
					message = sel[0].getTitle() + m7;
				}
			} else {
				message = m8;
			}
			Ext.Msg.show({
					title: m4,
					msg: message,
					buttons: {ok: 'OK' },
					width: 500,
					fn: this.msgResult,
					icon: Ext.MessageBox.WARNING
				});     
		}
    }
	
	/**
	 * handle click events
	 */
	function onClick_(e) {
		while ( controller.windows.pop() != null ) {};
		var m1 = ' IE just supports HTTP Get requests with less than 2000 characters! ';
		var m2 = 'following layers are not visible: ';
		var m3 = 'following layers are not queryable: ';
		var m4 = 'feature info';
		var m5 = 'no layer has been selected ( directly or via a layer group ) that supports feature info';
		var m6 = ' is not queryable';
		var m7 = ' is not visible';
		var m8 = 'no layer has been selected';
	
		var fact = new WMSRequestFactory();
		var lgs = controller.mapModel.getLayerList().getLayerGroups();
		
		var fi = false;
		// create and perform feature info requests
		for ( var i = 0; i < lgs.length; i++) {
			var s = null;
			if ( controller.vSessionKeeper != null ) {
				s = fact.createGetFeatureInfoRequest2( lgs[i], controller.mapModel, "text/html", 
						                               e.xy.x, e.xy.y, controller.vSessionKeeper.id);
			} else {
				s = fact.createGetFeatureInfoRequest2( lgs[i], controller.mapModel, "text/html", 
						                               e.xy.x, e.xy.y, null);
			}
			var isNav = ( navigator.appName.indexOf( "Netscape" ) >= 0 );
			if ( s != null ) {
				if ( s.length > 2000 && !isNav ) {
				    Ext.Msg.alert( m4, m1 );
					return;
				} else  {
					fi = true;
					var fiw = window.open( s ,"FeatureInfo" + i ,"width=550,height=400,left=" + (i*100) + ",top=" + (i*100) + ",scrollbars=yes" );
					fiw.focus();
					controller.windows.push( fiw );
				} 
			}
		}
		
		// find all layers that are selected and queryable but not visible
		var nv = new Array();
		for ( var i = 0; i < lgs.length; i++) {
			nv = this.findNotVisibleLayers( nv, lgs[i] );
		}
		var nvs = '';
		if ( nv.length  > 0 ) {						
			var nvs = m2;
			for ( var i = 0; i < nv.length; i++) {
				nvs = nvs + nv[i].getTitle() + "; ";
			}
		}
		
		// find all layers that are selected but not queryable
		var nq = new Array();
		for ( var i = 0; i < lgs.length; i++) {
			nq = this.findNotQueryableLayers( nq, lgs[i] );
		}
		var nqs = '';
		if ( nq.length  > 0 ) {
			nqs = m3;
			for ( var i = 0; i < nq.length; i++) {
				nqs = nqs + nq[i].getTitle() + "; ";
			}
		} 	
		
		// find all selected layers
		var sel = new Array();
		for ( var i = 0; i < lgs.length; i++) {
			sel = this.findSelectedLayers( sel, lgs[i] );
		}
		
		// if more than one layer has been selected and at least one of them is not queryable 
		// or not visible the user will be informed
		if ( (nv.length  > 0 || nq.length > 0)  && sel.length > 1 ) {				
			Ext.Msg.show({
				title: m4,
				msg: nqs + '<br><br>' + nvs,
				buttons: {ok: 'OK' },
				width: 500,
				fn: this.msgResult,
				icon: Ext.MessageBox.WARNING
			}); 
		}
		
		// if no feature info has been performed, create message depending on amount of selected layers
		if ( !fi ) {
		    var message = '';
			if ( sel.length > 1 ) {
				message = m5;
			} else if ( sel.length == 1 ) {
				if ( !sel[0].isQueryable() ) {
					message = sel[0].getTitle() + m6;
				} else {
					message = sel[0].getTitle() + m7;
				}
			} else {
				message = m8;
			}
			Ext.Msg.show({
					title: m4,
					msg: message,
					buttons: {ok: 'OK' },
					width: 500,
					fn: this.msgResult,
					icon: Ext.MessageBox.WARNING
				});     
		}
    }

	/**
	 * handle double click events
	 */
    function onDbClick(e) {
        alert( e.xy );
    }
    
	function msgResult(btn, text){
	    for ( var i =0; i < controller.windows.length; i++ ) {
	        controller.windows[i].focus();
	    }
    }			
	
	function findSelectedLayers(array, layerGroup) {
		var layers = layerGroup.getLayers();
		for (var i = 0; i < layers.length; i++) {
			if ( layers[i].isSelected() ) {
				array.push( layers[i] );
			} 
		}
		return array;
	}
	
	function findNotQueryableLayers(array, layerGroup) {
		var layers = layerGroup.getLayers();
		for (var i = 0; i < layers.length; i++) {
			if ( layers[i].isSelected() &&  !layers[i].isQueryable() ) {
				array.push( layers[i] );
			} 
		}
		return array;
	}
	
	function findNotVisibleLayers(array, layerGroup) {
	    var layers = layerGroup.getLayers();
		for (var i = 0; i < layers.length; i++) {
			if ( layers[i].isSelected() &&  layers[i].isQueryable() &&  !layers[i].isVisible() ) {
				array.push( layers[i] );
			} 
		}
		return array;
	}
	
	function performFeatureInfo(layerGroup, mapModel, x, y) {        
        var layers = layerGroup.getLayers();
        var j = 0;        
        for (var i = 0; i < layers.length; i++){
            if ( layers[i].isSelected() && layers[i].isVisible() && layers[i].isQueryable() ) {
            	j++;
            	var bean = JSON.stringify( new function() {
                    this.action = 'getFeatureInfo';
                    this.name = layers[i].getName();
                    this.title = layers[i].getTitle();
                    this.url = layerGroup.getServiceURL();
                    this.type = layerGroup.getServiceType();
                    this.bbox = controller.vOLMap.getMap().getExtent().toBBOX();
                    this.x = x;
                    this.y = y;
                    this.mapWidth = controller.mapModel.getWidth();
                    this.mapHeight = controller.mapModel.getHeight();
                    this.time = layerGroup.getTime();
                  }, null, false );			 
            	submitPostRequest( controllerURL, function(result) {
            		if ( result.indexOf( 'ExceptionBean' ) > -1 ) {
            	    	handleException( result );
            	    } else {
            	    	var data = JSON.parse( result );
	            		if ( controller.vDataTable ) {	            			
	            			controller.vDataTable.addData( data.name, data.columns, data.data );
	            		} else {
	            			controller.openDataTableWindow( data.name, data.columns, data.data );
	            		}
            	    }
            	}, bean );
            } 
        }    
        return j > 0;
    }
    
}
