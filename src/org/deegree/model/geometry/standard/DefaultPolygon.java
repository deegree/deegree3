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

import java.util.ArrayList;
import java.util.List;

import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.primitive.Polygon;
import org.deegree.model.geometry.primitive.Ring;
import org.deegree.model.geometry.primitive.Surface.SurfaceType;
import org.deegree.model.geometry.primitive.surfacepatches.SurfacePatch;
import org.deegree.model.geometry.standard.surfacepatches.DefaultPolygonPatch;

/**
 * Default implementation of {@link Polygon}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class DefaultPolygon extends DefaultSurface implements Polygon {

    private Ring exteriorRing;

    private List<Ring> interiorRings;

    /**
     * Creates a new {@link DefaultPolygon} instance from the given parameters.
     * 
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system. If the polygon does not have a CRS or it is not known
     *            {@link CRSFactory#createDummyCRS(String)} shall be used instead of <code>null</code>
     * @param exteriorRing
     *            ring that defines the outer boundary, may be null (see section 9.2.2.5 of GML spec)
     * @param interiorRings
     *            list of rings that define the inner boundaries, may be empty or null
     */
    public DefaultPolygon (String id, CoordinateSystem crs, Ring exteriorRing, List<Ring> interiorRings) {
        super (id, crs, createPatchList( exteriorRing, interiorRings ));
        this.exteriorRing = exteriorRing;
        this.interiorRings = interiorRings;
    }
    
    private static List<SurfacePatch> createPatchList(Ring exteriorRing, List<Ring> interiorRings) {
        List<SurfacePatch> patches = new ArrayList<SurfacePatch>(1);
        patches.add (new DefaultPolygonPatch(exteriorRing, interiorRings));
        return patches;
    }

    @Override
    public Ring getExteriorRing() {
        return exteriorRing;
    }

    @Override
    public List<Ring> getInteriorRings() {
        return interiorRings;
    }

    @Override
    public SurfaceType getSurfaceType() {
        return SurfaceType.Polygon;
    }    
}
