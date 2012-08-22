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
package org.deegree.model.spatialschema;

import java.io.Serializable;

import org.deegree.model.crs.CoordinateSystem;

/**
 * Default implementation of {@link MultiGeometry}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MultiGeometryImpl extends AggregateImpl implements MultiGeometry, Serializable {

    private static final long serialVersionUID = 1449961950954331996L;

    // = highest dimension of all member geometries
    private int dimension;

    /**
     * Creates an empty {@link MultiGeometryImpl} instance with a given {@link CoordinateSystem}.
     *
     * @param crs
     *            coordinate system
     */
    protected MultiGeometryImpl( CoordinateSystem crs ) {
        super( crs );
    }

    /**
     * Creates a {@link MultiGeometryImpl} with a given {@link CoordinateSystem} that contains the provided
     * {@link Geometry} objects.
     *
     * @param members
     *            geometries contained in the {@link MultiGeometry}
     * @param crs
     *            coordinate system
     */
    protected MultiGeometryImpl( Geometry[] members, CoordinateSystem crs ) {
        super( crs );
        for ( int i = 0; i < members.length; i++ ) {
            aggregate.add( members[i] );
        }
    }

    /**
     * The operation "coordinateDimension" shall return the dimension of the coordinates that define this Geometry,
     * which must be the same as the coordinate dimension of the coordinate reference system for this Geometry.
     *
     * @return the actual dimension
     */
    public int getCoordinateDimension() {
        if ( crs != null ) {
            return crs.getDimension();
        }
        return 2;
    }

    /**
     * Returns the dimension of the aggregation, i.e. the maximum dimension of all member geometries.
     *
     * @return the maximum dimension of all member geometries
     */
    public int getDimension() {
        if ( !isValid() ) {
            calculateParam();
        }
        return dimension;
    }

    /**
     * Calculates the value of the following member variables:
     * <ul>
     * <li>{@link #dimension}</li>
     * <li>{@link #envelope}</li>
     * <li>{@link #centroid}</li>
     * </ul>
     */
    @Override
    protected synchronized void calculateParam() {
        if ( aggregate.size() == 0 ) {
            throw new RuntimeException( "Cannot calculate params for empty MultiGeometry." );
        }
        dimension = calculateDimension();
        envelope = calculateEnvelope();
        centroid = calculateCentroid();
        setValid( true );
    }

    private int calculateDimension() {
        int dimension = -1;
        for ( Geometry member : aggregate ) {
            if ( member.getDimension() > dimension ) {
                dimension = member.getDimension();
            }
        }
        return dimension;
    }

    private Envelope calculateEnvelope() {

        double[] min;
        double[] max;
        Geometry firstMember = getObjectAt( 0 );
        if ( firstMember instanceof Point ) {
            min = ( (Point) firstMember ).getAsArray().clone();
            max = ( (Point) firstMember ).getAsArray().clone();
        } else {
            System.out.println( "Object at 0: " + getObjectAt( 0 ).getClass().getName() );
            Envelope bb = getObjectAt( 0 ).getEnvelope();
            min = bb.getMin().getAsArray().clone();
            max = bb.getMax().getAsArray().clone();
        }

        for ( int i = 1; i < getSize(); i++ ) {
            double[] pos1 = getObjectAt( i ).getEnvelope().getMin().getAsArray();
            double[] pos2 = getObjectAt( i ).getEnvelope().getMax().getAsArray();

            for ( int j = 0; j < pos1.length; j++ ) {
                if ( pos1[j] < min[j] ) {
                    min[j] = pos1[j];
                } else if ( pos1[j] > max[j] ) {
                    max[j] = pos1[j];
                }

                if ( pos2[j] < min[j] ) {
                    min[j] = pos2[j];
                } else if ( pos2[j] > max[j] ) {
                    max[j] = pos2[j];
                }
            }
        }
        return new EnvelopeImpl( new PositionImpl( min ), new PositionImpl( max ), this.crs );
    }

    private Position calculateCentroid() {
        int dim = getCoordinateDimension();
        double[] centroid = new double[dim];
        for ( Geometry member : aggregate ) {
            double[] pos = member.getCentroid().getAsArray();
            for ( int i = 0; i < dim; i++ ) {
                centroid[i] = centroid[i] + pos[i];
            }
        }
        for ( int i = 0; i < dim; i++ ) {
            centroid[i] = centroid[i] / aggregate.size();
        }
        return new PositionImpl( centroid );
    }
}
