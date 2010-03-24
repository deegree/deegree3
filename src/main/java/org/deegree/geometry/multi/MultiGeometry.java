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
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;

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

    /**
     * Convenience enum type for discriminating the different types of multi geometries.
     */
    public enum MultiGeometryType {
        /** Generic multi geometry. Member geometries can be all kinds of {@link Geometry} instances. */
        MULTI_GEOMETRY,
        /** Member geometries are {@link Point} instances. */
        MULTI_POINT,
        /** Member geometries are {@link Curve} instances. */
        MULTI_CURVE,
        /** Member geometries are {@link LineString} instances. */
        MULTI_LINE_STRING,
        /** Member geometries are {@link Surface} instances. */
        MULTI_SURFACE,
        /** Member geometries are {@link Polygon} instances. */
        MULTI_POLYGON,
        /** Member geometries are {@link Solid} instances. */
        MULTI_SOLID
    }

    /**
     * Must always return {@link Geometry.GeometryType#MULTI_GEOMETRY}.
     * 
     * @return {@link Geometry.GeometryType#MULTI_GEOMETRY}.
     */
    public GeometryType getGeometryType();

    /**
     * @return the type of MultiGeometry, see {@link MultiGeometryType}
     */
    public MultiGeometryType getMultiGeometryType();
}
