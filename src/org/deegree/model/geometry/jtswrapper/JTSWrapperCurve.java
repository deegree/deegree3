//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.model.geometry.jtswrapper;

import java.util.ArrayList;
import java.util.List;

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.CurveSegment;
import org.deegree.model.geometry.primitive.Point;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
class JTSWrapperCurve extends JTSWrapperGeometry implements Curve {

    private double[] coordinates;

    private List<Point> pointList;

    private List<CurveSegment> segments;

    private Curve.ORIENTATION orientation;

    /**
     * 
     * @param precision
     * @param crs
     * @param coordinateDimension
     * @param segments
     */
    public JTSWrapperCurve( double precision, CoordinateSystem crs, int coordinateDimension, CurveSegment segment,
                            Curve.ORIENTATION orientation ) {
        super( precision, crs, coordinateDimension );
        // A JTS geometry is simple; so it has just one segment -> segment and curve are
        // geometrical identic
        this.segments = new ArrayList<CurveSegment>(1);
        this.segments.add( segment );
        this.orientation = orientation;
        List<Coordinate> coords = new ArrayList<Coordinate>( segment.getPoints().size() );
        List<Point> points = segment.getPoints();
        if ( coordinateDimension == 2 ) {
            for ( Point point : points ) {
                coords.add( new Coordinate( point.getX(), point.getY() ) );
            }
        } else {
            for ( Point point : points ) {
                coords.add( new Coordinate( point.getX(), point.getY(), point.getZ() ) );
            }
        }

        geometry = jtsFactory.createLineString( coords.toArray( new Coordinate[coords.size()] ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Curve#getAsArray()
     */
    public double[] getAsArray() {
        if ( coordinates == null ) {
            LineString ls = (LineString) geometry;
            coordinates = new double[ls.getNumPoints()];
            int k = 0;
            for ( int i = 0; i < ls.getNumPoints(); i++ ) {
                Coordinate coord = ls.getPointN( i ).getCoordinate();
                coordinates[k++] = coord.x;
                coordinates[k++] = coord.y;
                if ( coordinateDimension == 3 ) {
                    coordinates[k++] = coord.z;
                }
            }
        }
        return coordinates;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Curve#getBoundary()
     */
    public List<Point> getBoundary() {
        LineString ls = (LineString) geometry;
        List<Point> boundary = new ArrayList<Point>( 2 );
        boundary.add( toPoint( ls.getCoordinateN( 0 ) ) );
        boundary.add( toPoint( ls.getCoordinateN( ls.getNumPoints() - 1 ) ) );
        return boundary;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Curve#getCurveSegments()
     */
    public List<CurveSegment> getCurveSegments() {
        return segments;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Curve#getLength()
     */
    public double getLength() {
        return ( (LineString) geometry ).getLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Curve#getOrientation()
     */
    public ORIENTATION getOrientation() {
        return orientation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Curve#getPoints()
     */
    public List<Point> getPoints() {
        if ( pointList == null ) {
            pointList = toPoints( ( (LineString) geometry ).getCoordinates() );
        }
        return pointList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Curve#getX()
     */
    public double[] getX() {
        double[] x = new double[coordinates.length];
        int k = 0;
        for ( int i = 0; i < x.length; i++ ) {
            x[i] = coordinates[k];
            k += coordinateDimension;
        }
        return x;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Curve#getY()
     */
    public double[] getY() {
        double[] y = new double[coordinates.length];
        int k = 1;
        for ( int i = 0; i < y.length; i++ ) {
            y[i] = coordinates[k];
            k += coordinateDimension;
        }
        return y;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Curve#getZ()
     */
    public double[] getZ() {
        double[] z = new double[coordinates.length];
        if ( coordinateDimension == 3 ) {
            int k = 2;
            for ( int i = 0; i < z.length; i++ ) {
                z[i] = coordinates[k];
                k += coordinateDimension;
            }
        } else {
            for ( int i = 0; i < z.length; i++ ) {
                z[i] = Double.NaN;
            }
        }
        return z;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Curve#isClosed()
     */
    public boolean isClosed() {
        return ( (LineString) geometry ).isClosed();
    }

}
