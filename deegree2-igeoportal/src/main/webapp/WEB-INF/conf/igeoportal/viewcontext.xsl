<!-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:cntxt="http://www.opengis.net/context" xmlns:sld="http://www.opengis.net/sld" xmlns:deegree="http://www.deegree.org/context" xmlns:xlink="http://www.w3.org/1999/xlink">

	<xsl:template match="cntxt:ViewContext">
	<html>
		<head>			
			<title>
				<xsl:value-of select="./cntxt:General/cntxt:Title"/>
			</title>		
			<!--[if lt IE 7.]>
		      <script defer type="text/javascript" src="./javascript/pngfix.js"></script>
		    <![endif]-->	
			<link rel="stylesheet" type="text/css" href="./javascript/ext-3.3.1/resources/css/ext-all.css" />			<link rel="stylesheet" type="text/css" title="blue" href="./javascript/ext-3.3.1/resources/css/xtheme-blue.css" /> 		    <link rel="stylesheet" type="text/css" title="gray" href="./javascript/ext-3.3.1/resources/css/xtheme-gray.css" />		    <link rel="stylesheet" type="text/css" title="black" href="./javascript/ext-3.3.1/resources/css/xtheme-access.css" />		   
			<script type="text/javascript" src="./javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
			<script type="text/javascript" src="./javascript/ext-3.3.1/ext-all-debug.js"></script>            <script type="text/javascript" src="./javascript/jquery/jquery-1.7.1.min.js"></script>			<link rel="stylesheet" type="text/css">
				<xsl:attribute name="href"><xsl:value-of select="./cntxt:General/cntxt:Extension/deegree:Frontend/deegree:Style"/></xsl:attribute>
			</link>
	
			<xsl:apply-templates select="./cntxt:General/cntxt:Extension/deegree:Frontend/deegree:CommonJS/deegree:Name"/>
			<xsl:apply-templates select="./cntxt:General/cntxt:Extension/deegree:Frontend/deegree:*/deegree:Module/deegree:ModuleJS"/>
						<script type="text/javascript" src="./javascript/history.js"></script>
			<script type="text/javascript" src="./javascript/rpc.js"></script>
			<script type="text/javascript" src="./javascript/state/zoomout.js"></script>
            <script type="text/javascript" src="./javascript/state/featureinfo.js"></script>			<script type="text/javascript" src="./javascript/state/recenter.js"></script>
			
			<script language="JavaScript">						var controller = null;			
			var repository = new Array();
			repository[0] = new Array();						// determine frame ajax controller URL	        var tmp = window.location.pathname.split( '/' );	        var s = '';	        for ( var i = 0; i &lt; tmp.length-1; i++) {	            s += (tmp[i] + '/' ); 	        } 	        var controllerURL = window.location.protocol + '//' + window.location.host + s + 'ajaxcontrol';

			<xsl:call-template name="INITEXTJS"/>						var currentStyle = 'blue';			function setActiveStyleSheet(newStyle) {			     if(newStyle) currentStyle = newStyle;			     var i, a;			     var links = document.getElementsByTagName("link");			     var len = links.length;			     for (i = 0; i &lt; len; i++) {			         a = links[i];			         if ( a.getAttribute( "rel" ).indexOf( "style" ) != -1 &amp;&amp; a.getAttribute( "title" ) ) {			             a.disabled = true;			             if ( a.getAttribute( "title" ) == currentStyle ) {			                a.disabled = false;			             }			         }			     }			    for ( var t = 0; t &lt; window.frames.length; t++ ) {			        links = window.frames[t].document.getElementsByTagName("link");			        len = links.length;			        for (i = 0; i &lt; len; i++) {			            a = links[i];			            if ( a.getAttribute("rel").indexOf("style") != -1 &amp;&amp; a.getAttribute("title") ) {			                a.disabled = true;			                if ( a.getAttribute( "title" ) == currentStyle ) {			                    a.disabled = false;			                }			            }			        }			    }			}			
			function addObjectToRepository( key, value ) {
			  repository[0][key] = value;
			}
			
			function getObjectFromRepository(key) {
			  return repository[0][key];
			}						function storeFrame( area, top, left, width, height, visible ) {			     if ( top &gt; 0 &amp;&amp; left &gt; 0 ) {			         var bean = JSON.stringify( new function() {                             this.action = 'storeFrame';                             this.top = top;                             this.left = left;                             this.width = width;                             this.height = height;                             this.area = area;                             this.visible = visible;                           }, null, false );				      try {				         // submitPostRequest( controllerURL, function(result) {}, bean );				      } catch(e) {				          alert( "storeFrame: " + JSON.stringify( e ) );				      }			     }  			}
			
			function Controller() {
				this.mapModel = null;
				this.windows = new Array();
				this.mode = null;				
				// object for transforming map coordinated to pixel coordinates
				// and vice versa
				this.gtrans = null;
				this.vMapController = null;				this.dataWindow;                this.dataWindowTabs;
				
				// methods 				
				this.init = init;
				this.history = new History( this, 100 );
				this.initMapModel = initMapModel;
				this.actionPerformed = actionPerformed;
				this.repaint = repaint;
				this.replace = replace;
				this.addLayersToModel = addLayersToModel;
				this.setBBOXToFullMap = setBBOXToFullMap;
				this.openDownloadDialog = openDownloadDialog;
				this.removeLayerGroup = removeLayerGroup;
				this.moveLayerGroupUp = moveLayerGroupUp;
				this.moveLayerGroupDown = moveLayerGroupDown;
				this.resetContext = resetContext;
				this.setMapSize = setMapSize;
				this.getFeatureTypes = getFeatureTypes;
				this.setMode = setMode;				this.getFrame = getFrame;				this.openDataTableWindow = openDataTableWindow;
                function checkControl(className, ctrlMode, mode) {                    var map = window.controller.vOLMap.map                    var list = map.getControlsByClass(className)                    for(var i = 0; i &lt; list.length; ++i) {                        if(mode === ctrlMode) {                          list[i].activate()                        } else {                          list[i].deactivate()                        }                    }                }

                function setMode(mode) {                    this.mode = null                    checkControl('OpenLayers.Control.DragPan', 'move', mode)                    checkControl('OpenLayers.Control.ZoomBox', 'zoomin', mode)//                    checkControl('OpenLayers.Control.ZoomOut', 'zoomout', mode)                    if ( mode === 'recenter' ) {
                        this.mode = new Recenter();
                    } else if ( mode === 'featureinfo' ) {
                        this.mode = new FeatureInfo();
                    } else if ( mode === 'zoomout' ) {                        this.mode = new Zoomout()                    }                }                                function getFrame(name) {	                for ( var i = 0; i &lt; window.frames.length; i++ ) {	                    if ( window.frames[i].name == name ) {	                        return frames[i];	                    }	                }   	                return null;                }                                /**                * open window for displaying passed data table. If window is already opened, passed table                * will be added within a new tab                */                function openDataTableWindow(name, columns, data) {                    				    if ( this.dataWindow == null ) {				        this.dataWindowTabs = new Ext.TabPanel({				            id: 'TABS',				            activeTab: 0,				            frame:true,				            enableTabScroll: true,				            width: columns.length*110,				            autoScroll:true, 				            items: [] 				        });   				        				        this.dataWindow = new Ext.Window({				            title: 'FeatureInfo',				            closeAction: 'hide',				            closable: true,				            animCollapse: false,				            plain: true,				            x: 50,				            y: 230,				            width: 600,				            height: 400,				            margins: '0 5 0 5',				            layout:'border',				            items :  [{				                        border: false,				                        region: 'center',				                        autoScroll:true,				                        items: [this.dataWindowTabs]				                      }]				        });				    } 				    				     var header = new Array();				     for (var i = 0; i &lt; columns.length; i++) {				         header.push( {				             header: columns[i],				             dataIndex: columns[i],				             editor: new Ext.form.TextField({					                   allowBlank: false					                })									             				         } );				     }				     var store = new Ext.data.ArrayStore({				         autoDestroy: true,				         idIndex: 0,  				         data: data,				         fields: columns				     });								     var listView = new Ext.grid.EditorGridPanel({				         store: store,				         title: name,				         autoHeight: true,				         columns: header				     });								     this.dataWindowTabs.add( listView );				     this.dataWindowTabs.activate( this.dataWindowTabs.getComponent( 0 ) );				    				     this.dataWindow.show( this );   				}				                
				
				function replace(page) {
					window.location.replace(page);
				}
                
				function setMapSize(width, height) {
					this.mapModel.setWidth( width );
					this.mapModel.setHeight( height );
					this.mapModel = this.vMapController.ensureAspectRatio();
					this.repaint();
				}
	
				/*
				 * adds a list of layers served by one WMS to the map model. the passed layer
				 * parameter is an associative array with one field for each layer. each layer
				 * contains 'name', 'title', 'queryable'
				 */
				function addLayersToModel(wmsname, url, version, layers, format, createLayerGroup) {
				    if ( createLayerGroup == null ) {
						createLayerGroup = true;
				    }
					var wmslayer  = new Array();
					var lay = null;
					
					for (var i = 0; i &lt; layers.length; i++) {	
					   lay = new WMSLayer( wmsname, layers[i]['name'], layers[i]['title'], layers[i]['abstract'], 
											'default', null, layers[i]['visible'], false, layers[i]['queryable'], layers[i]['minScale'], 
											layers[i]['maxScale'], null, layers[i]['metadataURL'], null, null, null );
						wmslayer.push( lay );
					}
					var lg = new LayerGroup( wmsname, 'OGC:WMS ' + version,  wmsname, url, wmslayer );
                    lg.format=format;
					
					if ( this.vLayerTree ) {
                        // update data/tree model with added layers
                        // this is just required if layertree module is used
                        var srs = this.mapModel.getSrs();
                            var bean = JSON.stringify( new function() {
                                this.action = 'addLayer';
                                this.srs = srs;
                                this.createLayerGroup = createLayerGroup;
                                this.layerGroup = lg;
                            } );
                        try {
                            var tmp = window.location.pathname.split( '/' );
                            var s = '';
                            for ( var i = 0; i &lt; tmp.length-1; i++) {
                                s += (tmp[i] + '/' ); 
                            } 
                            var baseUrl = window.location.protocol + '//' + window.location.host + s + 'ajaxcontrol'; 
                            submitPostRequest( baseUrl, function(response) {
                                    if ( response.indexOf( "ERROR:" ) == 0 ) {
                                        alert( response );
                                    } else {
                                        // update layer tree
                                        var ids = JSON.parse( response );
                                        for (var i = 0; i &lt; wmslayer.length; i++) {
                                        wmslayer[wmslayer.length-i-1].identifier = ids[i];
                                        }
                                        try {
                                            getElement( "IDLayerTree" ).contentWindow.location.reload( true );
                                        } catch(e) {
                                            alert( e );
                                        }
                                    }
                                    this.repaint();
                                }, bean );                    
                        } catch(e) {
                            alert( JSON.stringify( e ) );
                        }
                    }
										
					var ll = this.mapModel.getLayerList();
					ll.addLayerGroup( lg, false );
					this.mapModel.setChanged( true );
					this.repaint();
				}
	
				function removeLayerGroup(id) {
					var ll = this.mapModel.getLayerList();
					this.mapModel.setChanged( true );
					ll.removeLayerGroupById( id );
					this.repaint();
				}
	
				function moveLayerGroupUp(index) {
					var ll = this.mapModel.getLayerList();
					this.mapModel.setChanged( true );
					ll.swapLayerGroupOrder( index, index-1 );
					this.repaint();
				}
	
				function moveLayerGroupDown(index) {
					var ll = this.mapModel.getLayerList();
					this.mapModel.setChanged( true );
					ll.swapLayerGroupOrder( index, index+1 );
					this.repaint();
				}
	
				function getModeName(param) {
					var sp = param.split('|');
					return sp[0];
				}
	
				function getTooltip(param) {
					var sp = param.split('|');
					if ( sp.length > 1 ) {
						return sp[1];
					} else {
						return sp[0];
					}
				}
	
				function setBBOXToFullMap() {
					this.mapModel.setBoundingBox( this.mapModel.getInitialBoundingBox() );
					this.mapModel = this.vMapController.ensureAspectRatio();
				}
	
				/* download of WFS features in current bounding box for registered and unregistered users */
                function openDownloadDialog() {
                    var featureTypes = this.getFeatureTypes();
                    if ( featureTypes == null || featureTypes == "" ) {
                        alert( "The selected layer does not contain available data to download" );
                        return;
                    }
                    var sessionID = null;
                    if ( this.vSessionKeeper != null &amp;&amp; this.vSessionKeeper.id != null ) {
                        sessionID = this.vSessionKeeper.id;
                    }
                    var win = window.open( '../../control?action=mapView:initDownload&amp;sessionID=' + sessionID, 'download',
                                           'width=550,height=400,left=100,top=0,scrollbars=yes,resizable=yes' );
                    win.focus();
                }
	
				function getFeatureTypes() {
					var lList = this.mapModel.getLayerList();
					var lGroups = lList.getLayerGroups();
                    var featureTypes = "";
					
					for( var i = 0; i &lt; lGroups.length; i++ ){
						var layers = lGroups[i].getLayers();
						for( var j = 0; j &lt; layers.length; j++ ){
							//both FeatureType and GeometryType must be known for the layer to be downloadable
							if ( layers[j].getDSFeatureType() != null &amp;&amp; layers[j].getDSFeatureType() != "" &amp;&amp;
                                 layers[j].getDSGeomType() != null &amp;&amp; layers[j].getDSGeomType() != "" ) {
                                 
                                var v = layers[j].getDSFeatureType();
								if ( j &lt; layers.length-1 ) {
									v += ",";
								}
								featureTypes += v;
							}
						}
					}
					return featureTypes;
				}
				
				function resetContext() {
					var req = "control?rpc=&lt;?xml version='1.0' encoding='UTF-8'?&gt;&lt;methodCall&gt;" +
							 "&lt;methodName&gt;mapClient:resetContext&lt;/methodName&gt;&lt;/methodCall&gt;";
					this.replace(req);
				}
	
				function actionPerformed(event) {
									  
					<xsl:for-each select="./cntxt:General/cntxt:Extension/deegree:Frontend/deegree:*/deegree:Module">                 
					
						<xsl:if test="./@type = 'toolbar'">
							<xsl:for-each select="./deegree:ParameterList/deegree:Parameter">
								<xsl:if test="deegree:Name != 'selected'">
									<xsl:if test="deegree:Name != 'bgcolor'">
										if ( event.name == ( getModeName('<xsl:value-of select="deegree:Name"/>') + 'Pressed') ) {										
										<xsl:if test="deegree:Action">
											<!-- if a specific action is defined to be performed when pressing a toggle button print it here -->
											<xsl:value-of select="deegree:Action"/>
										</xsl:if>
										<!-- if a toggle button has been pressed, store the assigned mode -->
										<xsl:choose>
										  <xsl:when test="./deegree:Value = 'ToggleButton'">
										      this.setMode( event.value );
										  </xsl:when>
										  <xsl:when test="starts-with( ./deegree:Name, 'fullextent' )">
                                              this.setBBOXToFullMap(); 
                                              this.history.addEnvelope( this.mapModel.getBoundingBox() ); 
                                              this.repaint();
                                          </xsl:when>
                                          <xsl:when test="starts-with( ./deegree:Name, 'movetoprevious' )">
                                              this.history.moveBack();
                                          </xsl:when>
                                          <xsl:when test="starts-with( ./deegree:Name, 'movetonext' )">
                                              this.history.moveForward();
                                          </xsl:when>
                                          <xsl:when test="starts-with( ./deegree:Name, 'home' )">
                                              this.resetContext();
                                          </xsl:when>
                                          <xsl:when test="starts-with( ./deegree:Name, 'addwms')">
                                              var win = window.open( '../../modules/addservice/addwms.jsp' ,'add', 'width=650,height=400,left=100,top=0,scrollbars=yes,resizable=yes');
                                              win.focus();
                                          </xsl:when>
                                          <xsl:when test="starts-with( ./deegree:Name, 'pdfprint')">
                                              var win = window.open( '../../modules/pdfprint/printdialog.jsp', 'pdfprint', 'width=850,height=600,left=100,top=0,scrollbars=yes,resizable=yes');
                                              win.focus();
                                          </xsl:when>
                                          <xsl:when test="starts-with( ./deegree:Name, 'download')">
                                              this.openDownloadDialog();
                                          </xsl:when>
                                          <xsl:when test="starts-with( ./deegree:Name, 'zoom2layer')">
                                              doRecenterToLayerRequest( document, controller );
                                          </xsl:when>
										</xsl:choose>										
									} else
									</xsl:if>
								</xsl:if>
							</xsl:for-each>
                            if ( event.name == 'BBOX' ) {
								var env1 = new Envelope( event.value.minx, event.value.miny,
														 event.value.maxx, event.value.maxy );
								this.mapModel.setBoundingBox( env1 );
								this.mapModel = this.vMapController.ensureAspectRatio();
								this.history.addEnvelope( this.mapModel.getBoundingBox() );
								this.repaint();
							} else {
								alert('unknown event: ' + event.name + ' - ' + this.mode);
							}
						</xsl:if>
					</xsl:for-each>
				}			
				
				function repaint() {					// update with current map size
					var envelope = this.mapModel.getBoundingBox();
					this.gtrans = new GeoTransform( envelope.minx, envelope.miny, envelope.maxx,
													envelope.maxy, 0, 0, this.mapModel.getWidth()-1,
													this.mapModel.getHeight()-1 );
	
					<xsl:for-each select="./cntxt:General/cntxt:Extension/deegree:Frontend/deegree:*/deegree:Module">
						<xsl:if test="./@type != 'menu'">
							<xsl:if test="./@type != 'space'">
								if ( this.v<xsl:value-of select="./deegree:Name"/> != null ) {
									this.v<xsl:value-of select="./deegree:Name"/>.repaint();
								}
							</xsl:if>
						</xsl:if>
					</xsl:for-each>				}
	
				<!-- declare a variable and an initMethod for each module that is not of type 'menu' and not of type 'space'-->
				<xsl:for-each select="./cntxt:General/cntxt:Extension/deegree:Frontend/deegree:*/deegree:Module">
					<xsl:if test="./@type != 'menu'">
						<xsl:if test="./@type != 'space'">
							this.v<xsl:value-of select="./deegree:Name"/> = null;
							this.init<xsl:value-of select="./deegree:Name"/> = init<xsl:value-of select="./deegree:Name"/>;
						</xsl:if>
					</xsl:if>
				</xsl:for-each>
				
				function init() {					this.initMapModel();
					this.history.init( this.mapModel.getBoundingBox() );
					<xsl:for-each select="./cntxt:General/cntxt:Extension/deegree:Frontend/deegree:*/deegree:Module">
						addObjectToRepository( 'SRC:' + '<xsl:value-of select="./deegree:Name"/>', '<xsl:value-of select="./deegree:Content"/>' );
					</xsl:for-each>				}
	
				<!-- implement initMethod for each module that is of type 'content' or 'toolbar'-->
				<xsl:for-each select="./cntxt:General/cntxt:Extension/deegree:Frontend/deegree:*/deegree:Module">
					<xsl:if test="./@type = 'content' or ./@type = 'overlay' ">
						function init<xsl:value-of select="./deegree:Name"/>(doc) {
							<xsl:variable name="vParam">							
								<xsl:for-each select="./deegree:ParameterList/deegree:Parameter/deegree:Value">
                                    <!-- this value must be large enough for long values (like contextswitcher), but shorter than a gazetteer-search CDATA tag -->
									<xsl:if test="string-length( . ) &lt; 450">
									<xsl:value-of select="."/>,</xsl:if></xsl:for-each>
							</xsl:variable>
							if ( this.v<xsl:value-of select="./deegree:Name"/>  == null ) {
							this.v<xsl:value-of select="./deegree:Name"/> =
								new <xsl:value-of select="./deegree:Name"/>( <xsl:value-of select="substring( $vParam, 0, string-length($vParam) )"/> );
							}
							this.v<xsl:value-of select="./deegree:Name"/>.paint( doc, doc.getElementsByTagName('body')[0] );
						}
					</xsl:if>
					<xsl:if test="./@type = 'toolbar'">
						function init<xsl:value-of select="./deegree:Name"/>(doc) {
							var bt = null;
							var sn = null;
							var sp = null;
							var buttonGroups = new Array( new ButtonGroup('<xsl:value-of select="./deegree:Name"/>') );
							<xsl:for-each select="./deegree:ParameterList/deegree:Parameter">
								<xsl:if test="deegree:Name != 'selected'">
									<xsl:if test="deegree:Name != 'bgcolor'">
										sn = getModeName('<xsl:value-of select="deegree:Name"/>');
										sp = getTooltip('<xsl:value-of select="deegree:Name"/>');
										bt = new <xsl:value-of select="deegree:Value"/>(sn, sp, '../../images/' + sn+ '.gif', '../../images/' + sn + '_a.gif' );
										bt.setListener( controller );
										buttonGroups[0].addButton( bt );
									</xsl:if>
								</xsl:if>
							</xsl:for-each>
							<!-- JM: changes to enable ommitting param bgcolor in toolbar module configuration -->
							<xsl:variable name="vTBCol">
								<xsl:choose>
									<xsl:when test="boolean(deegree:ParameterList/deegree:Parameter[deegree:Name='bgcolor'])">
										<xsl:value-of select="deegree:ParameterList/deegree:Parameter[deegree:Name='bgcolor']/deegree:Value" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="'null'" />
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							
							this.v<xsl:value-of select="./deegree:Name"/> =
								new Toolbar('<xsl:value-of select="./deegree:Name"/>', buttonGroups,
											 <xsl:value-of select="$vMapWidth"/>, <xsl:value-of select="$vNorthHeight"/>,
											 <xsl:value-of select="$vTBCol"/>);
							this.v<xsl:value-of select="./deegree:Name"/>.paint( doc, doc.getElementsByTagName('body')[0] );
						}
					</xsl:if>
				</xsl:for-each>
	
						<!-- imlement initMapModel Method -->
				function initMapModel() {
					var layerGroups = new Array();
					var wmslayer  = new Array();
					var lay = null;
					var lg = null;
					<xsl:for-each select="./cntxt:LayerList/cntxt:Layer">
						<xsl:variable name="vHidden">
							<xsl:if test="./@hidden ='1'">false</xsl:if>
							<xsl:if test="./@hidden ='true'">false</xsl:if>
							<xsl:if test="./@hidden ='0'">true</xsl:if>
							<xsl:if test="./@hidden ='false'">true</xsl:if>
						</xsl:variable>
						<xsl:variable name="vQueryable">
							<xsl:if test="./@queryable ='1'">true</xsl:if>
							<xsl:if test="./@queryable ='true'">true</xsl:if>
							<xsl:if test="./@queryable ='0'">false</xsl:if>
							<xsl:if test="./@queryable ='false'">false</xsl:if>
						</xsl:variable>
						<xsl:variable name="vMetadataURL">
							<xsl:value-of select="./cntxt:MetadataURL/cntxt:OnlineResource/@xlink:href" />
						</xsl:variable>
						<xsl:variable name="vLegendURL">
							<xsl:value-of select="./cntxt:StyleList/cntxt:Style[@current = 1]/cntxt:LegendURL/cntxt:OnlineResource/@xlink:href"/>
						</xsl:variable>
						<xsl:variable name="vMinScale">
							<xsl:choose>
								<xsl:when test="./cntxt:Extension/deegree:ScaleHint/@min != ''">
									<xsl:value-of select="./cntxt:Extension/deegree:ScaleHint/@min"/>
								</xsl:when>
								<xsl:otherwise>0</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:variable name="vMaxScale">
							<xsl:choose>
								<xsl:when test="./cntxt:Extension/deegree:ScaleHint/@max != ''">
									<xsl:value-of select="./cntxt:Extension/deegree:ScaleHint/@max"/>
								</xsl:when>
								<xsl:otherwise>999999999</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:variable name="vCurrentStyle">
							<xsl:choose>
								<xsl:when test="./cntxt:StyleList/cntxt:Style">
									<xsl:value-of select="./cntxt:StyleList/cntxt:Style/cntxt:Name[../@current = 1]"/>
								</xsl:when>
								<xsl:otherwise>default</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:variable name="vSelectedForQuery">
							<xsl:choose>
								<xsl:when test="./cntxt:Extension/deegree:SelectedForQuery = 'true'">true</xsl:when>
								<xsl:otherwise>false</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:variable name="vDSResource">
							<xsl:value-of select="./cntxt:Extension/deegree:DataService/cntxt:Server/cntxt:OnlineResource/@xlink:href" />
						</xsl:variable>
						<xsl:variable name="vDSGeomType">
							<xsl:value-of select="./cntxt:Extension/deegree:DataService/deegree:GeometryType" />
						</xsl:variable>
						<xsl:variable name="vDSFeatureType">
							<xsl:value-of select="./cntxt:Extension/deegree:DataService/deegree:FeatureType" />
						</xsl:variable>						<xsl:variable name="vTiled">						  <xsl:choose>						      <xsl:when test="boolean(./cntxt:Extension/deegree:tiled)">						          <xsl:value-of select="./cntxt:Extension/deegree:tiled" />						      </xsl:when>						      <xsl:otherwise>false</xsl:otherwise>						  </xsl:choose>                        </xsl:variable>
						<xsl:variable name="vIdentifier">
							<xsl:value-of select="./cntxt:Extension/deegree:identifier" />
						</xsl:variable>
						
						lay = new WMSLayer( '<xsl:value-of select="./cntxt:Server/@title"/>',
											'<xsl:value-of select="./cntxt:Name"/>',
											'<xsl:value-of select="./cntxt:Title"/>',
											'<xsl:value-of select="./cntxt:Abstract"/>',
											'<xsl:value-of select="$vCurrentStyle"/>', null,
											<xsl:value-of select="$vHidden"/>, 
											<xsl:value-of select="$vSelectedForQuery"/>,
											<xsl:value-of select="$vQueryable"/>,
											<xsl:value-of select="$vMinScale"/>,
											<xsl:value-of select="$vMaxScale"/>,
											'<xsl:value-of select="$vLegendURL"/>',
											'<xsl:value-of select="$vMetadataURL"/>',
											'<xsl:value-of select="$vDSResource"/>',
											'<xsl:value-of select="$vDSGeomType"/>',
											'<xsl:value-of select="$vDSFeatureType"/>',
											'<xsl:value-of select="$vIdentifier"/>',											<xsl:value-of select="$vTiled"/> );
						wmslayer.push( lay );
						<xsl:variable name="vSer">
							<xsl:value-of select="./following::*/cntxt:Server/cntxt:OnlineResource/@xlink:href"/>
						</xsl:variable>
	
						<xsl:copy-of select="./cntxt:Server/cntxt:OnlineResource/attribute"></xsl:copy-of>
	
						<xsl:if test="./cntxt:Server/cntxt:OnlineResource/@xlink:href != string($vSer) or count(../cntxt:Layer) = position()">
							<xsl:choose>
								<xsl:when test="./cntxt:Server/cntxt:OnlineResource/@xlink:href">
									lg = new LayerGroup( <xsl:value-of select="position()"/>,
														 '<xsl:value-of select="./cntxt:Server/@service"/><xsl:text> </xsl:text><xsl:value-of select="./cntxt:Server/@version"/>',
														 '<xsl:value-of select="./cntxt:Server/@title"/>',
														 '<xsl:value-of select="./cntxt:Server/cntxt:OnlineResource/@xlink:href"/>',
														 wmslayer);
								</xsl:when>
								<xsl:otherwise>
									lg = new LayerGroup( <xsl:value-of select="position()"/>,
														 '<xsl:value-of select="./cntxt:Server/@service"/><xsl:text> </xsl:text><xsl:value-of select="./cntxt:Server/@version"/>',
														 '<xsl:value-of select="./cntxt:Server/@title"/>',
														 '<xsl:value-of select="./cntxt:Server/cntxt:OnlineResource/@href"/>',
														 wmslayer);
								</xsl:otherwise>
							</xsl:choose>
	
							lg.setFormat('<xsl:value-of select="./cntxt:FormatList/cntxt:Format[@current='1' or @current='true']"/>');
							layerGroups.push( lg );
							wmslayer  = new Array();
						</xsl:if>
					</xsl:for-each>
					var layerList = new LayerList( 'll1' );
					for (var k = 0; k &lt; layerGroups.length; k++){
						layerList.addLayerGroup( layerGroups[k] );
					}
					var envelope = new Envelope( <xsl:value-of select="$vMinx"/>,<xsl:value-of select="$vMiny"/>,
												 <xsl:value-of select="$vMaxx"/>,<xsl:value-of select="$vMaxy"/> );
                    
                 
                    var windowWidth = 0;
			        var windowHeigth = 0;
			        if ( navigator.appName.indexOf( "Microsoft" )!= -1 ) {              
			           windowWidth = document.body.offsetWidth;
			           windowHeigth = document.body.offsetHeight;              
			        } else {
			           windowWidth = window.innerWidth;
			           windowHeigth = window.innerHeight; 
			        }
                    
                    this.mapModel = new MapModel( layerList, "<xsl:value-of select="$vCRS"/>", envelope, 
                                                  windowWidth-horizontalAdjustment, windowHeigth-verticalAdjustment );

                    this.vMapController = new MapController( this.mapModel );
                    this.mapModel = this.vMapController.ensureAspectRatio();	
                    
					this.gtrans = new GeoTransform( envelope.minx, envelope.miny, envelope.maxx, envelope.maxy,
													0, 0, this.mapModel.getWidth()-1, this.mapModel.getHeight()-1 );
				}
			}
		            function fixIE(){                $('#mappanelheightfix').children().css('height', '100%')                    .children().css('height', '100%')            }    			</script>
		</head>
		        <body onload="init_iGeo(); setActiveStyleSheet('blue'); fixIE()">            <div id="menubar"/>            <div id="legendWindowContent" style="display:none"/>        </body>    
	</html>
	</xsl:template>
</xsl:stylesheet>
