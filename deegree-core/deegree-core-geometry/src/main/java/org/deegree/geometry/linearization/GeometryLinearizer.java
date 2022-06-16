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
package org.deegree.geometry.linearization;

import java.util.ArrayList;
import java.util.List;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSolid;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.geom.MultiPoint;

/**
 * Provides methods for creating linearized versions of {@link Geometry} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryLinearizer {

    private static final Logger LOG = LoggerFactory.getLogger( SurfaceLinearizer.class );

    private GeometryFactory geomFac;

    private CurveLinearizer curveLinearizer;

    private SurfaceLinearizer sfLinearizer;

    /**
     * Creates a new {@link GeometryLinearizer} instance.
     */
    public GeometryLinearizer() {
        this.geomFac = new GeometryFactory();
        this.curveLinearizer = new CurveLinearizer( geomFac );
        this.sfLinearizer = new SurfaceLinearizer( geomFac );
    }

    /**
     * Returns a linearized version of the given {@link Geometry}.
     * 
     * @param <T>
     *            geometry type
     * @param geom
     *            geometry to be linearized, must not be <code>null</code>
     * @param crit
     *            linearization criterion, must not be <code>null</code>
     * @return linearized version of the input geometry, never <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public <T extends Geometry> T linearize( T geom, LinearizationCriterion crit ) {
        T linearized = geom;
        if ( geom instanceof Curve ) {
            linearized = (T) curveLinearizer.linearize( (Curve) geom, crit );
        } else if ( geom instanceof Surface ) {
            linearized = (T) sfLinearizer.linearize( (Surface) geom, crit );
        } else if ( geom instanceof Solid ) {
            LOG.warn( "Linearization of Solids is not implemented yet." );
        } else if ( geom instanceof MultiGeometry<?> ) {
            linearized = (T) linearizeMulti( (MultiGeometry<?>) geom, crit );
        }
        return linearized;
    }

    @SuppressWarnings("unchecked")
    private <T extends MultiGeometry<?>> T linearizeMulti( T geom, LinearizationCriterion crit ) {
        T linearized = geom;
        if ( geom instanceof MultiPoint ) {
            // nothing to do
        } else if ( geom instanceof MultiLineString ) {
            // nothing to do
        } else if ( geom instanceof MultiSolid ) {
            LOG.warn( "Linearization of Solids is not implemented yet." );
        } else if ( geom instanceof MultiCurve ) {
            MultiCurve<Curve> mc = (MultiCurve<Curve>) geom;
            List<Curve> linearizedMembers = new ArrayList<Curve>( mc.size() );
            for ( Curve curve : mc ) {
                linearizedMembers.add( curveLinearizer.linearize( curve, crit ) );
            }
            linearized = (T) geomFac.createMultiCurve( geom.getId(), geom.getCoordinateSystem(), linearizedMembers );
        } else if ( geom instanceof MultiPolygon ) {
            MultiPolygon mp = (MultiPolygon) geom;
            List<Polygon> linearizedMembers = new ArrayList<Polygon>( mp.size() );
            for ( Polygon polygon : mp ) {
                linearizedMembers.add( sfLinearizer.linearize( polygon, crit ) );
            }
            linearized = (T) geomFac.createMultiPolygon( geom.getId(), geom.getCoordinateSystem(), linearizedMembers );
        } else if ( geom instanceof MultiSurface ) {
            MultiSurface<Surface> ms = (MultiSurface<Surface>) geom;
            List<Surface> linearizedMembers = new ArrayList<Surface>( ms.size() );
            for ( Surface polygon : ms ) {
                linearizedMembers.add( sfLinearizer.linearize( polygon, crit ) );
            }
            linearized = (T) geomFac.createMultiSurface( geom.getId(), geom.getCoordinateSystem(), linearizedMembers );
        } else {
            List<Geometry> linearizedMembers = new ArrayList<Geometry>( geom.size() );
            for ( Object member : geom ) {
                linearizedMembers.add( linearize( (Geometry) member, crit ) );
            }
            linearized = (T) geomFac.createMultiGeometry( geom.getId(), geom.getCoordinateSystem(), linearizedMembers );
        }
        return linearized;
    }
}