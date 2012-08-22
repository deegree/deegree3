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

function getGeometryType(geometry) {
	if ( geometry ) {
		if ( geometry.x ) {
			return POINT;
		}
		if ( geometry.points ) {
			var l = geometry.points.length-1;
			if ( geometry.points[0].equals( geometry.points[l] ) ) {
				return POLYGON;
			}
			return LINESTRING;
		}
	}
}

/**
* returns the distance between two points
*/
function pointPointDistance( point1, point2 ) {
	var d1 = point1.x - point2.x;
	var d2 = point1.y - point2.y;
	return Math.sqrt( d1 * d1 + d2 * d2 );
}
 
 /**
 * returns the distance between a line segment (p1 - p2) and a point (p3)
 */
function pointLineSegDistance( p1, p2, p3 ) {
	var x1 = p1.x;
  	var y1 = p1.y;
	var x2 = p2.x;
	var y2 = p2.y;
	var px = p3.x;
	var py = p3.y;

    x2 -= x1;
    y2 -= y1;

    px -= x1;
    py -= y1;
    var dotProd = px * x2 + py * y2;
    var projLenSq;
    if ( dotProd <= 0.0 ) {
        projLenSq = 0.0;
    } else {
        px = x2 - px;
        py = y2 - py;
        dotProd = px * x2 + py * y2;
        if ( dotProd <= 0.0 ) {
            projLenSq = 0.0;
        } else {
            projLenSq = dotProd * dotProd / ( x2 * x2 + y2 * y2 );
        }
    }
    var lenSq = px * px + py * py - projLenSq;
    if ( lenSq < 0 ) {
        lenSq = 0;
    }
    return Math.sqrt( lenSq );
}
 
/**
* returns true is the passed point is contained within the passed polygon
*/
function contains( polygon, point, tolerance ) {

     // TODO
     // consider tolerance value
     
     var positions = polygon.points;

     if ( positions.length <= 2 ) {
         return false;
     }

     var hits = 0;

     var lastx = positions[positions.length - 1].x;
     var lasty = positions[positions.length - 1].y;
     var curx = 0;;
     var cury = 0;

     // Walk the edges of the polygon
     for ( var i = 0; i < positions.length; lastx = curx, lasty = cury, i++ ) {
         curx = positions[i].x;
         cury = positions[i].y;

         if ( cury == lasty ) {
             continue;
         }

         var leftx;

         if ( curx < lastx ) {
             if ( point.x >= lastx ) {
                 continue;
             }
             leftx = curx;
         } else {
             if ( point.x >= curx ) {
                 continue;
             }
             leftx = lastx;
         }

         var test1;
         var test2;

         if ( cury < lasty ) {
             if ( ( point.y < cury ) || ( point.y >= lasty ) ) {
                 continue;
             }
             if ( point.x < leftx ) {
                 hits++;
                 continue;
             }
             test1 = point.x - curx;
             test2 = point.y - cury;
         } else {
             if ( ( point.y < lasty ) || ( point.y >= cury ) ) {
                 continue;
             }
             if ( point.x < leftx ) {
                 hits++;
                 continue;
             }
             test1 = point.x - lastx;
             test2 = point.y - lasty;
         }

         if ( test1 < ( test2 / ( lasty - cury ) * ( lastx - curx ) ) ) {
             hits++;
         }
     }

     return ( ( hits & 1 ) != 0 );
}

/**
 * 
 * @param geometry to move
 * @param dx distance in x direction
 * @param dy distance in y direction
 * @return moved geometry (same instance as passed)
 */
function moveGeometry(geometry, dx, dy) {
	var type = getGeometryType( geometry );
	if ( type == POINT ) {
		geometry.x += dx;
		geometry.y += dy;
	} else if ( type == LINESTRING ) {
		for ( var i = 0; i < geometry.points.length; i++) {
			geometry.points[i].x = geometry.points[i].x + dx;
			geometry.points[i].y = geometry.points[i].y + dy;
		}
	} else if ( type == POLYGON ) {
		for ( var i = 0; i < geometry.points.length; i++) {
			geometry.points[i].x = geometry.points[i].x + dx;
			geometry.points[i].y = geometry.points[i].y + dy;
		}
	} 
	return geometry;
}