//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/rendering/Java2DRenderer.java $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
package org.deegree.geometry.standard.primitive;

import java.util.List;
import java.util.UUID;

import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryFactoryCreator;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link Envelope}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultEnvelope extends AbstractDefaultGeometry implements Envelope {

    private static GeometryFactory geomFactory = GeometryFactoryCreator.getInstance().getGeometryFactory();

    private static double DELTA = 0.001;

    private Point max;

    private Point min;

    private Point centroid;

    /**
     * Creates a new <code>DefaultEnvelope</code> instance from the given parameters.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param min
     * @param max
     */
    public DefaultEnvelope( String id, CRS crs, Point min, Point max ) {
        super( id, crs );
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean is3D() {
        return min.is3D();
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
                                                     new double[] { newMaxX, newMaxY }, DELTA, null );
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

            // special case: the passed envelope lays completly inside this envelope
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
            case Curve: {
                // CAUTION: incorrect hack to move forward in WMS!
                double minx = min.getX();
                double miny = min.getY();
                double maxx = max.getX();
                double maxy = max.getY();

                Curve c = (Curve) primitive;

                for ( Point pt : c.getControlPoints() ) {
                    if ( minx <= pt.getX() && pt.getX() <= maxx && miny <= pt.getY() && pt.getY() <= maxy ) {
                        return true;
                    }
                }

                return false;
            }
            case Surface: {
                // CAUTION: incorrect hack to move forward in WMS!
                if ( primitive instanceof Polygon ) {
                    double minx = min.getX();
                    double miny = min.getY();
                    double maxx = max.getX();
                    double maxy = max.getY();

                    Polygon p = (Polygon) primitive;
                    for ( Point pt : p.getExteriorRingCoordinates() ) {
                        if ( minx <= pt.getX() && pt.getX() <= maxx && miny <= pt.getY() && pt.getY() <= maxy ) {
                            return true;
                        }
                    }

                    for ( List<Point> list : p.getInteriorRingsCoordinates() ) {
                        for ( Point pt : list ) {
                            if ( minx <= pt.getX() && pt.getX() <= maxx && miny <= pt.getY() && pt.getY() <= maxy ) {
                                return true;
                            }
                        }
                    }

                    return false;
                }
            }
            default: {
                throw new UnsupportedOperationException( "Intersects not implemented for Envelope/"
                                                         + primitive.getPrimitiveType().name() );
            }
            }
        }
        default: {
            throw new UnsupportedOperationException( "Intersects not implemented for Envelope/"
                                                     + geometry.getGeometryType().name() );
        }
        }
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
        default: {
            throw new UnsupportedOperationException();
        }
        }
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
        default: {
            throw new UnsupportedOperationException();
        }
        }
    }

    @Override
    public Envelope merge( Envelope other ) {
        int coordinateDimension = ( max.is3D() ? 3 : 2 );
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
        Point newMin = new DefaultPoint( null, getCoordinateSystem(), min );
        Point newMax = new DefaultPoint( null, getCoordinateSystem(), max );
        return new DefaultEnvelope( null, getCoordinateSystem(), newMin, newMax );
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
            GeometryFactory gf = GeometryFactoryCreator.getInstance().getGeometryFactory();
            double[] coordinates = new double[max.getAsArray().length];
            for ( int i = 0; i < coordinates.length; i++ ) {
                coordinates[i] = min.getAsArray()[i] + ( max.getAsArray()[i] - min.getAsArray()[i] ) / 2d;
            }
            centroid = gf.createPoint( UUID.randomUUID().toString(), coordinates, getCoordinateSystem() );
        }
        return centroid;
    }

    @Override
    public String toString() {
        return "min: " + min + ", max: " + max + ", crs: " + crs;
    }
}
