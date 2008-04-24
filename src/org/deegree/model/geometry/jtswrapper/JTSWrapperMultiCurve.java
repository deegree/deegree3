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
import org.deegree.model.geometry.multi.MultiCurve;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class JTSWrapperMultiCurve extends JTSWrapperGeometry implements MultiCurve<Curve> {
    
    private List<Curve> curves;

    /**
     * @param coordinateDimension
     * @param precision
     * @param crs
     */
    public JTSWrapperMultiCurve( List<Curve> curves, int coordinateDimension, double precision, CoordinateSystem crs ) {
        super( precision, crs, coordinateDimension );
        this.curves = curves;        
        LineString[] ls = new LinearRing[curves.size()];
        int i = 0;
        for ( Curve curve : curves ) {
            if ( curve instanceof JTSWrapperCurve ) {
                ls[i++] = (LineString) ((JTSWrapperGeometry) curve ).getJTSGeometry();
            } else {
                ls[i++] = (LineString)export( curve );
            }
        }
        geometry = jtsFactory.createMultiLineString( ls );
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.multi.MultiCurve#getLength()
     */
    public double getLength() {
        return ((MultiLineString)geometry).getLength();
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.multi.MultiGeometry#getCentroid()
     */
    public Point getCentroid() {
        return toPoint( ((MultiLineString)geometry).getCentroid().getCoordinate() );
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.multi.MultiGeometry#getGeometries()
     */
    public List<Curve> getGeometries() {
        return curves;
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.multi.MultiGeometry#getGeometryAt(int)
     */
    public Curve getGeometryAt( int index ) {        
        return (Curve)wrap( geometry.getGeometryN( index ) );
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.multi.MultiGeometry#getNumberOfGeometries()
     */
    public int getNumberOfGeometries() {
        return ((MultiLineString)geometry).getNumGeometries();
    }

}
