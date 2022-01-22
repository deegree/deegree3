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
package org.deegree.geometry.standard;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.points.PackedPoints;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.geom.LinearRing;

/**
 * Default implementation of {@link Envelope}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultEnvelope extends AbstractDefaultGeometry implements Envelope {

    private static final Logger LOG = LoggerFactory.getLogger( DefaultEnvelope.class );

    private Point max;

    private Point min;

    private Point centroid;

    private GeometryTransformer transformer;

    /**
     * Creates a new <code>DefaultEnvelope</code> instance with no id, crs and precisionmodel.
     * 
     * @param min
     * @param max
     */
    public DefaultEnvelope( Point min, Point max ) {
        this( null, null, null, min, max );
    }

    /**
     * Creates a new <code>DefaultEnvelope</code> instance from the given parameters.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param min
     * @param max
     */
    public DefaultEnvelope( String id, ICRS crs, PrecisionModel pm, Point min, Point max ) {
        super( id, crs, pm );
        this.min = min;
        this.max = max;
    }

    @Override
    public int getCoordinateDimension() {
        return min.getCoordinateDimension();
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.ENVELOPE;
    }

    @Override
    public Point getMax() {
        return max;
    }

    @Override
    public Point getMin() {
        return min;
    }

    @Override
    public double getSpan0() {
        return max.get0() - min.get0();
    }

    @Override
    public double getSpan1() {
        return max.get1() - min.get1();
    }

    @Override
    public double getSpan( int dim ) {
        return max.get( dim ) - min.get( dim );
    }

    @Override
    public Envelope merge( Envelope other ) {

        if ( this.getCoordinateSystem() != null && other.getCoordinateSystem() != null ) {
            if ( !this.getCoordinateSystem().equals( other.getCoordinateSystem() ) ) {
                synchronized ( this ) {
                    // TODO how about some central transformer caching?
                    if ( transformer == null ) {
                        transformer = new GeometryTransformer( this.getCoordinateSystem() );
                    }
                }
                try {
                    other = transformer.transform( other );
                } catch ( IllegalArgumentException e ) {
                    String msg = "Could not transform other envelope when merging: {}";
                    LOG.warn( msg, e.getLocalizedMessage() );
                } catch ( TransformationException e ) {
                    String msg = "Could not transform other envelope when merging: {}";
                    LOG.warn( msg, e.getLocalizedMessage() );
                } catch ( UnknownCRSException e ) {
                    String msg = "Could not transform other envelope when merging: {}";
                    LOG.warn( msg, e.getLocalizedMessage() );
                }
            }
        }

        int coordinateDimension = Math.min( getCoordinateDimension(), other.getCoordinateDimension() );
        double[] min = new double[coordinateDimension];
        double[] max = new double[coordinateDimension];

        double[] tMin = this.min.getAsArray();
        double[] oMin = other.getMin().getAsArray();

        double[] tMax = this.max.getAsArray();
        double[] oMax = other.getMax().getAsArray();

        for ( int i = 0; i < coordinateDimension; i++ ) {
            min[i] = Math.min( tMin[i], oMin[i] );
            max[i] = Math.max( tMax[i], oMax[i] );
            // if ( this.max.getAsArray()[i] > other.getMax().getAsArray()[i] ) {
            // max[i] = this.max.getAsArray()[i];
            // } else {
            // max[i] = other.getMax().getAsArray()[i];
            // }
        }
        Point newMin = new DefaultPoint( null, getCoordinateSystem(), pm, min );
        Point newMax = new DefaultPoint( null, getCoordinateSystem(), pm, max );
        return new DefaultEnvelope( null, getCoordinateSystem(), pm, newMin, newMax );
    }

    @Override
    public Envelope getEnvelope() {
        return this;
    }

    @Override
    public Point getCentroid() {
        if ( centroid == null ) {
            double[] coordinates = new double[max.getAsArray().length];
            for ( int i = 0; i < coordinates.length; i++ ) {
                coordinates[i] = min.getAsArray()[i] + ( max.getAsArray()[i] - min.getAsArray()[i] ) / 2d;
            }
            centroid = new DefaultPoint( null, getCoordinateSystem(), getPrecision(), coordinates );
        }
        return centroid;
    }

    @Override
    protected org.locationtech.jts.geom.Polygon buildJTSGeometry() {
        Points points = new PackedPoints( crs, new double[] { min.get0(), min.get1(), max.get0(), min.get1(),
                                                             max.get0(), max.get1(), min.get0(), max.get1(),
                                                             min.get0(), min.get1() }, 2 );
        LinearRing shell = jtsFactory.createLinearRing( ( points ) );
        return jtsFactory.createPolygon( shell, null );
    }

    @Override
    public String toString() {
        return "min: " + min + ", max: " + max + ", span0: " + getSpan0() + ", span1: " + getSpan1() + " , crs: " + crs;
    }

    @Override
    public boolean intersects( Geometry other ) {
        if ( !( other instanceof Envelope ) ) {
            return super.intersects( other );
        }

        Envelope e = (Envelope) other;

        double minX1 = this.getMin().get0();
        double minY1 = this.getMin().get1();
        double maxX1 = this.getMax().get0();
        double maxY1 = this.getMax().get1();

        double minX2 = e.getMin().get0();
        double minY2 = e.getMin().get1();
        double maxX2 = e.getMax().get0();
        double maxY2 = e.getMax().get1();

        return pointInside( minX1, minY1, e ) || pointInside( minX1, maxY1, e ) || pointInside( maxX1, minY1, e )
               || pointInside( maxX1, maxY1, e ) || pointInside( minX2, minY2, this )
               || pointInside( minX2, maxY2, this ) || pointInside( maxX2, minY2, this )
               || pointInside( maxX2, maxY2, this ) || noEdgeOverlap( e, this ) || noEdgeOverlap( this, e );
    }

    private static final boolean pointInside( final double x, final double y, final Envelope bbox ) {
        return x >= bbox.getMin().get0() && x <= bbox.getMax().get0() && y >= bbox.getMin().get1()
               && y <= bbox.getMax().get1();
    }

    private static final boolean noEdgeOverlap( final Envelope box1, final Envelope box2 ) {
        return box1.getMin().get0() <= box2.getMin().get0() && box2.getMax().get0() <= box1.getMax().get0()
               && box2.getMin().get1() <= box1.getMin().get1() && box1.getMax().get1() <= box2.getMax().get1();
    }
}
