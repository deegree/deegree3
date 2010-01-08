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
package org.deegree.geometry;

import java.util.ArrayList;
import java.util.List;

import org.deegree.geometry.Geometry.GeometryType;
import org.deegree.geometry.linearization.CurveLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.SurfaceLinearizer;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.GeometricPrimitive.PrimitiveType;
import org.deegree.geometry.standard.multi.DefaultMultiCurve;
import org.deegree.geometry.standard.multi.DefaultMultiGeometry;
import org.deegree.geometry.standard.multi.DefaultMultiPoint;
import org.deegree.geometry.standard.multi.DefaultMultiSolid;
import org.deegree.geometry.standard.multi.DefaultMultiSurface;

/**
 * The <code>GeometrySimplifier</code> class proposes to
 * <ul>
 * <li>Homogenize a MultiGeometry, i.e. when its members are of the same <code>PrimitiveType</code> (e.g. {@link Point},
 * {@link Curve}, {@link Solid}, {@link Surface}) then construct a specific MultiGeometry (i.e. <code>MultiPoint</code>,
 * <code>MultiCurve</code>, <code>MultiSurface</code>, <code>MultiSolid</code>).</li>
 * <li>Linearize the geometries that are Curves or Surfaces using the linearizing algorithms in {@link CurveLinearizer}
 * and {@link SurfaceLinearizer} respectively.</li>
 * </ul>
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GeometrySimplifier {

    private boolean homogenizeCollections = false;

    private LinearizationCriterion crit = null;

    /**
     * Set a {@LinearizationCriterion} if the geometry to be simplified is meant to be
     * linearized.
     * 
     * @param crit
     *            a {@LinearizationCriterion} instance
     */
    public void setLinearizationCriterion( LinearizationCriterion crit ) {
        this.crit = crit;
    }

    /**
     * Specify (boolean) whether the geometry to be simplified should be homogenized, i.e. if a DefaultMultiGeometry
     * with the same type (primitive type) of members, then it shall be reinstantiated to DefaultMultiPoint,
     * DefaultMultiCurve, DefaultMultiSurface, DefaultMultiSolid.
     * 
     * @param homogenizeCollections
     *            a boolean specifying whether the geometry shall or shall not be instantiated.
     */
    public void setHomogenizeCollections( boolean homogenizeCollections ) {
        this.homogenizeCollections = homogenizeCollections;
    }

    /**
     * Simplification means homogenizing and linearizaion
     * 
     * @param geometry
     *            the geometry to be simplified
     * @return the simplified geometry
     */
    public Geometry simplify( Geometry geometry ) {
        Geometry simplifiedG = homogenize( geometry );
        Geometry result = linearize( simplifiedG );
        return result;
    }

    private Geometry linearize( Geometry geometry ) {
        if ( crit != null ) {
            if ( geometry instanceof Curve ) {
                CurveLinearizer curveLinearizer = new CurveLinearizer( new GeometryFactory() );
                return curveLinearizer.linearize( (Curve) geometry, crit );
            }
            // else if ( geometry instanceof Surface ) {
            // SurfaceLinearizer surfaceLinearizer = new SurfaceLinearizer( new GeometryFactory() );
            // Geometry result = surfaceLinearizer.linearize( (Surface) geometry, crit );
            // return result;
            // }
        }
        return geometry;
    }

    @SuppressWarnings("unchecked")
    private Geometry homogenize( Geometry geometry ) {
        Geometry simplifiedG = geometry;
        if ( homogenizeCollections ) {
            if ( geometry.getClass().equals( DefaultMultiGeometry.class ) ) {
                throw new GeometryException( "Homogenizing is only possible for a DefaultMultiGeometry" );
            }

            // see if the multiGeometry members are of the same type
            PrimitiveType gType = null;
            for ( Geometry g : (DefaultMultiGeometry<Geometry>) geometry ) {
                if ( !geometry.getGeometryType().equals( GeometryType.PRIMITIVE_GEOMETRY ) ) {
                    throw new GeometryException(
                                                 "At this time the GeometrySimplifier only accepts MultiGeometries that have primitive geometries as members" );
                }
                PrimitiveType memberType = ( (GeometricPrimitive) g ).getPrimitiveType();
                if ( gType != null && !gType.equals( memberType ) ) {
                    throw new GeometryException(
                                                 "Cannot homogenize a MultiGeometry that contains members of different types." );
                }
                if ( gType == null ) {
                    gType = memberType;
                }
            }

            switch ( gType ) {
            case Curve:
                // convert all the member geometries to Curve
                List<Curve> membersCurve = new ArrayList<Curve>();
                for ( Geometry g : (DefaultMultiGeometry<Geometry>) geometry ) {
                    membersCurve.add( (Curve) g );
                }
                simplifiedG = new DefaultMultiCurve( geometry.getId(), geometry.getCoordinateSystem(),
                                                     geometry.getPrecision(), membersCurve );
                break;
            case Point:
                // convert all the member geometries to Point
                List<Point> membersPoint = new ArrayList<Point>();
                for ( Geometry g : (DefaultMultiGeometry<Geometry>) geometry ) {
                    membersPoint.add( (Point) g );
                }
                simplifiedG = new DefaultMultiPoint( geometry.getId(), geometry.getCoordinateSystem(),
                                                     geometry.getPrecision(), membersPoint );
                break;
            case Solid:
                // convert all the member geometries to Solid
                List<Solid> membersSolid = new ArrayList<Solid>();
                for ( Geometry g : (DefaultMultiGeometry<Geometry>) geometry ) {
                    membersSolid.add( (Solid) g );
                }
                simplifiedG = new DefaultMultiSolid( geometry.getId(), geometry.getCoordinateSystem(),
                                                     geometry.getPrecision(), membersSolid );
                break;
            case Surface:
                // convert all the member geometries to Surface
                List<Surface> membersSurface = new ArrayList<Surface>();
                for ( Geometry g : (DefaultMultiGeometry<Geometry>) geometry ) {
                    membersSurface.add( (Surface) g );
                }
                simplifiedG = new DefaultMultiSurface( geometry.getId(), geometry.getCoordinateSystem(),
                                                       geometry.getPrecision(), membersSurface );
                break;
            }
        }

        return simplifiedG;
    }
}
