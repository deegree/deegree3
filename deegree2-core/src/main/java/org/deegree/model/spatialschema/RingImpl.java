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
import java.util.Arrays;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CoordinateSystem;

/**
 * default implementation of the Ring interface of the
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class RingImpl extends OrientableCurveImpl implements Ring, Serializable {
    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = 9157144642050604928L;

    private static final ILogger LOG = LoggerFactory.getLogger( RingImpl.class );

    private SurfacePatch sp = null;

    private int nop = 0;

    private Position[] allPos;

    /**
     * Constructor, with an array and CoordinateSystem
     * 
     * @param points
     * @param crs
     * @throws GeometryException
     */
    protected RingImpl( Position[] points, CoordinateSystem crs ) throws GeometryException {
        super( crs );
        Position[][] tmp = new Position[1][];
        tmp[0] = points;
        setPositions( tmp );
    }

    /**
     * Constructor, with an array, CoordinateSystem and Orientation
     * 
     * @param points
     * @param crs
     * @param orientation
     * @throws GeometryException
     */
    protected RingImpl( Position[] points, CoordinateSystem crs, char orientation ) throws GeometryException {
        super( crs, orientation );
        Position[][] tmp = new Position[1][];
        tmp[0] = points;
        setPositions( tmp );
    }

    /**
     * Constructor, with curve segments, CoordinateSystem and Orientation
     * 
     * @param segments
     * @param crs
     * @param orientation
     * @throws GeometryException
     */
    protected RingImpl( CurveSegment[] segments, CoordinateSystem crs, char orientation ) throws GeometryException {
        super( crs, orientation );
        Position[][] tmp = new Position[segments.length][];
        for ( int i = 0; i < segments.length; i++ ) {
            tmp[i] = segments[i].getPositions();
        }
        setPositions( tmp );
    }

    /**
     * calculates the envelope
     */
    private void calculateEnvelope() {
        double[] min = allPos[0].getAsArray().clone();
        double[] max = min.clone();

        for ( int k = 1; k < allPos.length; k++ ) {
            double[] pos = allPos[k].getAsArray();

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
     * Ring must be closed, so isCycle returns TRUE.
     */
    public boolean isCycle() {
        return true;
    }

    /**
     * Ring is a PrimitiveBoundary, so isSimple returns TRUE.
     */
    public boolean isSimple() {
        return true;
    }

    /**
     * The operation "dimension" shall return the inherent dimension of this Geometry, which shall be less than or equal
     * to the coordinate dimension. The dimension of a collection of geometric objects shall be the largest dimension of
     * any of its pieces. Points are 0-dimensional, curves are 1-dimensional, surfaces are 2-dimensional, and solids are
     * 3-dimensional.
     */
    public int getDimension() {
        return 1;
    }

    /**
     * The operation "coordinateDimension" shall return the dimension of the coordinates that define this Geometry,
     * which must be the same as the coordinate dimension of the coordinate reference system for this Geometry.
     */
    public int getCoordinateDimension() {
        return getPositions()[0].getCoordinateDimension();
    }

    /**
     * gets the Ring as a Array of positions.
     */
    public Position[] getPositions() {
        if ( getOrientation() == '-' ) {
            Position[] temp = new Position[allPos.length];

            for ( int i = 0; i < allPos.length; i++ ) {
                temp[i] = allPos[( allPos.length - 1 ) - i];
            }

            return temp;
        }
        return allPos;
    }

    /**
     * sets the Ring as a ArrayList of points
     * 
     * @param positions
     * @throws GeometryException
     */
    protected void setPositions( Position[][] positions )
                            throws GeometryException {

        nop = 0;
        for ( int i = 0; i < positions.length; i++ ) {
            nop += positions[i].length;
        }
        allPos = new Position[nop];
        int k = 0;
        for ( int i = 0; i < positions.length; i++ ) {
            for ( int j = 0; j < positions[i].length; j++ ) {
                allPos[k++] = positions[i][j];
            }
        }

        // checks if the ring has more than 3 elements [!(points.length > 3)]
        if ( nop < 3 ) {
            throw new GeometryException( "invalid length of a Ring!" );
        }

        // checks if the startpoint = endpoint of the ring
        if ( !allPos[0].equals( allPos[allPos.length - 1] ) ) {
            throw new GeometryException( "StartPoint of ring isn't equal to EndPoint!" );
        }

        setValid( false );
    }

    /**
     * returns the Ring as one CurveSegment
     */
    public CurveSegment getAsCurveSegment()
                            throws GeometryException {
        return new LineStringImpl( allPos, getCoordinateSystem() );
    }

    /**
     * returns the Ring as a CurveSegments
     * 
     * @return curve segments
     */
    public CurveSegment[] getCurveSegments() {
        try {
            return new CurveSegment[] { getAsCurveSegment() };
        } catch ( GeometryException e ) {
            LOG.logError( e );
            return null;
        }
    }

    /**
     * returns the CurveBoundary of the Ring. For a CurveBoundary is defines as the first and the last point of a Curve
     * the CurveBoundary of a Ring contains two indentical point (because a Ring is closed)
     */
    public CurveBoundary getCurveBoundary() {
        return (CurveBoundary) boundary;
    }

    @Override
    public boolean equals( Object other ) {
        if ( !super.equals( other ) || !( other instanceof RingImpl ) ) {
            return false;
        }

        if ( !envelope.equals( ( (Geometry) other ).getEnvelope() ) ) {
            return false;
        }

        Position[] p2 = ( (Ring) other ).getPositions();

        if ( !Arrays.equals( allPos, p2 ) ) {
            return false;
        }

        return true;
    }

    @Override
    public Object clone() {
        Ring r = null;
        try {
            CurveSegment[] segments = getCurveSegments();
            for ( int i = 0; i < segments.length; i++ ) {
                segments[i] = new LineStringImpl( segments[i].getPositions(), getCoordinateSystem() );
            }
            r = new RingImpl( segments, getCoordinateSystem(), getOrientation() );
        } catch ( Exception ex ) {
            LOG.logError( ex.getMessage(), ex );
        }

        return r;
    }

    @Override
    public boolean intersects( Geometry gmo ) {
        boolean inter = false;

        try {
            // TODO
            // use segments
            CurveSegment sp = new LineStringImpl( allPos, crs );

            if ( gmo instanceof Point ) {
                double tolerance = ( (Point) gmo ).getTolerance();
                inter = LinearIntersects.intersects( ( (Point) gmo ).getPosition(), sp, tolerance );
            } else if ( gmo instanceof Curve ) {
                Curve curve = new CurveImpl( new CurveSegment[] { sp } );
                inter = LinearIntersects.intersects( (Curve) gmo, curve );
            } else if ( gmo instanceof Surface ) {
                Curve curve = new CurveImpl( new CurveSegment[] { sp } );
                inter = LinearIntersects.intersects( curve, (Surface) gmo );
            } else if ( gmo instanceof MultiPrimitive ) {
                inter = intersectsAggregate( (MultiPrimitive) gmo );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }

        return inter;
    }

    /**
     * the operations returns true if the submitted multi primitive intersects with the curve segment
     */
    private boolean intersectsAggregate( Aggregate mprim )
                            throws Exception {
        boolean inter = false;

        int cnt = mprim.getSize();

        for ( int i = 0; i < cnt; i++ ) {
            if ( intersects( mprim.getObjectAt( i ) ) ) {
                inter = true;
                break;
            }
        }

        return inter;
    }

    /**
     * The Boolean valued operation "contains" shall return TRUE if this Geometry contains another Geometry.
     * <p>
     * </p>
     * At the moment the operation just works with point geometries
     */
    @Override
    public boolean contains( Geometry gmo ) {

        try {
            if ( sp == null ) {
                sp = new PolygonImpl( new SurfaceInterpolationImpl(), allPos, null, crs );
            }
            return sp.contains( gmo );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }

        return false;
    }

    @Override
    public boolean contains( Position position ) {
        return contains( new PointImpl( position, null ) );
    }

    /**
     * calculates the centroid of the ring
     */
    protected void calculateCentroid() {
        double[] cen = new double[getCoordinateDimension()];

        for ( int k = 0; k < allPos.length; k++ ) {
            for ( int j = 0; j < getCoordinateDimension(); j++ ) {
                cen[j] += ( allPos[k].getAsArray()[j] / allPos.length );
            }
        }

        centroid = new PositionImpl( cen );
    }

    @Override
    protected void calculateParam() {
        calculateCentroid();
        calculateEnvelope();
        setValid( true );
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "positions = " + allPos.length + "\n";
        ret += ( "envelope = " + envelope + "\n" );
        return ret;
    }
}
