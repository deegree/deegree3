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

// zoom functions 
function zoomBoxByPoint(bbox, level, x, y) {
  level = level/100.0 + 1.0;
  var width = bbox.maxx - bbox.minx;
  var height = bbox.maxy - bbox.miny;
  width = width * level;
  height = height * level;
  var minx = x - (width/2.0);
  var maxx = x + (width/2.0);
  var miny = y - (height/2.0);
  var maxy = y + (height/2.0);
  var newBbox = new Envelope( minx, miny, maxx, maxy );
  return newBbox;
}        
        
function doZoom(x,y, zoomFactor){
  var newx = gtrans.getSourceX( x ) ;
  var newy = gtrans.getSourceY( y ) ;
  var coords = wpvsRequest.getPOI().split( ',' );
  wpvsRequest.setPOI( newx + "," + newy + "," + coords[2] );
		
  var b = wpvsRequest.getBboxAsArray();
  var bbox = new Envelope( b[0],  b[1],  b[2],  b[3] );
  bbox = zoomBoxByPoint(bbox, zoomFactor, newx, newy);
  wpvsRequest.setBboxArray( bbox.minx, bbox.miny, bbox.maxx, bbox.maxy ); 
  redraw(true);		
}
	
function setScreenBbox(bbox){
  var minx = this.gtrans.getSourceX(bbox.minx);
  var miny = this.gtrans.getSourceY(bbox.miny);
  var maxx = this.gtrans.getSourceX(bbox.maxx);
  var maxy = this.gtrans.getSourceY(bbox.maxy);
   	
  var coords = wpvsRequest.getPOI().split( ',' );
  var midx = (minx+maxx)/2;
  var midy = (miny+maxy)/2;
  wpvsRequest.setPOI( midx + "," + midy + "," + coords[2] );

  var env = new Envelope( minx, miny, maxx, maxy );
  env = ensureAspectRatio( env );
  wpvsRequest.setBboxArray( env.minx, env.miny, env.maxx, env.maxy ); 
	
  redraw(true);
}

function findCentroid(minx,miny,maxx,maxy){
  var midx = (minx+maxx)/2;
  var midy = (miny+maxy)/2;

  return new Array(midx,midy);
}

function doDisplacement(dx,dy){
  //UT 2007-04-17 fixed problem with box not being centred around poi	
  var coords = wpvsRequest.getPOI().split( ',' );
	
  var b = wpvsRequest.getBboxAsArray();
	
  // pan image
  var newx = -1*dx * (b[2]-b[0])/oviewWidth;
  var newy = dy * (b[3]-b[1])/oviewHeight;
	
  //put poi in the middle of box
  var newCx = (b[2]-b[0])/2 + b[0];
  var newCy = (b[3]-b[1])/2 + b[1];

  var newPoi = findCentroid( b[0] + newx,  b[1] + newy,  b[2] + newx,  b[3] + newy );

  wpvsRequest.setPOI( newPoi[0] + ',' + newPoi[1] + "," + coords[2] );
  wpvsRequest.setBboxArray( b[0] + newx,  b[1] + newy,  b[2] + newx,  b[3] + newy);

  redraw(true);
}
	      
//UT: fixed, as of 2007-04-16
function changePOI( direction ){
		
  var coords = wpvsRequest.getPOI().split( ',' );
  var x = parseFloat( coords[0] );
  var y = parseFloat( coords[1] );

  var yaw = wpvsRequest.getYaw();

  // 10% of distance
  var dist = 0.1 * wpvsRequest.getDistance();
  var angle = -((yaw - 90) * Math.PI / 180);
	
  var du = null;
  var dv = null;
	
  if( direction == 'UP' ){
    du = dist * Math.cos( angle );
    dv = dist * Math.sin( angle );
  } else if ( direction == 'DOWN' ){
    du = -dist * Math.cos( angle );
    dv = -dist * Math.sin( angle );	
  } else if ( direction == 'LEFT' ){
    du = -dist * Math.sin( angle );
    dv = dist * Math.cos( angle );	
  } else if ( direction == 'RIGHT' ){
    du = dist * Math.sin( angle );
    dv = -dist * Math.cos( angle );	
  } else {
    alert("No such direction: " + direction );
    return;
  }
	
  x = eval(x + du);
  y = eval(y + dv);

  var oldBox = wpvsRequest.getBboxAsArray();
					
  wpvsRequest.setBboxArray( oldBox[0] + du,
			    oldBox[1] + dv,
			    oldBox[2] + du,
			    oldBox[3] + dv );
	
  var poi = x + "," + y + "," + coords[2];
  wpvsRequest.setPOI( poi );
  poi = x.toFixed(2) + "," + y.toFixed(2) + "," + coords[2];
  setFooterText( " POI: " + poi );
	
  redraw(true);
}	      

function incrementYaw( increment ){
	
  var currentYaw = wpvsRequest.getYaw();
  currentYaw+=increment;
	
  //make sure 0 <= yaw <= 360
  if( currentYaw < 0) {
    currentYaw = 360 + currentYaw;
  }
  currentYaw = currentYaw % 360; 
	
  wpvsRequest.setYaw( currentYaw );
  redraw(false);
}

function setDistance( d ){ 
  wpvsRequest.setDistance(d);
  redraw();
}

function setPitch( p ){ 
  wpvsRequest.setPitch(p);
  redraw(false);
}

function ensureAspectRatio( bbox ) {

  var xmin = bbox.minx;
  var ymin = bbox.miny;
  var xmax = bbox.maxx;
  var ymax = bbox.maxy;
  var dx = xmax - xmin;
  var dy = ymax - ymin;
		
  var W = oviewWidth;
  var H = oviewHeight;
  var R = H/W;
		
  if ( dx >= dy ){
    var normCoords = getNormalizedCoords(dx, R, ymin, ymax);
    ymin = normCoords[0];
    ymax = normCoords[1];
  }else{
    R = W/H;
    var normCoords = getNormalizedCoords(dy, R, xmin, xmax);
    xmin = normCoords[0];
    xmax = normCoords[1];
  }
  return new Envelope( xmin, ymin, xmax, ymax );		
}
	
function getNormalizedCoords(normLen, ratio, min, max){
  var mid = (max - min)/2 + min;
  min = mid - (normLen/2)*ratio;
  max = mid + (normLen/2)*ratio;
  var newCoords = new Array(min,max);
  return newCoords;	
}
