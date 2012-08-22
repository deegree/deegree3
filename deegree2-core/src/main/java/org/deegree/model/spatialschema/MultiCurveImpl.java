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
 * default implementation of the MultiCurve interface from package deegree.model.spatialschema.
 *
 * ------------------------------------------------------------
 *
 * @version 12.6.2001
 * @author Andreas Poth
 */
public class MultiCurveImpl extends MultiPrimitiveImpl implements MultiCurve, Serializable {
    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = 2730942874409216686L;

    private static final ILogger LOG = LoggerFactory.getLogger( MultiCurveImpl.class );

    /**
     * Creates a new MultiCurveImpl object.
     *
     * @param crs
     */
    protected MultiCurveImpl( CoordinateSystem crs ) {
        super( crs );
    }

    /**
     * Creates a new MultiCurveImpl object.
     *
     * @param curve
     */
    protected MultiCurveImpl( Curve[] curve ) {
        super( curve[0].getCoordinateSystem() );

        for ( int i = 0; i < curve.length; i++ ) {
            aggregate.add( curve[i] );
        }

    }

    /**
     * Creates a new MultiCurveImpl object.
     *
     * @param curve
     * @param crs
     */
    protected MultiCurveImpl( Curve[] curve, CoordinateSystem crs ) {
        super( crs );

        for ( int i = 0; i < curve.length; i++ ) {
            aggregate.add( curve[i] );
        }

    }

    /**
     * adds a Curve to the aggregation
     */
    public void addCurve( Curve curve ) {
        super.add( curve );
    }

    /**
     * inserts a Curve in the aggregation. all elements with an index equal or larger index will be moved. if index is
     * larger then getSize() - 1 or smaller then 0 or curve equals null an exception will be thrown.
     *
     * @param curve
     *            Curve to insert.
     * @param index
     *            position where to insert the new Curve
     */
    public void insertCurveAt( Curve curve, int index )
                            throws GeometryException {
        super.insertObjectAt( curve, index );
    }

    /**
     * sets the submitted Curve at the submitted index. the element at the position <code>index</code> will be
     * removed. if index is larger then getSize() - 1 or smaller then 0 or curve equals null an exception will be
     * thrown.
     *
     * @param curve
     *            Curve to set.
     * @param index
     *            position where to set the new Curve
     */
    public void setCurveAt( Curve curve, int index )
                            throws GeometryException {
        setObjectAt( curve, index );
    }

    /**
     * removes the submitted Curve from the aggregation
     *
     * @return the removed Curve
     */
    public Curve removeCurve( Curve curve ) {
        return (Curve) super.removeObject( curve );
    }

    /**
     * removes the Curve at the submitted index from the aggregation. if index is larger then getSize() - 1 or smaller
     * then 0 an exception will be thrown.
     *
     * @return the removed Curve
     */
    public Curve removeCurveAt( int index )
                            throws GeometryException {
        return (Curve) super.removeObjectAt( index );
    }

    /**
     * removes all Curve from the aggregation.
     */
    @Override
    public void removeAll() {
        super.removeAll();
    }

    /**
     * returns the Curve at the submitted index.
     */
    public Curve getCurveAt( int index ) {
        return (Curve) super.getPrimitiveAt( index );
    }

    /**
     * returns all Curves as array
     */
    public Curve[] getAllCurves() {
        return aggregate.toArray( new Curve[getSize()] );
    }

    /**
     * returns true if the submitted Curve is within the aggregation
     *
     * @param curve
     * @return true if the submitted Curve is within the aggregation
     */
    public boolean isMember( Curve curve ) {
        return super.isMember( curve );
    }

    /**
     * returns the boundary of the MultiCurve
     * <p>
     * not implemented yet
     *
     * @return <code>null</code>
     */
    @Override
    public Boundary getBoundary() {
        return null;
    }

    /**
     * calculates the bounding box / envelope of the aggregation
     */
    protected void calculateEnvelope() {
        Envelope bb = getCurveAt( 0 ).getEnvelope();

        double[] min = bb.getMin().getAsArray().clone();
        double[] max = bb.getMax().getAsArray().clone();

        for ( int i = 1; i < getSize(); i++ ) {
            double[] pos1 = getCurveAt( i ).getEnvelope().getMin().getAsArray();
            double[] pos2 = getCurveAt( i ).getEnvelope().getMax().getAsArray();

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

        envelope = new EnvelopeImpl( new PositionImpl( min ), new PositionImpl( max ), this.crs );
    }

    /**
     * calculates the centroid of the aggregation
     */
    protected void calculateCentroid() {
        try {
            double cnt = 0;
            Point gmp = getCurveAt( 0 ).getCentroid();

            double[] cen = new double[gmp.getAsArray().length];

            for ( int i = 0; i < getSize(); i++ ) {
                cnt += getCurveAt( i ).getNumberOfCurveSegments();

                double[] pos = getCurveAt( i ).getCentroid().getAsArray();

                for ( int j = 0; j < getCoordinateDimension(); j++ ) {
                    cen[j] += pos[j];
                }
            }

            for ( int j = 0; j < getCoordinateDimension(); j++ ) {
                cen[j] = cen[j] / cnt / getSize();
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
        return 1;
    }

    @Override
    public int getCoordinateDimension() {
        return getCurveAt( 0 ).getCoordinateDimension();
    }

    @Override
    public Object clone() {
        MultiCurve mc = null;

        try {
            mc = new MultiCurveImpl( getCoordinateSystem() );

            for ( int i = 0; i < this.getSize(); i++ ) {
                CurveImpl ci = (CurveImpl) getCurveAt( i );
                mc.addCurve( (Curve) ci.clone() );
            }
        } catch ( Exception ex ) {
            LOG.logError( "MultiCurve_Impl.clone: ", ex );
        }

        return mc;
    }
}
