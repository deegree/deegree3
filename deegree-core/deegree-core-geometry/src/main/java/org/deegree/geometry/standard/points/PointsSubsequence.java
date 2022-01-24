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

import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;

/**
 * {@link Points} implementation that aggregates the members from a sequence of {@link Points} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PointsSubsequence implements Points {

    private Points points;

    private final int startIdx;

    /**
     * Creates a new {@link PointsSubsequence} instance from the given list of {@link Points}.
     * 
     * @param points
     * @param startIdx
     */
    public PointsSubsequence( Points points, int startIdx ) {
        this.points = points;
        this.startIdx = startIdx;
    }

    @Override
    public Point get( int i ) {
        int idx = startIdx + i;
        if ( idx >= points.size() ) {
            throw new NoSuchElementException();
        }
        return points.get( idx );
    }

    @Override
    public double[] getAsArray() {
        double[] coords = new double[getDimension() * ( size() - startIdx )];
        int i = 0;
        int j = 0;
        for ( Point p : this ) {
            if ( i++ >= startIdx ) {
                for ( double coord : p.getAsArray() ) {
                    coords[j++] = coord;
                }
            }
        }
        return coords;
    }

    @Override
    public int getDimension() {
        return points.getDimension();
    }

    @Override
    public int size() {
        return points.size() - startIdx;
    }

    @Override
    public Iterator<Point> iterator() {
        Iterator<Point> innerIter = points.iterator();
        for ( int i = 0; i < startIdx; i++ ) {
            innerIter.next();
        }
        return innerIter;
    }

    @Override
    public Point getEndPoint() {
        return points.getEndPoint();
    }

    @Override
    public Point getStartPoint() {
        return points.get( startIdx );
    }

    // -----------------------------------------------------------------------
    // Implementation of JTS methods
    // -----------------------------------------------------------------------

    @Override
    public Envelope expandEnvelope( Envelope env ) {
        for ( Point p : this ) {
            env.expandToInclude( p.get0(), p.get1() );
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
        Coordinate[] coords = new Coordinate[size()];
        int i = 0;
        for ( Point p : this ) {
            coords[i++] = new Coordinate( p.get0(), p.get1() );
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
