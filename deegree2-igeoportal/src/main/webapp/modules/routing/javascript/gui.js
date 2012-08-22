/**
 * This class encapsulates creation and managing of the GUI of the Routing module. It
 * creates and uses an instance of RoutingGUIActionHandler which is responsible for
 * handling events that mainly targets the GUI and does not has any effect on the data
 * model and calculation a route.<br>
 * Parameters that can be enter be a uses can be read through some getter methods. Because
 * in some cases it is required to set these parameter from outside access to a few of these 
 * parameters is provided by setter methods. 
 */
function RoutingGUI() {

	// GUI messages and labels
	this.messages;
	// list of all defined way points
	this.wayPoints = new Array();
	// counter variable for way points
	this.maxWP = 0;
	// start point
	this.startPoint;
	// end point
	this.endPoint;
	// access point for server requests
	this.url = controller.vRouting.getUrl();
	// selected transportation means
	this.transportation = 'motorcar';
	// frame of the routing module
	this.routingFrame = controller.getFrame('IDRouting');
	// handler for GUI events
	this.actionHandler = new RoutingGUIActionHandler(this, controller.vRouting);

	//GUI handling/creation methods
	this.initGUI = initGUI;
	this.loadMessages = loadMessages;
	this.addControlPanel = addControlPanel;
	this.addInformationPanel = addInformationPanel;
	this.resetStates = resetStates;
	this.createWayPointPanel = createWayPointPanel;
	this.createLowerControlPanel = createLowerControlPanel;
	this.addInformationPanel = addInformationPanel;
	
	//setter and getter
	this.getWayPoints = getWayPoints;
	this.setWayPoints = setWayPoints;
	this.getStartPoint = getStartPoint;
	this.setStartPoint = setStartPoint;
	this.getEndPoint = getEndPoint;
	this.setEndPoint = setEndPoint;
	this.getMaxWayPointId = getMaxWayPointId;
	this.setMaxWayPointId = setMaxWayPointId;
	this.getTransporation = getTransporation;
	this.getMessages = getMessages; 
	this.setDescription = setDescription;
	this.setLength = setLength;
	this.setNumberOfNodes = setNumberOfNodes;

	/**
	 * initializes the GUI by using extJS elements/framework
	 * 
	 */
	function initGUI() {

		this.routingFrame.Ext.QuickTips.init();

		// load messages for language specific text/lables
		this.loadMessages();

		var messages = this.messages;
		var rootPanel = new this.routingFrame.Ext.TabPanel( {
			region : 'center',
			border : false,
			bodyBorder : false,
			activeTab : 0,
			items : [ {
				id : 'tabControlPanel',
				border : false,
				bodyBorder : false,
				title : messages['IGEO_ROUTE_TAB1'],
				layout : 'border'
			}, {
				id : 'tabInfoPanel',
				border : false,
				bodyBorder : false,
				title : messages['IGEO_ROUTE_TAB2'],
				layout : 'border'
			} ]
		});

		this.addControlPanel();
		this.addInformationPanel();

		new this.routingFrame.Ext.Viewport( {
			id : 'VIEWPORT',
			layout : 'fit',
			items : [ rootPanel ],
			stateful: false
		});
	}

	/**
	 * resets all toggle buttons except the one is passed to the method
	 * 
	 * @param button
	 */
	function resetStates(button) {
		for ( var i = 0; i <= this.maxWP; i++) {
			var cmp = this.routingFrame.Ext.getCmp('btWP_' + i);
			if (cmp && cmp != button) {
				cmp.toggle(false, true);
			}
		}
		var cmp = this.routingFrame.Ext.getCmp('btStart');
		if (cmp && cmp != button) {
			cmp.toggle(false, true);
		}
		cmp = this.routingFrame.Ext.getCmp('btEnd');
		if (cmp && cmp != button) {
			cmp.toggle(false, true);
		}
	}

	/**
	 * 
	 * loads messages used within the GUI to enable language specific layout
	 */
	function loadMessages() {
		// create bean containing all required messages
		var bean = JSON.stringify(new function() {
			this.action = 'getMessages';
			this.keys = [ 'IGEO_ROUTE_TAB1', 'IGEO_ROUTE_TAB2', 'IGEO_ROUTE_STARTPTN_TOOLTIP', 'IGEO_ROUTE_ENDPTN_TOOLTIP',
			              'IGEO_ROUTE_SELECT_GEOGRNAME_OK_TT', 'IGEO_ROUTE_SELECT_GEOGRNAME_OK', 'IGEO_ROUTE_SELECT_GEOGRNAME_CANCEL',
			              'IGEO_ROUTE_SELECT_GEOGRNAME_CANCEL_TT', 'IGEO_ROUTE_SELECT_GEOGRNAME_TITLE',
			              'IGEO_ROUTE_FIND_BT_TT', 'IGEO_ROUTE_FIND_BT', 'IGEO_ROUTE_FIND_ERROR', 'IGEO_ROUTE_FIND_BT_OK',
			              'IGEO_ROUTE_LB_STARTPOINT', 'IGEO_ROUTE_LB_ENDPOINT', 'IGEO_ROUTE_LB_WAYPOINTS',
			              'IGEO_ROUTE_BT_ADDWAYPOINT_TT', 'IGEO_ROUTE_BT_ADDWAYPOINT', 'IGEO_ROUTE_BT_DELWAYPOINT_TT',
			              'IGEO_ROUTE_BT_SETWAYPOINT_TT', 'IGEO_ROUTE_CB_TRANSPORTATION', 'IGEO_ROUTE_LB_SHORTEST',
			              'IGEO_ROUTE_LB_FASTEST', 'IGEO_ROUTE_BT_CLEAR_TT', 'IGEO_ROUTE_BT_CLEAR', 'IGEO_ROUTE_BT_REVERSE_TT',
			              'IGEO_ROUTE_BT_REVERSE', 'IGEO_ROUTE_LB_DESCRIPTION', 'IGEO_ROUTE_LB_ROUTELENGTH',
			              'IGEO_ROUTE_LB_OSMNODES'];
		}, null, false);

		// send request/bean to the server part of the module
		var tis = this;
		try {
			submitPostRequest(this.url, function(result) {
				tis.messages = JSON.parse(result);
			}, bean, null, false);
		} catch (e) {
			alert(22 + JSON.stringify(e) + this.url);
		}
	}
	
	function getMessages() {
		return this.messages;
	}

	/**
	 * adds the control panel to the GUI/TabPanel
	 */
	function addControlPanel() {

		var startEndPanel = new this.routingFrame.Ext.Panel( {
			id : 'startEnd',
			border : false,
			layout : 'absolute',
			height : 120,
			y : 0,
			anchor : '100%'
		});

		// create start section
		var lbStart = new this.routingFrame.Ext.form.Label( {
			text : this.messages['IGEO_ROUTE_LB_STARTPOINT'],
			x : 10,
			y : 10,
			width : 300,
			height : 25
		});
		startEndPanel.add(lbStart);

		var tis = this;		
		var btStart = new this.routingFrame.Ext.Button( {
			id : 'btStart',
			enableToggle : true,
			x : 10,
			y : 30,
			tooltip : tis.messages['IGEO_ROUTE_STARTPTN_TOOLTIP'],
			icon : './images/flag_green.png',
			handler : function(toggled) {
				if (toggled) {
					tis.resetStates(this);
					controller.vRouting.setStatePerformed( 'selectStartPoint' );
				}
			}
		});
		startEndPanel.add(btStart);

		var tfStart = new this.routingFrame.Ext.form.TextField( {
			id : 'tfStart',
			x : 40,
			y : 30,
			anchor : '95%',
			onBlur : function(event, textfield) {
				tis.actionHandler
						.solveGeographicNamePerformed(event, textfield);
			}
		});
		startEndPanel.add(tfStart);

		// create end section
		var lbStart = new this.routingFrame.Ext.form.Label( {
			text : this.messages['IGEO_ROUTE_LB_ENDPOINT'],
			x : 10,
			y : 60,
			width : 300,
			height : 25
		});
		startEndPanel.add(lbStart);

		var btEnd = new this.routingFrame.Ext.Button( {
			id : 'btEnd',
			enableToggle : true,
			x : 10,
			y : 80,
			tooltip : tis.messages['IGEO_ROUTE_STARTPTN_TOOLTIP'],
			icon : './images/flag_red.png',
			handler : function(toggled) {
				if (toggled) {
					tis.resetStates(this);
					controller.vRouting.setStatePerformed('selectEndPoint');
				}
			}
		});
		startEndPanel.add(btEnd);

		var tfEnd = new this.routingFrame.Ext.form.TextField( {
			id : 'tfEnd',
			x : 40,
			y : 80,
			anchor : '95%',
			onBlur : function(event, textfield) {
				tis.actionHandler
						.solveGeographicNamePerformed(event, textfield);
			}
		});
		startEndPanel.add(tfEnd);

		// waypoint panel
		var waypointPanel = new this.routingFrame.Ext.Panel( {
			id : 'waypoints',
			layout : 'absolute',
			border : false,
			height : this.wayPoints.length * 40 + 230,
			y : 110,
			anchor : '100%'
		});

		// create start section
		var lbWayPoints = new this.routingFrame.Ext.form.Label( {
			text : this.messages['IGEO_ROUTE_LB_WAYPOINTS'],
			x : 10,
			y : 10,
			anchor : '100%',
			height : 25
		});
		waypointPanel.add(lbWayPoints);

		// vertical anchor for starting to add gui elements
		var yAnchor = 30;

		var routingGUI = this;
		var btAddWp = new this.routingFrame.Ext.Button( {
			id : 'btAddWP',
			x : 10,
			y : yAnchor + 5,
			tooltip : this.messages['IGEO_ROUTE_BT_ADDWAYPOINT_TT'],
			text : this.messages['IGEO_ROUTE_BT_ADDWAYPOINT'],
			icon : '../../images/add.png',
			handler : function(toggled) {
				// create a new way point and adds according GUI elements
			// because of this this button must be moved downwards
			var cmp = routingGUI.routingFrame.Ext.getCmp('btAddWP');
			waypointPanel.add( routingGUI.createWayPointPanel( cmp.getPosition(true)[1] - 5) );
			cmp.setPagePosition(10, cmp.getPosition()[1] + 30);
			cmp = routingGUI.routingFrame.Ext.getCmp( 'lowerControlPanel' );
			cmp.setPagePosition( 0, cmp.getPosition()[1] + 30 );
			waypointPanel.setHeight( waypointPanel.getHeight() + 30 );
			waypointPanel.doLayout();
			// create and store a new route point
			routingGUI.wayPoints.push( new RoutePoint( routingGUI.maxWP, 'waypoint', '', -9E9, -9E9, -9E9, -9E9) );
			routingGUI.maxWP++;		
		}
		});
		waypointPanel.add(btAddWp);
		waypointPanel.add(this.createLowerControlPanel(yAnchor + 30));

		var main = new this.routingFrame.Ext.Panel( {
			id : 'controlPanel',
			region : 'center',
			layout : 'absolute',
			border : false,
			items : [ startEndPanel, waypointPanel ]
		});

		var cmp = this.routingFrame.Ext.getCmp('tabControlPanel');
		cmp.add(main);
		cmp.doLayout();
	}

	/**
	 * adds a new way point to the control panel
	 * 
	 * @param i
	 *            number of the way point
	 * @param yAnchor
	 *            anchor (y-position) of the way point
	 * @return panel
	 */
	function createWayPointPanel(yAnchor) {
		var panel = new this.routingFrame.Ext.Panel( {
			id : 'wayPoint_' + this.maxWP,
			border : false,
			layout : 'absolute',
			anchor : '100%',
			height : 30,
			x : 0,
			y : yAnchor
		});

		var routingGUI = this;
		var wpNumber = this.maxWP
		var btWP = new this.routingFrame.Ext.Button( {
			id : 'btWP_' + this.maxWP,
			enableToggle : true,
			x : 10,
			y : 5,
			tooltip : this.messages['IGEO_ROUTE_BT_SETWAYPOINT_TT'],
			icon : './images/flag_blue.png',
			handler : function(toggled) {
				if (toggled) {
					routingGUI.resetStates(this);
					// invoke method of the routing module class
					controller.vRouting.setStatePerformed( wpNumber );
				}				
			}
		});
		panel.add(btWP);

		var routingGUI = this;
		var tfWP = new this.routingFrame.Ext.form.TextField( {
			id : 'tfWP_' + this.maxWP,
			x : 40,
			y : 5,
			width : 150,
			onBlur : function(event, textfield) {
				routingGUI.actionHandler.solveGeographicNamePerformed( event, textfield );
			}
		});
		panel.add( tfWP );

		var routingGUI = this;
		var btDel = new this.routingFrame.Ext.Button( {
			id : 'btWPDelete_' + this.maxWP,
			x : 200,
			y : 5,
			tooltip : this.messages['IGEO_ROUTE_BT_DELWAYPOINT_TT'],
			icon : '../../images/cancel.png',
			handler : function(button, event) {
				var waypointPanel = routingGUI.routingFrame.Ext.getCmp('waypoints');
				var tmp = button.getId().split('_');
				var idx = parseInt(tmp[1]);

				// recreate way point array without the removed way point
				var tmpArray = new Array();
				for ( var i = 0; i < routingGUI.wayPoints.length; i++) {
					if ( routingGUI.wayPoints[i].id != idx ) {
						tmpArray.push( routingGUI.wayPoints[i] );
					}
				}
				this.wayPoints = tmpArray;

				// re-positioning/resizing GUI elements
				waypointPanel.remove( routingGUI.routingFrame.Ext.getCmp( 'wayPoint_' + idx ) );
				for ( var i = idx; i <= routingGUI.maxWP; i++) {
					var cmp = routingGUI.routingFrame.Ext.getCmp( 'wayPoint_' + i );
					if (cmp) {
						var pos = cmp.getPosition();
						cmp.setPagePosition(pos[0], pos[1] - 30);
					}
				}
				var cmp = routingGUI.routingFrame.Ext.getCmp( 'lowerControlPanel' );
				cmp.setPagePosition( 10, cmp.getPosition()[1] - 30 );
				cmp = routingGUI.routingFrame.Ext.getCmp( 'btAddWP' );
				cmp.setPagePosition( 10, cmp.getPosition()[1] - 30 );
				waypointPanel.setHeight(waypointPanel.getHeight() - 30);
				waypointPanel.doLayout();

				// remove marker from the map
				controller.vRouting.removeMarker( idx );
			}
		});
		panel.add(btDel);			
		
		return panel;
	}

	/**
	 * 
	 * @param yAnchor
	 *            y-position of the panel
	 * @return lower control panel
	 */
	function createLowerControlPanel(yAnchor) {

		var messages = this.messages;
		var panel = new this.routingFrame.Ext.Panel( {
			id : 'lowerControlPanel',
			border : false,
			layout : 'absolute',
			anchor : '100%',
			height : 160,
			x : 0,
			y : yAnchor
		});

		var lbTransportation = new this.routingFrame.Ext.form.Label( {
			text : 'transportation',
			x : 10,
			y : 10,
			anchor : '100%',
			height : 25
		});
		panel.add(lbTransportation);

		var data = JSON.parse( this.messages['IGEO_ROUTE_CB_TRANSPORTATION'] );
		var combo = new this.routingFrame.Ext.form.ComboBox( {
			x : 10,
			y : 30,
			anchor : '95%',
			typeAhead : true,
			triggerAction : 'all',
			// lazyRender:true,
			mode : 'local',
			valueField : 'Id',
			value : 'motorcar',
			store : new this.routingFrame.Ext.data.ArrayStore( {
				id : 0,
				fields : [ 'Id', 'displayText' ],
				data : data
			}),
			valueField : 'Id',
			displayField : 'displayText',
			listeners : {
				'select' : function(cb, record, index) {
					this.transportation = record.get('Id');
				}
			}
		});
		panel.add(combo);

		var cbg = new this.routingFrame.Ext.form.RadioGroup( {
			x : 10,
			y : 65,
			anchor : '100%',
			height : 100,
			// Put all controls in a single column with width 100%
			columns : 1,
			items : [ {
				id : 'rdshortest',
				boxLabel : messages['IGEO_ROUTE_LB_SHORTEST'],
				name : 'cb-col-1',
				checked : true
			}, {
				id : 'rdfastest',
				boxLabel : messages['IGEO_ROUTE_LB_FASTEST'],
				name : 'cb-col-1'
			} ]
		});
		panel.add(cbg);

		var btFind = new this.routingFrame.Ext.Button( {
			id : 'findRoute',
			x : 10,
			y : 125,
			height : 25,
			width : 70,
			tooltip : messages['IGEO_ROUTE_FIND_BT_TT'],
			text : messages['IGEO_ROUTE_FIND_BT'],
			handler : function(button, event) {
				try {
					controller.vRouting.findRoutePerformed(button, event);
				} catch(e) {
					Ext.Msg.show({
						title: messages['IGEO_ROUTE_FIND_ERROR'],
						msg: e,
						buttons: {ok: messages['IGEO_ROUTE_FIND_BT_OK'] },
						width: 300,
						icon: Ext.MessageBox.ERROR
					}); 
				}
			}
		});
		panel.add(btFind);

		var routingGUI = this;
		var btClear = new this.routingFrame.Ext.Button( {
			id : 'clear',
			x : 85,
			y : 125,
			height : 25,
			width : 70,
			tooltip : messages['IGEO_ROUTE_BT_CLEAR_TT'],
			text : messages['IGEO_ROUTE_BT_CLEAR'],
			handler : function(button, event) {
				routingGUI.actionHandler.reset();
			}
		});
		panel.add(btClear);

		var btReverse = new this.routingFrame.Ext.Button( {
			id : 'reverse',
			x : 160,
			y : 125,
			height : 25,
			width : 70,
			tooltip : messages['IGEO_ROUTE_BT_REVERSE_TT'],
			text : messages['IGEO_ROUTE_BT_REVERSE'],
			handler : function(button, event) {
				routingGUI.actionHandler.reversePerformed(button, event);
			}
		});
		panel.add(btReverse);

		return panel;
	}

	/**
	 * adds a panel for presenting detailed information about a route to the
	 * second tab panel
	 */
	function addInformationPanel() {

		// header/label
		var lbDesc = new this.routingFrame.Ext.form.Label( {
			text : this.messages['IGEO_ROUTE_LB_DESCRIPTION'],
			x : 10,
			y : 5,
			anchor : '100%',
			height : 'auto'
		});

		var pnLabel = new this.routingFrame.Ext.Panel( {
			region : 'north',
			border : false,
			layout : 'absolute',
			height : 30,
			items : [ lbDesc ]
		});

		// description text area
		var taDesc = new this.routingFrame.Ext.form.TextArea( {
			id : 'infDesc',
			anchor : '100% 100%',
			border : false,
			value : ''
		});

		var pnTA = new this.routingFrame.Ext.Panel( {
			region : 'center',
			border : false,
			layout : 'absolute',
			height : 150,
			items : [ taDesc ]
		});

		// some label for info about route length, no. of nodes etc.
		var lbLength = new this.routingFrame.Ext.form.Label( {
			id : 'routeLength',
			text : this.messages['IGEO_ROUTE_LB_ROUTELENGTH'],
			x : 10,
			y : 5,
			anchor : '100%',
			height : 'auto'
		});

		var lbNodes = new this.routingFrame.Ext.form.Label( {
			id : 'osmNodes',
			text : this.messages['IGEO_ROUTE_LB_OSMNODES'],
			x : 10,
			y : 25,
			anchor : '100%',
			height : 'auto'
		});

//		var lbSegments = new this.routingFrame.Ext.form.Label( {
//			text : 'segments (way points): ',
//			x : 10,
//			y : 50,
//			anchor : '100%',
//			height : 'auto'
//		});

		var pnRI = new this.routingFrame.Ext.Panel( {
			region : 'south',
			border : false,
			layout : 'absolute',
			height : 150,
			items : [ lbLength, lbNodes ]
		});

		var main = new this.routingFrame.Ext.Panel( {
			id : 'infoPanel',
			border : false,
			region : 'center',
			layout : 'border',
			items : [ pnLabel, pnTA, pnRI ]
		});

		var cmp = this.routingFrame.Ext.getCmp('tabInfoPanel');
		cmp.add(main);
		cmp.doLayout();
	}

	/**
	 * 
	 * @return list of all defined way points
	 */
	function getWayPoints() {
		return this.wayPoints;
	}

	/**
	 * 
	 * @param wayPoints
	 *            list of all defined way points
	 */
	function setWayPoints(wayPoints) {
		this.wayPoints = wayPoints;
		for ( var i = 0; i < this.wayPoints.length; i++ ) {
			var cmp = this.routingFrame.Ext.getCmp( 'tfWP_' + this.wayPoints[i].id );
			if ( cmp ) {
				cmp.setValue( this.wayPoints[i].xlocal + '/' + this.wayPoints[i].ylocal);
			}
		}
	}

	/**
	 * 
	 * @return maximum (counter) value for way points IDs
	 */
	function getMaxWayPointId() {
		return this.maxWP;
	}

	/**
	 * can be used to reset the maxWP values
	 * 
	 * @param maxWP
	 *            maximum (counter) value for way points IDs
	 */
	function setMaxWayPointId(maxWP) {
		this.maxWP = maxWP;
	}

	/**
	 * 
	 * @return start point of a route
	 */
	function getStartPoint() {
		return this.startPoint;
	}

	/**
	 * 
	 * @param startPoint
	 *            start point of a route
	 */
	function setStartPoint(startPoint) {
		this.startPoint = startPoint;
		if ( this.startPoint != null ) {
			var s = startPoint.text;
			if ( s == null || s == '' ) {
				s = this.startPoint.xlocal + '/' + this.startPoint.ylocal;
			}
			this.routingFrame.Ext.getCmp('tfStart').setValue( s);
		}
	}

	/**
	 * 
	 * @return end point of a route
	 */
	function getEndPoint() {
		return this.endPoint;
	}

	/**
	 * 
	 * @param endPoint
	 *            end point of a route
	 */
	function setEndPoint(endPoint) {
		this.endPoint = endPoint;
		if ( this.endPoint != null ) {
			var s = endPoint.text;
			if ( s == null || s == '' ) {
				s = this.endPoint.xlocal + '/' + this.endPoint.ylocal;
			}
			this.routingFrame.Ext.getCmp('tfEnd').setValue( s );
		}
	}
	
	/**
	 * 
	 * @return selected transportation unit
	 */
	function getTransporation() {
		this.transportation;
	}
	
	/**
	 * 
	 * @param value description of a route
	 */
	function setDescription(value) {
		this.routingFrame.Ext.getCmp( 'infDesc' ).setValue( value );
	}
	
	/**
	 * 
	 * @param length length of a route
	 */
	function setLength(length) {
		this.routingFrame.Ext.getCmp( 'routeLength' ).setText( 'route length: ' + length + ' km' );
	}
	
	/**
	 * 
	 * @param numberOfNodes number of (OSM) nodes of a route
	 */
	function setNumberOfNodes(numberOfNodes) {
		this.routingFrame.Ext.getCmp( 'osmNodes' ).setText( 'OSM nodes: ' + numberOfNodes );
	}
	
}
