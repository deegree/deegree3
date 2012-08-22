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
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.LineString;

/**
 * Class representig a two dimensional ESRI PolyLine<BR>
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SHPPolyLine extends SHPGeometry {

    /**
     *
     */
    public int numParts;

    /**
     *
     */
    public int numPoints;

    /**
     *
     */
    public SHPPoint[][] points = null;

    /**
     * constructor: gets a stream <BR>
     *
     * @param recBuf
     */
    public SHPPolyLine( byte[] recBuf ) {

        // constructor invocation
        super( recBuf );

        int pointsStart = 0;
        int sumPoints = 0;

        envelope = ShapeUtils.readBox( recBuf, 4 );

        numParts = ByteUtils.readLEInt( recBuffer, 36 );
        numPoints = ByteUtils.readLEInt( recBuffer, 40 );

        pointsStart = ShapeConst.PARTS_START + ( numParts * 4 );

        points = new SHPPoint[numParts][];

        for ( int j = 0; j < numParts; j++ ) {

            int firstPointNo = 0;
            int nextFirstPointNo = 0;
            int offset = 0;
            int lnumPoints = 0;

            // get number of first point of current part out of ESRI shape Record:
            firstPointNo = ByteUtils.readLEInt( recBuffer, ShapeConst.PARTS_START + ( j * 4 ) );

            // calculate offset of part in bytes, count from the beginning of recordbuffer
            offset = pointsStart + ( firstPointNo * 16 );

            // get number of first point of next part ...
            if ( j < numParts - 1 ) {
                // ... usually out of ESRI shape Record
                nextFirstPointNo = ByteUtils.readLEInt( recBuffer, ShapeConst.PARTS_START + ( ( j + 1 ) * 4 ) );
            }
            // ... for the last part as total number of points
            else if ( j == numParts - 1 ) {
                nextFirstPointNo = numPoints;
            }

            // calculate number of points per part due to distance and
            // calculate some checksum for the total number of points to be worked
            lnumPoints = nextFirstPointNo - firstPointNo;
            sumPoints += lnumPoints;

            // allocate memory for the j-th part
            points[j] = new SHPPoint[lnumPoints];

            // create the points of the j-th part from the buffer
            for ( int i = 0; i < lnumPoints; i++ ) {
                points[j][i] = new SHPPoint( recBuf, offset + ( i * 16 ) );
            }

        }

    }

    /**
     * constructor: recieves a matrix of Points <BR>
     *
     * @param curve
     */
    public SHPPolyLine( Curve[] curve ) {

        double xmin = curve[0].getEnvelope().getMin().getX();
        double xmax = curve[0].getEnvelope().getMax().getX();
        double ymin = curve[0].getEnvelope().getMin().getY();
        double ymax = curve[0].getEnvelope().getMax().getY();

        numParts = curve.length;

        numPoints = 0;

        points = new SHPPoint[numParts][];

        try {
            // create SHPPoints from the Points array
            for ( int i = 0; i < numParts; i++ ) {

                LineString ls = curve[i].getAsLineString();

                numPoints += ls.getNumberOfPoints();

                points[i] = new SHPPoint[ls.getNumberOfPoints()];

                for ( int j = 0; j < ls.getNumberOfPoints(); j++ ) {
                    points[i][j] = new SHPPoint( ls.getPositionAt( j ) );
                    if ( points[i][j].x > xmax ) {
                        xmax = points[i][j].x;
                    } else if ( points[i][j].x < xmin ) {
                        xmin = points[i][j].x;
                    }
                    if ( points[i][j].y > ymax ) {
                        ymax = points[i][j].y;
                    } else if ( points[i][j].y < ymin ) {
                        ymin = points[i][j].y;
                    }
                }

            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        envelope = new SHPEnvelope( xmin, xmax, ymax, ymin );
    }

    /**
     *
     * @param bytearray
     * @param start
     */
    public void writeSHPPolyLine( byte[] bytearray, int start ) {

        int offset = start;

        double xmin = points[0][0].x;
        double xmax = points[0][0].x;
        double ymin = points[0][0].y;
        double ymax = points[0][0].y;

        // write shape type identifier ( 3 = polyline )
        ByteUtils.writeLEInt( bytearray, offset, 3 );

        offset += 4;
        // save offset of the bounding box
        int tmp1 = offset;

        // increment offset with size of the bounding box
        offset += ( 4 * 8 );

        // write numparts
        ByteUtils.writeLEInt( bytearray, offset, numParts );
        offset += 4;
        // write numpoints
        ByteUtils.writeLEInt( bytearray, offset, numPoints );
        offset += 4;

        // save offset of the list of offsets for each polyline
        int tmp2 = offset;

        // increment offset with numParts
        offset += ( 4 * numParts );

        int count = 0;
        for ( int i = 0; i < points.length; i++ ) {

            // stores the index of the i'th part
            ByteUtils.writeLEInt( bytearray, tmp2, count );
            tmp2 += 4;

            // write the points of the i'th part and calculate bounding box
            for ( int j = 0; j < points[i].length; j++ ) {

                count++;

                // calculate bounding box
                if ( points[i][j].x > xmax ) {
                    xmax = points[i][j].x;
                } else if ( points[i][j].x < xmin ) {
                    xmin = points[i][j].x;
                }

                if ( points[i][j].y > ymax ) {
                    ymax = points[i][j].y;
                } else if ( points[i][j].y < ymin ) {
                    ymin = points[i][j].y;
                }

                // write x-coordinate
                ByteUtils.writeLEDouble( bytearray, offset, points[i][j].x );
                offset += 8;

                // write y-coordinate
                ByteUtils.writeLEDouble( bytearray, offset, points[i][j].y );
                offset += 8;

            }

        }

        // jump back to the offset of the bounding box
        offset = tmp1;

        // write bounding box to the byte array
        ByteUtils.writeLEDouble( bytearray, offset, xmin );
        offset += 8;
        ByteUtils.writeLEDouble( bytearray, offset, ymin );
        offset += 8;
        ByteUtils.writeLEDouble( bytearray, offset, xmax );
        offset += 8;
        ByteUtils.writeLEDouble( bytearray, offset, ymax );

    }

    /**
     * returns the polyline shape size in bytes<BR>
     *
     * @return the polyline shape size in bytes
     */
    public int size() {
        return 44 + numParts * 4 + numPoints * 16;
    }

}
