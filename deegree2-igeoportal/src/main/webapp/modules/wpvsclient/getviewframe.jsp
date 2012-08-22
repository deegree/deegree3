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
        <script language="JavaScript1.2" type="text/javascript" src="js/mapnav_functions.js"></script>
        <script language="JavaScript1.2" type="text/javascript" src="js/box2.js"></script>
        <script language="JavaScript1.2" type="text/javascript" src="js/wz_jsgraphics.js"></script>
        <script language="JavaScript1.2" type="text/javascript" src="js/envelope.js"></script>
        <script language="JavaScript1.2" type="text/javascript" src="js/event_handler.js"></script>
        <script>
        <!--
        var loadingGetViewImage;
          
        function init(){    
            parent.getviewFrame = this;    
        }

        /**
         * A function to get the image from a wpvs and set it to this frame top image.
         */  
        function setWPVSRequest( r, viewWidth, viewHeight ){
            var imgNode = document.getElementById( 'getviewImg' );
            if( imgNode != null ){
              //imgNode.src = r;
            }
            var getviewImgNode = document.getElementById( 'getviewImgNode' );
            var children = getviewImgNode.childNodes;
            for( var i = 0;i<children.length;i++){
                //getviewImgNode.removeChild( children[i] );
                children[i].id = "old_" + i;
            }
            var x = new Image( viewWidth, viewHeight );
            //This is the place, where the wpvs is called.
            x.src = r;
            x.id = 'newImg';
            getviewImgNode.appendChild( x );
            
            imgNode = document.getElementById( 'progress' );
            if( imgNode != null ){
                imgNode.src = './images/progress.gif';
            }
            loadingGetViewImage = window.setInterval( "checkWPVSImageState()", 1000 );    
        }
  
        function checkWPVSImageState(){
            var finished = true;    
            for ( i = 0; i < document.images.length; ++i ) {
                if ( !document.images[i].complete ) {
                    // && document.images[i].id.indexOf('view') < 0 ) {
                    finished = false;
                } 
            }
            if ( finished ) {
                //clear progress monitor
                node = document.getElementById( 'progress' );
                node.src = './images/empty.gif';
                var getviewImgNode = document.getElementById( 'getviewImgNode' );
                          
                var children = getviewImgNode.childNodes;
                for( var i = 0;i<children.length;i++){
                    if( children[i].id != 'newImg' ){
                        getviewImgNode.removeChild( children[i] );
                    }
                }
                window.clearInterval(loadingGetViewImage);
            }
        }
        //-->
        </script>
    </head>
    <body onload="init();" marginheight="0" marginwidth="0" >
        <div id="progressImgNode" style="position:absolute; top:200px; left:300px; z-index:10;">
            <img id="progress"  src="">
        </div>  
        <div id="getviewImgNode" style="position:absolute; top:0px; left:0px; z-index:9;">
        </div>
    </body>
</html>
