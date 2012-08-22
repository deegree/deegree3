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

/**
 *
 * Defining the boundary of a surface. The surface boundary is defined as ring surrounding the exterior boundary of the
 * surface and the rings surrounding each interior ring of the surface.
 * <p>
 * A SurfaceBoundary consists of some number of Rings, corresponding to the various components of its boundary. In the
 * normal 2D case, one of these rings is distinguished as being the exterior boundary. In a general manifold this is not
 * always possible, in which case all boundaries shall be listed as interior boundaries, and the exterior will be empty.
 *
 * <p>
 * -----------------------------------------------------
 * </p>
 *
 * @author Axel Schaefer
 * @version $Revision$ $Date$
 *          <p>
 */

public interface SurfaceBoundary extends PrimitiveBoundary {

    /**
     * get the exterior ring
     *
     * @return the exterior ring
     */
    public Ring getExteriorRing();

    /**
     * gets the interior ring(s)
     *
     * @return the interior ring(s)
     */
    public Ring[] getInteriorRings();

}
