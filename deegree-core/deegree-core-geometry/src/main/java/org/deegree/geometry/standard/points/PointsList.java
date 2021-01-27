//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.geometry.standard.points;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deegree.commons.tom.Reference;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;

/**
 * <code>List</code>-based {@link Points} implementation that allows to hold identifiable {@link Point} objects (with id
 * or even references to local or remote {@link Point} instances}.
 * <p>
 * This implementation is rather expensive, as every contained point is represented as an individual {@link Point}
 * object. Whenever possible, {@link PackedPoints} or {@link PointsPoints} should be used instead.
 * </p>
 *
 * @see PackedPoints
 * @see PointsPoints
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class PointsList implements Points {

    protected List<Point> points;

    /**
     * Creates a new {@link PointsList} instance based on the given list.
     *
     * @param points
     */
    public PointsList( List<Point> points ) {
        this.points = points;
    }

    @Override
    public int getDimension() {
        for ( final Point point : points ) {
            if ( !( point instanceof Reference<?> ) ) {
                return point.getCoordinateDimension();
            }
        }
        int dimension = 2;
        for ( final Point point : points ) {
            try {
                dimension = point.getCoordinateDimension();
            } catch ( Exception e ) {
                // nothing to do, try next
            }
        }
        return dimension;
    }

    @Override
    public int size() {
        return points.size();
    }

    @Override
    public Iterator<Point> iterator() {
        return points.iterator();
    }

    @Override
    public Point get( int i ) {
        return points.get( i );
    }

    @Override
    public double[] getAsArray() {
        double[] coords = new double[getDimension() * size()];
        int i = 0;
        for ( Point p : this ) {
            for ( double coord : p.getAsArray() ) {
                coords[i++] = coord;
            }
        }
        return coords;
    }

    @Override
    public Point getEndPoint() {
        return get( size() - 1 );
    }

    @Override
    public Point getStartPoint() {
        return get( 0 );
    }

    // -----------------------------------------------------------------------
    // Implementation of JTS methods
    // -----------------------------------------------------------------------

    @Override
    public Envelope expandEnvelope( Envelope env ) {
        for ( Point p : points ) {
            env.expandToInclude( p.get0(), p.get1() );
        }
        return env;
    }

    @Override
    public Coordinate getCoordinate( int index ) {
        if ( index < points.size() && index >= 0 ) {
            Point point = points.get( index );
            return new Coordinate( point.get0(), point.get1(), point.get2() );
        }
        return null;
    }

    @Override
    public void getCoordinate( int index, Coordinate coord ) {
        if ( index < points.size() && index >= 0 ) {
            Point point = points.get( index );
            coord.x = point.get0();
            coord.y = point.get1();
            coord.z = point.get2();
        }
    }

    @Override
    public Coordinate getCoordinateCopy( int index ) {
        Point point = points.get( index );
        return new Coordinate( point.get0(), point.get1(), point.get2() );
    }

    @Override
    public double getOrdinate( int index, int ordinateIndex ) {
        return points.get( index ).get( ordinateIndex );
    }

    @Override
    public double getX( int index ) {
        return points.get( index ).get0();
    }

    @Override
    public double getY( int index ) {
        return points.get( index ).get1();
    }

    @Override
    public void setOrdinate( int index, int ordinateIndex, double value ) {
        double[] coords = points.get( index ).getAsArray();
        if ( coords.length > ordinateIndex && ordinateIndex >= 0 )
            coords[ordinateIndex] = value;
        else
            throw new IndexOutOfBoundsException();
    }

    @Override
    public Coordinate[] toCoordinateArray() {
        Coordinate[] coords = new Coordinate[size()];
        int i = 0;
        for ( Point p : this ) {
            coords[i++] = new Coordinate( p.get0(), p.get1() );
        }
        return coords;
    }

    @Override
    public Object clone() {
        return new PointsList( new ArrayList<Point>( points ) );
    }

    @Override
    public CoordinateSequence copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "Points list: " + points;
    }

}
