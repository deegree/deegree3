//$HeadURL$
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

function MapController(mapModel) {

	// variable declaration
	this.mapModel = mapModel;

	// method declaration
	this.zoomByPoint = zoomByPoint;
	this.zoom = zoom;
	this.pan = pan;
	this.zoomToFullExtent = zoomToFullExtent;
	this.ensureAspectRatio = ensureAspectRatio;

	// method implementation	
	
	function zoomByPoint(level, x, y) {
		level = level/100.0 + 1.0;
		var bbox = this.mapModel.getBoundingBox();
		var width = bbox.getWidth();
		var height = bbox.getHeight();
		width = width * level;
		height = height * level;
		var minx = x - (width/2.0);
		var maxx = x + (width/2.0);
		var miny = y - (height/2.0);
		var maxy = y + (height/2.0);
		bbox = new Envelope( minx, miny, maxx, maxy );
		this.mapModel.setBoundingBox( bbox );		
		return this.mapModel;
	}

	function zoom(level) {
		var bbox = this.mapModel.getBoundingBox();
		var width = bbox.getWidth();
		var height = bbox.getHeight();
		var cx = bbox.maxx - (width/2.0);
		var cy = bbox.maxy - (height/2.0);
		return this.zoomByPoint(level, cx, cy);
	}

	function pan(direction, level) {
		level = level/100.0;
		var bbox = this.mapModel.getBoundingBox();
		var dx = (bbox.maxx - bbox.minx) * level;
		var dy = (bbox.maxy - bbox.miny) * level;
		var minx = bbox.minx;
		var miny = bbox.miny;
		var maxx = bbox.maxx;
		var maxy = bbox.maxy;

		if ( direction.indexOf('W') > -1 ) {
			minx = minx - dx;
			maxx = maxx - dx;
		} else if ( direction.indexOf('E') > -1 ) {
			minx = minx + dx;
			maxx = maxx + dx;
		} 
		if ( direction.indexOf('S') > -1 ) {
			miny = miny - dy;
			maxy = maxy - dy;
		} else if ( direction.indexOf('N') > -1 ) {
			miny = miny + dy;
			maxy = maxy + dy;
		} 
		bbox = new Envelope( minx, miny, maxx, maxy );
		this.mapModel.setBoundingBox( bbox );
		return this.mapModel;
	}

	function zoomToFullExtent() {
		var bbox = this.mapModel.getInitialBoundingBox();
		this.mapModel.setBoundingBox( bbox );
		return this.mapModel;
	}

	function ensureAspectRatio() {
		var bbox = this.mapModel.getBoundingBox();
        var xmin = bbox.minx;
        var ymin = bbox.miny;
        var xmax = bbox.maxx;
        var ymax = bbox.maxy;
		var dx = xmax - xmin;
		var dy = ymax - ymin;
		
		var W = this.mapModel.getWidth()
		var H = this.mapModel.getHeight();
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
		var env = new Envelope( xmin, ymin, xmax, ymax );
		this.mapModel.setBoundingBox(env)
		return this.mapModel;
	}
	
	function getNormalizedCoords(normLen, ratio, min, max){
		var mid = (max - min)/2 + min;
		min = mid - (normLen/2)*ratio;
		max = mid + (normLen/2)*ratio;
		var newCoords = new Array(min,max);
		return newCoords;	
		
	}		

}
