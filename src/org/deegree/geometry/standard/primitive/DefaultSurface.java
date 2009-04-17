//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
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
package org.deegree.geometry.standard.primitive;

import java.util.ArrayList;
import java.util.List;

import org.deegree.crs.CRS;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.geometry.primitive.surfacepatches.SurfacePatch;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link Surface}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultSurface extends AbstractDefaultGeometry implements Surface {

    private List<SurfacePatch> patches;

    /**
     * Creates a new {@link DefaultSurface} instance from the given parameters.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param patches
     *            patches that constitute the surface
     */
    public DefaultSurface( String id, CRS crs, List<SurfacePatch> patches ) {
        super( id, crs );
        this.patches = patches;
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
    public double getArea() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point getCentroid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SurfacePatch> getPatches() {
        return patches;
    }

    @Override
    public double getPerimeter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Point> getExteriorRingCoordinates() {
        List<Point> controlPoints = new ArrayList<Point>();
        if ( patches.size() == 1 ) {
            if ( patches.get( 0 ) instanceof PolygonPatch ) {
                PolygonPatch patch = (PolygonPatch) patches.get( 0 );
                controlPoints.addAll( patch.getExteriorRing().getControlPoints() );
            } else {
                String msg = "Cannot determine control points for surface exterior ring, surface is non-planar.";
                throw new IllegalArgumentException( msg );
            }
        } else {
            String msg = "Cannot determine control points for surface exterior ring, surface has more than one patch.";
            throw new IllegalArgumentException( msg );
        }
        return controlPoints;
    }

    @Override
    public List<List<Point>> getInteriorRingsCoordinates() {
       List<List<Point>> controlPoints = new ArrayList<List<Point>>();
        if ( patches.size() == 1 ) {
            if ( patches.get( 0 ) instanceof PolygonPatch ) {
                PolygonPatch patch = (PolygonPatch) patches.get( 0 );
                List<Ring> interiorRings = patch.getInteriorRings();
                for ( Ring ring : interiorRings ) {
                    controlPoints.add( ring.getControlPoints() );
                }
            } else {
                String msg = "Cannot determine control points for surface exterior ring, surface is non-planar.";
                throw new IllegalArgumentException( msg );
            }
        } else {
            String msg = "Cannot determine control points for surface exterior ring, surface has more than one patch.";
            throw new IllegalArgumentException( msg );
        }
        return controlPoints;
    }
}
