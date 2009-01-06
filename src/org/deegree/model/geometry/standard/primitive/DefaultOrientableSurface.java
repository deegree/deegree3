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

package org.deegree.model.geometry.standard.primitive;

import java.util.List;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.Envelope;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.primitive.OrientableSurface;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.surfacepatches.SurfacePatch;

/**
 * Default implementation of {@link OrientableSurface}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultOrientableSurface implements OrientableSurface {

    private String id;

    private CoordinateSystem crs;

    private Surface baseSurface;

    private boolean isReversed;

    /**
     * Creates a new <code>DefaultOrientableSurface</code> instance from the given parameters.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param baseSurface
     *            base surface
     * @param isReversed
     *            set to true, if the orientation of the base Surface shall be reversed
     */
    public DefaultOrientableSurface( String id, CoordinateSystem crs, Surface baseSurface, boolean isReversed ) {
        this.id = id;
        this.crs = crs;
        this.baseSurface = baseSurface;
        this.isReversed = isReversed;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public CoordinateSystem getCoordinateSystem() {
        return crs;
    }    

    @Override    
    public SurfaceType getSurfaceType() {
        return SurfaceType.OrientableSurface;
    }

    @Override
    public Surface getBaseSurface() {
        return baseSurface;
    }

    @Override
    public boolean isReversed() {
        return isReversed;
    }    
    
    // -----------------------------------------------------------------------
    // Surface methods that are just delegated to the wrapped base surface
    // -----------------------------------------------------------------------    
    
    public boolean contains( Geometry geometry ) {
        return baseSurface.contains( geometry );
    }

    public Geometry difference( Geometry geometry ) {
        return baseSurface.difference( geometry );
    }

    public double distance( Geometry geometry ) {
        return baseSurface.distance( geometry );
    }

    public boolean equals( Geometry geometry ) {
        return baseSurface.equals( geometry );
    }

    public double getArea() {
        return baseSurface.getArea();
    }

    public Geometry getBuffer( double distance ) {
        return baseSurface.getBuffer( distance );
    }

    public Point getCentroid() {
        return baseSurface.getCentroid();
    }

    public Geometry getConvexHull() {
        return baseSurface.getConvexHull();
    }

    public int getCoordinateDimension() {
        return baseSurface.getCoordinateDimension();
    }

    public Envelope getEnvelope() {
        return baseSurface.getEnvelope();
    }

    public GeometryType getGeometryType() {
        return baseSurface.getGeometryType();
    }

    public List<SurfacePatch> getPatches() {
        return baseSurface.getPatches();
    }

    public double getPerimeter() {
        return baseSurface.getPerimeter();
    }

    public double getPrecision() {
        return baseSurface.getPrecision();
    }

    public PrimitiveType getPrimitiveType() {
        return baseSurface.getPrimitiveType();
    }

    public Geometry intersection( Geometry geometry ) {
        return baseSurface.intersection( geometry );
    }

    public boolean intersects( Geometry geometry ) {
        return baseSurface.intersects( geometry );
    }

    public boolean isBeyond( Geometry geometry, double distance ) {
        return baseSurface.isBeyond( geometry, distance );
    }

    public boolean isWithin( Geometry geometry ) {
        return baseSurface.isWithin( geometry );
    }

    public boolean isWithinDistance( Geometry geometry, double distance ) {
        return baseSurface.isWithinDistance( geometry, distance );
    }

    public Geometry union( Geometry geometry ) {
        return baseSurface.union( geometry );
    }

    @Override
    public List<Point> getExteriorRingCoordinates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<List<Point>> getInteriorRingsCoordinates() {
        throw new UnsupportedOperationException();
    }
}
