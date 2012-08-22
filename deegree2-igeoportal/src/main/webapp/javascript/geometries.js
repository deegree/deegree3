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

var POINT = 1000001;
var LINESTRING = 1000002;
var POLYGON = 1000003;

function Point(x, y, crs) {
	this.x = x;
	this.y = y;
	this.tolerance = 0.0001;
	this.equals = equals;
	
	function equals(point) {
		return this.x > point.x - this.tolerance && this.x < point.x + this.tolerance &&
			   this.y > point.y - this.tolerance && this.y < point.y + this.tolerance;
	}
}

function LineString(points, crs) {
	this.points = points;
	this.getSize = getSize;
	this.length = length;
	this.getEnvelope = getEnvelope;
	
	function getSize() {
		return this.points.length;
	}
	
	function length() {
		var d = 0;
		for ( var i = 0; i < this.points.length-1; i++) {
			var x1 = this.points[i].x;
			var y1 = this.points[i].y;
			var x2 = this.points[i+1].x;
			var y2 = this.points[i+1].y;
			d = d + Math.sqrt( (x2 -x1 ) * (x2 -x1) +  (y2 -y1 ) * (y2 -y1) );
    	}
		return Math.round(d * 10)/10;
	}
	
	function getEnvelope() {
		var xmin = this.points[0].x;
		var ymin = this.points[0].y;
		var xmax = this.points[0].x;
		var ymax = this.points[0].y;
		for ( var i = 0; i < this.points.length-1; i++) {	
			if ( this.points[i].x < xmin ) {
				xmin = this.points[i].x;
			} 
			if ( this.points[i].x > xmax ) {
				xmax = this.points[i].x;
			} 
			if ( this.points[i].y < ymin ) {
				ymin = this.points[i].y;
			} 
			if ( this.points[i].y > ymax ) {
				ymax = this.points[i].y;
			} 
		}
		return new Envelope( xmin, ymin, xmax, ymax );
	}
}

function Polygon(points, crs) {
	// attributes
	this.points = points;
	
	// method declaration
	this.isValid = isValid;
	this.getSize = getSize;
	this.length = length;
	this.area = area;
	this.centroid = centroid;
	this.getEnvelope = getEnvelope;
	
	// implementation
	
	function getSize() {
		return this.points.length;
	}
	
	function isValid() {
		return this.points.length > 3 && this.points[0].equals( this.points[this.points.length-1] );
	}
	
	function length() {
		var d = 0;
		for ( var i = 0; i < this.points.length-1; i++) {
			var x1 = this.points[i].x;
			var y1 = this.points[i].y;
			var x2 = this.points[i+1].x;
			var y2 = this.points[i+1].y;
			d = d + Math.sqrt( (x2 -x1 ) * (x2 -x1) +  (y2 -y1 ) * (y2 -y1) );
    	}
    	
		if( this.points.length == 5 ){// when only two points, len must be corrected
			d = d/2;
		}
		
    	return Math.round(d * 10)/10;
	}
	
	function area() {
		var atmp = 0;
		var i = 0;
		var j = 0;
		for ( i = this.points.length - 1, j = 0; j < this.points.length; i = j, j++ ) {
			var xi = this.points[i].x - this.points[0].x;
			var yi = this.points[i].y - this.points[0].y;
			var xj = this.points[j].x - this.points[0].x;
			var yj = this.points[j].y - this.points[0].y;
		    var ai = ( xi * yj ) - ( xj * yi );
		    atmp += ai;
		}

		return Math.abs( atmp / 2 );
	}
	
	function getEnvelope() {
		var xmin = this.points[0].x;
		var ymin = this.points[0].y;
		var xmax = this.points[0].x;
		var ymax = this.points[0].y;
		for ( var i = 0; i < this.points.length-1; i++) {	
			if ( this.points[i].x < xmin ) {
				xmin = this.points[i].x;
			} 
			if ( this.points[i].x > xmax ) {
				xmax = this.points[i].x;
			} 
			if ( this.points[i].y < ymin ) {
				ymin = this.points[i].y;
			} 
			if ( this.points[i].y > ymax ) {
				ymax = this.points[i].y;
			} 
		}
		return new Envelope( xmin, ymin, xmax, ymax );
	}
	
	function centroid( ) {

        var i;
        var j;
        var ai;
        var x;
        var y;
        var atmp = 0;
        var xtmp = 0;
        var ytmp = 0;

		// move points to the origin of the coordinate space
		// (to solve precision issues) 
		var transX = this.points[0].x;
		var transY = this.points[0].y;

		for ( i = this.points.length - 1, j = 0; j < this.points.length; i = j, j++ ) {
			var x1 = this.points[i].x - transX;
			var y1 = this.points[i].y - transY;
			var x2 = this.points[j].x - transX;
			var y2 = this.points[j].y - transY;
			ai = ( x1 * y2 ) - ( x2 * y1 );
			atmp += ai;
			xtmp += ( ( x2 + x1 ) * ai );
			ytmp += ( ( y2 + y1 ) * ai );
		}

		if ( atmp != 0 ) {
			x = xtmp / ( 3 * atmp ) + transX;
			y = ytmp / ( 3 * atmp ) + transY;
		} else {
			x = this.points[0].x;
			y = this.points[0].y;
		}

        return new Point( x, y );
    }
}
