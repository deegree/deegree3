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
package org.deegree.geometry.multi;

import java.util.List;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.primitive.Point;

/**
 * Basic aggregation type for {@link Geometry} objects.
 * <p>
 * In contrast to a {@link CompositeGeometry}, a <code>MultiGeometry</code> has no constraints on the topological
 * relations between the contained geometries, i.e. their interiors may intersect.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version. $Revision$, $Date$
 *
 * @param <T>
 *            the type of the contained geometries
 */
public interface MultiGeometry<T extends Geometry> extends Geometry, List<T> {

    public enum MultiGeometryType {
        MULTI_GEOMETRY,
        MULTI_POINT,
        MULTI_CURVE,
        MULTI_LINE_STRING,
        MULTI_SURFACE,
        MULTI_POLYGON,
        MULTI_SOLID
    }

    /**
     * Must always return {@link Geometry.GeometryType#MULTI_GEOMETRY}.
     *
     * @return {@link Geometry.GeometryType#MULTI_GEOMETRY}.
     */
    @Override
    public GeometryType getGeometryType();

    /**
     * @return the type of MultiGeometry, see {@link MultiGeometryType}
     */
    public MultiGeometryType getMultiGeometryType();

    /**
     * Returns the centroid of the contained geometries.
     *
     * @return the centroid
     */
    public Point getCentroid();

}
