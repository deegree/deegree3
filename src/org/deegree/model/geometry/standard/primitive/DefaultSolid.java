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
package org.deegree.model.geometry.standard.primitive;

import java.util.List;

import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.primitive.Solid;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link Solid}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class DefaultSolid extends AbstractDefaultGeometry implements Solid {

    private Surface exteriorSurface;

    private List<Surface> interiorSurfaces;

    /**
     * Creates a new {@link DefaultSolid} instance from the given parameters.
     * 
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            solids coordinate reference system. If a point does not have a CRS or it is not known
     *            {@link CRSFactory#createDummyCRS(String)} shall be used instead of <code>null</code>
     * @param exteriorSurface
     *            the exterior surface (shell) of the solid, may be null
     * @param interiorSurfaces
     *            the interior surfaces of the solid, may be null or empty
     */
    public DefaultSolid (String id, CoordinateSystem crs, Surface exteriorSurface, List<Surface> interiorSurfaces) {
        super (id, crs);
        this.exteriorSurface = exteriorSurface;
        this.interiorSurfaces = interiorSurfaces;
    }    

    @Override
    public Surface getExteriorSurface() {
        return exteriorSurface;
    }

    @Override
    public List<Surface> getInteriorSurfaces() {
        return interiorSurfaces;
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.Solid;
    }

    @Override
    public SolidType getSolidType() {
        return SolidType.Solid;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PRIMITIVE_GEOMETRY;
    }

    @Override
    public double getArea() {
       throw new UnsupportedOperationException();
    }

    @Override
    public double getVolume() {
        throw new UnsupportedOperationException();
    }    
}
