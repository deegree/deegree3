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

import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CoordinateSystem;

/**
 * default implementation of
 * 
 * @see org.deegree.model.spatialschema.Curve
 * 
 * @author Andreas Poth
 * @version $Revision$ $Date$
 */
public class CurveImpl extends OrientableCurveImpl implements Curve, GenericCurve {

    private static final ILogger LOG = LoggerFactory.getLogger( CurveImpl.class );

    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = 4060425075179654976L;

    protected ArrayList<CurveSegment> segments = null;

    /**
     * initialize the curve by submitting a spatial reference system and an array of curve segments. the orientation of
     * the curve is '+'
     * 
     * @param segments
     *            array of CurveSegment
     * @throws GeometryException
     */
    protected CurveImpl( CurveSegment segments ) throws GeometryException {
        this( '+', new CurveSegment[] { segments } );
    }

    /**
     * initialize the curve by submitting a spatial reference system and an array of curve segments. the orientation of
     * the curve is '+'
     * 
     * @param segments
     *            array of CurveSegment
     * @throws GeometryException
     */
    protected CurveImpl( CurveSegment[] segments ) throws GeometryException {
        this( '+', segments );
    }

    /**
     * initialize the curve by submitting a spatial reference system and an array of curve segments. the orientation of
     * the curve is '+'
     * 
     * @param segments
     *            array of CurveSegment
     * @param crs
     * @throws GeometryException
     */
    protected CurveImpl( CurveSegment[] segments, CoordinateSystem crs ) throws GeometryException {
        this( '+', segments );
        this.crs = crs;
    }

    /**
     * initialize the curve by submitting a spatial reference system, an array of curve segments and the orientation of
     * the curve
     * 
     * @param segments
     *            array of CurveSegment
     * @param orientation
     *            of the curve
     * @throws GeometryException
     */
    protected CurveImpl( char orientation, CurveSegment[] segments ) throws GeometryException {
        super( segments[0].getCoordinateSystem(), orientation );
        this.segments = new ArrayList<CurveSegment>( segments.length );
        for ( int i = 0; i < segments.length; i++ ) {
            this.segments.add( segments[i] );
            // TODO check if segments touch
            if ( i > 0 ) {
                if ( !segments[i - 1].getEndPoint().equals( segments[i].getStartPoint() ) ) {
                    String msg = "Topological error in Curve: end-point of segment " + ( i - 1 ) + ": "
                                 + WKTAdapter.export( segments[i - 1].getEndPoint() )
                                 + "doesn't match start-point of segment " + i + ": "
                                 + WKTAdapter.export( segments[i].getStartPoint() ) + "!";
                    throw new GeometryException( msg );
                }
            }
        }
        setValid( false );
    }

    /**
     * calculates the envelope of the Curve
     */
    private void calculateEnvelope() {
        try {
            Position[] positions = getAsLineString().getPositions();

            double[] min = positions[0].getAsArray().clone();
            double[] max = min.clone();

            for ( int i = 1; i < positions.length; i++ ) {
                double[] pos = positions[i].getAsArray();

                for ( int j = 0; j < pos.length; j++ ) {
                    if ( pos[j] < min[j] ) {
                        min[j] = pos[j];
                    } else if ( pos[j] > max[j] ) {
                        max[j] = pos[j];
                    }
                }
            }

            envelope = new EnvelopeImpl( new PositionImpl( min ), new PositionImpl( max ), this.crs );
        } catch ( GeometryException e ) {
            // do nothing
        }
    }

    /**
     * calculates the boundary of the Curve
     */
    private void calculateBoundary() {
        boundary = new CurveBoundaryImpl( getCoordinateSystem(), getStartPoint().getPosition(),
                                          getEndPoint().getPosition() );
    }

    /**
     * calculates the centroid of the Curve
     */
    private void calculateCentroid() {
        Position[] positions = null;
        try {
            positions = getAsLineString().getPositions();
        } catch ( GeometryException e ) {
            LOG.logError( e.getMessage(), e );
        }
        if ( positions != null ) {

            double[] cen = new double[positions[0].getAsArray().length];

            for ( int i = 0; i < positions.length; i++ ) {
                double[] pos = positions[i].getAsArray();

                for ( int j = 0; j < pos.length; j++ ) {
                    cen[j] += ( pos[j] / positions.length );
                }
            }

            centroid = new PositionImpl( cen );
        }
    }

