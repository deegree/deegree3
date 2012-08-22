//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) (2005) by:

 Florian Rengers / grit GmbH

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

 Florian Rengers, grit GmbH
 Landwehrstraße 143
 59368 Werne
 http://www.grit.de

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

function GeometryFactory() {
	
	// methods
	this.createPoint = createPoint;
	this.createLineStringFromPoints = createLineStringFromPoints;
	this.createLineStringFromCoords = createLineStringFromCoords;
	this.createPolygonFromPoints = createPolygonFromPoints;
	this.createPolygonFromCoords = createPolygonFromCoords;
	this.createPolygonFromEnvelope = createPolygonFromEnvelope;
	
	// implementation
	
	function createPoint( x, y, crs ) {
		return new Point( x, y, crs );
	}
	
	function createLineStringFromPoints(points, crs) {
		return new LineString( points, crs );
	}
	
	function createLineStringFromCoords(x, y, crs) {
		if ( x.length != y.length ) {
			throw new Ecxeption( "GeometryFactory.createLineStringFromCoords", "x and y must have the same length" );
		}
		if ( x.length < 2 ) {
			throw new Ecxeption( "GeometryFactory.createLineStringFromCoords", "x and y must contain at least two values" );
		}
	}
	
	function createPolygonFromPoints(points, crs) {
		return new Polygon( points, crs );
	}
	
	function createPolygonFromCoords(x, y, crs) {
		if ( x.length != y.length ) {
			throw new Ecxeption( "GeometryFactory.createPolygonFromCoords", "x and y must have the same length" );
		}
		if ( x.length < 2 ) {
			throw new Ecxeption( "GeometryFactory.createPolygonFromCoords", "x and y must contain at least two values" );
		}
	}
	
	function createPolygonFromEnvelope(envelope, crs) {
		var points = new Array();
		points.push( new this.createPoint( envelope.minx, envelope.miny, crs ) );
		points.push( new this.createPoint( envelope.minx, envelope.maxy, crs ) );
		points.push( new this.createPoint( envelope.maxx, envelope.maxy, crs ) );
		points.push( new this.createPoint( envelope.maxx, envelope.miny, crs ) );
		points.push( new this.createPoint( envelope.minx, envelope.miny, crs ) );
		return this.createPolygonFromPoints( points, crs );
	}
}
