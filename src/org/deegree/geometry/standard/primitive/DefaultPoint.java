//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
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

import java.util.Arrays;

import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link Point}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultPoint extends AbstractDefaultGeometry implements Point {

    private double[] coordinates;

    /**
     * Creates a new <code>DefaultPoint</code> instance from the given parameters.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param coordinates
     *            coordinates of the point
     */
    public DefaultPoint( String id, CoordinateSystem crs, double[] coordinates ) {
        super( id, crs );
        this.coordinates = Arrays.copyOf( coordinates, coordinates.length );
    }

    @Override
    public double get( int dimension ) {
        if ( coordinates.length > dimension && dimension >= 0 ) {
            return coordinates[dimension];
        }
        throw new IndexOutOfBoundsException( "Dimemsion not inside coordinate array." );
    }

    @Override
    public double[] getAsArray() {
        return coordinates;
    }

    @Override
    public double getX() {
        return coordinates[0];
    }

    @Override
    public double getY() {
        return coordinates[1];
    }

    @Override
    public double getZ() {
        if ( coordinates.length > 2 ) {
            return coordinates[2];
        }
        return Double.NaN;
    }

    @Override
    public boolean contains( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Geometry difference( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double distance( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals( Geometry geometry ) {
        if ( !( geometry instanceof Point ) ) {
            return false;
        }
        double[] coordinates = ( (Point) geometry ).getAsArray();
        if ( coordinates.length != this.coordinates.length ) {
            return false;
        }
        for ( int i = 0; i < coordinates.length; i++ ) {
            if ( !( coordinates[i] == this.coordinates[i] ) ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Geometry getBuffer( double distance ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Geometry getConvexHull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCoordinateDimension() {
        return coordinates.length;
    }

    @Override
    public Envelope getEnvelope() {
        return new DefaultEnvelope( null, crs, this, this );
    }

    @Override
    public Geometry intersection( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean intersects( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBeyond( Geometry geometry, double distance ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWithin( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWithinDistance( Geometry geometry, double distance ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Geometry union( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.Point;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PRIMITIVE_GEOMETRY;
    }

    @Override
    public String toString() {
        String s = "(" + coordinates[0];
        for ( int i = 1; i < coordinates.length; i++ ) {
            s += "," + coordinates[i];
        }
        s += ")";
        return s;
    }
}
