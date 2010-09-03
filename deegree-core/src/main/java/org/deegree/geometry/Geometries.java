//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSolid;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;

/**
 * Contains utility methods for common tasks on {@link Geometry} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Geometries {

    /**
     * Homogenizes the given generic {@link MultiGeometry}, i.e. returns a {@link MultiPoint}, {@link MultiCurve},
     * {@link MultiLineString}, {@link MultiSurface}, {@link MultiPolygon} or {@link MultiSolid} (depending on the
     * members).
     * 
     * @param geometry
     *            generic multi geometry to be homogenized, must not be <code>null</code>
     * @return the homogenized multi geometry
     */
    @SuppressWarnings("unchecked")
    public static MultiGeometry<?> homogenize( MultiGeometry<?> geometry ) {

        MultiGeometry<?> homogenized = null;

        List<?> deepMembers = new ArrayList( geometry.size() );
        for ( Geometry member : geometry ) {
            collectMembersDeep( member, (List<GeometricPrimitive>) deepMembers );
        }

        // check if all members have the same dimension
        int dim = -1;
        for ( Object o : deepMembers ) {
            GeometricPrimitive member = (GeometricPrimitive) o;
            int memberDim = -1;
            switch ( member.getPrimitiveType() ) {
            case Point: {
                memberDim = 0;
                break;
            }
            case Curve: {
                memberDim = 1;
                break;
            }
            case Surface: {
                memberDim = 2;
                break;
            }
            case Solid: {
                memberDim = 3;
                break;
            }
            }
            if ( dim == -1 ) {
                dim = memberDim;
            } else if ( dim != memberDim ) {
                String msg = "Cannot homogenize MultiGeometry: contains members with dimension " + dim + " and "
                             + memberDim + ".";
                throw new IllegalArgumentException( msg );
            }
        }

        switch ( dim ) {
        case -1: {
            homogenized = new GeometryFactory().createMultiPoint( geometry.getId(), geometry.getCoordinateSystem(),
                                                                  Collections.EMPTY_LIST );
            break;
        }
        case 0: {
            homogenized = new GeometryFactory().createMultiPoint( geometry.getId(), geometry.getCoordinateSystem(),
                                                                  (List<Point>) deepMembers );
            break;
        }
        case 1: {
            homogenized = new GeometryFactory().createMultiLineString( geometry.getId(),
                                                                       geometry.getCoordinateSystem(),
                                                                       (List<LineString>) deepMembers );
            break;
        }
        case 2: {
            homogenized = new GeometryFactory().createMultiPolygon( geometry.getId(), geometry.getCoordinateSystem(),
                                                                    (List<Polygon>) deepMembers );
            break;
        }
        default: {
            String msg = "Cannot homogenize MultiGeometry: contains members with dimension " + dim + ".";
            throw new IllegalArgumentException( msg );
        }
        }
        return homogenized;
    }

    private static void collectMembersDeep( Geometry geometry, List<GeometricPrimitive> deepMembers ) {
        switch ( geometry.getGeometryType() ) {
        case COMPOSITE_GEOMETRY: {
            // TODO
            break;
        }
        case ENVELOPE: {
            // TODO
            break;
        }
        case PRIMITIVE_GEOMETRY: {
            deepMembers.add( (GeometricPrimitive) geometry );
        }
        case MULTI_GEOMETRY: {
            MultiGeometry<?> multi = (MultiGeometry<?>) geometry;
            for ( Geometry member : multi ) {
                collectMembersDeep( member, deepMembers );
            }
        }
        }
    }
}
