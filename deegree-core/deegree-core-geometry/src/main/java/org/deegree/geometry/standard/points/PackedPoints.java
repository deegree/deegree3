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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.primitive.DefaultPoint;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;

/**
 * {@link Points} implementation based on a coordinate array.
 * <p>
 * This implementation is quite memory efficient, but only allows to hold anonymous {@link Point} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PackedPoints implements Points {

    private ICRS crs;

    private int dimension;

    private double[] coordinates;

    public PackedPoints( ICRS crs, double[] coordinates, int coordinatesDimension ) {
        this.crs = crs;
        this.coordinates = coordinates;
        this.dimension = coordinatesDimension;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public int size() {
        return coordinates.length / dimension;
    }

    /**
     * Provides acccess to an arbitrary {@link Point} in the sequence (expensive!).
     */
    @Override
    public Point get( int i ) {
        double[] pointCoordinates = new double[dimension];
        int idx = i * dimension;
        for ( int d = 0; d < dimension; d++ ) {
            pointCoordinates[d] = coordinates[idx + d];
        }
        return new DefaultPoint( null, crs, null, pointCoordinates );
    }

    @Override
    public Iterator<Point> iterator() {

        return new Iterator<Point>() {

            private int idx = 0;

            @SuppressWarnings("synthetic-access")
            private double[] pointCoordinates = new double[dimension];

            @SuppressWarnings("synthetic-access")
            private DefaultPoint point = new DefaultPoint( null, crs, null, pointCoordinates );

            @Override
            public boolean hasNext() {
                return idx < coordinates.length;
            }

            @Override
            public Point next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                for ( int i = 0; i < dimension; i++ ) {
                    pointCoordinates[i] = coordinates[idx + i];
                }
                idx += dimension;
                return point;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public double[] getAsArray() {
        return coordinates;
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
        for ( int i = 0; i < coordinates.length; i += 2 ) {
            env.expandToInclude( coordinates[i], coordinates[i + 1] );
        }
        return env;
    }

    @Override
    public Coordinate getCoordinate( int index ) {
        Point point = get( index );
        return new Coordinate( point.get0(), point.get1(), point.get2() );
    }

    @Override
    public void getCoordinate( int index, Coordinate coord ) {
        Point point = get( index );
        coord.x = point.get0();
        coord.y = point.get1();
        coord.z = point.get2();
    }

    @Override
    public Coordinate getCoordinateCopy( int index ) {
        Point point = get( index );
        return new Coordinate( point.get0(), point.get1(), point.get2() );
    }

    @Override
    public double getOrdinate( int index, int ordinateIndex ) {
        return get( index ).get( ordinateIndex );
    }

    @Override
    public double getX( int index ) {
        return get( index ).get0();
    }

    @Override
    public double getY( int index ) {
        return get( index ).get1();
    }

    @Override
    public void setOrdinate( int index, int ordinateIndex, double value ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Coordinate[] toCoordinateArray() {
        Coordinate[] coords = new Coordinate[coordinates.length / dimension];
        for ( int i = 0; i < coords.length; i++ ) {
            coords[i] = new Coordinate( coordinates[i * 2], coordinates[i * 2 + 1] );
        }
        return coords;
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CoordinateSequence copy() {
        throw new UnsupportedOperationException();
    }
}
