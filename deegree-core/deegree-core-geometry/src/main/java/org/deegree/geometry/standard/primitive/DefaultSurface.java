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
package org.deegree.geometry.standard.primitive;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.i18n.Messages;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

import org.locationtech.jts.algorithm.InteriorPointArea;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LinearRing;

/**
 * Default implementation of {@link Surface}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultSurface extends AbstractDefaultGeometry implements Surface {

    protected List<? extends SurfacePatch> patches;

    /**
     * Creates a new {@link DefaultSurface} instance from the given parameters.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param patches
     *            patches that constitute the surface
     */
    public DefaultSurface( String id, ICRS crs, PrecisionModel pm, List<? extends SurfacePatch> patches ) {
        super( id, crs, pm );
        this.patches = patches;
    }

    @Override
    public int getCoordinateDimension() {
        return patches.get( 0 ).getCoordinateDimension();
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PRIMITIVE_GEOMETRY;
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.Surface;
    }

    @Override
    public SurfaceType getSurfaceType() {
        return SurfaceType.Surface;
    }

    @Override
    public Measure getArea( Unit requestedBaseUnit ) {
        return new Measure( BigDecimal.valueOf( getJTSGeometry().getArea() ), null );
    }

    /**
     * @return an interior point of this geometry
     */
    public Point getInteriorPoint() {
        Coordinate coord = new InteriorPointArea( getJTSGeometry() ).getInteriorPoint();
        return new GeometryFactory().createPoint( null, coord.x, coord.y, crs );
    }

    @Override
    public List<? extends SurfacePatch> getPatches() {
        return patches;
    }

    @Override
    public Measure getPerimeter( Unit requestedUnit ) {
        return new Measure( BigDecimal.valueOf( getJTSGeometry().getLength() ), null );
    }

    @Override
    public Points getExteriorRingCoordinates() {
        if ( patches.size() == 1 ) {
            if ( patches.get( 0 ) instanceof PolygonPatch ) {
                PolygonPatch patch = (PolygonPatch) patches.get( 0 );
                return patch.getExteriorRing().getControlPoints();
            }
            throw new IllegalArgumentException( Messages.getMessage( "SURFACE_IS_NON_PLANAR" ) );
        }
        throw new IllegalArgumentException( Messages.getMessage( "SURFACE_MORE_THAN_ONE_PATCH" ) );
    }

    @Override
    public List<Points> getInteriorRingsCoordinates() {
        List<Points> controlPoints = new ArrayList<Points>();
        if ( patches.size() == 1 ) {
            if ( patches.get( 0 ) instanceof PolygonPatch ) {
                PolygonPatch patch = (PolygonPatch) patches.get( 0 );
                List<Ring> interiorRings = patch.getInteriorRings();
                for ( Ring ring : interiorRings ) {
                    controlPoints.add( ring.getControlPoints() );
                }
            } else {
                throw new IllegalArgumentException( Messages.getMessage( "SURFACE_IS_NON_PLANAR" ) );
            }
        } else {
            throw new IllegalArgumentException( Messages.getMessage( "SURFACE_MORE_THAN_ONE_PATCH" ) );
        }
        return controlPoints;
    }

    @Override
    protected org.locationtech.jts.geom.Geometry buildJTSGeometry() {

        if ( patches.size() < 1 || !( patches.get( 0 ) instanceof PolygonPatch ) ) {
            throw new IllegalArgumentException( Messages.getMessage( "SURFACE_NOT_EQUIVALENT_TO_POLYGON" ) );
        }

        // TODO handle the other patches as well
        PolygonPatch patch = (PolygonPatch) patches.get( 0 );
        Ring exteriorRing = patch.getExteriorRing();
        List<Ring> interiorRings = patch.getInteriorRings();

        LinearRing shell = (LinearRing) getAsDefaultGeometry( exteriorRing ).getJTSGeometry();
        LinearRing[] holes = null;
        if ( interiorRings != null ) {
            holes = new LinearRing[interiorRings.size()];
            int i = 0;
            for ( Ring ring : interiorRings ) {
                holes[i++] = (LinearRing) getAsDefaultGeometry( ring ).getJTSGeometry();
            }
        }
        return jtsFactory.createPolygon( shell, holes );
    }
}