    @Override
    protected void calculateParam() {
        calculateCentroid();
        calculateEnvelope();
        calculateBoundary();
        setValid( true );
    }

    /**
     * returns the boundary of the curve
     */
    public CurveBoundary getCurveBoundary() {
        return (CurveBoundary) boundary;
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
        return getStartPoint().getPosition().getCoordinateDimension();
    }

    /**
     * The Boolean valued operation "intersects" shall return TRUE if this Geometry intersects another Geometry. Within
     * a Complex, the Primitives do not intersect one another. In general, topologically structured data uses shared
     * geometric objects to capture intersection information.
     * <p>
     * </p>
     * dummy implementation
     */
    @Override
    public boolean intersects( Geometry gmo ) {
        boolean inter = false;

        try {
            for ( int i = 0; i < segments.size(); i++ ) {
                CurveSegment cs = getCurveSegmentAt( i );

                if ( cs.intersects( gmo ) ) {
                    inter = true;
                    break;
                }
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }

        return inter;
    }

    /**
     * returns the length of the curve in units of the related spatial reference system
     */
    public double getLength() {
        double d = 0;
        for ( int i = 0; i < segments.size(); i++ ) {
            d += segments.get( i ).getLength();
        }
        return d;
    }

    /**
     * returns the number of segments building the curve
     */
    public int getNumberOfCurveSegments() {
        return segments.size();
    }

    /**
     * returns the first point of the curve. if the curve doesn't contain a segment or the first segment doesn't contain
     * a point null will be returned
     */
    public Point getStartPoint() {
        if ( getNumberOfCurveSegments() == 0 ) {
            return null;
        }

        Point gmp = null;

        try {
            gmp = getCurveSegmentAt( 0 ).getStartPoint();
        } catch ( GeometryException e ) {
            LOG.logError( "", e );
        }

        return gmp;
    }

    /**
     * returns the last point of the curve.if the curve doesn't contain a segment or the last segment doesn't contain a
     * point null will be returned
     */
    public Point getEndPoint() {
        if ( getNumberOfCurveSegments() == 0 ) {
            return null;
        }

        Point gmp = null;

        try {
            gmp = getCurveSegmentAt( getNumberOfCurveSegments() - 1 ).getEndPoint();
        } catch ( GeometryException e ) {
            LOG.logError( "", e );
        }

        return gmp;
    }

    /**
     * returns the curve as LineString. if there isn't a curve segment within the curve null will be returned
     */
    public LineString getAsLineString()
                            throws GeometryException {
        if ( getNumberOfCurveSegments() == 0 ) {
            return null;
        }

        Position[] tmp = null;

        // normal orientaton
        if ( getOrientation() == '+' ) {
            int cnt = 0;

            for ( int i = 0; i < getNumberOfCurveSegments(); i++ ) {
                cnt += getCurveSegmentAt( i ).getNumberOfPoints();
            }

            tmp = new Position[cnt];

            int k = 0;

            for ( int i = 0; i < getNumberOfCurveSegments(); i++ ) {
                Position[] gmps = getCurveSegmentAt( i ).getPositions();

                for ( int j = 0; j < gmps.length; j++ ) {
                    tmp[k++] = gmps[j];
                }
            }
        } else {
            // inverse orientation
            int cnt = 0;

            for ( int i = getNumberOfCurveSegments() - 1; i >= 0; i-- ) {
                cnt += getCurveSegmentAt( i ).getNumberOfPoints();
            }

            tmp = new Position[cnt];

            int k = 0;

            for ( int i = getNumberOfCurveSegments() - 1; i >= 0; i-- ) {
                Position[] gmps = getCurveSegmentAt( i ).getPositions();

                for ( int j = gmps.length - 1; j >= 0; j-- ) {
                    tmp[k++] = gmps[j];
                }
            }
        }

        return new LineStringImpl( tmp, this.crs );
    }

    /**
     * returns the curve segment at the submitted index
     * 
     * @param index
     *            index of the curve segment that should be returned
     * @exception GeometryException
     *                a exception will be thrown if <tt>index</tt> is smaller than '0' or larger than
     *                <tt>getNumberOfCurveSegments()-1</tt>
     */
    public CurveSegment getCurveSegmentAt( int index )
                            throws GeometryException {
        if ( ( index < 0 ) || ( index > getNumberOfCurveSegments() - 1 ) ) {
            throw new GeometryException( "invalid index/position to get a segment!" );
        }

        return segments.get( index );
    }

    /**
     * 
     * @return all segments of a Curve
     * @throws GeometryException
     */
    public CurveSegment[] getCurveSegments()
                            throws GeometryException {
        return segments.toArray( new CurveSegment[segments.size()] );
    }

    /**
     * @return true if no segment is within the curve
     */
    @Override
    public boolean isEmpty() {
        return ( getNumberOfCurveSegments() == 0 );
    }

    /**
     * translate each point of the curve with the values of the submitted double array.
     */
    @Override
    public void translate( double[] d ) {
        try {
            for ( int i = 0; i < segments.size(); i++ ) {
                Position[] pos = getCurveSegmentAt( i ).getPositions();

                for ( int j = 0; j < pos.length; j++ ) {
                    pos[j].translate( d );
                }
            }
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage(), e );
        }
        setValid( false );
    }

