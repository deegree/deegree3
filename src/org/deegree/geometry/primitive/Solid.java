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
     * Must always return {@link GeometricPrimitive.PrimitiveType#Solid}.
     * 
     * @return {@link GeometricPrimitive.PrimitiveType#Solid}
     */
    @Override
    public PrimitiveType getPrimitiveType();

    /**
     * Returns the type of solid.
     * 
     * @return the type of solid
     */
    public SolidType getSolidType();

    /**
     * 
     * @param requestedBaseUnit 
     * @return volume of the solid
     */
    public Measure getVolume( Unit requestedBaseUnit );

    /**
     * 
     * @param requestedBaseUnit 
     * @return area of the solid's boundary
     */
    public Measure getArea( Unit requestedBaseUnit );

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
