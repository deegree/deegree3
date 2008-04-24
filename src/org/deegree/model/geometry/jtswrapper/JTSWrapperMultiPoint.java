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

import java.util.List;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.multi.MultiPoint;
import org.deegree.model.geometry.primitive.Point;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class JTSWrapperMultiPoint extends JTSWrapperGeometry implements MultiPoint<Point> {

    private List<Point> points;

    /**
     * 
     * @param precision
     * @param crs
     * @param coordinateDimension
     * @param points
     */
    public JTSWrapperMultiPoint( double precision, CoordinateSystem crs, int coordinateDimension, List<Point> points ) {
        super( precision, crs, coordinateDimension );
        this.points = points;
        com.vividsolutions.jts.geom.Point[] pts = new com.vividsolutions.jts.geom.Point[points.size()];
        int i = 0;
        for ( Point point : points ) {
            pts[i++] = (com.vividsolutions.jts.geom.Point) export( point );
        }
        geometry = jtsFactory.createMultiPoint( pts );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.MultiPoint#getGeometries()
     */
    public List<Point> getGeometries() {
        return points;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.MultiPoint#getGeometryAt(int)
     */
    public Point getGeometryAt( int index ) {
        com.vividsolutions.jts.geom.MultiPoint mp = (com.vividsolutions.jts.geom.MultiPoint) geometry;
        return toPoint( ( (com.vividsolutions.jts.geom.Point) mp.getGeometryN( index ) ).getCoordinate() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.MultiGeometry#getCentroid()
     */
    public Point getCentroid() {
        return toPoint( geometry.getCentroid().getCoordinate() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.MultiGeometry#getSize()
     */
    public int getNumberOfGeometries() {
        return ( (com.vividsolutions.jts.geom.MultiPoint) geometry ).getNumGeometries();
    }

}
