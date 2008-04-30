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

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.model.geometry.primitive.Point;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class JTSWrapperPoint extends JTSWrapperGeometry implements Point {
    
    private double[] pos;

    /**
     * 
     * @param precision
     * @param crs
     * @param pos
     */
    public JTSWrapperPoint( double precision, CoordinateSystem crs, double[] pos ) {
        super( precision, crs, pos.length );
        this.pos = pos;
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.primitive.Point#get(int)
     */
    public double get( int dimension ) {
        if ( dimension > this.pos.length-1 ) {
            return Double.NaN;
        }
        return this.pos[dimension];
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.primitive.Point#getAsArray()
     */
    public double[] getAsArray() {
        return this.pos;
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.primitive.Point#getX()
     */
    public double getX() {
        return this.pos[0];
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.primitive.Point#getY()
     */
    public double getY() {
        return this.pos[1];
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.primitive.Point#getZ()
     */
    public double getZ() {
        if ( coordinateDimension < 3 ) {
            return Double.NaN;
        }
        return this.pos[2];
    }
    
    /* (non-Javadoc)
     * @see org.deegree.model.geometry.primitive.Point#getEnvelope()
     */
    public Envelope getEnvelope() {
        throw new UnsupportedOperationException( "envelope for a point is not defined" );
    }

}
