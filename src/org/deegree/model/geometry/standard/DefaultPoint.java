//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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
package org.deegree.model.geometry.standard;

import java.util.Arrays;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.Envelope;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.primitive.Point;

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
        return coordinates.length;
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
        return coordinates[2];
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public double getPrecision() {
        throw new UnsupportedOperationException();
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
}
