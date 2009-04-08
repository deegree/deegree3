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
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.composite.CompositeSolid;
import org.deegree.geometry.composite.CompositeSurface;

/**
 * A {@link GeometricPrimitive} is a contigous geometries with single dimensionality.
 * <p>
 * For every dimensionality, a specialized interface exists:
 * <ul>
 * <li>0D: {@link Point}</li>
 * <li>1D: {@link Curve}</li>
 * <li>2D: {@link Surface}</li>
 * <li>3D: {@link Solid}</li>
 * </ul>
 * </p>
 * Please note that the specializations of {@link CompositeGeometry} extend this interface as well:
 * <ul>
 * <li>1D: {@link CompositeCurve}</li>
 * <li>2D: {@link CompositeSurface}</li>
 * <li>3D: {@link CompositeSolid}</li>
 * </ul>
 * This is due to the fact that these types imply a primitive semantic, e.g. a {@link CompositeCurve} is* a
 * {@link Curve}, because it defines a sequence of curve segments.
 * <p>
 * However, the generic {@link CompositeGeometry} is *not* a primitive, because it allows the composition of primitives
 * with different dimensionality.
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
     * Returns the type of primitive.
     * 
     * @return the type of primitive
     */
    public PrimitiveType getPrimitiveType();

    /**
     * Must either return {@link Geometry.GeometryType#PRIMITIVE_GEOMETRY} or
     * {@link Geometry.GeometryType#COMPOSITE_PRIMITIVE}.
     * 
     * @return either {@link Geometry.GeometryType#PRIMITIVE_GEOMETRY} or
     *         {@link Geometry.GeometryType#COMPOSITE_PRIMITIVE}
     */
    @Override
    public GeometryType getGeometryType();     
}
