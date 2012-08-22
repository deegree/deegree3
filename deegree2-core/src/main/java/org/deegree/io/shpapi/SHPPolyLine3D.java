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
 *
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class SHPPolyLine3D extends SHPPolyLine {

    /**
     * @param recBuf
     */
    public SHPPolyLine3D( byte[] recBuf ) {

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

            int zPos = pointsStart + 16 * numPoints + 16;

            // create the points of the j-th part from the buffer
            for ( int i = 0; i < lnumPoints; i++ ) {
                double z = ByteUtils.readLEDouble( recBuffer, zPos );
                zPos += 8;
                SHPPoint dummyPoint = new SHPPoint( recBuf, offset + ( i * 16 ) );
                points[j][i] = new SHPPoint3D( dummyPoint.x, dummyPoint.y, z );
            }

        }

    }

    /**
     * @param curve
     */
    public SHPPolyLine3D( Curve[] curve ) {
        super( curve );
        double xmin = curve[0].getEnvelope().getMin().getX();
        double xmax = curve[0].getEnvelope().getMax().getX();
        double ymin = curve[0].getEnvelope().getMin().getY();
        double ymax = curve[0].getEnvelope().getMax().getY();

        numParts = curve.length;

        numPoints = 0;

        points = new SHPPoint[numParts][];

        try {
            // create SHPPoints from the GM_Points array
            for ( int i = 0; i < numParts; i++ ) {

                LineString ls = curve[i].getAsLineString();

                numPoints += ls.getNumberOfPoints();

                points[i] = new SHPPoint3D[ls.getNumberOfPoints()];

                for ( int j = 0; j < ls.getNumberOfPoints(); j++ ) {
                    points[i][j] = new SHPPoint3D( ls.getPositionAt( j ) );
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
            System.out.println( "SHPPolyLine:: " + e );
        }

        envelope = new SHPEnvelope( xmin, xmax, ymax, ymin );
    }

    /**
     * @param points
     */
    public SHPPolyLine3D( SHPPoint3D[][] points ) {
        super( (Curve[]) null );
        this.points = points;
        this.numParts = points.length;

    }

}
