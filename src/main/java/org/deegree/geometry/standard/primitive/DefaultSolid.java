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
package org.deegree.geometry.standard.primitive;

import java.util.List;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.cs.CRS;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

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
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param exteriorSurface
     *            the exterior surface (shell) of the solid, may be null
     * @param interiorSurfaces
     *            the interior surfaces of the solid, may be null or empty
     */
    public DefaultSolid( String id, CRS crs, PrecisionModel pm, Surface exteriorSurface, List<Surface> interiorSurfaces ) {
        super( id, crs, pm );
        this.exteriorSurface = exteriorSurface;
        this.interiorSurfaces = interiorSurfaces;
    }

    @Override
    public int getCoordinateDimension() {
        return exteriorSurface.getCoordinateDimension();
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
    public Measure getArea( Unit requestedBaseUnit ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Measure getVolume( Unit requestedBaseUnit ) {
        throw new UnsupportedOperationException();
    }
}
