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
import org.deegree.geometry.Geometry;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.primitive.DefaultPoint;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;

/**
 * {@link Points} implementation based on a JTS coordinate sequence.
 * <p>
 * This implementation is useful when JTS geometries have to converted into {@link Geometry} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JTSPoints implements Points {

    private final ICRS crs;

    private final CoordinateSequence seq;

    private int dim = 0;

    public JTSPoints( ICRS crs, CoordinateSequence seq ) {
        this.crs = crs;
        this.seq = seq;
        // TODO is this really necessary? Why does seq.getDimension() always return 3?
        if ( seq.size() > 0 ) {
            for ( int i = 0; i < seq.getDimension(); i++ ) {
                if ( Double.isNaN( seq.getOrdinate( 0, i ) ) ) {
                    break;
                }
                dim++;
            }
        }
    }

    @Override
    public int getDimension() {
        return dim;
    }

    @Override
    public int size() {
        return seq.size();
    }

    /**
     * Provides acccess to an arbitrary {@link Point} in the sequence (expensive!).
     */
    @Override
    public Point get( int i ) {
        double[] pointCoordinates = new double[getDimension()];
        for ( int d = 0; d < getDimension(); d++ ) {
            pointCoordinates[d] = getOrdinate( i, d );
        }
        return new DefaultPoint( null, crs, null, pointCoordinates );
    }

    @Override
    public Iterator<Point> iterator() {

        return new Iterator<Point>() {

            private int idx = 0;

            private double[] pointCoordinates = new double[getDimension()];

            @SuppressWarnings("synthetic-access")
            private DefaultPoint point = new DefaultPoint( null, crs, null, pointCoordinates );

            @Override
            public boolean hasNext() {
                return idx < size();
            }

            @Override
            public Point next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                for ( int i = 0; i < getDimension(); i++ ) {
                    pointCoordinates[i] = getOrdinate( idx, i );
                }
                idx++;
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
        return seq.expandEnvelope( env );
    }

    @Override
    public Coordinate getCoordinate( int index ) {
        return seq.getCoordinate( index );
    }

    @Override
    public void getCoordinate( int index, Coordinate coord ) {
        seq.getCoordinate( index, coord );
    }

    @Override
    public Coordinate getCoordinateCopy( int index ) {
        return seq.getCoordinateCopy( index );
    }

    @Override
    public double getOrdinate( int index, int ordinateIndex ) {
        return seq.getOrdinate( index, ordinateIndex );
    }

    @Override
    public double getX( int index ) {
        return seq.getX( index );
    }

    @Override
    public double getY( int index ) {
        return seq.getY( index );
    }

    @Override
    public void setOrdinate( int index, int ordinateIndex, double value ) {
        seq.setOrdinate( index, ordinateIndex, value );
    }

    @Override
    public Coordinate[] toCoordinateArray() {
        return seq.toCoordinateArray();
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
