//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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

package org.deegree.model.geometry.jtswrapper;

import java.util.ArrayList;
import java.util.List;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.surfacepatches.SurfacePatch;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
class JTSWrapperSurface extends JTSWrapperGeometry implements Surface {

    private List<Curve> boundary;

    // because JTS just is able to match simple feature gemeotries a Surface can not
    // have more than one patch -> surface and patch are geometricly identic
    private List<SurfacePatch> patches = new ArrayList<SurfacePatch>( 1 );

    @Override
    public SurfaceType getSurfaceType() {
        return SurfaceType.Surface;
    }    
    
    /**
     * 
     * @param id 
     * @param precision
     * @param crs
     * @param coordinateDimension
     * @param patch
     */
    public JTSWrapperSurface( String id, double precision, CoordinateSystem crs, int coordinateDimension, SurfacePatch patch ) {
        super( id, precision, crs, coordinateDimension );
        this.patches.add( patch );
//
//        CoordinateSequenceFactory fac = CoordinateArraySequenceFactory.instance();
//        List<Curve> patchBoundary = patch.getBoundaries();
//        List<Point> outer = patchBoundary.get( 0 ).getAsLineString().getControlPoints();
//        Coordinate[] coords = toCoordinates( outer );
//
//        LinearRing shell = new LinearRing( fac.create( coords ), jtsFactory );
//
//        LinearRing[] holes = null;
//        if ( patchBoundary.size() > 1 ) {
//            holes = new LinearRing[patchBoundary.size() - 1];
//            for ( int i = 1; i < patchBoundary.size(); i++ ) {
//                coords = toCoordinates( patchBoundary.get( i ).getAsLineString().getControlPoints() );
//                holes[i - 1] = new LinearRing( fac.create( coords ), jtsFactory );
//            }
//        } else {
//            holes = new LinearRing[0];
//        }
//        geometry = jtsFactory.createPolygon( shell, holes );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Surface#getArea()
     */
    public double getArea() {
        return ( (Polygon) geometry ).getArea();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Surface#getBoundary()
     */
    public List<Curve> getBoundary() {
        if ( boundary == null ) {
            Geometry geom = ( (Polygon) geometry ).getBoundary();
            if ( geom instanceof LinearRing ) {
                boundary = new ArrayList<Curve>();
                LinearRing ls = (LinearRing) geom;
                boundary.add( (Curve) wrap( ls ) );
            } else if ( geom instanceof MultiLineString ) {
                MultiLineString mls = (MultiLineString) geom;
                boundary = new ArrayList<Curve>( mls.getNumGeometries() );
                for ( int i = 0; i < mls.getNumGeometries(); i++ ) {
                    boundary.add( (Curve) wrap( mls.getGeometryN( i ) ) );
                }
            }
        }
        return boundary;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Surface#getCentroid()
     */
    public Point getCentroid() {
        return toPoint( ( (Polygon) geometry ).getCentroid().getCoordinate() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Surface#getPatches()
     */
    public List<SurfacePatch> getPatches() {
        return patches;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Surface#getPerimeter()
     */
    public double getPerimeter() {
        return ( (Polygon) geometry ).getLength();
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.Surface;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PRIMITIVE_GEOMETRY;
    }

    @Override
    public List<Point> getExteriorRingCoordinates() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Point>> getInteriorRingsCoordinates() {
        // TODO Auto-generated method stub
        return null;
    }
}
