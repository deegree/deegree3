//$HeadURL$

/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/

package org.deegree.io.shpapi;

import org.deegree.model.spatialschema.ByteUtils;
import org.deegree.model.spatialschema.Position;

/**
 * Class representig a two dimensional point<BR>
 *
 * @version 14.08.2000
 * @author Andreas Poth
 *
 */
public class SHPPoint extends SHPGeometry {

    /**
     *
     */
    public double x;

    /**
     *
     */
    public double y;

    /**
     *
     */
    public SHPPoint() {
        // with zero values
    }

    /**
     * constructor: gets a stream and the start index <BR>
     * of point on it <BR>
     * @param recBuf
     * @param xStart
     */
    public SHPPoint( byte[] recBuf, int xStart ) {

        super( recBuf );

        // get x out of recordbuffer
        this.x = ByteUtils.readLEDouble( recBuffer, xStart );
        // get y out of recordbuffer
        this.y = ByteUtils.readLEDouble( recBuffer, xStart + 8 );

    }

    /**
     * constructor: creates a SHPPoint from a WKS Geometrie<BR>
     * @param point
     */
    public SHPPoint( Position point ) {
        x = point.getX();
        y = point.getY();
    }

    /**
     * method: writeSHPPoint: writes a SHPPoint Objekt to a recBuffer <BR>
     * @param byteArray
     * @param start
     */
    public void writeSHPPoint( byte[] byteArray, int start ) {

        int offset = start;

        // write shape type identifier ( 1 = point )
        ByteUtils.writeLEInt( byteArray, offset, 1 );

        offset += 4;

        // write x into the recbuffer
        ByteUtils.writeLEDouble( byteArray, offset, x );

        offset += 8;

        // write y into the recbuffer
        ByteUtils.writeLEDouble( byteArray, offset, y );

    }

    /**
     * returns the size of the point shape in bytes<BR>
     * @return the size of the point shape in bytes<BR>
     */
    public int size() {
        return 20;
    }

    @Override
    public String toString() {

        return "SHPPOINT" + "[" + this.x + "; " + this.y + "]";

    }

}
