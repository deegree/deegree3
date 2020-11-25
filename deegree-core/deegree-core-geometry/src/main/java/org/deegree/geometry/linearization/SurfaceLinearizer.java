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

package org.deegree.geometry.linearization;

import java.util.ArrayList;
import java.util.List;

import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.geom.LinearRing;

/**
 * Provides methods for the linearization of planar surfaces, i.e. {@link PolygonPatch}es and {@link Polygon}s.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: elmasri$
 * 
 * @version $Revision$, $Date: 9 May 2008 13:09:29$
 */
public class SurfaceLinearizer {

    private static final Logger LOG = LoggerFactory.getLogger( SurfaceLinearizer.class );

    private GeometryFactory geomFac;

    private CurveLinearizer curveLinearizer;

    /**
     * @param geomFac
     */
    public SurfaceLinearizer( GeometryFactory geomFac ) {
        this.geomFac = geomFac;
        this.curveLinearizer = new CurveLinearizer( geomFac );
    }

    /**
     * Returns a linearized version of the given {@link Surface} geometry, i.e. the patches only use linear interpolated
     * curves as boundaries.
     * <p>
     * NOTE: This method respects the semantic difference between {@link Surface} and {@link Polygon} geometries: if the
     * input is a {@link Polygon}, a polygon geometry will be returned.
     * 
     * @param <T>
     *            subtype of Surface
     * @param surface
     * @param crit
     * @return linearized version of the input curve
     */
    @SuppressWarnings("unchecked")
    public <T extends Surface> T linearize( T surface, LinearizationCriterion crit ) {
        T linearizedSurface = null;
        switch ( surface.getSurfaceType() ) {
        case Polygon: {
            Polygon polygon = (Polygon) surface;
            Ring exteriorRing = polygon.getExteriorRing();
            Ring linearizedExteriorRing = (Ring) curveLinearizer.linearize( exteriorRing, crit );
            List<Ring> interiorRings = polygon.getInteriorRings();
            List<Ring> linearizedInteriorRings = new ArrayList<Ring>( interiorRings.size() );
            for ( Ring interiorRing : interiorRings ) {
                linearizedInteriorRings.add( (Ring) curveLinearizer.linearize( interiorRing, crit ) );
            }
            linearizedSurface = (T) geomFac.createPolygon( polygon.getId(), polygon.getCoordinateSystem(),
                                                           linearizedExteriorRing, linearizedInteriorRings );
            break;
        }
        case PolyhedralSurface: {
            List<? extends SurfacePatch> patches = surface.getPatches();
            List<PolygonPatch> linearizedPatches = new ArrayList<PolygonPatch>( patches.size() );
            for ( SurfacePatch patch : surface.getPatches() ) {
                if ( !( patch instanceof PolygonPatch ) ) {
                    String msg = "Linearization of non planar surface patches is not implemented";
                    throw new IllegalArgumentException( msg );
                }
                linearizedPatches.add( linearize( (PolygonPatch) patch, crit ) );
            }
            linearizedSurface = (T) geomFac.createPolyhedralSurface( surface.getId(), surface.getCoordinateSystem(),
                                                                     linearizedPatches );
            break;
        }
        case Surface: {
            List<? extends SurfacePatch> patches = surface.getPatches();
            List<SurfacePatch> linearizedPatches = new ArrayList<SurfacePatch>( patches.size() );
            for ( SurfacePatch patch : surface.getPatches() ) {
                if ( !( patch instanceof PolygonPatch ) ) {
                    String msg = "Linearization of non planar surface patches is not implemented";
                    throw new IllegalArgumentException( msg );
                }
                linearizedPatches.add( linearize( (PolygonPatch) patch, crit ) );
            }
            linearizedSurface = (T) geomFac.createSurface( surface.getId(), linearizedPatches,
                                                           surface.getCoordinateSystem() );
            break;
        }
        default: {
            // TODO
            LOG.warn( "The surface type " + surface.getSurfaceType()
                      + " currently cannot be linearized. It's being returned as it is." );
            linearizedSurface = surface;
        }
        }
        return linearizedSurface;
    }

    /**
     * Returns a linearized version (i.e. a {@link PolygonPatch} that only uses {@link LinearRing}s as boundaries) of
     * the given {@link PolygonPatch}.
     * 
     * @param patch
     * @param crit
     *            determines the interpolation quality / number of interpolation points
     * @return {@link PolygonPatch} that only uses {@link LinearRing}s as boundaries
     */
    public PolygonPatch linearize( PolygonPatch patch, LinearizationCriterion crit ) {

        Ring exteriorRing = patch.getExteriorRing();
        Ring linearizedExteriorRing = null;
        if ( exteriorRing != null ) {
            linearizedExteriorRing = (Ring) curveLinearizer.linearize( exteriorRing, crit );
        }

        List<Ring> interiorRings = patch.getInteriorRings();
        List<Ring> linearizedInteriorRings = new ArrayList<Ring>( interiorRings.size() );
        for ( Ring interiorRing : interiorRings ) {
            linearizedInteriorRings.add( (Ring) curveLinearizer.linearize( interiorRing, crit ) );
        }
        return geomFac.createPolygonPatch( linearizedExteriorRing, linearizedInteriorRings );
    }
}
