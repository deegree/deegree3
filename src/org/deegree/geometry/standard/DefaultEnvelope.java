//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/rendering/Java2DRenderer.java $
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

import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.points.PackedPoints;
import org.deegree.geometry.standard.primitive.DefaultPoint;

import com.vividsolutions.jts.geom.LinearRing;

/**
 * Default implementation of {@link Envelope}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultEnvelope extends AbstractDefaultGeometry implements Envelope {

    private static GeometryFactory geomFactory = new GeometryFactory();

    private Point max;

    private Point min;

    private Point centroid;

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
    public DefaultEnvelope( String id, CRS crs, PrecisionModel pm, Point min, Point max ) {
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
    public double getHeight() {
        return max.getY() - min.getY();
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
    public double getWidth() {
        return max.getX() - min.getX();
    }

    @Override
    public Geometry intersection( Geometry geometry ) {
        Geometry result = null;
        switch ( geometry.getGeometryType() ) {
        case ENVELOPE: {
            Envelope other = (Envelope) geometry;

            double minX1 = this.getMin().getX();
            double minY1 = this.getMin().getY();
            double maxX1 = this.getMax().getX();
            double maxY1 = this.getMax().getY();

            double minX2 = other.getMin().getX();
            double minY2 = other.getMin().getY();
            double maxX2 = other.getMax().getX();
            double maxY2 = other.getMax().getY();

            if ( this.intersects( other ) ) {
                double newMinX;
                double newMinY;
                double newMaxX;
                double newMaxY;

                if ( minX2 > minX1 ) {
                    newMinX = minX2;
                } else {
                    newMinX = minX1;
                }
                if ( maxX1 > maxX2 ) {
                    newMaxX = maxX2;
                } else {
                    newMaxX = maxX1;
                }

                if ( minY2 > minY1 ) {
                    newMinY = minY2;
                } else {
                    newMinY = minY1;
                }
                if ( maxY1 > maxY2 ) {
                    newMaxY = maxY2;
                } else {
                    newMaxY = maxY1;
                }

                result = geomFactory.createEnvelope( new double[] { newMinX, newMinY },
                                                     new double[] { newMaxX, newMaxY }, null );
            } else {

            }
            break;
        }
        default: {
            throw new UnsupportedOperationException();
        }
        }
        return result;
    }

    @Override
    public boolean intersects( Geometry geometry ) {

        switch ( geometry.getGeometryType() ) {
        case ENVELOPE: {
            Envelope other = (Envelope) geometry;
            double minX1 = this.getMin().getX();
            double minY1 = this.getMin().getY();
            double maxX1 = this.getMax().getX();
            double maxY1 = this.getMax().getY();

            double minX2 = other.getMin().getX();
            double minY2 = other.getMin().getY();
            double maxX2 = other.getMax().getX();
            double maxY2 = other.getMax().getY();

            // special case: the passed envelope lays completely inside this envelope
            if ( other.contains( this ) ) {
                return true;
            }

            // left or right border of the passed envelope lays inside the y-band of this envelope
            if ( ( minX2 >= minX1 && minX2 <= maxX1 ) || ( maxX2 <= maxX1 && maxX2 >= minX1 ) ) {
                if ( minY2 <= maxY1 && maxY2 >= minY1 ) {
                    return true;
                }
            }

            // top or bottom border of the passed envelope lays inside the x-band of this envelope
            if ( ( minY2 >= minY1 && minY2 <= maxY1 ) || ( maxY2 <= maxY1 && maxY2 >= minY1 ) ) {
                if ( minX2 <= maxX1 && maxX2 >= minX1 ) {
                    return true;
                }
            }

            return false;
        }
        case PRIMITIVE_GEOMETRY: {
            GeometricPrimitive primitive = (GeometricPrimitive) geometry;
            switch ( primitive.getPrimitiveType() ) {
            case Point: {
                Point point = (Point) primitive;
                double px = point.getX();
                double py = point.getY();
                double minX1 = this.getMin().getX();
                double minY1 = this.getMin().getY();
                double maxX1 = this.getMax().getX();
                double maxY1 = this.getMax().getY();
                return px >= minX1 && px <= maxX1 && py >= minY1 && py <= maxY1;
            }
            }
        }
        }

        return getJTSGeometry().intersects( geometry.getJTSGeometry() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.geometry.standard.AbstractDefaultGeometry#contains(org.deegree.geometry.Geometry)
     */
    @Override
    public boolean contains( Geometry geometry ) {
        switch ( geometry.getGeometryType() ) {
        case ENVELOPE: {
            Envelope other = (Envelope) geometry;

            double minX1 = this.getMin().getX();
            double minY1 = this.getMin().getY();
            double maxX1 = this.getMax().getX();
            double maxY1 = this.getMax().getY();

            double minX2 = other.getMin().getX();
            double minY2 = other.getMin().getY();
            double maxX2 = other.getMax().getX();
            double maxY2 = other.getMax().getY();

            if ( minX2 >= minX1 && maxX2 <= maxX1 && minY2 >= minY1 && maxY2 <= maxY1 ) {
                return true;
            }

            return false;
        }
        case PRIMITIVE_GEOMETRY: {
            GeometricPrimitive primitive = (GeometricPrimitive) geometry;
            switch ( primitive.getPrimitiveType() ) {
            case Point: {
                Point point = (Point) primitive;
                double px = point.getX();
                double py = point.getY();
                double minX1 = this.getMin().getX();
                double minY1 = this.getMin().getY();
                double maxX1 = this.getMax().getX();
                double maxY1 = this.getMax().getY();
                return px >= minX1 && px <= maxX1 && py >= minY1 && py <= maxY1;
            }
            }
        }
        }
        return getJTSGeometry().contains( geometry.getJTSGeometry() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.geometry.standard.AbstractDefaultGeometry#equals(org.deegree.geometry.Geometry)
     */
    @Override
    public boolean equals( Geometry geometry ) {
        switch ( geometry.getGeometryType() ) {
        case ENVELOPE: {
            Envelope other = (Envelope) geometry;
            if ( this.getMin().getX() == other.getMin().getX() && this.getMin().getY() == other.getMin().getY()
                 && this.getMax().getX() == other.getMax().getX() && this.getMax().getY() == other.getMax().getY() ) {
                return true;
            }
            return false;
        }
        }
        return getJTSGeometry().equals( geometry.getJTSGeometry() );
    }

    @Override
    public Envelope merge( Envelope other ) {
        int coordinateDimension = getCoordinateDimension();
        double[] min = new double[coordinateDimension];
        double[] max = new double[coordinateDimension];
        for ( int i = 0; i < coordinateDimension; i++ ) {
            if ( this.min.getAsArray()[i] < other.getMin().getAsArray()[i] ) {
                min[i] = this.min.getAsArray()[i];
            } else {
                min[i] = other.getMin().getAsArray()[i];
            }
            if ( this.max.getAsArray()[i] > other.getMax().getAsArray()[i] ) {
                max[i] = this.max.getAsArray()[i];
            } else {
                max[i] = other.getMax().getAsArray()[i];
            }
        }
        Point newMin = new DefaultPoint( null, getCoordinateSystem(), pm, min );
        Point newMax = new DefaultPoint( null, getCoordinateSystem(), pm, max );
        return new DefaultEnvelope( null, getCoordinateSystem(), pm, newMin, newMax );
    }

    @Override
    public Envelope getEnvelope() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.geometry.Envelope#getCentroid()
     */
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
    protected com.vividsolutions.jts.geom.Polygon buildJTSGeometry() {
        Points points = new PackedPoints( new double[] { min.getX(), min.getY(), max.getX(), min.getY(), max.getX(),
                                                        max.getY(), min.getX(), max.getY(), min.getX(), min.getY() }, 2 );
        LinearRing shell = jtsFactory.createLinearRing( ( points ) );
        return jtsFactory.createPolygon( shell, null );
    }

    @Override
    public String toString() {
        return "min: " + min + ", max: " + max + ", crs: " + crs;
    }
}
