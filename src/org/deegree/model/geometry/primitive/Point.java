//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
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

import org.deegree.model.geometry.Geometry;

/**
 * 0-dimensional primitive.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface Point extends GeometricPrimitive {

    /**
     * Must always return {@link GeometricPrimitive.PrimitiveType#Point}.
     * 
     * @return {@link GeometricPrimitive.PrimitiveType#Point}
     */
    @Override
    public PrimitiveType getPrimitiveType();

    /**
     * Must either return {@link Geometry.GeometryType#PRIMITIVE_GEOMETRY}.
     * 
     * @return {@link Geometry.GeometryType#PRIMITIVE_GEOMETRY}
     */
    @Override
    public GeometryType getGeometryType();

    /**
     * 
     * @return x coordinate of a point
     */
    public double getX();

    /**
     * 
     * @return y coordinate of a point
     */
    public double getY();

    /**
     * 
     * @return z coordinate of a point
     */
    public double getZ();

    /**
     * 
     * @param dimension
     * @return coordinate of passed dimension. If passed dimension is not supported by a point Double.NAN will be
     *         returned
     */
    public double get( int dimension );

    /**
     * 
     * @return a points coordinates as an array
     */
    public double[] getAsArray();

}