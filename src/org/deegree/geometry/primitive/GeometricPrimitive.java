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

import org.deegree.geometry.Geometry;

/**
 * A {@link GeometricPrimitive} is a contigous geometry with single dimensionality.
 * <p>
 * For every dimensionality, a specialized interface exists:
 * <ul>
 * <li>0D: {@link Point}</li>
 * <li>1D: {@link Curve}</li>
 * <li>2D: {@link Surface}</li>
 * <li>3D: {@link Solid}</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface GeometricPrimitive extends Geometry {

    /**
     * Convenience enum type for discriminating the different primitive variants.
     */
    public enum PrimitiveType {
        /** 0-dimensional primitive */
        Point,
        /** 1-dimensional primitive */
        Curve,
        /** 2-dimensional primitive */
        Surface,
        /** 3-dimensional primitive */
        Solid
    }

    /**
     * Must always return {@link Geometry.GeometryType#PRIMITIVE_GEOMETRY}.
     * 
     * @return must always return {@link Geometry.GeometryType#PRIMITIVE_GEOMETRY}
     */
    @Override
    public GeometryType getGeometryType();

    /**
     * Returns the type of primitive.
     * 
     * @return the type of primitive
     */
    public PrimitiveType getPrimitiveType();
}
