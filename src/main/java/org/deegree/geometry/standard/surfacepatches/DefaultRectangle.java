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
package org.deegree.geometry.standard.surfacepatches;

import java.util.Collections;
import java.util.List;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.patches.Rectangle;

/**
 * Default implementation of {@link Rectangle}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultRectangle implements Rectangle {

    private LinearRing exterior;

    /**
     * Creates a new {@link DefaultRectangle} instance from the given parameters.
     * 
     * @param exterior
     *            ring that contains exactly five planar points, the first and last point must be identical
     */
    public DefaultRectangle( LinearRing exterior ) {
        if ( exterior.getControlPoints().size() != 5 ) {
            String msg = "The exterior ring of a rectangle must contain exactly five points.";
            throw new IllegalArgumentException( msg );
        }
        this.exterior = exterior;
    }

    @Override
    public LinearRing getExteriorRing() {
        return exterior;
    }

    @Override
    public Point getPoint1() {
        return exterior.getControlPoints().get( 0 );
    }

    @Override
    public Point getPoint2() {
        return exterior.getControlPoints().get( 1 );
    }

    @Override
    public Point getPoint3() {
        return exterior.getControlPoints().get( 2 );
    }

    @Override
    public Point getPoint4() {
        return exterior.getControlPoints().get( 3 );
    }

    @Override
    public Measure getArea( Unit requestedBaseUnit ) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCoordinateDimension() {
        return getPoint1().getCoordinateDimension();
    }

    @Override
    public List<Ring> getInteriorRings() {
        return Collections.emptyList();
    }

    @Override
    public List<LinearRing> getBoundaryRings() {
        return Collections.singletonList( exterior );
    }

    @Override
    public SurfacePatchType getSurfacePatchType() {
        return SurfacePatchType.POLYGON_PATCH;
    }

    @Override
    public PolygonPatchType getPolygonPatchType() {
        return PolygonPatchType.RECTANGLE;
    }
}
