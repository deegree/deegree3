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
package org.deegree.model.geometry.standard.primitive;

import java.util.List;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.TriangulatedSurface;
import org.deegree.model.geometry.primitive.surfacepatches.SurfacePatch;
import org.deegree.model.geometry.primitive.surfacepatches.Triangle;
import org.deegree.model.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link TriangulatedSurface}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class DefaultTriangulatedSurface extends AbstractDefaultGeometry implements TriangulatedSurface {

    private List<?> patches;

    /**
     * Creates a new {@link DefaultTriangulatedSurface} instance from the given parameters.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param patches
     *            patches that constitute the surface
     */
    public DefaultTriangulatedSurface (String id, CoordinateSystem crs, List<Triangle> patches) {
        super (id, crs);
        this.patches = patches;
    }
    
    @Override
    public double getArea() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point getCentroid() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SurfacePatch> getPatches() {        
        return (List<SurfacePatch>) patches;
    }

    @Override
    public double getPerimeter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.Surface;
    }

    @Override
    public SurfaceType getSurfaceType() {
        return SurfaceType.TriangulatedSurface;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PRIMITIVE_GEOMETRY;
    }

    @Override
    public List<Point> getExteriorRingCoordinates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<List<Point>> getInteriorRingsCoordinates() {
        throw new UnsupportedOperationException();
    }
}
