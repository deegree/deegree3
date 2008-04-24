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
import org.deegree.model.geometry.multi.MultiSurface;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class JTSWrapperMultiSurface extends JTSWrapperGeometry implements MultiSurface<Surface> {

    private List<Surface> surfaces;

    /**
     * @param coordinateDimension
     * @param precision
     * @param crs
     */
    public JTSWrapperMultiSurface( List<Surface> surfaces, int coordinateDimension, double precision,
                                   CoordinateSystem crs ) {
        super( precision, crs, coordinateDimension );
        this.surfaces = surfaces;
        Polygon[] mp = new Polygon[surfaces.size()];
        int i = 0;
        for ( Surface surface : surfaces ) {
            if ( surface instanceof JTSWrapperSurface ) {
                mp[i++] = (Polygon) ( (JTSWrapperSurface) surface ).getJTSGeometry();
            } else {
                mp[i++] = (Polygon) export( surface );
            }
        }
        geometry = jtsFactory.createMultiPolygon( mp );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.MultiSurface#getArea()
     */
    public double getArea() {
        return ( (MultiPolygon) geometry ).getArea();
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
     * @see org.deegree.model.geometry.multi.MultiGeometry#getGeometries()
     */
    public List<Surface> getGeometries() {
        return surfaces;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.MultiGeometry#getGeometryAt(int)
     */
    public Surface getGeometryAt( int index ) {
        return (Surface) wrap( ( (MultiPolygon) geometry ).getGeometryN( index ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.MultiGeometry#getNumberOfGeometries()
     */
    public int getNumberOfGeometries() {
        return ( (MultiPolygon) geometry ).getNumGeometries();
    }

}
