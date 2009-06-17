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
