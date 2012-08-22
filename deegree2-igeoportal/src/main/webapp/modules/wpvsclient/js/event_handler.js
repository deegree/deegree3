//$HeadURL$
//$Id$
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

function detectBrowser() {
    isNav = (navigator.appName.indexOf("Netscape")>=0);  
}


function initEventHandling() {
  detectBrowser();
  if ( isNav ) {
    onmousedown = initMouseDown;
    onmouseup = initMouseUp;
  } else {        
    var layer = window.document.all[0];
    document.onmousedown = initMouseDown;
    layer.onmouseup = initMouseUp;
  }
}

function initMouseDown(e) {
  if ( !isNav ) {
    // microsoft IE special!!!
    var layer = window.document.all[0];
    layer.setCapture();                
  }
  if ( mode == 'zoomin' ) {
    evnt = 0;
    var xy = getEventCoordinates(e);
    box.mapTool( xy[0], xy[1]);                
  } else if ( mode == 'zoomout' ) {
    evnt = 1;
  } else if ( mode == 'move' ) {
    evnt = 2;
    var xy = getEventCoordinates(e);
    initX = xy[0];
    initY = xy[1];
  } 
}

function initMouseUp(e) {
  if ( evnt == 0 ) {//zoom in
  box.chkMouseUp(e);
  var bb =  box.getZoomBox();
    if( bb.minx == bb.maxx && bb.miny == bb.maxy ){
      var xy = getEventCoordinates(e);
      parent.doZoom(xy[0], xy[1], -25);
    } else{
      parent.setScreenBbox( box.getZoomBox() );
    }
    box.clear();
  } 
  
  if ( evnt == 2 ) {// move/pan
    var xy = getEventCoordinates(e);
    box.chkMouseUp(e);
    parent.doDisplacement((xy[0] - initX),(xy[1] - initY));
  } else if ( evnt == 1 ) { //zoom out
    var xy = getEventCoordinates(e); 
    parent.doZoom(xy[0], xy[1], 25);
  }
  
  if ( !isNav ) {
  // microsoft IE special!!!
    var layer = window.document.all[0];
    layer.releaseCapture();
    document.onmousedown = initMouseDown;
  }            
  evnt = -1;
}

function initMouseMove(e) {
  var xy = getEventCoordinates(e);
  if ( box != null ) {
    if ( evnt == 0 ) {
      box.getMouse( xy[0], xy[1] );
    } else if ( evnt == 2 ) {
      var hl = document.getElementById( 'overviewImgNode' );
      if ( hl != null ) {
        hl.style.top = xy[1] - initY;
        hl.style.left = xy[0] - initX;
      }
    }
  }
}

function getEventCoordinates(e) {
  var x  = 0;
  var y  = 0;
  
  if (isNav) {
    x = e.layerX;
    y = e.layerY;
  } else {
    if ( evnt == 2 ) {
      x = window.event.screenX;
      y = window.event.screenY;
    } else {
      x = window.event.offsetX;
      y = window.event.offsetY;
    }
  }

    return new Array( x, y);
}