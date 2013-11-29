//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2013 by:

 IDgis bv

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

 IDgis bv
 Boomkamp 16
 7461 AX Rijssen
 The Netherlands
 http://idgis.nl/ 

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
package org.deegree.sqldialect.oracle.sdo;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;

import org.deegree.sqldialect.oracle.sdo.SDOGeometryConverter.Triplet;

import org.junit.Test;

/**
 * Unit tests for {@link SDOGeometryConverter}
 * 
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * 
 */
public class SDOGeometryConverterTest {

    final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    public void testOrientation() {
        final double[] exteriorRing = { 2, 4, 4, 3, 10, 3, 13, 5, 13, 9, 11, 13, 5, 13, 2, 11, 2, 4 };
        final double[] interiorRing = { 7, 5, 7, 10, 10, 10, 10, 5, 7, 5 };

        assertPoints( toPolygon( exteriorRing, interiorRing ), exteriorRing, interiorRing );
        assertPoints( toPolygon( reverseRing( exteriorRing ), reverseRing( interiorRing ) ), exteriorRing,
                          interiorRing );
    }

    private double[] reverseRing( final double[] ring ) {
        final double[] reversedRing = new double[ring.length];

        final int hLength = reversedRing.length / 2;
        for ( int i = 0; i < hLength; i++ ) {
            final int ringI = hLength - i - 1;
            reversedRing[i * 2] = ring[ringI * 2];
            reversedRing[i * 2 + 1] = ring[ringI * 2 + 1];
        }

        return reversedRing;
    }

    private void assertPoints( final Polygon polygon, final double[] exteriorRing, final double[]... interiorRings ) {
        final ArrayList<Triplet> info = new ArrayList<Triplet>();
        final ArrayList<Point> pnts = new ArrayList<Point>();
        final SDOGeometryConverter converter = new SDOGeometryConverter();
        final int gtyp = converter.buildPrimitive( info, pnts, polygon );

        assertEquals( SDOGTypeTT.POLYGON, gtyp );
        int i = 0;
        for ( ; i < exteriorRing.length / 2; i++ ) {
            final Point point = pnts.get( i );
            assertEquals( "exterior[" + i + "].x invalid", exteriorRing[i * 2], point.get0(), 0.05 );
            assertEquals( "exterior[" + i + "].y invalid", exteriorRing[i * 2 + 1], point.get1(), 0.05 );
        }

        for ( int j = 0; j < interiorRings.length; j++) {
            int ringI = 0;
            final double[] interiorRing = interiorRings[j];
            
            final int next = i + interiorRing.length / 2;            
            for ( ; i < next; i++ ) {
                final Point point = pnts.get( i );
                assertEquals( "interior[" + 0 + "][" + i + "].x invalid", interiorRing[ringI++], point.get0(), 0.05 );
                assertEquals( "interior[" + 0 + "][" + i + "].y invalid", interiorRing[ringI++], point.get1(), 0.05 );
            }
        }

        assertEquals( i, pnts.size() );
    }

    private Polygon toPolygon( final double[] exteriorRing, final double[]... interiorRings ) {
        ArrayList<Ring> rngs = new ArrayList<Ring>();
        for ( double[] interiorRing : interiorRings ) {
            rngs.add( toRing( interiorRing ) );
        }
        return geometryFactory.createPolygon( "polygon", null, toRing( exteriorRing ), rngs );
    }

    private Ring toRing( final double[] pnts ) {
        final ArrayList<Point> pointList = new ArrayList<Point>();
        for ( int i = 0; i < pnts.length; ) {
            pointList.add( geometryFactory.createPoint( "point" + ( i / 2 ), pnts[i++], pnts[i++], null ) );
        }
        final Points points = geometryFactory.createPoints( pointList );
        final Curve curve = geometryFactory.createLineString( "curve", null, points );
        final Ring ring = geometryFactory.createRing( "ring", null, Arrays.asList( curve ) );
        return ring;
    }
}
