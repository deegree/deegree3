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
package org.deegree.model.geometry.composite;

import java.util.List;

import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.primitive.GeometricPrimitive;

/**
 * A <code>CompositeGeometry</code> is a geometric complex with an underlying core geometry that is isomorphic to a
 * primitive. Thus, a composite curve is a collection of curves whose geometry interface could be satisfied by a single
 * curve (albeit a much more complex one). Composites are intended for use as attribute values in datasets in which the
 * underlying geometry has been decomposed, usually to expose its topological nature.
 * 
 * @param <T>
 *            type of the composited geometries
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface CompositeGeometry<T extends GeometricPrimitive> extends Geometry, List<T> {

    /**
     * Must either return {@link Geometry.GeometryType#COMPOSITE_GEOMETRY} or
     * {@link Geometry.GeometryType#COMPOSITE_PRIMITIVE}.
     * 
     * @return either {@link Geometry.GeometryType#COMPOSITE_GEOMETRY} or
     *         {@link Geometry.GeometryType#COMPOSITE_PRIMITIVE}
     */
    @Override
    public GeometryType getGeometryType();
}