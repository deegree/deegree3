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

/**
 * Main class of the routing module, It is the only access to any kind of functionality 
 * of the routing module from outside (controller). Any module and object other than the
 * this class shall not access any part/element of the module directly (even if this is
 * possible through JavaScript).<br>
 * This class also implements methods:<br>
 * - onClick(e) and <br>
 * - onDbClick(e)<br>
 * so it can be registered to the controller as a state/mode that - if active - will be
 * informed about any mouse click onto the map.  
 */
function Routing(id) {
	this.id = id;
	this.url;
	this.markers = null;
	this.state;
	this.url;
	this.gui;
	/**
	 * store current markers
	 */
	this.markerArray = new Array();
	this.markerArray[0] = new Object();

	// method declaration
	this.addMarker = addMarker;
	this.drawRoute = drawRoute;
	this.findRoutePerformed = findRoutePerformed;
	this.initMarkers = initMarkers;
	this.kill = kill;
	this.onClick = onClick;
	this.onDbClick = onDbClick;
	this.paint = paint;
	this.removeMarker = removeMarker;
	this.removeAllMarkers = removeAllMarkers;
	this.repaint = repaint;
	this.setStatePerformed = setStatePerformed;
	this.getUrl = getUrl;
	this.updateRoutePoint = updateRoutePoint;
	this.validateCalcRouteRequest = validateCalcRouteRequest;
	this.addRoutingLayer = addRoutingLayer;
	this.removeRoutingLayer = removeRoutingLayer;

	//////////////////////////////////////////////////////////////////////
	// implementation
	//////////////////////////////////////////////////////////////////////	
	
    /**
     * 
     * @param targetDocument
     * @param parentNode
     */
	function paint(targetDocument, parentNode) {

		var tmp = window.location.pathname.split('/');
		var s = '';
		for ( var i = 0; i < tmp.length - 1; i++) {
			s += (tmp[i] + '/');
		}
		this.url = window.location.protocol + '//' + window.location.host + s + 'ajaxcontrol';

		// init module's GUI
		this.gui = new RoutingGUI();
		this.gui.initGUI();		
	}

	/**
	 * 
	 */
	function repaint() {
		// nothing to do
	}

	/**
	 * 
	 * @param routeDescription
	 */
	function drawRoute(routeDescription) {		
		if ( controller.mapModel.getLayerByName( 'highlight' ) != null ) {		
			this.removeRoutingLayer();
		}
		
		var env = new Envelope( routeDescription.bbox[0], routeDescription.bbox[1],
							    routeDescription.bbox[2], routeDescription.bbox[3] );
		if ( env.getWidth() > env.getHeight() ) {
			env = env.getBuffer( env.getWidth() );
		} else {
			env = env.getBuffer( env.getHeight() );
		}
		controller.actionPerformed( new Event( this, "BBOX", env ) );
		
        this.addRoutingLayer();
        this.gui.setDescription( routeDescription.description );
        this.gui.setLength( routeDescription.distance );
        this.gui.setNumberOfNodes( routeDescription.numberOfNodes );
	}
	
	/**
	 * 
	 */
	function addRoutingLayer() {
		// get URL of deegree session WMS
        var tmp = window.location.pathname.split( '/' );
        var s = '';
        for ( var i = 0; i < tmp.length-1; i++) {
            s += (tmp[i] + '/' ); 
        } 
        var layers = new Array();
        layers[0] = new Object();
        
        layers[0]['name'] = 'highlight';
        layers[0]['title'] = 'route';
        layers[0]['queryable']  = false;
        layers[0]['metadataURL'] = null;
        layers[0]['minScale'] = '0';
        layers[0]['maxScale'] = '99999999999';
        layers[0]['visible'] = true;
        var wmsurl = window.location.protocol + '//' + window.location.host + s + 'highlightwms';
        controller.addLayersToModel( "route highlight", wmsurl, "1.1.1", layers, "image/png", false, true );        
		controller.vOLMap.getMap().addLayer(this.markers);
	}
	
	/**
	 * 
	 * 
	 */
	function removeRoutingLayer() {            
        try {
            var layer = controller.mapModel.getLayerByName( 'highlight' );            
            if ( layer != null ) {
                 var bean = JSON.stringify( new function() {
                     this.action = 'REMOVENODE';
                     this.node = layer.getIdentifier();
                  }, null, false );
                 
                  try {
                      submitPostRequest( this.url, function(result) {}, bean, null, false );                    
                  } catch(e) {
                      alert( 99 + " " + e );
                  }
                  getElement( "IDLayerTree" ).contentWindow.location.reload( true );                 
                  controller.mapModel.removeLayer( layer );
                  controller.mapModel.setChanged( true );
                  controller.repaint();
            } 
        } catch( e ) {
            alert( 9 + " " + e );
        }
    }

	/**
	 * 
	 * @return base URL of the ajax controller
	 */
	function getUrl() {
		return this.url;
	}

	/**
	 * 
	 * @param state
	 *            application state to be set
	 */
	function setStatePerformed(state) {
		this.state = state;
		// use this object a handler for click events fired by the map
		// by assigning it a mode to the controller
		controller.mode = this;

	}

	/**
	 * will be invoked if findRoute button has been pressed. Checks form
	 * elements for correct values and performs a request against the server
	 * side of iGeoPortal for calculating a route
	 * 
	 * @param button
	 *            button that has been pressed
	 * @param event
	 *            event that has been fired
	 */
	function findRoutePerformed(button, event) {		
		
		var routing = controller.getFrame('IDRouting');
		var wp = new Array();
		var wpLocal = new Array();
		var wayPoints = this.gui.getWayPoints();
		for ( var i = 0; i < wayPoints.length; i++) {
			wp.push( [ wayPoints[i].x, wayPoints[i].y ]);
			wpLocal.push( [ wayPoints[i].xlocal, wayPoints[i].ylocal ]);
		}
		
		var tmpGUI = this.gui;
		// will throw an exception if request is not valid
		this.validateCalcRouteRequest( tmpGUI.getStartPoint(), tmpGUI.getEndPoint() );
		var bean = new function() {
			this.action = 'calculateRoute';
			this.transportation = routing.transportation;
			this.fastest = routing.Ext.getCmp('rdfastest').getValue();
			this.startText = tmpGUI.getStartPoint().text;
			this.endText = tmpGUI.getEndPoint().text;
			this.startWGS84x = tmpGUI.getStartPoint().x;
			this.startWGS84y = tmpGUI.getStartPoint().y;
			this.endWGS84x = tmpGUI.getEndPoint().x;
			this.endWGS84y = tmpGUI.getEndPoint().y;
			this.startLocalx = tmpGUI.getStartPoint().xlocal;
			this.startLocaly = tmpGUI.getStartPoint().ylocal;
			this.endLocalx = tmpGUI.getEndPoint().xlocal;
			this.endLocaly = tmpGUI.getEndPoint().ylocal;
			this.wp = wp;
			this.wpLocal = wpLocal;
		};
		
		var s = JSON.stringify(bean, null, false);
		var tis = this;
		submitPostRequest(this.url, function(result) {
				if (result.indexOf('ExceptionBean ') > -1) {
					Ext.Msg.show({
						title: 'route calculation',
						msg: 'calculating a route for desired start-, end- and way points failed',
						buttons: {ok: 'OK' },
						width: 300,
						icon: Ext.MessageBox.ERROR
					}); 
				} else {
					tis.drawRoute( JSON.parse( result ) );
				}
			}, s, null, false);		
	}
	
	/**
	 * Validates start and end point for a calculate route request. If one of these
	 * points is not valid an exception will be thrown 
	 * @param startPoint
	 * @param endPoint
	 */
	function validateCalcRouteRequest( startPoint, endPoint  ) { 
		if ( startPoint == null || 
			 (startPoint.x == null && startPoint.xlocal == null) ||  
			 (startPoint.y == null && startPoint.ylocal == null)) {
			throw "start point must be set";
		}
		if ( endPoint == null || 
			 (endPoint.x == null && endPoint.xlocal == null) ||  
			 (endPoint.y == null && endPoint.ylocal == null)) {
			throw "end point must be set";
		}		
	}

	/**
	 * releases allocated resources
	 */
	function kill() {
		if (this.markers != null) {
			controller.vOLMap.getMap().removeLayer(this.markers);
			this.markers = null;
			this.removeRoutingLayer();
		}
	}

	/**
	 * handle click events
	 */
	function onClick(e) {
		if (this.markers == null) {
			this.initMarkers();
		}
		var x = controller.gtrans.getSourceX( e.xy.x );
		var y = controller.gtrans.getSourceY( e.xy.y );
		var iconURL;
		var routing = controller.getFrame('IDRouting');
		var markerID;
		if (this.state == 'selectStartPoint') {
			iconURL = this.url + '/../modules/routing/images/flag_green.png';
			this.gui.setStartPoint( new RoutePoint(1000, 'start', x + '/' + y, -9E9, -9E9, x, y) );
			markerID = 1000;
		} else if (this.state == 'selectEndPoint') {
			iconURL = this.url + '/../modules/routing/images/flag_red.png';			
			this.gui.setEndPoint( new RoutePoint(1001, 'end', x + '/' + y, -9E9, -9E9, x, y) );
			markerID = 1001;
		} else {
			markerID = this.state;
			iconURL = this.url + '/../modules/routing/images/flag_blue.png';
			var wayPoints = this.gui.getWayPoints();
			for ( var i = 0; i < wayPoints.length; i++) {
				if ( wayPoints[i].id == this.state) {
					wayPoints[i].xlocal = x;
					wayPoints[i].ylocal = y;
					wayPoints[i].text =  x + '/' + y;					
					break;
				}			
			}
			this.gui.setWayPoints( wayPoints );
		}
		this.removeMarker(markerID);
		this.addMarker(markerID, x, y, iconURL);
	}

	/**
	 * adds a new marker to the marker layer
	 * 
	 * @param markerID
	 * @param x
	 * @param y
	 * @param iconURL
	 */
	function addMarker(markerID, x, y, iconURL) {
		if (this.markers == null) {
			this.initMarkers();
		}
		var size = new OpenLayers.Size(16, 16);
		var offset = new OpenLayers.Pixel(-(size.w / 2), -size.h);
		var icon = new OpenLayers.Icon(iconURL, size, offset);
		var olmap = controller.getFrame('IDOLMap');
		var marker = new olmap.OpenLayers.Marker(new olmap.OpenLayers.LonLat(x, y), icon);
		this.markerArray[0][markerID] = marker;
		this.markers.addMarker(marker);
	}

	/**
	 * initializes OpenLayers markers when first flag will be set
	 */
	function initMarkers() {
		if ( controller.vOLMap != null && controller.vOLMap.getMap() != null ) {
			var olmap = controller.getFrame('IDOLMap');
			if ( this.markers == null ) {
				this.markers = new olmap.OpenLayers.Layer.Markers("Markers");
				controller.vOLMap.getMap().addLayer(this.markers);
			}
		}
	}

	/**
	 * handle double click events
	 */
	function onDbClick(e) {
		alert(e.xy);
	}

	/**
	 * updates/draws a route point onto the map
	 * 
	 * @param routePoint
	 */
	function updateRoutePoint(routePoint) {
		this.removeMarker(routePoint.id);
		if (routePoint.id == 1000) {
			iconURL = this.url + '/../modules/routing/images/flag_green.png';
		} else if (routePoint.id == 1001) {
			iconURL = this.url + '/../modules/routing/images/flag_red.png';
		} else {
			iconURL = this.url + '/../modules/routing/images/flag_blue.png';
		}
		this.addMarker(routePoint.id, routePoint.xlocal, routePoint.ylocal,
				iconURL);
	}

	/**
	 * removes and destroys marker with passed ID
	 * 
	 * @param id
	 */
	function removeMarker(id) {
		if (this.markerArray[0][id] != null) {
			this.markers.removeMarker(this.markerArray[0][id]);
			this.markerArray[0][id].destroy();
			this.markerArray[0][id] = null;
		}
	}

	/**
	 * removes and destroys all markers
	 */
	function removeAllMarkers() {
		for ( var key in this.markerArray[0]) {
			var m = this.markerArray[0][key];
			this.markers.removeMarker( m );
			m.destroy();
		}
		this.markerArray = new Array();
		this.markerArray[0] = new Object();
	}
}
