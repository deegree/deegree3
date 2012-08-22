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
        <title>iGeoPortal Gazetteer Panel</title>
        <meta name="author" content="lat/lon GmbH" />
        <meta name="KeyWords" content="deegree iGeoPortal lat/lon" />
        <link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
        <link rel="stylesheet" type="text/css" href="../../javascript/ext-3.3.1/resources/css/ext-all.css" />
        <link rel="stylesheet" type="text/css" title="blue"  href="../../javascript/ext-3.3.1/resources/css/xtheme-blue.css" /> 
        <link rel="stylesheet" type="text/css" title="gray"  href="../../javascript/ext-3.3.1/resources/css/xtheme-gray.css" />
        <link rel="stylesheet" type="text/css" title="black" href="../../javascript/ext-3.3.1/resources/css/xtheme-access.css" />
        <script type="text/javascript" src="gazetteer.js"></script>
        <script type="text/javascript" src="../../javascript/request_handler.js"></script>
        <script type="text/javascript" src="../../javascript/json2.js"></script>
        <script type="text/javascript" src="../../javascript/utils.js"></script>
        <script type="text/javascript" src="../../javascript/validation.js"></script>
        <script type="text/javascript" src="../../javascript/envelope.js"></script>
        <script type="text/javascript" src="../../javascript/event.js"></script>
        <script type="text/javascript" src="../../javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="../../javascript/ext-3.3.1/ext-all.js"></script>
        <script type="text/javascript">

        var url = null;
        var selectID;
        var selectedIndex;
        var maxLevel;
        var diItems = new Array();
        
        function register() {
            if ( parent.controller == null ) {
                parent.controller = new parent.Controller();
                parent.controller.init();
            }               
            parent.setActiveStyleSheet();
        }

        function initGazetteer() {
            clearPage();
            selectedIndex = 0;
            parent.controller.initGazetteer(document);
            var tmp = window.location.pathname.split( '/' );
            var s = '';
            for ( var i = 0; i < tmp.length-3; i++) {
                s += (tmp[i] + '/' ); 
            } 
            url = window.location.protocol + '//' + window.location.host + s + 'ajaxcontrol';
            try {
                submitGetRequest( url + "?action=loadHierarchyList", handleLoadTHierarchyList, null, false );
            } catch(e) {
                //alert( 1 + JSON.stringify( e ) );
            }
            var layer = parent.controller.mapModel.getLayerByName( 'highlight' );
            var cb = getElement( 'INP_HIGHLIGHT' );            
            if ( layer != null && layer.isVisible() ) {                
                cb.checked = true; 
            } else {
                cb.checked = false;
            }
            initGUI();
        }

        function initGUI() {
            Ext.QuickTips.init();   
            new Ext.Button({
                tooltip: 'in der Karte anzeigen',
                icon: '../../images/search.png',
                text: '<%=Messages.get( loc, "IGEO_STD_BTN_ZOOMTO" ) %>',
                renderTo: 'ZOOM',
                width: 85,
                height: 25,
                handler: function(toggled){                               
                	zoomTo();
                }
            });
            /*
            new Ext.Button({
                enableToggle: true,
                tooltip: 'suchen',
                font-color: black,
                icon: '../../images/search.png',
                text: '<%=Messages.get( loc, "IGEO_STD_EGAZ_HIGHLIGHT_CB" ) %>',
                renderTo: 'HIGHLIGHT',
                width: 85,
                height: 25,
                handler: function(toggled){                               
                    removeLayer();
                }
            });
            */
            
        }

        function clearPage() {
            try {               
                var tmp = true;
                while ( tmp ) {
                    tmp = false;
                    var divs = document.getElementsByTagName( "div" );
                    for ( var i = 0; i < divs.length; i++ ) {
                        if ( divs[i].id != null && divs[i].id.indexOf( "NAVIGATION" ) == 0 ) {
                            removeElement( divs[i] );
                            tmp = true;
                        }
                    }
                }
                var inps = document.getElementsByTagName( "input" );
                for ( var i = 0; i < inps.length; i++ ) {
                    if ( inps[i].id != null && inps[i].id.indexOf( "INP_HIGHLIGHT" ) < 0 && inps[i].id.indexOf( "ext-gen" ) < 0 ) {
                        removeElement( inps[i] );       
                    }          
                }
            } catch(e) {
                alert( 10 + " " + JSON.stringify( e ) );
            }
            selectID = 0;
            maxLevel = 0;
        }

        function handleLoadTHierarchyList(response) {
            var list = JSON.parse( response );
            var select = getElement( "TYPES" );
            removeChildren( select );
            for (var i = 0; i < list.length; i++ ) {        
                if ( i == 0 ) {
                    appendOption( select, list[i], '' + i, true );
                } else {        
                    appendOption( select, list[i], '' + i, false );
                }
            }    
            var cb = new Ext.form.ComboBox({
                listeners:{ 'select': loadHierarchy },   
                itemId: 'hList',                    
                typeAhead: true,
                triggerAction: 'all',
                editable: false,
                transform:'TYPES',
                width:130,
                listWidth:130,
                x: 90,
                y: -17,
                forceSelection:true
            });

            selectedIndex = 0;             
            var bean = JSON.stringify( new function() {
                this.action = 'loadHierarchy';
                this.hierarchy = '' + 0;
             }, null, false );
             try {
                 submitPostRequest( url, handleLoadHierarchy, bean );                    
             } catch(e) {
                 alert( 22+ JSON.stringify( e ) );
             }
        }       

        function loadHierarchy(cb, record, index) {
            if ( index >= 0 ) {                                
                selectedIndex = record.data['value']; 
                var bean = JSON.stringify( new function() {
                   this.action = 'loadHierarchy';
                   this.hierarchy = '' + record.data['value'];
                }, null, false );
                try {
                    submitPostRequest( url, handleLoadHierarchy, bean );                    
                } catch(e) {
                    alert( 2 + JSON.stringify( e ) );
                }
            }
        }

        function handleLoadHierarchy(response) {
        	clearPage();
           try {        	   
               var body = document.getElementsByTagName( "body" )[0];
               var beans = JSON.parse( response );
               maxLevel = beans.length-1;       
               for ( var i = 0; i <= beans.length; i++ ) {
                   var div = createElement( "div", "NAVIGATION" + i, "NAVIGATION" + i, null, body );
                   div.style.position = "absolute";
                   div.style.top = ( 60 + i * 30 )+"px";
               }
               getElement( "HIGHLIGHT" ).style.top = ( 90 + beans.length * 30 ) + "px";
               getElement( "HIGHLIGHT" ).style.visibility = 'visible';     
               getElement( "ZOOM" ).style.top = ( 120 + beans.length * 30 ) + "px";
               getElement( "ZOOM" ).style.visibility = 'visible'; 
               for ( var i = 0; i < beans.length; i++ ) {                  
                   if ( beans[i].supportFreeText ) {
                       getElement( 'NAVIGATION' + i ).innerHTML = getFreeSearch( i, beans[i].name );
                       getElement( 'NAVIGATION' + (i + 1) ).innerHTML = getValueList( i, beans[i].name ); 
                   } else {
                       getElement( 'NAVIGATION' + (i + 1) ).innerHTML = getValueList( i, beans[i].name );
                       if ( i == 0 ) {
                           // if no free text search is defined initial list must be loaded automaticly
                           // while initializing GUI 
                           
                           // dummy element required to perform loading item list
                           var inp = createElement( "input", "" + (1000 + i), "" + i, null, body );
                           inp.style.visibility = 'hidden';
                           inp.value = '*';
                           loadValues1( i );
                       }                       
                   }
               }
           } catch( e ) {
               alert( 11 + " " + JSON.stringify( e ) );
           }          
        }

        function getFreeSearch( no, name ) {
            // use 1000+no as ID to enable having a freetext search plus a values list for
            // one hierarchy level
            return '<div style="position:absolute; width:100px; text-decoration:none"><%=Messages.get( loc, "IGEO_STD_EGAZ_SEARCH" ) %>' +
                   '<input id="' + (1000 + no) + '" style="position:absolute; left:90px; width:100px; color:#000000">' +
                   '<img style="position:absolute; left:220px" alt="search" src="../../images/png/forward_green.png" '+
                   'onclick="loadValues1( ' + no + ' )" title="<%=Messages.get( loc, "IGEO_STD_EGAZ_SEARCH_TT" ) %>"></div>';
        }

        function getValueList(no, name) {
            return '<div style="position:absolute; width:100px; text-decoration:none" >' + name + ':' +
                    '<select id="' + no + '" style="position:absolute; left:90px; width:130px" '+
                    'onchange="loadValues2(' + no + ')"></select></div>'
        }

        function loadValues1( id ) {
             // id equals to index of assigned hierarchy level 
             selectID = id;
             var bean = JSON.stringify( new function() {
                this.action = 'performFreeGazetteerSearch';
                this.level = id;
                // see getFreeSearch(..)
                var ins = document.getElementsByTagName( 'input' );
                for ( var i = 0; i < ins.length; i++) {
                    if ( ins[i].type == 'text' ) {
                    	this.searchString = ins[i].value; 
                    }        
                }
                this.hierarchyIndex = '' + selectedIndex;
             }, null, false );
             try {
                 submitPostRequest( url, handleLoadValues1, bean );                    
             } catch(e) {
                 alert( 3 + " " + e );
             }
        }

        function handleLoadValues1(response) {
            try {
                var items = JSON.parse( response );      
                var select = getElement( '' + selectID );
                removeChildren( select );      
                appendOption( select, '<%=Messages.get( loc, "IGEO_STD_EGAZ_SELECT_ENTRY" ) %>' , "-", true );
                for (var i = 0; i < items.length; i++) {
                    appendOption( select, items[i].displayName, items[i].geographicIdentifier, false, 
                                  items[i].alternativeGeographicIdentifier );
                }
            } catch(e) {
                alert( 4 + " " + e );
            }
        }

        function loadValues2( id ) {
            // id equals to index of assigned hierarchy level           
            selectID = id;
            if ( id < maxLevel ) {
                // for last location type no child list can be loaded 
                var bean = JSON.stringify( new function() {
                   this.action = 'performGazetteerLoadList';
                   this.level = id + 1;
                   this.hierarchyIndex = '' + selectedIndex;
                   this.geographicIdentifier = getSelectedValue( '' + id, null, '-' );
                }, null, false );
                try {
                    submitPostRequest( url, handleLoadValues2, bean );                    
                } catch(e) {
                    alert( 5 + " " + e );
                }
            }
        }

        function handleLoadValues2(response) {
            try {            	
                var items = JSON.parse( response );      
                var select = getElement( '' + (selectID + 1) );
                removeChildren( select );      
                appendOption( select, '<%=Messages.get( loc, "IGEO_STD_EGAZ_SELECT_ENTRY2" ) %>', "-", true );
                for (var i = 0; i < items.length; i++) {
                    appendOption( select, items[i].displayName, items[i].geographicIdentifier, false,
                    	          items[i].alternativeGeographicIdentifier );
                }
            } catch(e) {
                alert( 6 + " " + e );
            }
        }

        function zoomTo() {
        	removeLayer();
        	if ( getSelectedValue( '' + selectID, null, '-' ) != '-' ) {
	            var bean = JSON.stringify( new function() {
	               this.action = 'loadGazetteerItemBBox';
	               this.level = selectID;
	               this.hierarchyIndex = '' + selectedIndex;
	               this.geographicIdentifier = getSelectedValue( '' + selectID, null, '-' );
	            }, null, false );
	            try {
	                submitPostRequest( url, handlezoomTo, bean );                    
	            } catch(e) {
	                alert( 7 + " " + e );
	            }
        	} else {
                parent.Ext.Msg.show({
                    title: '<%=Messages.get( loc, "IGEO_STD_EGAZ_ERROR_WND_TITLE" ) %>',
                    msg: '<%=Messages.get( loc, "IGEO_STD_EGAZ_ERROR_WND_MSG" ) %>',
                    buttons: {ok: 'OK' },
                    width: 300,
                    icon: parent.Ext.MessageBox.ERROR
                });
            }
        }

        function handlezoomTo(response) {
            try {
                var tmp = response.split( ',' );
                var env = new Envelope( parseFloat( tmp[0] ),
                                        parseFloat( tmp[1] ),
                                        parseFloat( tmp[2] ),
                                        parseFloat( tmp[3] ) );
                env = env.getBuffer( env.getWidth() );
                parent.controller.actionPerformed( new Event( this, "BBOX", env ) );
                if ( getElement( "INP_HIGHLIGHT" ).checked ) {                    
                    if ( parent.controller.mapModel.getLayerByName( 'highlight' ) == null ) {
                        // just add new layer if a highlight layer is not already available
                        var tmp = window.location.pathname.split( '/' );
                        var s = '';
                        for ( var i = 0; i < tmp.length-3; i++) {
                            s += (tmp[i] + '/' ); 
                        } 
                        var layers = new Array();
                        layers[0] = new Array();
                        
                        layers[0]['name'] = 'highlight';
                        layers[0]['title'] = '<%=Messages.get( loc, "IGEO_STD_EGAZ_HIGHLIGHT_LAYER" ) %>';
                        layers[0]['queryable']  = false;
                        layers[0]['metadataURL'] = null;
                        layers[0]['minScale'] = '0';
                        layers[0]['maxScale'] = '99999999999';
                        layers[0]['visible'] = true;
                        var wmsurl = window.location.protocol + '//' + window.location.host + s + 'highlightwms';
                        parent.controller.addLayersToModel("gazetteer highlight", wmsurl, "1.1.1", layers, "image/gif", false, true );
                    }
                }
            } catch( e ) {
                alert( 8 + " " + e );
            }
        }

        function removeLayer() {            
            try {
                var layer = parent.controller.mapModel.getLayerByName( 'highlight' );            
                if ( layer != null ) {
                     var bean = JSON.stringify( new function() {
                         this.action = 'REMOVENODE';
                         this.node = layer.getIdentifier();
                      }, null, false );
                     
                      try {
                          submitPostRequest( url, handleRemoveLayer, bean );                    
                      } catch(e) {
                          alert( 99 + " " + e );
                      }
                                        
                      parent.controller.mapModel.removeLayer( layer );
                      parent.controller.mapModel.setChanged( true );
                      parent.controller.repaint();
                } else if ( getElement( "INP_HIGHLIGHT" ).checked  ) {         
                	if ( getSelectedValue( '' + selectID, null, '-' ) != '-' ) {
                	  //  zoomTo();
                	}
                }
            } catch( e ) {
                alert( 9 + " " + e );
            }
        }

        function handleRemoveLayer(response) {
            try {
                if ( response.indexOf( "ERROR:") == 0 ) {
                    alert( response );
                } else {                
                    parent.getElement( "IDLayerTree" ).contentWindow.location.reload( true );
                }
            } catch( e ) {
                alert( 10 + " " + e );
            }
        }

       </script>
    </head>
    <body onload="register(); initGazetteer();" class="pEasyGaz">
        <br>
        <div id="HIERARCHIES"><%=Messages.get( loc, "IGEO_STD_EGAZ_TYPE" ) %><select name="TYPES" id="TYPES" style="width:140px;" onchange="loadHierarchy()"></select></div>
        <br>
        <div id="HIGHLIGHT" style="position:absolute; top:220px; visibility:hidden">
            <input id="INP_HIGHLIGHT" type="checkbox" onclick="removeLayer()"> <%=Messages.get( loc, "IGEO_STD_EGAZ_HIGHLIGHT_CB" ) %><br> 
        </div>
        <div id="ZOOM" style="position:absolute; top:220px; visibility:hidden;"></div>       
    </body>
</html>