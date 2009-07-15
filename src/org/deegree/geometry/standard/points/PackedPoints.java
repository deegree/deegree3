//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
import org.deegree.geometry.standard.primitive.DefaultPoint;

/**
 * {@link Points} implementation based on a coordinate array.
 * <p>
 * This implementation is quite memory efficient, but only allows to hold anonymous {@link Point} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class PackedPoints implements Points {

    private int coordinatesDimension;

    private double[] coordinates;

    public PackedPoints( double[] coordinates, int coordinatesDimension ) {
        this.coordinates = coordinates;
        this.coordinatesDimension = coordinatesDimension;
    }

    @Override
    public int getCoordinateDimension() {
        return coordinatesDimension;
    }

    @Override
    public int size() {
        return coordinates.length / coordinatesDimension;
    }

    /**
     * Provides acccess to an arbitrary {@link Point} in the sequence (expensive!).
     */
    @Override
    public Point get( int i ) {
        double[] pointCoordinates = new double[coordinatesDimension];
        int idx = i * coordinatesDimension;
        for ( int d = 0; d < coordinatesDimension; d++ ) {
            pointCoordinates[i] = coordinates[idx + d];
        }
        return new DefaultPoint( null, null, null, pointCoordinates );
    }

    @Override
    public Iterator<Point> iterator() {

        return new Iterator<Point>() {

            private int idx = 0;

            private double[] pointCoordinates = new double[coordinatesDimension];

            private DefaultPoint point = new DefaultPoint( null, null, null, pointCoordinates );

            @Override
            public boolean hasNext() {
                return idx < coordinates.length;
            }

            @Override
            public Point next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                for ( int i = 0; i < coordinatesDimension; i++ ) {
                    pointCoordinates[i] = coordinates[idx + i];
                }
                idx += coordinatesDimension;
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
}
