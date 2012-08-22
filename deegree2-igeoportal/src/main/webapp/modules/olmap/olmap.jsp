<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Insert title here</title>
		
		<link rel="stylesheet" href="../../javascript/openlayers/theme/default/style.css" type="text/css" />
        <script src='../../javascript/openlayers/lib/OpenLayers.js'></script>
		<script src="../../javascript/ol_extended/LoadingPanel.js"></script>
		       
        <script src='olmap.js'></script>
        <script src='digitizeFunctions.js'></script>
        <script src='measureFunctions.js'></script>
        <script src='../../javascript/utils.js'></script>
        <script src='../../javascript/jquery/jquery-1.7.1.min.js'></script>
        
        <style type="text/css">
	       
	        .olControlLoadingPanel {
	            background-image:url(../../images/progress.gif);
	            margin-left: 30%;
	            margin-top: 40%;
	            position: relative;
	            width: 230px;
	            height: 183px;
	            background-position:center;
	            background-repeat:no-repeat;
	            display: none;
	        }
	    </style>
	        
        
		
		<script type="text/javascript">
		<!--

            var baseURL = null;
            var map;
            var lon = 0;
            var lat = 0;
            var zoom = 1;
           
            function register() {
                if ( parent.controller == null ) {
                    parent.controller = new parent.Controller();
                    parent.controller.init();
                }                             
            }
            
            function initMapView() {
                parent.controller.initOLMap( document );
                parent.controller.repaint();
                initOpenLayers();        
            }

            function initOpenLayers(){  
            	var mapSRS = parent.controller.mapModel.getSrs();
                var bbox = parent.controller.mapModel.getBoundingBox();
                var options = {     
                        controls:[  new OpenLayers.Control.ZoomBox({autoActivate: false}),
                                    new OpenLayers.Control.ZoomOut(),
                                    new OpenLayers.Control.Navigation({zoomBoxEnabled: false}),
                                    new OpenLayers.Control.MousePosition({emptyString: '-/-'}),
                                    new OpenLayers.Control.LoadingPanel(),
                                    new OpenLayers.Control.ScaleLine()],
                                         
                        minResolution: "auto",
                        maxResolution: "auto",
                        scales: [10000000,
                                 5000000, 
                                 3000000, 
                                 1000000,
                                  800000,
                                  600000, 
                                  500000,
                                  400000,
                                  300000,
                                  250000,
                                  200000,
                                  150000,
                                  120000,
                                  100000,
                                   80000,
                                   60000,
                                   50000,
                                   40000,
                                   30000,
                                   25000,
                                   20000,
                                   15000,
                                   12000,
                                   10000,
                                   8000,
                                   6000,
                                   5000,
                                   4000,
                                   3000,
                                   2500,
                                   2000,
                                   1500,
                                   1000,
                                   750,
                                   500,
                                   300,
                                   250,
                                   125],
                        units: 'm',            
                        maxExtent: new OpenLayers.Bounds(  bbox.minx, bbox.miny, bbox.maxx, bbox.maxy ),
                        projection: mapSRS
                     };      
            	map = new OpenLayers.Map( 'map', options );   
                
                // assign map to OLMap object      
                parent.controller.vOLMap.setMap( map );

                addMeasureControl();	              
             }

            /**
            * adds a layer with a box that can be dragged to the map
            */
            function initFence( bbox, callback ) {
            	var boxes = new OpenLayers.Layer.Vector( "_BOXESMARKER_" );
            	boxes.setMap(map)
            	map.addLayer( boxes );
            	
            	var control =  new OpenLayers.Control.DragFeature( boxes, { 
            		                         onComplete: function(feature, pixel) { callback( feature );  } 
                                             } );
                map.addControl( control );
                control.activate();   
                
                var style = {
                          strokeColor: "#000000",
                          strokeOpacity: 1,
                          strokeWidth: 2,
                          fillColor: "#FFFFFF",
                          fillOpacity: 0.6
                      };
              
	             var p1 = new OpenLayers.Geometry.Point(bbox.minx, bbox.miny);
	             var p2 = new OpenLayers.Geometry.Point(bbox.minx, bbox.maxy);
	             var p3 = new OpenLayers.Geometry.Point(bbox.maxx, bbox.maxy);
	             var p4 = new OpenLayers.Geometry.Point(bbox.maxx, bbox.miny);
	             var p5 = new OpenLayers.Geometry.Point(bbox.minx, bbox.miny);
	               	             
	             // create a polygon feature from a list of points
	             var linearRing = new OpenLayers.Geometry.LinearRing( [p1,p2,p3,p4,p5] );
	             var box = new OpenLayers.Feature.Vector( new OpenLayers.Geometry.Polygon([linearRing]), null, style );                
	             boxes.addFeatures( [ box ] );	      
            }

            var w = window
            while(window.parent !== w) {
            	w = window.parent
            }
            
            $(w.document).ready(function(){
                register()
                initMapView()
            })

         --></script>

	</head>
	<body style="width: 100%; height: 100%;">
	   <div id="map" ></div>
	</body>
</html>