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
import java.util.List;
import java.util.NoSuchElementException;

import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;

/**
 * {@link Points} implementation that aggregates the members from a sequence of {@link Points} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class PointsPoints implements Points {

    private List<Points> pointsList;

    /**
     * Creates a new {@link PointsPoints} instance from the given list of {@link Points}.
     * 
     * @param pointsList
     */
    public PointsPoints( List<Points> pointsList ) {
        this.pointsList = pointsList;
    }

    @Override
    public Point get( int i ) {
        int offset = 0;
        for ( Points points : pointsList ) {
            if ( i - offset < points.size() ) {
                return points.get( i - offset );
            }
            offset += points.size();
        }
        throw new NoSuchElementException();
    }

    @Override
    public double[] getAsArray() {
        double[] coords = new double[getCoordinateDimension() * size()];
        int i = 0;
        for ( Point p : this ) {
            for ( double coord : p.getAsArray() ) {
                coords[i++] = coord;
            }
        }
        return coords;
    }

    @Override
    public int getCoordinateDimension() {
        return pointsList.get( 0 ).getCoordinateDimension();
    }

    @Override
    public int size() {
        int size = 0;
        for ( Points points : pointsList ) {
            size += points.size();
        }
        return size;
    }

    @Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {

            private Iterator<Points> pointsListIter = pointsList.iterator();

            private Iterator<Point> currentIter = pointsListIter.next().iterator();

            @Override
            public boolean hasNext() {
                if ( currentIter.hasNext() ) {
                    return true;
                }
                return pointsListIter.hasNext();
            }

            @Override
            public Point next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                if ( currentIter.hasNext() ) {
                    return currentIter.next();
                }
                currentIter = pointsListIter.next().iterator();
                return currentIter.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Point getEndPoint() {
        Points p = pointsList.get( pointsList.size() - 1 );
        return p.getEndPoint();
    }

    @Override
    public Point getStartPoint() {
        return get( 0 );
    }
}
