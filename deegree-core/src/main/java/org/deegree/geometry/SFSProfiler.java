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

import org.deegree.geometry.linearization.CurveLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.SurfaceLinearizer;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;

/**
 * Converts a generic deegree {@link Geometry} instance (which may be anything from the ISO 19107 model) into a
 * {@link Geometry} that matches the capabilities of the Simple Feature Specification (SFS).
 * <p/>
 * <h4>General conversion scheme</h4>
 * <ul>
 * <li>{@link Point} -> {@link Point}</li>
 * <li>{@link LineString} -> {@link LineString}</li>
 * <li>{@link Curve} -> {@link LineString}</li>
 * <li>{@link Polygon} -> {@link Polygon} (using only a patch with linear interpolated boundaries)</li>
 * <li>{@link Surface} -> {@link Polygon}/{@link MultiPolygon} (one member for every surface patch, each using linear
 * interpolated boundaries)</li>
 * <li>{@link MultiPoint} -> {@link MultiPoint}</li>
 * <li>{@link MultiLineString} -> {@link MultiLineString}</li>
 * <li>{@link MultiCurve} -> {@link MultiLineString}</li>
 * <li>{@link MultiPolygon} -> {@link MultiPolygon} (members using only a patch with linear interpolated boundaries)</li>
 * <li>{@link MultiSurface} -> {@link MultiPolygon} (members using only a single patches with linear interpolated
 * boundaries)</li>
 * </ul>
 * TODO Solids, Composites
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SFSProfiler {

    private final GeometryFactory geomFac = new GeometryFactory();

    private final CurveLinearizer curveLinearizer = new CurveLinearizer( geomFac );

    private final SurfaceLinearizer surfaceLinearizer = new SurfaceLinearizer( geomFac );

    private final LinearizationCriterion crit;

    /**
     * Creates a new {@link SFSProfiler} that uses the given {@link LinearizationCriterion} for linearizing non-linear
     * curve segments / surface boundaries.
     * 
     * @param crit
     *            linearization criterion, must not be <code>null</code>
     */
    public SFSProfiler( LinearizationCriterion crit ) {
        this.crit = crit;
    }

    /**
     * Simplifies the given {@link Geometry}.
     * 
     * @param geometry
     *            the geometry to be simplified, must not be <code>null</code>
     * @return the simplified geometry
     */
    public Geometry simplify( Geometry geometry ) {

        Geometry simplified = null;

        switch ( geometry.getGeometryType() ) {
        case COMPOSITE_GEOMETRY: {
            throw new UnsupportedOperationException( "Simplifying of composites is not implemented yet." );
        }
        case ENVELOPE: {
            throw new UnsupportedOperationException( "Simplifying of envelopes is not implemented yet." );
        }
        case MULTI_GEOMETRY: {
            simplified = simplify( (MultiGeometry<?>) geometry );
            break;
        }
        case PRIMITIVE_GEOMETRY: {
            simplified = simplify( (GeometricPrimitive) geometry );
            break;
        }
        }
        return simplified;
    }

    /**
     * Simplifies the given {@link GeometricPrimitive}.
     * 
     * @param geometry
     *            the geometry to be simplified, must not be <code>null</code>
     * @return the simplified geometry
     */
    public Geometry simplify( GeometricPrimitive geometry ) {

        Geometry simplified = null;

        switch ( geometry.getPrimitiveType() ) {
        case Curve: {
            simplified = simplify( (Curve) geometry );
            break;
        }
        case Point: {
            simplified = simplify( (Point) geometry );
            break;
        }
        case Solid: {
            simplified = simplify( (Solid) geometry );
            break;
        }
        case Surface: {
            simplified = simplify( (Surface) geometry );
            break;
        }
        }
        return simplified;
    }

    /**
     * Simplifies the given {@link Point}.
     * 
     * @param geometry
     *            the geometry to be simplified, must not be <code>null</code>
     * @return the simplified geometry
     */
    public Point simplify( Point geometry ) {
        return geometry;
    }

    /**
     * Simplifies the given {@link Curve}.
     * 
     * @param geometry
     *            the geometry to be simplified, must not be <code>null</code>
     * @return the simplified geometry
     */
    public LineString simplify( Curve geometry ) {
        LineString simplified = null;
        switch ( geometry.getCurveType() ) {
        case LineString: {
            simplified = (LineString) geometry;
            break;
        }
        case CompositeCurve:
        case Curve:
        case OrientableCurve:
        case Ring: {
            Curve linearized = curveLinearizer.linearize( geometry, crit );
            simplified = linearized.getAsLineString();
            break;
        }
        }
        return simplified;
    }

    /**
     * Simplifies the given {@link Surface}.
     * 
     * @param geometry
     *            the geometry to be simplified, must not be <code>null</code>
     * @return the simplified geometry, either a {@link Polygon} or a {@link MultiPolygon} (if the input surface has
     *         multiple patches)
     */
    public Geometry simplify( Surface geometry ) {
        Geometry simplified = null;
        switch ( geometry.getSurfaceType() ) {
        case Polygon: {
            // an ISO polygon may actually use non-linear rings
            simplified = surfaceLinearizer.linearize( geometry, crit );
            break;
        }
        case CompositeSurface:
        case OrientableSurface:
        case PolyhedralSurface:
        case Surface:
        case Tin:
        case TriangulatedSurface: {
            Surface linearized = surfaceLinearizer.linearize( geometry, crit );
            if ( linearized.getPatches().size() == 1 ) {
                // only a single patch -> Polygon
                PolygonPatch patch = (PolygonPatch) linearized.getPatches().get( 0 );
                Ring exteriorRing = patch.getExteriorRing();
                List<Ring> interiorRings = patch.getInteriorRings();
                simplified = geomFac.createPolygon( geometry.getId(), geometry.getCoordinateSystem(), exteriorRing,
                                                    interiorRings );
            } else {
                // multiple patches -> MultiPolygon
                List<Polygon> members = new ArrayList<Polygon>( linearized.getPatches().size() );
                for ( SurfacePatch patch : linearized.getPatches() ) {
                    Ring exteriorRing = ( (PolygonPatch) patch ).getExteriorRing();
                    List<Ring> interiorRings = ( (PolygonPatch) patch ).getInteriorRings();
                    members.add( geomFac.createPolygon( null, geometry.getCoordinateSystem(), exteriorRing,
                                                        interiorRings ) );
                }
                simplified = geomFac.createMultiPolygon( geometry.getId(), geometry.getCoordinateSystem(), members );
            }
            break;
        }
        }
        return simplified;
    }

    /**
     * Simplifies the given {@link Solid}.
     * 
     * @param geometry
     *            the geometry to be simplified, must not be <code>null</code>
     * @return the simplified geometry
     */
    public Solid simplify( Solid geometry ) {
        throw new UnsupportedOperationException( "Simplifying of solids is not implemented yet." );
    }

    /**
     * Simplifies the given {@link MultiGeometry}.
     * 
     * @param geometry
     *            the geometry to be simplified, must not be <code>null</code>
     * @return the simplified geometry
     */
    @SuppressWarnings("unchecked")
    public MultiGeometry<?> simplify( MultiGeometry<?> geometry ) {

        MultiGeometry<?> simplified = null;

        switch ( geometry.getMultiGeometryType() ) {
        case MULTI_LINE_STRING:
        case MULTI_POINT: {
            // nothing to do
            simplified = geometry;
            break;
        }
        case MULTI_CURVE: {
            MultiCurve mc = (MultiCurve) geometry;
            List simplifiedMembers = new ArrayList( mc.size() );
            for ( Curve member : mc ) {
                simplifiedMembers.add( simplify( member ) );
            }
            simplified = geomFac.createMultiLineString( mc.getId(), mc.getCoordinateSystem(), simplifiedMembers );
            break;            
        }
        case MULTI_GEOMETRY: {
            MultiGeometry<Geometry> mg = (MultiGeometry<Geometry>) geometry;
            List<Geometry> simplifiedMembers = new ArrayList<Geometry>( mg.size() );
            for ( Geometry member : mg ) {
                simplifiedMembers.add( simplify( member ) );
            }
            simplified = geomFac.createMultiGeometry( mg.getId(), mg.getCoordinateSystem(), simplifiedMembers );
            break;            
        }
        case MULTI_POLYGON: {
            MultiPolygon mp = (MultiPolygon) geometry;
            List<Polygon> simplifiedMembers = new ArrayList<Polygon>( mp.size() );
            for ( Polygon member : mp ) {
                simplifiedMembers.add( (Polygon) simplify( member ) );
            }
            simplified = geomFac.createMultiPolygon( mp.getId(), mp.getCoordinateSystem(), simplifiedMembers );
            break;            
        }
        case MULTI_SURFACE: {
            MultiSurface ms = (MultiSurface) geometry;
            List<Polygon> simplifiedMembers = new ArrayList<Polygon>( ms.size() );
            for ( Surface member : ms ) {
                Geometry simplifiedMember = simplify( member );
                if ( simplifiedMember instanceof Polygon ) {
                    simplifiedMembers.add( (Polygon) simplifiedMember );
                } else {
                    // must be a MultiPolygon then
                    MultiPolygon mp = (MultiPolygon) simplifiedMember;
                    for ( Polygon polygon : mp ) {
                        simplifiedMembers.add( polygon );
                    }
                }
            }
            simplified = geomFac.createMultiPolygon( ms.getId(), ms.getCoordinateSystem(), simplifiedMembers );
            break;            
        }
        case MULTI_SOLID: {
            throw new IllegalArgumentException( "Simplifying of solids is not implemented yet." );
        }
        }
        return simplified;
    }
}
