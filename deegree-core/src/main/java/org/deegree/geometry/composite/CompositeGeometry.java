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
package org.deegree.geometry.composite;

import java.util.List;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.GeometricPrimitive;

/**
 * A <code>CompositeGeometry</code> is a geometric complex with underlying core geometries that are (as a whole)
 * isomorphic to a geometry primitive. E.g., a composite curve is a collection of curves whose geometry interface could
 * be satisfied by a single curve (albeit a much more complex one). Composites are intended for use as attribute values
 * in datasets in which the underlying geometry has been decomposed, usually to expose its topological nature.
 * 
 * @param <T>
 *            type of the composited geometries
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface CompositeGeometry<T extends GeometricPrimitive> extends Geometry, List<T> {

    /**
     * Returns {@link Geometry.GeometryType#COMPOSITE_GEOMETRY}.
     * 
     * @return {@link Geometry.GeometryType#COMPOSITE_GEOMETRY}
     */
    @Override
    public GeometryType getGeometryType();
}
