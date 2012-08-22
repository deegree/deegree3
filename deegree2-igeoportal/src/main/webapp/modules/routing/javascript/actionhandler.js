/**
 * 
 * @param gui
 */
function RoutingGUIActionHandler(gui, routing) {
	
	this.gui = gui;
	this.routing = routing;
	this.routingFrame = controller.getFrame( 'IDRouting' );
	
	this.reversePerformed = reversePerformed;
	this.solveGeographicNamePerformed = solveGeographicNamePerformed;
	this.handleCheckGeographicName = handleCheckGeographicName;
	this.createRoutePoint = createRoutePoint;
	this.isCoordinates = isCoordinates;
	this.reset = reset;
	this.isNumber = isNumber;
	this.getCoordinates = getCoordinates;

	
	/**
	 * reverse the direction of the route start-, end- and way points
	 * @param button button that has been pressed
	 * @param event event that has been fired
	 */
	function reversePerformed(button, event) {
		var start = this.routingFrame.Ext.getCmp( 'tfStart' ).getValue();
		var end = this.routingFrame.Ext.getCmp( 'tfEnd' ).getValue();
		var tmp = start;
		this.routingFrame.Ext.getCmp( 'tfStart' ).setValue( end );
		this.routingFrame.Ext.getCmp( 'tfEnd' ).setValue( tmp );
		// reverse way points array
		var wayPoints = this.gui.getWayPoints();
		wayPoints.reverse();
		for (var i = 0; i < this.gui.wayPoints.length; i++) {		
			wayPoints[i].id = i;
		}
		
		// set reversed text to the GUI text fields
		var k = 0;
		for ( var i = 0; i <= gui.getMaxWayPointId(); i++ ) {		
			var wp = this.routingFrame.Ext.getCmp( 'tfWP_' + i );
			if ( wp ) {
		        wp.setValue( wayPoints[k++].text ); 	        
			}
		}
		this.gui.setWayPoints( wayPoints );
	}
	
	/**
	 * If the content of a text field is not a pair of coordinates the server
	 * side of iGeoPortal will be ask if content string is a valid name and if
	 * so which coordinates it has 
	 * @param event event that has been fired
	 * @param textfield
	 */
	function solveGeographicNamePerformed(event, textfield) {
		if ( textfield.value == null || textfield.value == '' ) {
			// nothing to do because the use did not enter something
			return;
		}
		
		if ( !this.isCoordinates( textfield.value ) ) {
			// if the value of a text field is not a pair of coordinates we have
			// to ask the server for checking if it's a valid geographic name		
			var bean = JSON.stringify( new function() {
					this.action = 'checkGeographicName';
					this.QUERYSTRING = textfield.value
				}, null, false );
			try {
	            var result = submitPostRequest( gui.url, function(result) {}, bean, null, false );
	            this.handleCheckGeographicName( result, textfield ); 
	        } catch(e) {
	            alert( 5 + " " + e );
	        }
		} else {
			var coords = this.getCoordinates( textfield.value );
			this.createRoutePoint( textfield.id, textfield.value, coords[0], coords[1], -9E9, -9E9 );
		}
	}
	
	/**
	 * 
	 * @param result result of a request against the server for validating a geographic name.
	 *               Result may be empty, exact one match and a list of matches 
	 * @param textfield
	 */
	function handleCheckGeographicName(result, textfield) {
	
		var messages = this.gui.messages;
		var items = JSON.parse( result );
		
		if ( items.length == 0 ) {
			Ext.Msg.show({
				title: 'route point search',
				msg: 'no geographic location found',
				buttons: {ok: 'OK' },
				width: 300,
				icon: Ext.MessageBox.WARNING
			}); 
		} else if ( items.length == 1 ) {
		} else {
			// in case where several matching locations has been found we present a selection
			// of matching locations to the user so he may choose one or reset text field to
			// enter e new name
			
			var store = new Ext.data.ArrayStore({
		        data   : items,
		        fields : ['name', 'p1', 'p2', 'p3', 'p4']
		    });
			
			var combo = new Ext.form.ComboBox({
				        store        : store,
				        displayField : 'name',
				        width	     : 500,
				        x			 : 10,
				        y			 : 15,
				        mode         : 'local'
				    });
			
			var tis = this;
			var btOK = new Ext.Button({    
				   x: 10,			   
			       y: 55,
			       height: 25,
			       width: 30,
			       tooltip: messages['IGEO_ROUTE_SELECT_GEOGRNAME_OK_TT'],
			       text: messages['IGEO_ROUTE_SELECT_GEOGRNAME_OK'],
			       handler: function(button, event){
								var idx = store.find('name', combo.getValue() );
								// fill text field with selected value/location
								textfield.value = items[idx][0];
								// WGS 84 coordinate
								var x1 = parseFloat( items[idx][1].split(' ')[0] );
								var y1 = parseFloat( items[idx][1].split(' ')[1] );
								// coordinate using CRS of current map
								var x2 = parseFloat( items[idx][2].split(' ')[0] );
								var y2 = parseFloat( items[idx][2].split(' ')[1] );
								// create a new route point object for select value
								var routePoint = tis.createRoutePoint( textfield.id, items[idx][0], x1, y1, x2, y2 );
								controller.vRouting.updateRoutePoint( routePoint );
								button.ownerCt.close();
							}
			});
			 var btCancel = new Ext.Button({    
				   x: 50,			   
			       y: 55,
			       height: 25,
			       width: 60,
			       tooltip: messages['IGEO_ROUTE_SELECT_GEOGRNAME_CANCEL_TT'],
			       text: messages['IGEO_ROUTE_SELECT_GEOGRNAME_CANCEL'],
			       handler: function(button, event){
				 				textfield.value = '';
				 				button.ownerCt.close();
							}
			});
			var w = new Ext.Window({
		        title      : messages['IGEO_ROUTE_SELECT_GEOGRNAME_TITLE'],
		        height     : 120,
		        layout     : 'absolute',
		        width	   : 530,
		        bodyStyle  : 'padding: 5px',
		        modal	   : true,
		        items      : [combo, btOK, btCancel]
		    });
			w.show();
		}
	}
	
	/**
	 * creates a new route point object depending on the type of the text field
	 * that has been used (can be solved by analyzing the passed id). 
	 * 
	 * @param id text field id
	 * @param value value/text of a route point
	 * @param x1 WGS84 x coordinate
	 * @param y1 WGS84 y coordinate
	 * @param x2 map crs x coordinate
	 * @param y2 map crs y coordinate
	 * @return
	 */
	function createRoutePoint(id, value, x1, y1, x2, y2 ) {
		if ( id.indexOf( 'tfWP_') > - 1 ) {
			// set entered text to according way point object
			var idx = parseInt( id.split('_')[1] );
			var wayPoints = this.gui.getWayPoints();
			for (var i = 0; i < wayPoints.length; i++) {
				if ( wayPoints[i].id == idx ) {
					wayPoints[i].text = value;
					wayPoints[i].x = x1;
					wayPoints[i].y = y1;
					wayPoints[i].xlocal = x2;
					wayPoints[i].ylocal = y2;
					this.gui.setWayPoints( wayPoints );
					return wayPoints[i];
				}
			}
			
		} else if ( id == 'tfStart' ) {
			var startPoint = new RoutePoint( 1000, 'start', value, x1, y1, x2, y2 );
			this.gui.setStartPoint( startPoint ); 
			return startPoint;
		} else {
			var endPoint = new RoutePoint( 1001, 'end', value, x1, y1, x2, y2 );
			this.gui.setEndPoint( endPoint );
			return endPoint;
		}
	}
	
	/**
	 * 
	 * @param value value to check for being a pair of coordinates. A valid pair of coordinates
	 *        contains two number separated by a '/', a ';' or a ' '. E.g: 12.45/32.435
	 * @return true or false
	 */
	function isCoordinates(value) {
		var tmp = value.split('/');
		if ( tmp.length == 1 ) {
			tmp = value.split(';');
		}
		if ( tmp.length == 1 ) {
			tmp = value.split(' ');
		}
		if ( tmp.length != 2 ) {
			// no valid pair of coordinates
			return false;
		}
		// if both array fields are valid numbers true will be returned
		return this.isNumber( tmp[0] ) && isNumber( tmp[1] ); 
	}
	
	function isNumber (o) {
	  return ! isNaN (o-0);
	}
	
	/**
	 * 
	 * @param value
	 * @return coordinate as float array
	 */
	function getCoordinates(value) {
		var tmp = value.split('/');
		if ( tmp.length == 1 ) {
			tmp = value.split(';');
		}
		if ( tmp.length == 1 ) {
			tmp = value.split(' ');
		}
		return [parseFloat(tmp[0]), parseFloat(tmp[1])];
	}
	
	
	/**
	 * clears all text fields of the control panel and removes all way points
	 */
	function reset() {
		this.routingFrame.Ext.getCmp( 'tfStart' ).setValue( '' );
		this.routingFrame.Ext.getCmp( 'tfEnd' ).setValue( '' );
		// remove all way points
		var waypointPanel = this.routingFrame.Ext.getCmp( 'waypoints' );
		for ( var i = 0; i <= gui.maxWP; i++ ) {		
			var wp = this.routingFrame.Ext.getCmp( 'wayPoint_' + i );
			if ( wp ) {
		        waypointPanel.remove( this.routingFrame.Ext.getCmp( 'wayPoint_' + i ) );        
		        var cmp = this.routingFrame.Ext.getCmp( 'lowerControlPanel' );
		        cmp.setPagePosition( 10, cmp.getPosition()[1] - 30 );
		        cmp = this.routingFrame.Ext.getCmp( 'btAddWP' );				
		        cmp.setPagePosition( 10, cmp.getPosition()[1] - 30 );
		        waypointPanel.setHeight( waypointPanel.getHeight() - 30 );
			}
		}
		// reset internal GUI variables
		gui.setWayPoints( new Array() );
		gui.setMaxWayPointId( 0 );
		gui.setStartPoint( null );
		gui.setEndPoint( null );
		// update way point panel
	    waypointPanel.doLayout();	
	    // remove markers from the map
	    this.routing.removeAllMarkers();
	}

}