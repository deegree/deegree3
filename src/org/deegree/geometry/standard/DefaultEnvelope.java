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
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
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

    private Point max;

    private Point min;

    private Point centroid;

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
    protected com.vividsolutions.jts.geom.Polygon buildJTSGeometry() {
        Points points = new PackedPoints( new double[] { min.get0(), min.get1(), max.get0(), min.get1(), max.get0(),
                                                        max.get1(), min.get0(), max.get1(), min.get0(), min.get1() }, 2 );
        LinearRing shell = jtsFactory.createLinearRing( ( points ) );
        return jtsFactory.createPolygon( shell, null );
    }

    @Override
    public String toString() {
        return "min: " + min + ", max: " + max + ", crs: " + crs;
    }
}
