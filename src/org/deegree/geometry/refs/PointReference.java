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

import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Point;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class PointReference extends GeometryReference implements Point {

    private Point point;

    public PointReference (String href) {
        super (href);
    }
    
    public void resolve (Point point) {
        if (this.point != null) {
            String msg = "Internal error: Geometry reference (" + href + ") has already been resolved.";
            throw new RuntimeException(msg);
        }
        this.point = point;
    }    
    
    public boolean contains( Geometry geometry ) {
        return point.contains( geometry );
    }

    public Geometry difference( Geometry geometry ) {
        return point.difference( geometry );
    }

    public double distance( Geometry geometry ) {
        return point.distance( geometry );
    }

    public boolean equals( Geometry geometry ) {
        return point.equals( geometry );
    }

    public double get( int dimension ) {
        return point.get( dimension );
    }

    public double[] getAsArray() {
        return point.getAsArray();
    }

    public Geometry getBuffer( double distance ) {
        return point.getBuffer( distance );
    }

    public Geometry getConvexHull() {
        return point.getConvexHull();
    }

    public int getCoordinateDimension() {
        return point.getCoordinateDimension();
    }

    public CRS getCoordinateSystem() {
        return point.getCoordinateSystem();
    }

    public Envelope getEnvelope() {
        return point.getEnvelope();
    }

    public GeometryType getGeometryType() {
        return point.getGeometryType();
    }

    public String getId() {
        return point.getId();
    }

    public double getPrecision() {
        return point.getPrecision();
    }

    public PrimitiveType getPrimitiveType() {
        return point.getPrimitiveType();
    }

    public double getX() {
        return point.getX();
    }

    public double getY() {
        return point.getY();
    }

    public double getZ() {
        return point.getZ();
    }

    public Geometry intersection( Geometry geometry ) {
        return point.intersection( geometry );
    }

    public boolean intersects( Geometry geometry ) {
        return point.intersects( geometry );
    }

    public boolean isBeyond( Geometry geometry, double distance ) {
        return point.isBeyond( geometry, distance );
    }

    public boolean isWithin( Geometry geometry ) {
        return point.isWithin( geometry );
    }

    public boolean isWithinDistance( Geometry geometry, double distance ) {
        return point.isWithinDistance( geometry, distance );
    }

    public Geometry union( Geometry geometry ) {
        return point.union( geometry );
    }
}
