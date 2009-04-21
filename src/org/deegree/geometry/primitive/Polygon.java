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
package org.deegree.geometry.primitive;

import java.util.List;

import org.deegree.geometry.primitive.surfacepatches.PolygonPatch;

/**
 * A <code>Polygon</code> is a {@link Surface} that consists of one single planar patch ({@link PolygonPatch}).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface Polygon extends Surface {

    /**
     * Must always return {@link Surface.SurfaceType#Polygon}.
     * 
     * @return {@link Surface.SurfaceType#Polygon}
     */
    @Override
    public SurfaceType getSurfaceType();

    /**
     * Returns the exterior ring of the polygon.
     * <p>
     * Please note that this method may return null. The following explanation is from the GML 3.1.1 spec (section
     * 9.2.2.5): In the normal 2D case, one of these rings is distinguished as being the exterior boundary. In a general
     * manifold this is not always possible, in which case all boundaries shall be listed as interior boundaries, and
     * the exterior will be empty.
     * 
     * @return the exterior ring, or null if no ring is distinguished as being the exterior boundary
     */
    public Ring getExteriorRing();

    /**
     * Returns the interior rings (holes) of the polygon.
     * 
     * @return the interior rings (holes) of the polygon, list may be empty (but not null)
     */
    public List<Ring> getInteriorRings();
    
    @Override
    public List<PolygonPatch> getPatches();    
}
