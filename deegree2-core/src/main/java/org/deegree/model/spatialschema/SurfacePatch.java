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

package org.deegree.model.spatialschema;

import org.deegree.model.crs.CoordinateSystem;

/**
 *
 * Defining the iso geometry <code>SurfacePatch</code> which is used for building surfaces. A surface patch is made of
 * one exterior ring and 0..n interior rings. By definition there can't be a surface patch with no exterior ring. A
 * polygon is a specialized surface patch.
 *
 * -----------------------------------------------------
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */

public interface SurfacePatch extends GenericSurface {

    /**
     * The interpolation determines the surface interpolation mechanism used for this SurfacePatch. This mechanism uses
     * the control points and control parameters defined in the various subclasses to determine the position of this
     * SurfacePatch.
     *
     * @return the surface interpolation of this surface
     */
    SurfaceInterpolation getInterpolation();

    /**
     * @return the exterior ring of a surfacePatch
     */
    Position[] getExteriorRing();

    /**
     * @return the interior rings of a surfacePatch
     */
    Position[][] getInteriorRings();

    /**
     *
     * @return the exterior ring of a surfacePatch
     */
    Ring getExterior();

    /**
     *
     * @return the interior rings of a surfacePatch
     */
    Ring[] getInterior();

    /**
     * @return the coordinate system of the surface patch
     */
    CoordinateSystem getCoordinateSystem();

    /**
     * The Boolean valued operation "intersects" shall return TRUE if this surfacepatch intersects another Geometry.
     * Within a Complex, the Primitives do not intersect one another. In general, topologically structured data uses
     * shared geometric objects to capture intersection information.
     *
     * @param gmo
     * @return true if this surfacepatch intersects with given Geometry
     */
    boolean intersects( Geometry gmo );

    /**
     * The Boolean valued operation "contains" shall return TRUE if this Geometry contains another Geometry.
     *
     * @param gmo
     * @return true if this surfacepatch contains given Geometry
     */
    boolean contains( Geometry gmo );

    /**
     * The operation "centroid" shall return the mathematical centroid for this surfacepatch. The result is not
     * guaranteed to be on the object.
     *
     * @return the centroid
     */
    public Point getCentroid();

    /**
     * The operation "area" shall return the area of this GenericSurface. The area of a 2 dimensional geometric object
     * shall be a numeric measure of its surface area Since area is an accumulation (integral) of the product of two
     * distances, its return value shall be in a unit of measure appropriate for measuring distances squared.
     */
    public double getArea();

}
