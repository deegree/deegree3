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

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CoordinateSystem;

/**
 * default implementation of the MultiPoint interface of package deegree.model.spatialschema.
 * 
 * <p>
 * ------------------------------------------------------------
 * </p>
 * 
 * @version 12.6.2001
 * @author Andreas Poth href="mailto:poth@lat-lon.de"
 *         <p>
 */
public class MultiPointImpl extends MultiPrimitiveImpl implements MultiPoint, Serializable {
    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = -1105623021535230655L;

    private static final ILogger LOG = LoggerFactory.getLogger( MultiPointImpl.class );

    /**
     * Creates a new MultiPointImpl object.
     * 
     * @param crs
     */
    protected MultiPointImpl( CoordinateSystem crs ) {
        super( crs );
    }

    /**
     * Creates a new MultiPointImpl object.
     * 
     * @param gmp
     */
    protected MultiPointImpl( Point[] gmp ) {
        super( gmp[0].getCoordinateSystem() );

        for ( int i = 0; i < gmp.length; i++ ) {
            aggregate.add( gmp[i] );
        }

    }

    /**
     * Creates a new MultiPointImpl object.
     * 
     * @param gmp
     * @param crs
     */
    protected MultiPointImpl( Point[] gmp, CoordinateSystem crs ) {
        super( crs );

        for ( int i = 0; i < gmp.length; i++ ) {
            aggregate.add( gmp[i] );
        }

    }

    /**
     * adds a Point to the aggregation
     */
    public void addPoint( Point gmp ) {
        super.add( gmp );
    }

    /**
     * inserts a Point into the aggregation. all elements with an index equal or larger index will be moved. if index is
     * larger then getSize() - 1 or smaller then 0 or gmp equals null an exception will be thrown.
     * 
     * @param gmp
     *            Point to insert.
     * @param index
     *            position where to insert the new Point
     */
    public void insertPointAt( Point gmp, int index )
                            throws GeometryException {
        super.insertObjectAt( gmp, index );
    }

    /**
     * sets the submitted Point at the submitted index. the element at the position <code>index</code> will be removed.
     * if index is larger then getSize() - 1 or smaller then 0 or gmp equals null an exception will be thrown.
     * 
     * @param gmp
     *            Point to set.
     * @param index
     *            position where to set the new Point
     */
    public void setPointAt( Point gmp, int index )
                            throws GeometryException {
        setObjectAt( gmp, index );
    }

    /**
     * removes the submitted Point from the aggregation
     * 
     * @return the removed Point
     */
    public Point removePoint( Point gmp ) {
        return (Point) super.removeObject( gmp );
    }

    /**
     * removes the Point at the submitted index from the aggregation. if index is larger then getSize() - 1 or smaller
     * then 0 an exception will be thrown.
     * 
     * @return the removed Point
     */
    public Point removePointAt( int index )
                            throws GeometryException {
        return (Point) super.removeObjectAt( index );
    }

    /**
     * returns the Point at the submitted index.
     */
    public Point getPointAt( int index ) {
        return (Point) super.getPrimitiveAt( index );
    }

    /**
     * returns all Points as array
     */
    public Point[] getAllPoints() {
        return aggregate.toArray( new Point[getSize()] );
    }

    /**
     * updates the bounding box of the aggregation
     */
    private void calculateEnvelope() {
        Point gmp = getPointAt( 0 );

        double[] min = gmp.getAsArray().clone();
        double[] max = min.clone();

        for ( int i = 1; i < getSize(); i++ ) {
            double[] pos = getPointAt( i ).getAsArray();

            for ( int j = 0; j < pos.length; j++ ) {
                if ( pos[j] < min[j] ) {
                    min[j] = pos[j];
                } else if ( pos[j] > max[j] ) {
                    max[j] = pos[j];
                }
            }
        }

        envelope = new EnvelopeImpl( new PositionImpl( min ), new PositionImpl( max ), this.crs );
    }

    /**
     * calculates the centroid of the surface
     */
    private void calculateCentroid() {
        try {
            Point gmp = getPointAt( 0 );

            double[] cen = new double[gmp.getAsArray().length];

            for ( int i = 0; i < getSize(); i++ ) {
                double[] pos = getPointAt( i ).getAsArray();

                for ( int j = 0; j < pos.length; j++ ) {
                    cen[j] += ( pos[j] / getSize() );
                }
            }

            centroid = new PositionImpl( cen );
        } catch ( Exception ex ) {
            LOG.logError( "", ex );
        }
    }

    /**
     * calculates the centroid and envelope of the aggregation
     */
    @Override
    protected void calculateParam() {
        calculateCentroid();
        calculateEnvelope();
        setValid( true );
    }

    @Override
    public int getDimension() {
        return 0;
    }

    @Override
    public int getCoordinateDimension() {
        return getPointAt( 0 ).getCoordinateDimension();
    }

    @Override
    public Object clone() {
        MultiPoint mp = null;

        try {
            mp = new MultiPointImpl( getCoordinateSystem() );

            for ( int i = 0; i < this.getSize(); i++ ) {
                PointImpl pi = (PointImpl) getPointAt( i );
                mp.add( (Point) pi.clone() );
            }
        } catch ( Exception ex ) {
            LOG.logError( "MultiPoint_Impl.clone: ", ex );
        }

        return mp;
    }
}