    /**
     * checks if this curve is completely equal to the submitted geometry
     * 
     * @param other
     *            object to compare to
     */
    @Override
    public boolean equals( Object other ) {
        if ( envelope == null ) {
            calculateEnvelope();
        }
        if ( !super.equals( other ) ) {
            return false;
        }

        if ( !( other instanceof CurveImpl ) ) {
            return false;
        }

        if ( !envelope.equals( ( (Geometry) other ).getEnvelope() ) ) {
            return false;
        }

        if ( segments.size() != ( (Curve) other ).getNumberOfCurveSegments() ) {
            return false;
        }

        try {
            for ( int i = 0; i < segments.size(); i++ ) {
                if ( !getCurveSegmentAt( i ).equals( ( (Curve) other ).getCurveSegmentAt( i ) ) ) {
                    return false;
                }
            }
        } catch ( Exception e ) {
            return false;
        }

        return true;
    }

    @Override
    public Geometry union( Geometry other ) {
        if ( other instanceof Curve ) {
            Geometry result = null;
            try {
                Position[] p1 = getAsLineString().getPositions();
                Position[] p2 = ( (Curve) other ).getAsLineString().getPositions();
                List<Position> list = null;
                if ( p1[0].equals( p2[0] ) ) {
                    list = new ArrayList<Position>( p1.length + p2.length - 1);
                    for ( int i = p1.length-1; i > 0; i-- ) {
                        list.add( p1[i] );
                    }
                    for ( Position position : p2 ) {
                        list.add( position );
                    }                    
                } else if ( p1[0].equals( p2[p2.length - 1] ) ) {
                    list = new ArrayList<Position>( p1.length + p2.length - 1);
                    for ( Position position : p2 ) {
                        list.add( position );
                    }
                    for ( int i = 1; i < p1.length; i++ ) {
                        list.add( p1[i] );
                    }
                } else if ( p1[p1.length - 1].equals( p2[0] ) ) {
                    list = new ArrayList<Position>( p1.length + p2.length - 1);
                    for ( Position position : p1 ) {
                        list.add( position );
                    }
                    for ( int i = 1; i < p2.length; i++ ) {
                        list.add( p2[i] );
                    }
                } else if ( p1[p1.length - 1].equals( p2[p2.length - 1] ) ) {
                    list = new ArrayList<Position>( p1.length + p2.length - 1);
                    for ( Position position : p1 ) {
                        list.add( position );
                    }
                    for ( int i = p2.length-2; i >= 0; i-- ) {
                        list.add( p2[i] );
                    }
                } else {
                    // curves not touching at start or end point                    
                    result = GeometryFactory.createMultiCurve( new Curve[] { this, (Curve) other } );
                }
                if ( result == null ) {
                    // curves touching at start or end point
                    result = GeometryFactory.createCurve( list.toArray( new Position[list.size()] ), crs );
                }
            } catch ( GeometryException e ) {
                throw new RuntimeException( e );
            }
            return result;
        } else {
            return super.union( other );
        }
    }

    @Override
    public Object clone() {
        CurveImpl c = null;
        try {
            CurveSegment[] cs = new CurveSegment[getNumberOfCurveSegments()];
            for ( int i = 0; i < segments.size(); i++ ) {
                cs[i] = (CurveSegment) ( (CurveSegmentImpl) segments.get( i ) ).clone();
            }
            c = new CurveImpl( getOrientation(), cs );
            c.crs = crs;
        } catch ( Exception ex ) {
            LOG.logError( "CurveImpl.clone: ", ex );
        }
        return c;
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "segments = " + segments + "\n";
        ret += ( "envelope = " + envelope + "\n" );
        return ret;
    }
}
