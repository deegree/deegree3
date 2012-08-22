//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/io/shpapi/shape_new/ShapePolylineTest.java $
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
package org.deegree.io.shpapi.shape_new;

import org.deegree.model.spatialschema.ByteUtils;

import junit.framework.TestCase;

/**
 * Test cases for <code>ShapePolyline</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 */
public class ShapePolylineTest extends TestCase {

    /**
     * Tests a Polyline.
     */
    public void testPolyline() {
        int size = 44 + 4 + 16*2;
        byte[] bytes = new byte[size];
        ByteUtils.writeLEInt( bytes, 0, ShapeFile.POLYLINE );
        ByteUtils.writeLEDouble( bytes, 4, -10 );
        ByteUtils.writeLEDouble( bytes, 12, -10 );
        ByteUtils.writeLEDouble( bytes, 20, 10 );
        ByteUtils.writeLEDouble( bytes, 28, 10 );
        ByteUtils.writeLEInt( bytes, 36, 1 );
        ByteUtils.writeLEInt( bytes, 40, 2 );
        ByteUtils.writeLEInt( bytes, 44, 0 );
        ByteUtils.writeLEDouble( bytes, 48, 33.3 );
        ByteUtils.writeLEDouble( bytes, 56, 22.2 );
        ByteUtils.writeLEDouble( bytes, 64, 11.1 );
        ByteUtils.writeLEDouble( bytes, 72, 12 );

        ShapePolyline p = new ShapePolyline( false, false );
        p.read( bytes, 0 );
        assertTrue( p.points[0][0].x == 33.3 );
        assertTrue( p.points[0][0].y == 22.2 );
        assertTrue( p.points[0][1].x == 11.1 );
        assertTrue( p.points[0][1].y == 12 );
        assertTrue( p.getByteLength() == size );
    }

    /**
     * Tests a PolylineZ.
     */
    public void testPolylineZ() {
        int size = 44 + 4 + 32*2 + 32;
        byte[] bytes = new byte[size];
        ByteUtils.writeLEInt( bytes, 0, ShapeFile.POLYLINEZ );
        ByteUtils.writeLEDouble( bytes, 4, -10 );
        ByteUtils.writeLEDouble( bytes, 12, -10 );
        ByteUtils.writeLEDouble( bytes, 20, 10 );
        ByteUtils.writeLEDouble( bytes, 28, 10 );
        ByteUtils.writeLEInt( bytes, 36, 1 );
        ByteUtils.writeLEInt( bytes, 40, 2 );
        ByteUtils.writeLEInt( bytes, 44, 0 );
        ByteUtils.writeLEDouble( bytes, 48, 33.3 );
        ByteUtils.writeLEDouble( bytes, 56, 22.2 );
        ByteUtils.writeLEDouble( bytes, 64, 11.1 );
        ByteUtils.writeLEDouble( bytes, 72, 12 );
        ByteUtils.writeLEDouble( bytes, 80, -10 );
        ByteUtils.writeLEDouble( bytes, 88, 10 );
        ByteUtils.writeLEDouble( bytes, 96, 123.123 );
        ByteUtils.writeLEDouble( bytes, 104, 321.321 );
        ByteUtils.writeLEDouble( bytes, 112, -10 );
        ByteUtils.writeLEDouble( bytes, 120, 10 );
        ByteUtils.writeLEDouble( bytes, 128, 111.111 );
        ByteUtils.writeLEDouble( bytes, 136, 222.222 );

        ShapePolyline p = new ShapePolyline( false, false );
        p.read( bytes, 0 );
        assertTrue( p.points[0][0].x == 33.3 );
        assertTrue( p.points[0][0].y == 22.2 );
        assertTrue( p.points[0][0].z == 123.123 );
        assertTrue( p.points[0][0].m == 111.111 );
        assertTrue( p.points[0][1].x == 11.1 );
        assertTrue( p.points[0][1].y == 12 );
        assertTrue( p.points[0][1].z == 321.321 );
        assertTrue( p.points[0][1].m == 222.222 );
        assertTrue( p.getByteLength() == size );
    }

    /**
     * Tests a PolylineM.
     */
    public void testPolylineM() {
        int size = 44 + 4 + 24*2 + 16;
        byte[] bytes = new byte[size];
        ByteUtils.writeLEInt( bytes, 0, ShapeFile.POLYLINEM );
        ByteUtils.writeLEDouble( bytes, 4, -10 );
        ByteUtils.writeLEDouble( bytes, 12, -10 );
        ByteUtils.writeLEDouble( bytes, 20, 10 );
        ByteUtils.writeLEDouble( bytes, 28, 10 );
        ByteUtils.writeLEInt( bytes, 36, 1 );
        ByteUtils.writeLEInt( bytes, 40, 2 );
        ByteUtils.writeLEInt( bytes, 44, 0 );
        ByteUtils.writeLEDouble( bytes, 48, 33.3 );
        ByteUtils.writeLEDouble( bytes, 56, 22.2 );
        ByteUtils.writeLEDouble( bytes, 64, 11.1 );
        ByteUtils.writeLEDouble( bytes, 72, 12 );
        ByteUtils.writeLEDouble( bytes, 80, -10 );
        ByteUtils.writeLEDouble( bytes, 88, 10 );
        ByteUtils.writeLEDouble( bytes, 96, 123.123 );
        ByteUtils.writeLEDouble( bytes, 104, 321.321 );

        ShapePolyline p = new ShapePolyline( false, false );
        p.read( bytes, 0 );
        assertTrue( p.points[0][0].x == 33.3 );
        assertTrue( p.points[0][0].y == 22.2 );
        assertTrue( p.points[0][0].m == 123.123 );
        assertTrue( p.points[0][1].x == 11.1 );
        assertTrue( p.points[0][1].y == 12 );
        assertTrue( p.points[0][1].m == 321.321 );
        assertTrue( p.getByteLength() == size );
    }

}

/*********************************************************************
<code>
 Changes to this class. What the people have been up to:

 $Log$
 Revision 1.1  2007/02/26 14:26:49  schmitz
 Added a new implementation of the ShapeFile API that implements the Z and M variants of the datatypes as well.
 Also added some basic tests for the API as well as a new version of the GML/Shape converters.

</code>
***********************************************************************/
