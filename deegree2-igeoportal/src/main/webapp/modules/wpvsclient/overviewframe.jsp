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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Internal Frame for Overview Map</title>
        <script language="JavaScript1.2" type="text/javascript" src="js/envelope.js"></script>
        <script language="JavaScript1.2" type="text/javascript" src="js/wz_jsgraphics.js"></script>
        <script language="JavaScript1.2" type="text/javascript" src="js/box2.js"></script>
        <script language="JavaScript1.2" type="text/javascript" src="js/event_handler.js"></script>
        <script>
        <!--
        var box = null;
        var isNav = true;
        var initX = 0;
        var initY = 0;
        var evnt = 0; 
        var mode = "zoomin";
        var loading;
        var count = 0;
        function init(){
        	//All defined in js/event_handler.js
        	document.onmousemove = initMouseMove;
        	document.onmousedown = initMouseDown;
        	document.onmouseup = initMouseUp;
        	parent.overviewFrame = this;
        	if ( box == null ) {
        		box = new DragBox (parent.oviewHeight, parent.oviewHeight, 2, 2, '#FF0000');
        		box.init();
        		box.setListener( parent );
        	}
            //defined in js/event_handler.js
        	initEventHandling();
        }
	
    	/**
    	* Sets the mode e.g. zoomin, zoomout or move
    	*/
    	function setMode( m ){
    		mode = m;
    		if( box != null ){
    			box.clear();
    		}
    	}
	
        /**
        * @param r the wms request.
        * Set the request to the wms and update the overview image
        */
    	function setWMSRequest( r ){
    		var imgNode = document.getElementById( 'overviewImg' );
    		if( imgNode != null ){
                var node = document.getElementById( 'overviewImgNode' );
                node.style.display='block';
        
                //set the imgNode 
                imgNode.style.display='block';
    			imgNode.src = r;
                var noImageNode = document.getElementById( 'noImage' );
                if( noImageNode != null ){
                   noImageNode.style.display='none';
                }
    		} 
    		loading = window.setInterval("checkImageState()", 500 );		
    	}
	
    	function checkImageState(){
            var imgNode = document.getElementById( 'overviewImg' );
            //some dirty hacks:
            //if the overview image couldnot find the wms, the browser uses it's no image image, which is smaller as 50 pixels, 
            //the image is therefore completed *arghhhh*. The counter is a safe guard for slow wms's
    	  	if( count++ == 10 || document.images[0].complete || imgNode.width > 50 ){
                var node = document.getElementById( 'overviewImgNode' );
                node.style.top = "0px";
                node.style.left = "0px";
                if( ( count == 11 && !document.images[0].complete ) || imgNode.width < 50 ){
                  node.style.display='none';
    
                  //display the no image availabe.
                  var noImageNode = document.getElementById( 'noImage' );
                  if( noImageNode != null ){
                    noImageNode.style.display='block';
                  }
               }	  	
    			window.clearInterval(loading);
                count = 0;
            }
    	}
        // -->
        </script>
    </head>
    <!-- initDragBox is definded in event_handler.js -->
    <body onload="init()" marginheight="0" marginwidth="0"-->
        <!-- 
        <div id="progressImgNode" style="position:absolute; top:0px; left:10px; z-index:10;">
            <img id="progress" src="./images/empty.gif">
        </div-->
        <div id="overviewImgNode" style="position: absolute; top: 0px; left: 0px; z-index: 9;">
            <img id="overviewImg" src="">
        </div>
        <div id="noImage" style="font-size: smaller; position: absolute; top: 0px; left: 0px; z-index: 6; display: none;">
            <br />
            No image<br />
            available<br />
            or<br>
            no wms defined<br />
            <br />
        </div>
    </body>
</html>
