//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
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
package org.deegree.model.geometry.primitive;

import java.util.List;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.model.geometry.primitive.surfacepatches.SurfacePatch;
import org.deegree.model.geometry.primitive.surfacepatches.Triangle;

/**
 * <code>Surface</code> instances are 2D-geometries that consist of an arbitrary number of surface patches which are not
 * necessarily planar.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface Surface extends GeometricPrimitive {

    /**
     * Convenience enum type for discriminating the different surface variants.
     */
    public enum SurfaceType {
        /** Generic surface that consists of an arbitrary number of surface patches which are not necessarily planar. */
        Surface,
        /** Surface that consists of a single planar surface patch ({@link PolygonPatch}). */
        Polygon,
        /** Surface that consists of (planar) {@link PolygonPatch}es only. */
        PolyhedralSurface,
        /** Surface that consists of (planar) {@link Triangle}s only. */
        TriangulatedSurface, Tin,
        /** Surface composited from multiple members surfaces. */
        CompositeSurface,
        /** Surface that wraps a base surface with additional orientation flag. */
        OrientableSurface,
    }

    /**
     * Returns the type of surface.
     * 
     * @return the type of surface
     */
    public SurfaceType getSurfaceType();

    /**
     * Must always return {@link GeometricPrimitive.PrimitiveType#Surface}.
     * 
     * @return {@link GeometricPrimitive.PrimitiveType#Surface}
     */
    @Override
    public PrimitiveType getPrimitiveType();

    /**
     * 
     * @return area of a Surface measured in units of the assigned {@link CoordinateSystem}
     */
    public double getArea();

    /**
     * 
     * @return perimeter of a Surface measured in units of the assigned {@link CoordinateSystem}
     */
    public double getPerimeter();

    /**
     * 
     * @return centroid of a Surface
     */
    public Point getCentroid();

    /**
     * Returns the patches that constitute this surface.
     * 
     * @return the patches that constitute this surface
     */
    public List<SurfacePatch> getPatches();

    /**
     * Convenience method for accessing the control points of the exterior ring of a simple polygon surface.
     * <p>
     * NOTE: This method is only safe to use when the surface consists of a single planar patch that has a linear
     * interpolated exterior ring.
     * </p>
     * 
     * @return the control points
     * @throws IllegalArgumentException
     *             if the surface has more than one patch, the patch is not planar or the exterior boundary is not
     *             completely described by linear interpolated segments
     */
    public List<Point> getExteriorRingCoordinates();

    /**
     * Convenience method for accessing the control points of the interior rings of a simple polygon surface.
     * <p>
     * NOTE: This method is only safe to use when the surface consists of a single planar patch that has linear
     * interpolated interior rings.
     * </p>
     * 
     * @return the control points
     * @throws IllegalArgumentException
     *             if the surface has more than one patch, the patch is not planar or the interior boundaries are not
     *             completely described by linear interpolated segments
     */
    public List<List<Point>> getInteriorRingsCoordinates();    
}