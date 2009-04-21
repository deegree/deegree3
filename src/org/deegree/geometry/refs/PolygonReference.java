//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

package org.deegree.geometry.refs;

import java.util.List;

import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.surfacepatches.PolygonPatch;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class PolygonReference extends GeometryReference implements Polygon {

    protected Polygon geometry;    
    
    public PolygonReference (String href) {
        super (href);
    }
    
    @Override
    public void resolve (Geometry geometry) {
        if (this.geometry != null) {
            String msg = "Internal error: Geometry reference (" + href + ") has already been resolved.";
            throw new RuntimeException(msg);
        }
        this.geometry = (Polygon) geometry;
    }

    public boolean contains( Geometry geometry ) {
        return geometry.contains( geometry );
    }

    public Geometry difference( Geometry geometry ) {
        return geometry.difference( geometry );
    }

    public double distance( Geometry geometry ) {
        return geometry.distance( geometry );
    }

    public boolean equals( Geometry geometry ) {
        return geometry.equals( geometry );
    }

    public double getArea() {
        return geometry.getArea();
    }

    public Geometry getBuffer( double distance ) {
        return geometry.getBuffer( distance );
    }

    public Point getCentroid() {
        return geometry.getCentroid();
    }

    public Geometry getConvexHull() {
        return geometry.getConvexHull();
    }

    public int getCoordinateDimension() {
        return geometry.getCoordinateDimension();
    }

    public CRS getCoordinateSystem() {
        return geometry.getCoordinateSystem();
    }

    public Envelope getEnvelope() {
        return geometry.getEnvelope();
    }

    public Ring getExteriorRing() {
        return geometry.getExteriorRing();
    }

    public List<Point> getExteriorRingCoordinates() {
        return geometry.getExteriorRingCoordinates();
    }

    public GeometryType getGeometryType() {
        return geometry.getGeometryType();
    }

    public List<Ring> getInteriorRings() {
        return geometry.getInteriorRings();
    }

    public List<List<Point>> getInteriorRingsCoordinates() {
        return geometry.getInteriorRingsCoordinates();
    }

    public List<PolygonPatch> getPatches() {
        return geometry.getPatches();
    }

    public double getPerimeter() {
        return geometry.getPerimeter();
    }

    public double getPrecision() {
        return geometry.getPrecision();
    }

    public PrimitiveType getPrimitiveType() {
        return geometry.getPrimitiveType();
    }

    public SurfaceType getSurfaceType() {
        return geometry.getSurfaceType();
    }

    public Geometry intersection( Geometry geometry ) {
        return geometry.intersection( geometry );
    }

    public boolean intersects( Geometry geometry ) {
        return geometry.intersects( geometry );
    }

    public boolean isBeyond( Geometry geometry, double distance ) {
        return geometry.isBeyond( geometry, distance );
    }

    public boolean isWithin( Geometry geometry ) {
        return geometry.isWithin( geometry );
    }

    public boolean isWithinDistance( Geometry geometry, double distance ) {
        return geometry.isWithinDistance( geometry, distance );
    }

    public Geometry union( Geometry geometry ) {
        return geometry.union( geometry );
    }
}
