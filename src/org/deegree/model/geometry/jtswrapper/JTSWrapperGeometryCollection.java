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
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.multi.GeometryCollection;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
public class JTSWrapperGeometryCollection extends JTSWrapperGeometry implements GeometryCollection<Geometry> {

    private List<Geometry> geometries;

    private boolean doContainsCollections = false;

    private boolean doContainsCurves = false;

    private boolean doContainsPoints = false;

    private boolean doContainsSurface = false;

    /**
     * @param precision
     * @param crs
     * @param coordinateDimension
     */
    public JTSWrapperGeometryCollection( double precision, CoordinateSystem crs, int coordinateDimension,
                                         List<Geometry> geometries ) {
        super( precision, crs, coordinateDimension );
        this.geometries = geometries;
        com.vividsolutions.jts.geom.Geometry[] gs = new com.vividsolutions.jts.geom.Geometry[geometries.size()];
        int i = 0;
        for ( Geometry geom : geometries ) {
            gs[i] = export( geom );
            if ( !doContainsCurves && geom instanceof Curve ) {
                doContainsCurves = true;
            } else if ( !doContainsSurface && geom instanceof Surface ) {
                doContainsSurface = true;
            } else if ( !doContainsPoints && geom instanceof Point ) {
                doContainsPoints = true;
            } else if ( !doContainsCollections && geom instanceof GeometryCollection ) {
                doContainsCollections = true;
            }
        }
        geometry = jtsFactory.createGeometryCollection( gs );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.GeometryCollection#containsCollections()
     */
    public boolean containsCollections() {
        return doContainsCollections;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.GeometryCollection#containsComplexes()
     */
    public boolean containsComplexes() {
        // JTS does not know complex geometries so return will always be false
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.GeometryCollection#containsCurves()
     */
    public boolean containsCurves() {
        return doContainsCurves;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.GeometryCollection#containsPoints()
     */
    public boolean containsPoints() {
        return doContainsPoints;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.GeometryCollection#containsSolids()
     */
    public boolean containsSolids() {
        // JTS does not know Solids so return will always be false
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.GeometryCollection#containsSurfaces()
     */
    public boolean containsSurfaces() {
        return doContainsSurface;
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
    public List<Geometry> getGeometries() {
        return geometries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.MultiGeometry#getGeometryAt(int)
     */
    public Geometry getGeometryAt( int index ) {
        com.vividsolutions.jts.geom.GeometryCollection gc = (com.vividsolutions.jts.geom.GeometryCollection) geometry;
        return wrap( gc.getGeometryN( index ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.MultiGeometry#getNumberOfGeometries()
     */
    public int getNumberOfGeometries() {
        com.vividsolutions.jts.geom.GeometryCollection gc = (com.vividsolutions.jts.geom.GeometryCollection) geometry;
        return gc.getNumGeometries();
    }

}
