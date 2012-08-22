//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/io/shpapi/shape_new/ShapeMultiPointTest.java $
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
 * Test cases for <code>ShapeMultiPoint</code>.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 */
public class ShapeMultiPointTest extends TestCase {

    /**
     * Test for MultiPoint.
     */
    public void testMultiPoint() {
        int size = 44 + 32;
        byte[] bytes = new byte[size];
        ByteUtils.writeLEInt( bytes, 0, ShapeFile.MULTIPOINT );
        ByteUtils.writeLEDouble( bytes, 4, -10 );
        ByteUtils.writeLEDouble( bytes, 12, -10 );
        ByteUtils.writeLEDouble( bytes, 20, 10 );
        ByteUtils.writeLEDouble( bytes, 28, 10 );
        ByteUtils.writeLEInt( bytes, 36, 2 );
        ByteUtils.writeLEDouble( bytes, 40, 123.123 );
        ByteUtils.writeLEDouble( bytes, 48, 321.321 );
        ByteUtils.writeLEDouble( bytes, 56, 12.12 );
        ByteUtils.writeLEDouble( bytes, 64, 32.32 );
        ShapeMultiPoint p = new ShapeMultiPoint( false, false );
        p.read( bytes, 0 );
        assertTrue( p.points[0].x == 123.123 );
        assertTrue( p.points[0].y == 321.321 );
        assertTrue( p.points[1].x == 12.12 );
        assertTrue( p.points[1].y == 32.32 );
        assertTrue( p.getByteLength() == size );
    }

    /**
     * Test for MultiPointM.
     */
    public void testMultiPointM() {
        int size = 44 + 32 + 16 + 16;
        byte[] bytes = new byte[size];
        ByteUtils.writeLEInt( bytes, 0, ShapeFile.MULTIPOINTM );
        ByteUtils.writeLEDouble( bytes, 4, -10 );
        ByteUtils.writeLEDouble( bytes, 12, -10 );
        ByteUtils.writeLEDouble( bytes, 20, 10 );
        ByteUtils.writeLEDouble( bytes, 28, 10 );
        ByteUtils.writeLEInt( bytes, 36, 2 );
        ByteUtils.writeLEDouble( bytes, 40, 123.123 );
        ByteUtils.writeLEDouble( bytes, 48, 321.321 );
        ByteUtils.writeLEDouble( bytes, 56, 12.12 );
        ByteUtils.writeLEDouble( bytes, 64, 32.32 );
        ByteUtils.writeLEDouble( bytes, 72, -10 );
        ByteUtils.writeLEDouble( bytes, 80, 10 );
        ByteUtils.writeLEDouble( bytes, 88, 77.77 );
        ByteUtils.writeLEDouble( bytes, 96, 88.88 );

        ShapeMultiPoint p = new ShapeMultiPoint( false, true );
        p.read( bytes, 0 );
        assertTrue( p.points[0].x == 123.123 );
        assertTrue( p.points[0].y == 321.321 );
        assertTrue( p.points[0].m == 77.77);
        assertTrue( p.points[1].x == 12.12 );
        assertTrue( p.points[1].y == 32.32 );
        assertTrue( p.points[1].m == 88.88 );
        assertTrue( p.getByteLength() == size );
    }

    /**
     * Test for MultiPointZ.
     */
    public void testMultiPointZ() {
        int size = 44 + 32 + 16 + 16 + 32;
        byte[] bytes = new byte[size];
        ByteUtils.writeLEInt( bytes, 0, ShapeFile.MULTIPOINTZ );
        ByteUtils.writeLEDouble( bytes, 4, -10 );
        ByteUtils.writeLEDouble( bytes, 12, -10 );
        ByteUtils.writeLEDouble( bytes, 20, 10 );
        ByteUtils.writeLEDouble( bytes, 28, 10 );
        ByteUtils.writeLEInt( bytes, 36, 2 );
        ByteUtils.writeLEDouble( bytes, 40, 123.123 );
        ByteUtils.writeLEDouble( bytes, 48, 321.321 );
        ByteUtils.writeLEDouble( bytes, 56, 12.12 );
        ByteUtils.writeLEDouble( bytes, 64, 32.32 );
        ByteUtils.writeLEDouble( bytes, 72, -10 );
        ByteUtils.writeLEDouble( bytes, 80, 10 );
        ByteUtils.writeLEDouble( bytes, 88, 77.77 );
        ByteUtils.writeLEDouble( bytes, 96, 88.88 );
        ByteUtils.writeLEDouble( bytes, 104, -10 );
        ByteUtils.writeLEDouble( bytes, 112, 10 );
        ByteUtils.writeLEDouble( bytes, 120, 33.3 );
        ByteUtils.writeLEDouble( bytes, 128, 22.2 );

        ShapeMultiPoint p = new ShapeMultiPoint( true, false );
        p.read( bytes, 0 );
        assertTrue( p.points[0].x == 123.123 );
        assertTrue( p.points[0].y == 321.321 );
        assertTrue( p.points[0].z == 77.77 );
        assertTrue( p.points[0].m == 33.3 );
        assertTrue( p.points[1].x == 12.12 );
        assertTrue( p.points[1].y == 32.32 );
        assertTrue( p.points[1].z == 88.88 );
        assertTrue( p.points[1].m == 22.2 );
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
