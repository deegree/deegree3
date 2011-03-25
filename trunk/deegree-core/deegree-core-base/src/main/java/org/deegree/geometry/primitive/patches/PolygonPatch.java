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
package org.deegree.geometry.primitive.patches;

import java.util.List;

import org.deegree.geometry.primitive.Ring;

/**
 * A {@link PolygonPatch} is a planar {@link SurfacePatch} that is defined by a set of boundary curves and an underlying
 * surface to which these curves adhere. The curves are coplanar and the polygon uses planar interpolation in its
 * interior. Implements <code>GM_Polygon</code> of ISO 19107.
 * <p>
 * Please note that a {@link PolygonPatch} is not restricted to use linear interpolation for its exterior and interior
 * rings.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author: markusschneider $
 * 
 * @version $Revision: 33706 $, $Date: 2009-08-07 00:49:16 +0200 (Fr, 07 Aug 2009) $
 */
public interface PolygonPatch extends SurfacePatch {

    /**
     * Simple enum defining the different possible {@link PolygonPatch} instances.
     */
    public enum PolygonPatchType {
        /** Patch is a generic {@link PolygonPatch}. */
        POLYGON_PATCH,
        /** Patch is a {@link Rectangle}. Which does not have interior rings. */
        RECTANGLE,
        /** Patch is a {@link Triangle}. Which does not have interior rings. */
        TRIANGLE
    }

    /**
     * Returns the boundary rings (interior + exteriors)
     * 
     * @return the boundary rings, list may be empty (but not null)
     */
    public List<? extends Ring> getBoundaryRings();

    /**
     * Returns the exterior ring of the patch.
     * <p>
     * Please note that the exterior may be empty (null). The following explanation is from the GML 3.1.1 spec (section
     * 9.2.2.5): In the normal 2D case, one of these rings is distinguished as being the exterior boundary. In a general
     * manifold this is not always possible, in which case all boundaries shall be listed as interior boundaries, and
     * the exterior will be empty.
     * 
     * @return the exterior ring, or null
     */
    public Ring getExteriorRing();

    /**
     * Returns the interior rings (holes) of the patch.
     * 
     * @return the interior rings (holes) of the patch, list may be empty (but not null)
     */
    public List<Ring> getInteriorRings();

    /**
     * @return the type of this polygon patch.
     */
    public PolygonPatchType getPolygonPatchType();
}
