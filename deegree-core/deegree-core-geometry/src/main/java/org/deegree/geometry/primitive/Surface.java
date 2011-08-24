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
package org.deegree.geometry.primitive;

import java.util.List;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.patches.Triangle;

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
        /** Surface that consists of {@link Triangle}s only. */
        TriangulatedSurface,
        /** Surface that consists of {@link Triangle}s only (which meet the Delaunay criterion). */
        Tin,
        /** Surface composited from multiple members surfaces. */
        CompositeSurface,
        /** Surface that wraps a base surface with additional orientation flag. */
        OrientableSurface,
    }

    /**
     * Must always return {@link GeometricPrimitive.PrimitiveType#Surface}.
     * 
     * @return {@link GeometricPrimitive.PrimitiveType#Surface}
     */
    @Override
    public PrimitiveType getPrimitiveType();

    /**
     * Returns the type of surface.
     * 
     * @return the type of surface
     */
    public SurfaceType getSurfaceType();

    /**
     * 
     * @param requestedBaseUnit
     * @return area of the surface
     */
    public Measure getArea( Unit requestedBaseUnit );

    /**
     * 
     * @param requestedUnit
     * @return perimeter of the surface
     */
    public Measure getPerimeter( Unit requestedUnit );

    /**
     * Returns the patches that constitute this surface.
     * 
     * @return the patches that constitute this surface
     */
    public List<? extends SurfacePatch> getPatches();

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
    public Points getExteriorRingCoordinates();

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
    public List<Points> getInteriorRingsCoordinates();
}
