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
<%@ page pageEncoding="UTF-8" %>
<%@ page import="org.deegree.i18n.Messages" %>
<%@ page import="java.util.Locale" %>
<%
    Locale loc = request.getLocale();
%>
<html>
    <head>
		<link rel="stylesheet" type="text/css" href="../../javascript/ext-3.3.1/resources/css/ext-all.css" />
		<link rel="stylesheet" type="text/css" title="blue"  href="../../javascript/ext-3.3.1/resources/css/xtheme-blue.css" /> 
        <link rel="stylesheet" type="text/css" title="gray"  href="../../javascript/ext-3.3.1/resources/css/xtheme-gray.css" />
        <link rel="stylesheet" type="text/css" title="black" href="../../javascript/ext-3.3.1/resources/css/xtheme-access.css" />
		<!--[if lt IE 7.]>
		<script defer type="text/javascript" src="../../javascript/igeoportal/pngfix.js"></script>
		<![endif]-->
		<script type="text/javascript" src="../../javascript/model/layergroup.js"></script>
		<script type="text/javascript" src="../../javascript/request_handler.js"></script>
		<script type="text/javascript" src="../../javascript/utils.js"></script>
		<script type="text/javascript" src="../../javascript/json2.js"></SCRIPT>
		<script type="text/javascript" src="../../javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
		<script type="text/javascript" src="../../javascript/ext-3.3.1/ext-all.js"></script>		
		<script type="text/javascript" src="layerTreePanel.js"></script>
		<script TYPE="text/javascript"><!--
		    var DELAY = 1000;
	        var baseURL =  getRootURL() + 'ajaxcontrol'
		    var mapModel;
		    var delayedMapPaint;
		    var tree = null;
		    var nodeIDCounter = 0;		    
		    Ext.BLANK_IMAGE_URL = "../../javascript/ext-3.3.1/resources/images/default/s.gif";

		    function showErrorMessage(msg) {                    
                parent.Ext.Msg.show({
                    title: '<%=Messages.get( loc,"IGEO_STD_LAYTREE_ERROR_TITLE" ) %>',
                    msg: msg,
                    buttons: {ok: '<%=Messages.get( loc,"IGEO_STD_LAYTREE_ERROR_OK" ) %>' },
                    width: 300,
                    icon: parent.Ext.MessageBox.ERROR
                });                    
            }
		    
		    Ext.onReady( function() {
		    	Ext.QuickTips.init();
                register();
                initLayerTree();
		    	mapModel = parent.controller.mapModel;
		        delayedMapPaint = new Ext.util.DelayedTask( parent.controller.repaint, parent.controller );
		        
		        // callback functions for toolbar
		        var toolbarAddNode = function() {
		            var newNode = tree.addNode();
		            var renameCallback = function(btn, text) {
		                if ( btn == 'ok' ) {
		                    newNode.setText(text);
		                    sendContextUpdate({
		                        action: 'ADDNODE',
		                        node: newNode.id,
		                        nodeTitle: newNode.text,
		                        parentNode: newNode.parentNode ? newNode.parentNode.id : ""  
		                    }, addNodeCallback );
		                } else {
		                    newNode.remove();
		                }
		            };
		            renameNodeDialog( newNode, renameCallback );
		        };

		        // invoked after adding node on serverside
		        var addNodeCallback = function(response) {
		        	if ( response.indexOf( "ERROR:") == 0 ) {
		        		showErrorMessage( response );
                    } 
		        }
		        
		        var toolbarRemoveNode = function() { 
		            var node = tree.getSelectionModel().getSelectedNode();		            
		            var removeCallback = function(btn) {
		            	if ( btn == 'ok' ) {
			                node.remove();
			                sendContextUpdate({
			                    action: 'REMOVENODE',
			                    node:  node.id
			                }, removeNodeCallback );
						}
					};
					parent.Ext.Msg.show({
		            	title:'<%=Messages.get( loc,"IGEO_STD_LAYTREE_REMOVENODE_TITLE" ) %>',
			            msg: '<%=Messages.get( loc,"IGEO_STD_LAYTREE_REMOVENODE_MSG" ) %>',
		    	        buttons: {ok:'<%=Messages.get( loc,"IGEO_STD_LAYTREE_REMOVENODE_OK" ) %>', 
		        	      		  cancel:'<%=Messages.get( loc,"IGEO_STD_LAYTREE_REMOVENODE_CANCEL" ) %>'},
			            width: 300,
		    	        fn: removeCallback,
		                icon: parent.Ext.MessageBox.QUESTION
		            });
		        };

		        // invoked after removing on serverside
		        var removeNodeCallback = function(response) {
			        if ( response.indexOf( "ERROR:") == 0 ) {
			        	showErrorMessage( response );
			        } else {				        
			        	var layers = JSON.parse( response );                       
                        for ( var i = 0; i < layers.length; i++) {
                            var layer = mapModel.getLayerByName( layers[i] );
                            mapModel.removeLayer( layer );
                        }
                        mapModel.setChanged(true);
                        delayedMapPaint.delay( DELAY );
			        }
                }
		        
		        var toolbarRenameNode = function() {
		            var node = tree.getSelectionModel().getSelectedNode();
		            var renameCallback = function(btn, text) {
		                if ( btn == 'ok' ) {
		                    node.setText(text);
		                    sendContextUpdate({
		                        action: 'RENAMENODE',
		                        node:  node.id,
		                        nodeTitle: text
		                    }, renameNodeCallback );
		                }
		            }
		            renameNodeDialog( node, renameCallback );
		        };

		        // invoked after renaming node on serverside
		        var renameNodeCallback = function(response) {
		        	if ( response.indexOf( "ERROR:") == 0 ) {
		        		showErrorMessage( response );
                    }
                }

		        var toolbarOpenAbstract = function() {
                    var node = tree.getSelectionModel().getSelectedNode();
                    if ( node == null ) {
                    	showErrorMessage( "<%=Messages.get( loc,"IGEO_STD_LAYTREE_ABSTRACT_NO_NODE" ) %>" );
                    } else if ( !node.isLeaf() ) {
                        showErrorMessage( "<%=Messages.get( loc,"IGEO_STD_LAYTREE_ABSTRACT_NO_LEAF" ) %>" );
                    } else {
                        var s = "<%=Messages.get( loc,"IGEO_STD_LAYTREE_NO_ABSTRACT" ) %>";
                        var ll = mapModel.getLayerList();
                        var lgs = ll.getLayerGroups();
                        var l = null;
                        for (var i = 0; i < lgs.length;i++) {
                            lgs[i].setChanged( true );
                            var ls = lgs[i].getLayers();
                            for (var j = 0; j < ls.length;j++) {
                                if ( ls[j].isSelected( ) ) {
                                    l = ls[j];
                                }                             
                            }                           
                        }
                        if ( l.getAbstract() != null && l.getAbstract() != '' ) {
                            s = l.getAbstract();
                        }
                        
                    	var panel = new parent.Ext.Panel({
                            html: '<br/><h2>' + l.getTitle() + '</h2><br/><a style="color:#000000">' + s + '</a>',
                            border: true
                        });
                        var win = new parent.Ext.Window( {
                            title: '<%=Messages.get( loc,"IGEO_STD_LAYTREE_ABSTRACT_TITLE" ) %>',
                            closeAction: 'hide',
                            shadow: false,       
                            autoScroll: true,               
                            height: 500,
                            width: 450,
                            pageX: 500,
                            pageY: 250,
                            items: [ panel ]
                        });
                        win.show();
                    }
                };

                var toolbarOpenMetadata = function() {
                    var node = tree.getSelectionModel().getSelectedNode();
                    if ( node == null ) {
                        showErrorMessage( "<%=Messages.get( loc,"IGEO_STD_LAYTREE_ABSTRACT_NO_NODE" ) %>" );
                    } else if ( !node.isLeaf() ) {
                        showErrorMessage( "<%=Messages.get( loc,"IGEO_STD_LAYTREE_ABSTRACT_NO_LEAF" ) %>" );
                    } else {
                    	var s = "<%=Messages.get( loc,"IGEO_STD_LAYTREE_NO_METADATAURL" ) %>";
                        var ll = mapModel.getLayerList();
                        var lgs = ll.getLayerGroups();
                        var l = null;
                        for (var i = 0; i < lgs.length;i++) {
                            lgs[i].setChanged( true );
                            var ls = lgs[i].getLayers();
                            for (var j = 0; j < ls.length;j++) {
                                if ( ls[j].isSelected( ) ) {
                                    l = ls[j];
                                }                             
                            }                           
                        }
                        var mdURL = l.getMetadataURL();
                        if ( mdURL != null ) {           
                            var bean = new function() {
                            	this.className = 'getFullMetadataset';
                            	this.request = mdURL; 
                            }             	
                        	submitPostRequest( baseURL, mdHandler, JSON.stringify( bean ) );
                        }
                    }
                };

                var mdHandler = function( result ) {                    
                    var panel = new parent.Ext.Panel({
                                    html: '<br></br>' +result,
                                    border: true
                                });
                    var win = new parent.Ext.Window( {
                        title: '<%=Messages.get( loc,"IGEO_STD_LAYTREE_METADATA_TITLE" ) %>',
                        closeAction: 'hide',
                        shadow: false,       
                        autoScroll: true,               
                        height: 500,
                        width: 650,
                        pageX: 500,
                        pageY: 200,
                        items: [ panel ]
                    });
                    win.show();
                }    
		        
		        var renameNodeDialog = function( node, callback ) {
		            // first fix for firefox focus flaw -> http://extjs.com/forum/showthread.php?t=1519
		            parent.Ext.Msg.getDialog().on("show", function(d) {
		                var div = parent.Ext.get(d.el);
		                div.setStyle("overflow", "auto");
		                var text = div.select(".ext-mb-textarea", true);
		                if (!text.item(0))
		                    text = div.select(".ext-mb-text", true);
		                if (text.item(0))
		                    text.item(0).dom.select();
		            });
		            parent.Ext.Msg.show({
		                title: '<%=Messages.get( loc,"IGEO_STD_LAYTREE_RENAME_TITLE" ) %>',
		                msg: '<%=Messages.get( loc,"IGEO_STD_LAYTREE_RENAME_MSG" ) %>',
		                buttons: {ok:'<%=Messages.get( loc,"IGEO_STD_LAYTREE_RENAME_OK" ) %>', 
			                      cancel:'<%=Messages.get( loc,"IGEO_STD_LAYTREE_RENAME_CANCEL" ) %>'},
		                prompt: true,
		                value: node.attributes.text,
		                width: 300,
		                fn: callback,
		                icon: parent.Ext.MessageBox.QUESTION
		            });
		        }
		        
		        // create tree panel
		        tree = new deegree.igeo.layerTreePanel({
			        enableDD: true,
		            dataUrl: (baseURL + '?action=getTreeData'),
		            autoScroll: false,
		            border: false,
		            width: 300,
		            ddScroll: false,
		            tbar : [ { icon : '../../images/png/add_layergroup.png', 
		            	       tooltip: '<%=Messages.get( loc,"IGEO_STD_LAYTREE_ADDNODE" ) %>',	            	       
		            	       handler: toolbarAddNode },
		                     { icon : '../../images/png/delete_layer.png', 
		            	       tooltip: '<%=Messages.get( loc,"IGEO_STD_LAYTREE_REMOVENODE" ) %>',
				               handler: toolbarRemoveNode },
		                     { icon : '../../images/png/rename_layer.png' ,
				               tooltip: '<%=Messages.get( loc,"IGEO_STD_LAYTREE_RENAMENODE" ) %>',
		                       handler: toolbarRenameNode }
		                       //,
				             //{ icon : '../../images/png/show_metadata.png' ,
		                    //   tooltip: '<%=Messages.get( loc,"IGEO_STD_LAYTREE_OPEN_METADATA" ) %>',
	                         //  handler: toolbarOpenMetadata },
				             //{ icon: '../../images/png/show_abstract.png', 
	                         // tooltip: '<%=Messages.get( loc,"IGEO_STD_LAYTREE_OPEN_ABSTRACT" ) %>',
                             //handler: toolbarOpenAbstract }
		                   ]
		        });
		        tree.render( 'layerTree' );		  

		        // initially select all layers
		        var lgs = mapModel.getLayerList().getLayerGroups();
                for (var i = 0; i < lgs.length;i++) {
                    var ls = lgs[i].getLayers();
                    for (var j = 0; j < ls.length;j++) {
                        ls[j].setSelected( true );
                    }                           
                }
		              
	            // inform layertree object about tree instance to enable repaints
	            parent.controller.vLayerTree.setTree( tree );
	            
		        tree.on('collapsenode', function(node, el) {
		        	sendContextUpdate({
                        action: 'collapseNode',
                        node: node.id
                    }); 
                });

                tree.on('expandnode', function(node, el) {
                	sendContextUpdate({
                        action: 'expandNode',
                        node: node.id
                    });
                });

		        // invoked after changing visibility on serverside
		        var setVisibilityCallback = function(response) {		        	
		        	var layers = JSON.parse( response, null );
                    try {
                    	var ll = mapModel.getLayerList();
                        var lgs = ll.getLayerGroups();
                        for ( var i = 0; i < lgs.length; i++ ) {
                            lgs[i].setChanged( true );
                            var ls = lgs[i].getLayers();
                            for ( var j = 0; j < ls.length;j++ ) {
                                ls[j].setVisible( false );
                                for ( var k = 0; k < layers.length; k++ ) {
                                    if ( layers[k][0] == ls[j].getName() && layers[k][1] == lgs[i].getServiceURL() ) { 
                                        ls[j].setVisible( true );                                       
                                    } 
                                }
                            }                           
                        }       
                    } catch (e) {
                    }                    
                    delayedMapPaint.delay( DELAY );      
                }

		        // invoked after moving node on serverside
                var moveNodeCallback = function(response) {
                    try {
	                    var layerBeans = JSON.parse( response );
	                    var layerList = mapModel.getLayerList();
	                    var layerGroups = new Array();                    
	                    var layers = new Array();     
	                    var layer = mapModel.getLayer( layerBeans[0].name, layerBeans[0].serviceURL );
	                    layers.push( layer );       
	                    for (var i = 1; i < layerBeans.length; i++) {
	                        var layer = mapModel.getLayer( layerBeans[i].name, layerBeans[i].serviceURL );
	                        if ( layerBeans[i].serviceURL == layerBeans[i-1].serviceURL ) {
	                            layers.push( layer );  
	                        } else {		                        
		                        var lg = new LayerGroup( ""+i, layerBeans[i-1].serviceType, 
				                                         layerBeans[i-1].wmsName, 
				                                         layerBeans[i-1].serviceURL, layers );
                                lg.setFormat( layerBeans[i-1].format );
	                        	layerGroups.push( lg );
	                        	layers = new Array();
	                        	layers.push( layer );
	                        }
	                    } 
	                    
	                    var k = layerBeans.length-1;
	                    var lg = new LayerGroup( ""+(k+1), layerBeans[k].serviceType, layerBeans[k].wmsName, 
	    	                                     layerBeans[k].serviceURL, layers );
	                    lg.setFormat( layerBeans[k].format );
	                    layerGroups.push( lg );
	                    
	                    layerList.setLayerGroups( layerGroups );
                    } catch(e) {
                        alert( "1 "+ JSON.stringify( e ) );
                    }
                	delayedMapPaint.delay( DELAY );
                }
                            
		  
		        // create a node append callback function. 
		        // this function will be called once for every new node that is added to the tree panel.
		        // use it to attach events to each node. (deegree.igeo.layerTreePanel specific)
		        tree.nodeAppendCallback = function(tree, node, child) {
		        	
		            // change visibility
		            child.on( 'checkchange', function(node, checked) {
		                sendContextUpdate({
		                    action: 'SETVISIBILITY',
		                    node: node.id,
		                    hidden: !checked
		                }, setVisibilityCallback ); 
		            });		            
		            
		            child.on( 'checkchange', function(node, checked) {
			            updateCheckboxUp( node );
		                if ( !node.leaf ) {
		                   updateCheckboxDown( node.ui.isChecked(), node );
		                }
		            });

		            // double click fires a second toggle event, toggle againg to get the right state
		            child.on( 'dblclick', function( node ) {
		            	node.ui.toggleCheck();
		            });
		            
		            // when node/layer moved, update web map context on server
		            child.on( 'move', function( tree, node, oldParent, newParent, index ) {
		            	node.inBeforeMoveHandling = false;
		                sendContextUpdate({
		                    action: 'MOVENODE',
		                    node: node.id,
		                    beforeNode: node.nextSibling ? node.nextSibling.id : "",
		                    parentNode: node.parentNode.id
		                }, moveNodeCallback );
		            });		           

		            // show legend window
		            child.on('click', function(node, event) {
			            var xy = event.xy;			            
			            var offset = node.getDepth()*16 + 2;
			            if ( xy[0] >= offset && xy[0] <= (offset + 15) ) {
				            // just show legend if user has clicked onto legend symbol
			                if ( !node.attributes.img ) {
			                    var win = parent.Ext.getCmp( 'legendWindow' );
			                    if (win) win.hide();
			                } else {			                    
			                    parent.Ext.DomHelper.overwrite('legendWindowContent', { tag: 'div', children: [ 
			                                            { tag: 'h3', id: 'legendWindowText', html: node.attributes.text }, 
			                                            { tag: 'img', id: 'legendWindowImage', src: node.attributes.img } ] } );
			                    parent.Ext.getDom('legendWindowImage').src = node.attributes.img;

			                    getLegendWindow();
			                    //window.setTimeout( "setLegendWinSize()", 2000 );
			                }
			            }
		            });		           
		          
				};
				

				// get all selected child layer 
                var getSelectedLayers = function( node ) {                	
                    if ( node.isLeaf() ) {
                    	// if ( node.ui.isChecked()) {
                        return [node.id];
                   // } else {
                     //   return [];
                   // }
                    } else {
                        var filayers = [];
                        for ( var i = 0; i < node.childNodes.length; i++ ) {
                            filayers = filayers.concat( getSelectedLayers( node.childNodes[i] ) );
                        }
                        return filayers;
                    }
                };      				
				       
		        // set feature info layers
		        tree.getSelectionModel().on( 'selectionchange', function( selectionModel, node ) {		
			        if ( node != null ) {
			        	var layerIDs = JSON.stringify( getSelectedLayers(node ) );			        	
			        	sendContextUpdate({
	                        action: 'GETLAYERSINFO',
	                        layers:  layerIDs
	                    }, layersInfoCallback );
			        }
                    
			    });

		        var layersInfoCallback = function(response) {
			        try   {
			            // reset map model (unselect all layers)
			            var ll = mapModel.getLayerList();
	                    var lgs = ll.getLayerGroups();
	                    for (var i = 0; i < lgs.length;i++) {
	                        lgs[i].setChanged( true );
	                        var ls = lgs[i].getLayers();
	                        for (var j = 0; j < ls.length;j++) {
	                            ls[j].setSelected( false );	                            
	                        }                           
	                    }    
				        
				        // mark layers as selected
				        var layerBeans = JSON.parse( response );
				        for ( var i = 0; i < layerBeans.length; i++ ) {
					        var bean = layerBeans[i];
					        var layer = mapModel.getLayer( bean.name, bean.serviceURL );
					        layer.setSelected( true ); 
				        }
			        } catch(e) {
				        alert( e);
			        }
		        }
				
		        // propagate change upwards
		        var updateCheckboxUp = function( node ) {
		            try {
			            var lg = node.parentNode;
			            if ( lg ) { 
			            	var visible = false;
	                        for ( var i = 0; i < lg.childNodes.length; i++ ) {		                        
	                            if ( lg.childNodes[i].ui.isChecked() ) {
	                                visible = true;
	                                break;
	                            }
	                        }	
	                        if ( lg.text != 'root' &&  lg.text != 'dummy' && lg.ui.isChecked() != visible ) {
	                            lg.ui.toggleCheck( visible );
	                            updateCheckboxUp( lg );
	                        }
			            }
		            } catch ( e ) {
			            alert( "1 " + e );
		            }		           
		        };

		        // propagate change downwards
	            var updateCheckboxDown = function( visible, node ) {             
	                try {
		                if ( !node.isLeaf() ) {
	                	tree.expandPath( node.getPath() );
		                }
                        for ( var i = 0; i < node.childNodes.length; i++ ) {
                            node.childNodes[i].ui.toggleCheck( visible );	                                 
                            if ( !node.childNodes[i].leaf ) { 
                            	updateCheckboxDown( visible, node.childNodes[i] );
                            }
                        }	                    
	                } catch ( e ) {
	                    alert( "2 " + e );
	                }
	            };
	            
		    });
				    
		    // update web map context on server
		    // takes a key/value map for options
		    var sendContextUpdate = function( map, callback ) {
			    if ( callback == null ) {
				    // popup response if no callback is defined 
				    callback = function(res) {};
			    }
		    	submitPostRequest( baseURL, callback, JSON.stringify( map ) );
		    };

		    
		    // returns the legend window
		    var getLegendWindow = function() {
		        var win = parent.Ext.getCmp('legendWindow');
		        if ( !win ) {
		        	parent.Ext.getDom('legendWindowContent').style.display = "block";
		            var panel = new parent.Ext.Panel({
		                id: 'legendPanel',
		                contentEl: 'legendWindowContent',
		                border: true
		            });
		            win = new parent.Ext.Window( {
		                id: 'legendWindow',
		                title: '<%=Messages.get( loc,"IGEO_STD_LAYTREE_LEGEND_TITLE" ) %>',
		                closeAction: 'hide',
		                shadow: false,		 
		                autoScroll: true,               
		                height: auto,
		                width: auto,
		                pageX: 500,
		                pageY: 200,
		                items: [ panel ]
		            });
		        }
		        win.show();
		        return win;
		    };


		    function setLegendWinSize() {
                var win = getLegendWindow();
                var imageSize = parent.Ext.get( 'legendWindowImage' ).getSize();              
                
                var metrics = parent.Ext.util.TextMetrics.createInstance( 'legendWindowText' );
                var textSize = 100;

                // assure a min width, otherwise the titlebar can get messed up
                var w = Math.max( 80, Math.max( textSize.width, imageSize.width ) ) + 15;
                var h = imageSize.height + textSize.height + 15;                                
                parent.Ext.getCmp( 'legendPanel' ).setSize( { width: w, height: h } );
                if ( h > 300 ) {
                    h = 300;
                }
                win.setSize(w + win.getFrameWidth(), h + win.getFrameHeight() );
             //   win.show();
            }
            
            function register() {
                if ( parent.controller == null ) {
                    parent.controller = new parent.Controller();
                    parent.controller.init();
                }
                parent.setActiveStyleSheet();
            }

            function initLayerTree() {
                parent.controller.initLayerTree( document );      
                parent.controller.vLayerTree.repaint();          
            }
		--></script>
	</head>
	<body onload="register(); initLayerTree();" style="width:100%">
		<div id='legendWindowContent' style="display:none"></div>
		<div id='layerTree' style="width:300px"></div>
    </body>
</html>