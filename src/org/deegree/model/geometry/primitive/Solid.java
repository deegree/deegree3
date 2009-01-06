//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

/**
 * <code>Solid</code> instances are 3D-geometries that ...
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface Solid extends GeometricPrimitive {

    /**
     * Convenience enum type for discriminating the different solid variants.
     */
    public enum SolidType {
        /** Generic solid that consists of an arbitrary number of */
        Solid,
        /** Solid composited from multiple members solids. */
        CompositeSolid
    }

    /**
     * Returns the type of solid.
     * 
     * @return the type of solid
     */
    public SolidType getSolidType();

    /**
     * Must always return {@link GeometricPrimitive.PrimitiveType#Solid}.
     * 
     * @return {@link GeometricPrimitive.PrimitiveType#Solid}
     */
    @Override
    public PrimitiveType getPrimitiveType();

    /**
     * 
     * @return volume of a Solid measured in units of the assigned {@link CoordinateSystem}
     */
    public double getVolume();

    /**
     * 
     * @return area of a Solids boundary measured in units of the assigend {@link CoordinateSystem}
     */
    public double getArea();

    /**
     * Returns the exterior surface (shell) of the solid.
     * <p>
     * Please note that this method may return null. The following explanation is from the GML 3.1.1 schema
     * (geometryPrimitives.xsd): In normal 3-dimensional Euclidean space, one (composite) surface is distinguished as
     * the exterior. In the more general case, this is not always possible.
     * 
     * @return the exterior surface, or null if no surface is distinguished as being the exterior boundary
     */
    public Surface getExteriorSurface();

    /**
     * Returns the interior surfaces of the solid.
     * 
     * @return the interior surfaces, list may be empty (but not null)
     */
    public List<Surface> getInteriorSurfaces();
}